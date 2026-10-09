import { describe, it, expect } from 'vitest';
import {
  enriquecerActivos, calcularResumen, filtrarDetalle, listarSubtipos, opcionesDeTipo, etiquetaDeTipo, TODAS,
} from '../dashboardStats';

const oficinas = [
  { id_oficina: 1, nombre: 'Matriz', activo: true },
  { id_oficina: 2, nombre: 'Latacunga', activo: true },
  { id_oficina: 3, nombre: 'Cerrada', activo: false },
];
const empleados = [
  { id_empleado: 10, nombre: 'Ana', apellido: 'Loor' },
  { id_empleado: 11, nombre: 'Luis', apellido: 'Paz' },
];
const activos = [
  { id_activo: 1, tipo_activo: 'desktop', estado: 'OPERATIVO', id_oficina: 1 },
  { id_activo: 2, tipo_activo: 'laptop', estado: 'OPERATIVO', id_oficina: 1 },
  { id_activo: 3, tipo_activo: 'periferico', estado: 'OPERATIVO', id_oficina: 2 },
  { id_activo: 4, tipo_activo: 'desktop', estado: 'DADO_DE_BAJA', id_oficina: 1 },
  { id_activo: 5, tipo_activo: 'impresora_termica', estado: 'OPERATIVO', id_oficina: 2 },
];
const perifericos = [
  { id_activo: 20, tipo_activo: 'periferico', estado: 'OPERATIVO', id_oficina: 1, detalle: { tipo_dispositivo: 'Diadema' } },
  { id_activo: 21, tipo_activo: 'periferico', estado: 'OPERATIVO', id_oficina: 2, detalle: { tipo_dispositivo: ' diademas ' } },
  { id_activo: 22, tipo_activo: 'periferico', estado: 'OPERATIVO', id_oficina: 2, detalle: { tipo_dispositivo: 'Audífonos' } },
  { id_activo: 23, tipo_activo: 'periferico', estado: 'OPERATIVO', id_oficina: 1, detalle: {} },
  { id_activo: 24, tipo_activo: 'periferico', estado: 'DADO_DE_BAJA', id_oficina: 1, detalle: { tipo_dispositivo: 'Mouse' } },
];
const actas = [
  { id_activo: 1, id_empleado: 10, estado_asignacion: 'activa' },
  { id_activo: 2, id_empleado: 11, estado_asignacion: 'custodia' },
  { id_activo: 2, id_empleado: 10, estado_asignacion: 'devuelta' },
  { id_activo: 3, id_empleado: 10, estado_asignacion: 'activa' },
  { id_activo: 5, id_empleado: 11, estado_asignacion: 'activa' },
];

describe('enriquecerActivos', () => {
  const enriquecidos = enriquecerActivos(activos, oficinas, empleados, actas);

  it('resuelve oficina y custodio desde el acta abierta', () => {
    expect(enriquecidos[0]).toMatchObject({
      oficina_nombre: 'Matriz', empleado_asignado: 'Ana Loor', en_bodega: false,
    });
  });

  it('marca en bodega cuando el acta abierta es de custodia', () => {
    expect(enriquecidos[1]).toMatchObject({ empleado_asignado: 'Luis Paz', en_bodega: true });
  });

  it('un activo sin acta abierta no tiene custodio ni está en bodega', () => {
    expect(enriquecidos[3]).toMatchObject({ empleado_asignado: '', en_bodega: false });
  });
});

describe('subtipos de periférico', () => {
  const lista = listarSubtipos(perifericos.filter((a) => a.estado === 'OPERATIVO'));

  it('agrupa variantes de mayúsculas, espacios y plural en un solo subtipo', () => {
    const diadema = lista.find((s) => s.clave === 'diadema');
    expect(diadema).toMatchObject({ etiqueta: 'Diadema', cantidad: 2 });
  });

  it('ordena por cantidad y deja "Sin especificar" al final', () => {
    expect(lista.map((s) => s.etiqueta)).toEqual(['Diadema', 'Audífonos', 'Sin especificar']);
  });

  it('los subtipos aparecen en kpis, filas y totales del resumen', () => {
    const datos = enriquecerActivos([...activos, ...perifericos], oficinas, empleados, actas);
    const r = calcularResumen(datos, oficinas);
    expect(r.subtipos.map((s) => s.clave)).toEqual(['diadema', 'audifono', 'sin-especificar']);
    expect(r.totales.subtipos).toEqual({ diadema: 2, audifono: 1, 'sin-especificar': 2 });
    expect(r.filas.find((f) => f.nombre === 'Matriz').subtipos.diadema).toBe(1);
    expect(r.filas.find((f) => f.nombre === 'Latacunga').subtipos.diadema).toBe(1);
  });

  it('no cuenta los subtipos de periféricos dados de baja', () => {
    expect(lista.find((s) => s.clave === 'mouse')).toBeUndefined();
  });

  it('el filtro de tipo acepta un subtipo y las opciones lo incluyen', () => {
    const datos = enriquecerActivos(perifericos, oficinas, empleados, actas);
    expect(filtrarDetalle(datos, TODAS, 'periferico:diadema').map((a) => a.id_activo)).toEqual([20, 21]);
    expect(opcionesDeTipo(lista).map((o) => o.value)).toContain('periferico:audifono');
  });

  it('la etiqueta de tipo de un periférico incluye su subtipo', () => {
    expect(etiquetaDeTipo(perifericos[0])).toBe('Periférico (Diadema)');
    expect(etiquetaDeTipo(perifericos[3])).toBe('Periférico');
    expect(etiquetaDeTipo(activos[0])).toBe('Desktop');
  });
});

