import {
  ESTADO_ACTIVO,
  ESTADO_ACTA,
  TIPO_ACTIVO_OPTIONS,
  esActaAbierta,
} from '../constants/activosConstants';

export const TIPO = {
  DESKTOP: 'desktop',
  LAPTOP: 'laptop',
  PERIFERICO: 'periferico',
  IMPRESORA: 'impresora_termica',
  MOVIL: 'dispositivo_movil',
};

export const TODAS = 'todas';

/** Prefijo del valor del filtro de tipo cuando se elige un subtipo de periférico. */
const PREFIJO_SUBTIPO = 'periferico:';
const SIN_ESPECIFICAR = 'sin-especificar';

/** Un activo dado de baja o robado/perdido ya no cuenta en los totales del inventario. */
export const estaFueraDeServicio = (activo) =>
  activo.estado === ESTADO_ACTIVO.DADO_DE_BAJA || activo.estado === ESTADO_ACTIVO.ROBADO_PERDIDO;

// ---------------------------------------------------------------------------
// Subtipos de periférico (audífonos, diademas, teclados, mouse...)
// El subtipo se escribe a mano en el campo "Tipo de Periférico" del detalle, así
// que se normaliza para que "Diadema", "diademas " y "DIADEMA" cuenten como uno.
// ---------------------------------------------------------------------------

const claveDeTexto = (texto) => {
  const base = texto
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/\s+/g, ' ')
    .trim();
  // Plural sencillo: "diademas" y "diadema" son el mismo subtipo.
  return base.length > 3 && base.endsWith('s') ? base.slice(0, -1) : base;
};

const capitalizar = (texto) => texto.charAt(0).toUpperCase() + texto.slice(1);

/** Subtipo de un periférico, o null si el activo no es un periférico. */
export const subtipoDe = (activo) => {
  if (activo.tipo_activo !== TIPO.PERIFERICO) return null;
  const crudo = (activo.detalle?.tipo_dispositivo || '').trim();
  if (!crudo) return { clave: SIN_ESPECIFICAR, etiqueta: 'Sin especificar' };
  return { clave: claveDeTexto(crudo), etiqueta: capitalizar(crudo) };
};

/** Subtipos presentes en la lista, del más numeroso al menos numeroso. */
export const listarSubtipos = (activos) => {
  const porClave = new Map();
  activos.forEach((activo) => {
    const subtipo = subtipoDe(activo);
    if (!subtipo) return;
    const actual = porClave.get(subtipo.clave);
    if (actual) actual.cantidad += 1;
    else porClave.set(subtipo.clave, { ...subtipo, cantidad: 1 });
  });
  return [...porClave.values()].sort(
    (a, b) =>
      (a.clave === SIN_ESPECIFICAR) - (b.clave === SIN_ESPECIFICAR) ||
      b.cantidad - a.cantidad ||
      a.etiqueta.localeCompare(b.etiqueta)
  );
};

const contarSubtipos = (activos, subtipos) =>
  Object.fromEntries(
    subtipos.map((s) => [s.clave, activos.filter((a) => subtipoDe(a)?.clave === s.clave).length])
  );

/** Texto del tipo para mostrar: los periféricos indican además su subtipo. */
export const etiquetaDeTipo = (activo) => {
  const etiqueta =
    TIPO_ACTIVO_OPTIONS.find((o) => o.value === activo.tipo_activo)?.label || activo.tipo_activo || '';
  const subtipo = subtipoDe(activo);
  return subtipo && subtipo.clave !== SIN_ESPECIFICAR ? `${etiqueta} (${subtipo.etiqueta})` : etiqueta;
};

// ---------------------------------------------------------------------------

/**
 * Agrega a cada activo su oficina, su custodio y si está en bodega. El custodio
 * sale siempre del acta abierta (custodia o entrega): el activo no guarda a
 * quién está entregado. Es la misma regla que usa la pantalla de Activos.
 */
export const enriquecerActivos = (activos, oficinas, empleados, actas) => {
  const oficinaPorId = new Map(oficinas.map((o) => [o.id_oficina, o.nombre]));
  const empleadoPorId = new Map(
    empleados.map((e) => [e.id_empleado, `${e.nombre} ${e.apellido}`.trim()])
  );
  const actaAbiertaPorActivo = new Map();
  actas.forEach((acta) => {
    if (esActaAbierta(acta.estado_asignacion) && !actaAbiertaPorActivo.has(acta.id_activo)) {
      actaAbiertaPorActivo.set(acta.id_activo, acta);
    }
  });

  return activos.map((activo) => {
    const acta = actaAbiertaPorActivo.get(activo.id_activo);
    return {
      ...activo,
      oficina_nombre: oficinaPorId.get(activo.id_oficina) || '',
      empleado_asignado: acta ? empleadoPorId.get(acta.id_empleado) || '' : '',
      en_bodega: acta?.estado_asignacion === ESTADO_ACTA.CUSTODIA,
    };
  });
};

