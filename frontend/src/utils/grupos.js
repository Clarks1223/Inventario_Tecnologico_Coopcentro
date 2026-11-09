import { ESTADO_ACTIVO } from '../constants/activosConstants';

// Estado de un grupo de activos. Lo calcula el backend a partir de las actas
// abiertas de sus miembros; aquí solo se presenta.
export const ESTADO_GRUPO = {
  VACIO: 'VACIO',
  EN_BODEGA: 'EN_BODEGA',
  ASIGNADO: 'ASIGNADO',
  MIXTO: 'MIXTO',
};

const ETIQUETAS = {
  [ESTADO_GRUPO.VACIO]: 'Vacío',
  [ESTADO_GRUPO.EN_BODEGA]: 'En bodega',
  [ESTADO_GRUPO.MIXTO]: 'Mixto',
};

const COLORES = {
  [ESTADO_GRUPO.VACIO]: 'default',
  [ESTADO_GRUPO.EN_BODEGA]: 'success',
  [ESTADO_GRUPO.ASIGNADO]: 'info',
  [ESTADO_GRUPO.MIXTO]: 'warning',
};

export const etiquetaEstadoGrupo = (grupo) => {
  if (grupo?.estado === ESTADO_GRUPO.ASIGNADO) {
    return `Asignado a ${grupo.nombre_empleado_asignado || 'un empleado'}`;
  }
  return ETIQUETAS[grupo?.estado] || grupo?.estado || '';
};

export const colorEstadoGrupo = (grupo) => COLORES[grupo?.estado] || 'default';

/** Solo un grupo con todos sus activos en bodega se puede entregar. */
export const esAsignable = (grupo) => grupo?.estado === ESTADO_GRUPO.EN_BODEGA;

/** Activos que pueden entrar a un grupo: operativos y sin grupo. */
export const activosAgrupables = (activos) =>
  activos.filter((a) => a.estado === ESTADO_ACTIVO.OPERATIVO && a.id_grupo == null);