describe('calcularResumen', () => {
  const resumen = calcularResumen(
    enriquecerActivos(activos, oficinas, empleados, actas),
    oficinas
  );

  it('calcula los indicadores sin contar los activos dados de baja', () => {
    expect(resumen.kpis).toEqual({
      total: 4, pcs: 1, laptops: 1, pcs_laptops: 2, perifericos: 1, en_bodega: 1, fuera_de_servicio: 1,
    });
  });

  it('arma una fila por oficina activa, ordenada por total', () => {
    expect(resumen.filas.map((f) => f.nombre)).toEqual(['Latacunga', 'Matriz']);
    const matriz = resumen.filas.find((f) => f.nombre === 'Matriz');
    expect(matriz).toMatchObject({ pcs: 1, laptops: 1, total: 2, entregados: 1, en_bodega: 1 });
  });

  it('el porcentaje se calcula sobre el total de la tabla', () => {
    expect(resumen.filas.map((f) => f.porcentaje)).toEqual([0.5, 0.5]);
  });

  it('los totales cuadran y el control de oficina no listada es cero', () => {
    expect(resumen.totales).toMatchObject({ total: 4, impresoras: 1, entregados: 3, en_bodega: 1 });
    expect(resumen.sin_oficina_listada).toBe(0);
  });

  it('el control detecta equipos con oficina no listada', () => {
    const conHuerfano = [...activos, { id_activo: 9, tipo_activo: 'desktop', estado: 'OPERATIVO', id_oficina: null }];
    const r = calcularResumen(enriquecerActivos(conHuerfano, oficinas, empleados, actas), oficinas);
    expect(r.sin_oficina_listada).toBe(1);
  });

  it('una oficina inactiva aparece solo si tiene equipos', () => {
    const conEquipo = [...activos, { id_activo: 8, tipo_activo: 'laptop', estado: 'OPERATIVO', id_oficina: 3 }];
    const r = calcularResumen(enriquecerActivos(conEquipo, oficinas, empleados, actas), oficinas);
    expect(r.filas.map((f) => f.nombre)).toContain('Cerrada');
  });

  it('no divide por cero sin activos', () => {
    const r = calcularResumen([], oficinas);
    expect(r.kpis.total).toBe(0);
    expect(r.filas.every((f) => f.porcentaje === 0)).toBe(true);
  });
});

describe('incluir dados de baja y robados', () => {
  const datos = enriquecerActivos(
    [...activos, { id_activo: 30, tipo_activo: 'laptop', estado: 'ROBADO_PERDIDO', id_oficina: 2 }],
    oficinas, empleados, actas
  );

  it('apagado, no los cuenta en los totales', () => {
    const r = calcularResumen(datos, oficinas, false);
    expect(r.kpis).toMatchObject({ total: 4, laptops: 1, fuera_de_servicio: 2 });
    expect(r.totales.fuera_de_servicio).toBe(0);
  });

  it('encendido, los suma al total, por tipo y por oficina', () => {
    const r = calcularResumen(datos, oficinas, true);
    expect(r.kpis).toMatchObject({ total: 6, pcs: 2, laptops: 2, fuera_de_servicio: 2 });
    expect(r.totales).toMatchObject({ total: 6, entregados: 3, en_bodega: 1, fuera_de_servicio: 2 });
    expect(r.filas.find((f) => f.nombre === 'Latacunga').fuera_de_servicio).toBe(1);
    expect(r.sin_oficina_listada).toBe(0);
  });

  it('el detalle los incluye solo cuando se pide', () => {
    expect(filtrarDetalle(datos, TODAS, '')).toHaveLength(4);
    expect(filtrarDetalle(datos, TODAS, '', true)).toHaveLength(6);
  });
});

describe('filtrarDetalle', () => {
  const enriquecidos = enriquecerActivos(activos, oficinas, empleados, actas);

  it('filtra por oficina y excluye los dados de baja', () => {
    expect(filtrarDetalle(enriquecidos, 1, '').map((a) => a.id_activo)).toEqual([1, 2]);
  });

  it('filtra por tipo', () => {
    expect(filtrarDetalle(enriquecidos, TODAS, 'desktop').map((a) => a.id_activo)).toEqual([1]);
  });

  it('todas las oficinas devuelve todos los equipos en servicio', () => {
    expect(filtrarDetalle(enriquecidos, TODAS, '')).toHaveLength(4);
  });
});
