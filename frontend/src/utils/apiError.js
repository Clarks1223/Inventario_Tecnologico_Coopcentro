/**
 * Extrae el mensaje que envía el backend en `{ message: "..." }`.
 *
 * Las peticiones con `responseType: 'blob'` (la impresión de actas) devuelven
 * el cuerpo del error como Blob y no como JSON, así que hay que leerlo antes
 * de poder mostrarlo. Sin esto el mensaje real se perdía y siempre se veía un
 * texto genérico.
 */
export const mensajeDeError = async (error, mensajePorDefecto) => {
  const data = error?.response?.data;

  if (data instanceof Blob) {
    try {
      return JSON.parse(await data.text())?.message || mensajePorDefecto;
    } catch {
      return mensajePorDefecto;
    }
  }

  return data?.message || mensajePorDefecto;
};
