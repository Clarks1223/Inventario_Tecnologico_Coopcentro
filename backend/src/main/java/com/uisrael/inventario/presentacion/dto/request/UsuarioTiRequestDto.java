package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioTiRequestDto {

	@NotNull
	private Integer idEmpleado;

	@NotBlank
	private String correo;

}
