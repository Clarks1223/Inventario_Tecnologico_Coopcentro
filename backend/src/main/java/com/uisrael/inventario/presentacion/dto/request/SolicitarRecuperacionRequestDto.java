package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SolicitarRecuperacionRequestDto {

	@NotBlank
	@Email
	private String correo;

}
