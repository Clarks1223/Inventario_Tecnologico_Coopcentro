export const TIPO_ACTIVO_OPTIONS = [
  { value: 'periferico', label: 'Periférico' },
  { value: 'desktop', label: 'Desktop' },
  { value: 'laptop', label: 'Laptop' },
  { value: 'dispositivo_movil', label: 'Dispositivo Móvil' },
  { value: 'impresora_termica', label: 'Impresora Térmica' },
];

export const MARCAS_OPTIONS = [
  'HP', 'DELL', 'LENOVO', 'ASUS', 'ACER', 'APPLE',
  'EPSON', 'GENIUS', 'LOGITECH', 'XIAOMI', 'SAMSUNG',
];

// Situación del activo. A quién está entregado NO se guarda aquí: se deriva
// del acta abierta que tenga (ver ESTADO_ACTA).
export const ESTADO_ACTIVO = {
  OPERATIVO: 'OPERATIVO',
  DADO_DE_BAJA: 'DADO_DE_BAJA',
  ROBADO_PERDIDO: 'ROBADO_PERDIDO',
};

export const ESTADO_OPTIONS = [
  { value: ESTADO_ACTIVO.OPERATIVO, label: 'Operativo' },
  { value: ESTADO_ACTIVO.DADO_DE_BAJA, label: 'Dado de Baja' },
  { value: ESTADO_ACTIVO.ROBADO_PERDIDO, label: 'Robado/Perdido' },
];

export const ESTADO_ACTIVO_CHIP = {
  [ESTADO_ACTIVO.OPERATIVO]: { label: 'Operativo', color: 'success' },
  [ESTADO_ACTIVO.DADO_DE_BAJA]: { label: 'Dado de Baja', color: 'default' },
  [ESTADO_ACTIVO.ROBADO_PERDIDO]: { label: 'Robado/Perdido', color: 'warning' },
};

/**
 * Etiqueta legible del estado. Un valor desconocido se muestra en rojo con su
 * texto crudo: son filas antiguas (NO_ASIGNADO / ASIGNADO) que quedaron sin
 * migrar, y conviene que salten a la vista en vez de disfrazarse de normales.
 */
export const chipDeEstadoActivo = (estado) =>
  ESTADO_ACTIVO_CHIP[estado] || { label: estado || 'Sin estado', color: 'error' };

export const etiquetaDeEstadoActivo = (estado) => chipDeEstadoActivo(estado).label;

// Dónde está el activo ahora mismo. No es un campo de la tabla activos: se
// deriva de si su acta abierta es de custodia o de entrega. Sustituye al viejo
// filtro por estado NO_ASIGNADO / ASIGNADO.
export const CUSTODIA_FILTRO = {
  BODEGA: 'bodega',
  ENTREGADO: 'entregado',
};

export const CUSTODIA_FILTRO_OPTIONS = [
  { value: CUSTODIA_FILTRO.BODEGA, label: 'En bodega (TI)' },
  { value: CUSTODIA_FILTRO.ENTREGADO, label: 'Entregado' },
];

/**
 * Un activo ya enriquecido tiene tres situaciones posibles de custodia. Los
 * que no están operativos no tienen acta abierta, así que no son ni bodega ni
 * entrega: se muestran con un guion.
 */
export const etiquetaDeCustodia = (activo) => {
  if (activo.en_bodega) return 'En bodega (TI)';
  return activo.empleado_asignado ? 'Entregado' : '—';
};

// Estado del acta dentro de la cadena de custodia. Un activo operativo tiene
// siempre exactamente un acta abierta (custodia o activa), de modo que nunca
// queda sin responsable.
export const ESTADO_ACTA = {
  CUSTODIA: 'custodia',
  ACTIVA: 'activa',
  DEVUELTA: 'devuelta',
};

export const ESTADO_ACTA_OPTIONS = [
  { value: ESTADO_ACTA.CUSTODIA, label: 'En bodega (TI)' },
  { value: ESTADO_ACTA.ACTIVA, label: 'Entregada' },
  { value: ESTADO_ACTA.DEVUELTA, label: 'Devuelta' },
];

export const ESTADO_ACTA_CHIP = {
  [ESTADO_ACTA.CUSTODIA]: { label: 'En bodega (TI)', color: 'info' },
  [ESTADO_ACTA.ACTIVA]: { label: 'Entregada', color: 'success' },
  [ESTADO_ACTA.DEVUELTA]: { label: 'Devuelta', color: 'default' },
};

export const esActaAbierta = (estadoAsignacion) =>
  estadoAsignacion === ESTADO_ACTA.CUSTODIA || estadoAsignacion === ESTADO_ACTA.ACTIVA;
