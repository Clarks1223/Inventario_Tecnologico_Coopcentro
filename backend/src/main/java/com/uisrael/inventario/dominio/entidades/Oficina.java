package com.uisrael.inventario.dominio.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Oficina {

	private int idOficina;
	private String nombre;
	private String direccion;
	private boolean activo;

}
