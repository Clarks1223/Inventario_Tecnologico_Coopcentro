package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivoRequestDto {

	@NotBlank
	private String codigoInventario;

	@NotBlank
	private String tipoActivo;

	private String marca;

	private String modelo;

	private String serial;

	@NotBlank
	private String estado;

	@NotNull
	private Integer idOficina;

	private String observaciones;

}
