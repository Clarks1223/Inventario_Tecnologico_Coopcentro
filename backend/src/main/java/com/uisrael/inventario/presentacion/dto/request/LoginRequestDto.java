package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {

	@NotBlank(message = "es obligatorio")
	@Email(message = "debe ser un correo electrónico válido")
	private String correo;

	@NotBlank(message = "es obligatorio")
	private String contrasena;

}
