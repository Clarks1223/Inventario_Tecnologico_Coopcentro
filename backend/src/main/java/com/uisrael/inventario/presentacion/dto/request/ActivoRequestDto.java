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

	@NotBlank
	@Size(max = 50)
	private String codigoInventario;

	@NotBlank
	@Pattern(regexp = "impresora_termica|dispositivo_movil|desktop|laptop|periferico",
			message = "tipoActivo debe ser impresora_termica, dispositivo_movil, desktop, laptop o periferico")
	private String tipoActivo;

	@NotBlank
	@Size(min = 2, max = 20)
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]+$",
			message = "Solo se permiten letras y espacios."
	)
	private String marca;

	@NotBlank
	@Size(max = 100)
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü0-9\\s]+$",
			message = "Solo se permiten letras, números y espacios."
	)
	@Size(min = 3, max = 40)
	private String modelo;

	@NotBlank
	@Size(min = 5, max = 25)
	@Pattern(
			regexp = "^[\\p{L}0-9\\s/\\-]+$",
			message = "Solo se permiten letras, números, espacios, guiones (-) y barras (/)."
	)
	private String serial;

	@NotBlank
	@Pattern(regexp = "NO_ASIGNADO|ASIGNADO|DADO_DE_BAJA|ROBADO_PERDIDO",
			message = "estado debe ser NO_ASIGNADO, ASIGNADO, DADO_DE_BAJA o ROBADO_PERDIDO")
	private String estado;

	@NotNull
	@Positive
	private Integer idOficina;

	private String observaciones;

	@Valid
	private ActivoDetalleRequestDto detalle;

}
