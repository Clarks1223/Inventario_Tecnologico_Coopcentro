package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambiarContrasenaRequestDto {

	@NotBlank(message = "es obligatorio")
	private String contrasenaActual;

	@NotBlank(message = "es obligatorio")
	private String contrasenaNueva;

	@NotBlank(message = "es obligatorio")
	private String confirmarContrasenaNueva;

}
