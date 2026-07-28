package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambiarContrasenaRequestDto {

	@NotBlank
	private String contrasenaActual;

	@NotBlank
	private String contrasenaNueva;

	@NotBlank
	private String confirmarContrasenaNueva;

}
