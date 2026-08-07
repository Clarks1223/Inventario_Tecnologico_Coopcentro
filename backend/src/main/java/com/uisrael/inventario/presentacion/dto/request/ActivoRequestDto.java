package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivoRequestDto {

	@NotBlank(message = "es obligatorio")
	@Size(max = 50, message = "no debe superar 50 caracteres")
	private String codigoInventario;

	@NotBlank(message = "es obligatorio")
	@Pattern(regexp = "impresora_termica|dispositivo_movil|desktop|laptop|periferico",
			message = "debe ser impresora_termica, dispositivo_movil, desktop, laptop o periferico")
	private String tipoActivo;

	@NotBlank(message = "es obligatorio")
	@Size(min = 2, max = 20, message = "debe tener entre 2 y 20 caracteres")
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]+$",
			message = "solo puede contener letras y espacios"
	)
	private String marca;

	@NotBlank(message = "es obligatorio")
	@Size(min = 3, max = 40, message = "debe tener entre 3 y 40 caracteres")
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü0-9\\s]+$",
			message = "solo puede contener letras, números y espacios"
	)
	private String modelo;

	@NotBlank(message = "es obligatorio")
	@Size(min = 5, max = 25, message = "debe tener entre 5 y 25 caracteres")
	@Pattern(
			regexp = "^[\\p{L}0-9\\s/\\-]+$",
			message = "solo puede contener letras, números, espacios, guiones (-) y barras (/)"
	)
	private String serial;

	// Situacion del activo. A quien esta entregado NO se guarda aqui: se deriva
	// del acta abierta. Al crear un activo este campo se ignora y siempre nace
	// OPERATIVO bajo la custodia de quien lo registra.
	@NotBlank(message = "es obligatorio")
	@Pattern(regexp = "OPERATIVO|DADO_DE_BAJA|ROBADO_PERDIDO",
			message = "debe ser OPERATIVO, DADO_DE_BAJA o ROBADO_PERDIDO")
	private String estado;

	@NotNull(message = "es obligatorio")
	@Positive(message = "debe ser un número positivo")
	private Integer idOficina;

	@NotNull(message = "es obligatorio")
	@Positive(message = "debe ser un número positivo")
	private Integer idUsuarioTi;

	// El limite lo impone el acta: en la celda de observacion mas angosta
	// (147 pt de las plantillas de PC) caben 40 caracteres al tamano de fuente
	// del formulario. Se deja en 30 para que quepa holgado en cualquier caso.
	@Size(max = 30, message = "no debe superar 30 caracteres")
	private String observaciones;

	@Valid
	private ActivoDetalleRequestDto detalle;

}
