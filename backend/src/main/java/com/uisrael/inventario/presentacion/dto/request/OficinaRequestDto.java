package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OficinaRequestDto {

	@NotBlank
	private String nombre;

	@NotBlank
	private String direccion;

	@NotNull
	private Boolean activo;

}
