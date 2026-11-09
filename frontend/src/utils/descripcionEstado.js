// Descripción libre y opcional del estado físico del equipo. Mismos límites
// que valida el backend (ActivoRequestDto.descripcionEstado).
export const MIN_DESCRIPCION_ESTADO = 3;
export const MAX_DESCRIPCION_ESTADO = 250;

/** Mensaje de error para el campo, o null si es válido (vacío es válido). */
export const errorDescripcionEstado = (texto) => {
  const valor = (texto || '').trim();
  if (!valor) return null;
  if (valor.length < MIN_DESCRIPCION_ESTADO) return `Debe tener al menos ${MIN_DESCRIPCION_ESTADO} caracteres`;
  if (valor.length > MAX_DESCRIPCION_ESTADO) return `No debe superar ${MAX_DESCRIPCION_ESTADO} caracteres`;
  return null;
};

/** Valor a enviar al backend: null si está vacío, recortado si no. */
export const normalizarDescripcionEstado = (texto) => {
  const valor = (texto || '').trim();
  return valor || null;
};
