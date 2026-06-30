package com.uisrael.inventario.presentacion.dto.response;

import lombok.Data;

@Data
public class EmpleadoResponseDto {

	private int idEmpleado;
	private String nombre;
	private String apellido;
	private String cedula;
	private String correo;
	private String extensionTelefonica;
	private int idOficina;
	private int idCargo;
	private String numeroTelefono;
	private boolean activo;

}
