/**
 * Elimina los espacios al inicio y al final, colapsa los espacios internos
 * repetidos y deja cada palabra con la primera letra en mayúscula y el resto
 * en minúsculas. Ej.: "  dayana   ALEXANDRA " -> "Dayana Alexandra".
 * Debe coincidir con la regla PATRON_NOMBRE_PROPIO de EmpleadoRequestDto.
 */
export const formatearNombrePropio = (valor) =>
  (valor || '')
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .map((palabra) => palabra.charAt(0).toLocaleUpperCase('es') + palabra.slice(1).toLocaleLowerCase('es'))
    .join(' ');
