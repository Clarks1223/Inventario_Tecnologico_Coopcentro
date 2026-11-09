/**
 * filterOptions para los Autocomplete de activos: busca en todos los campos
 * visibles (serial, código, marca, modelo y tipo), no solo en el serial.
 */
export const filtrarActivosPorTexto = (options, { inputValue }) => {
  const texto = inputValue.trim().toLowerCase();
  if (!texto) return options;
  return options.filter((a) =>
    [a.serial, a.codigo_inventario, a.marca, a.modelo, a.tipo_activo]
      .filter(Boolean)
      .some((campo) => String(campo).toLowerCase().includes(texto))
  );
};
