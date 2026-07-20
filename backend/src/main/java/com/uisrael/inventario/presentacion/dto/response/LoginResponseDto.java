package com.uisrael.inventario.presentacion.dto.response;

import lombok.Data;

@Data
public class LoginResponseDto {

	private int idUsuarioTi;
	private int idEmpleado;
	private String nombre;
	private String apellido;
	private String correo;
	private String rol;

}
