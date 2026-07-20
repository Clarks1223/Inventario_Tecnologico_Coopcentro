package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ActivoRequestDto {

	@NotBlank
	private String codigoInventario;

	@NotBlank
	@Pattern(regexp = "impresora_termica|dispositivo_movil|desktop|laptop|periferico",
			message = "tipoActivo debe ser impresora_termica, dispositivo_movil, desktop, laptop o periferico")
	private String tipoActivo;

	private String marca;

	private String modelo;

	private String serial;

	@NotBlank
	private String estado;

	@NotNull
	private Integer idOficina;

	private String observaciones;

	private ActivoDetalleRequestDto detalle;

}
