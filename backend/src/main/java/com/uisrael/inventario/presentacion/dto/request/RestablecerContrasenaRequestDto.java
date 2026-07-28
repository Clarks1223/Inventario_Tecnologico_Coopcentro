package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestablecerContrasenaRequestDto {

	@NotBlank(message = "es obligatorio")
	private String token;

	@NotBlank(message = "es obligatorio")
	private String contrasenaNueva;

	@NotBlank(message = "es obligatorio")
	private String confirmarContrasenaNueva;

}
