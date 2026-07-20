package com.uisrael.inventario.dominio.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SesionUsuario {

	private int idUsuarioTi;
	private int idEmpleado;
	private String nombre;
	private String apellido;
	private String correo;
	private String rol;

}
