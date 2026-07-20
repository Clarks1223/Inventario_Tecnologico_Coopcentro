package com.uisrael.inventario.dominio.entidades;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioTi {

	private int idUsuarioTi;
	private int idEmpleado;
	private String correo;
	private String contrasena;
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaActualizacion;

}
