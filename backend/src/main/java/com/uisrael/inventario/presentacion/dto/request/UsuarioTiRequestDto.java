package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioTiRequestDto {

	@NotNull
	@Positive
	private Integer idEmpleado;

	@NotBlank
	@Email
	@Size(max = 254)
	private String correo;

}