const contarTipo = (lista, tipo) => lista.filter((a) => a.tipo_activo === tipo).length;

const filaDeOficina = (oficina, enServicio, subtipos) => {
  const propios = enServicio.filter((a) => a.id_oficina === oficina.id_oficina);
  const pcs = contarTipo(propios, TIPO.DESKTOP);
  const laptops = contarTipo(propios, TIPO.LAPTOP);
  return {
    id_oficina: oficina.id_oficina,
    nombre: oficina.nombre,
    pcs,
    laptops,
    pcs_laptops: pcs + laptops,
    perifericos: contarTipo(propios, TIPO.PERIFERICO),
    subtipos: contarSubtipos(propios, subtipos),
    impresoras: contarTipo(propios, TIPO.IMPRESORA),
    moviles: contarTipo(propios, TIPO.MOVIL),
    total: propios.length,
    entregados: propios.filter((a) => !a.en_bodega && Boolean(a.empleado_asignado)).length,
    en_bodega: propios.filter((a) => a.en_bodega).length,
    fuera_de_servicio: propios.filter(estaFueraDeServicio).length,
  };
};

const sumar = (filas, campo) => filas.reduce((acc, f) => acc + f[campo], 0);

/**
 * Calcula los indicadores y la tabla de resumen por oficina. Recibe los
 * activos ya enriquecidos. Las oficinas salen ordenadas por cantidad de
 * equipos, de mayor a menor. Con incluirFueraDeServicio los equipos dados de
 * baja o robados/perdidos también cuentan en los totales; no tienen acta
 * abierta, así que no suman en Entregados ni en En bodega.
 */
export const calcularResumen = (activos, oficinas, incluirFueraDeServicio = false) => {
  const enServicio = activos.filter((a) => !estaFueraDeServicio(a));
  const contados = incluirFueraDeServicio ? activos : enServicio;
  const subtipos = listarSubtipos(contados);

  const conEquipos = new Set(contados.map((a) => a.id_oficina));
  const filas = oficinas
    .filter((o) => o.activo !== false || conEquipos.has(o.id_oficina))
    .map((o) => filaDeOficina(o, contados, subtipos))
    .sort((a, b) => b.total - a.total || a.nombre.localeCompare(b.nombre));

  const total = sumar(filas, 'total');
  const filasConPorcentaje = filas.map((f) => ({
    ...f,
    porcentaje: total === 0 ? 0 : f.total / total,
  }));

  const pcs = contarTipo(contados, TIPO.DESKTOP);
  const laptops = contarTipo(contados, TIPO.LAPTOP);

  return {
    subtipos,
    kpis: {
      total: contados.length,
      pcs,
      laptops,
      pcs_laptops: pcs + laptops,
      perifericos: contarTipo(contados, TIPO.PERIFERICO),
      en_bodega: contados.filter((a) => a.en_bodega).length,
      fuera_de_servicio: activos.length - enServicio.length,
    },
    filas: filasConPorcentaje,
    totales: {
      pcs: sumar(filas, 'pcs'),
      laptops: sumar(filas, 'laptops'),
      pcs_laptops: sumar(filas, 'pcs_laptops'),
      perifericos: sumar(filas, 'perifericos'),
      subtipos: Object.fromEntries(
        subtipos.map((s) => [s.clave, filas.reduce((acc, f) => acc + f.subtipos[s.clave], 0)])
      ),
      impresoras: sumar(filas, 'impresoras'),
      moviles: sumar(filas, 'moviles'),
      total,
      entregados: sumar(filas, 'entregados'),
      en_bodega: sumar(filas, 'en_bodega'),
      fuera_de_servicio: sumar(filas, 'fuera_de_servicio'),
    },
    // Equipos contados cuya oficina no aparece en la tabla (control de cuadre).
    sin_oficina_listada: contados.length - total,
  };
};

/**
 * Opciones del filtro de tipo del detalle: los tipos de activo y, a
 * continuación, un valor por cada subtipo de periférico existente.
 */
export const opcionesDeTipo = (subtipos) => [
  ...TIPO_ACTIVO_OPTIONS,
  ...subtipos.map((s) => ({
    value: `${PREFIJO_SUBTIPO}${s.clave}`,
    label: `Periférico: ${s.etiqueta}`,
  })),
];

/** Equipos de una oficina (o de todas) filtrados por tipo o subtipo. Por defecto solo los que están en servicio. */
export const filtrarDetalle = (activos, idOficina, tipo, incluirFueraDeServicio = false) =>
  activos
    .filter((a) => incluirFueraDeServicio || !estaFueraDeServicio(a))
    .filter((a) => idOficina === TODAS || a.id_oficina === idOficina)
    .filter((a) => {
      if (!tipo) return true;
      if (tipo.startsWith(PREFIJO_SUBTIPO)) {
        return subtipoDe(a)?.clave === tipo.slice(PREFIJO_SUBTIPO.length);
      }
      return a.tipo_activo === tipo;
    });
