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

	@NotBlank(message = "es obligatorio")
	@Pattern(regexp = "NO_ASIGNADO|ASIGNADO|DADO_DE_BAJA|ROBADO_PERDIDO",
			message = "debe ser NO_ASIGNADO, ASIGNADO, DADO_DE_BAJA o ROBADO_PERDIDO")
	private String estado;

	@NotNull(message = "es obligatorio")
	@Positive(message = "debe ser un número positivo")
	private Integer idOficina;

	private String observaciones;

	@Valid
	private ActivoDetalleRequestDto detalle;

}
