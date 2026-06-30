package com.uisrael.inventario.dominio.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Empleado {

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
