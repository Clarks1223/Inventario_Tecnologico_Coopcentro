package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioTiRequestDto {

	@NotNull(message = "es obligatorio")
	@Positive(message = "debe ser un número positivo")
	private Integer idEmpleado;

	@NotBlank(message = "es obligatorio")
	@Email(message = "debe ser un correo electrónico válido")
	@Size(max = 254, message = "no debe superar 254 caracteres")
	private String correo;

}
