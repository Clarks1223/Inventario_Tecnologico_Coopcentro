package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestablecerContrasenaRequestDto {

	@NotBlank
	private String token;

	@NotBlank
	private String contrasenaNueva;

	@NotBlank
	private String confirmarContrasenaNueva;

}
