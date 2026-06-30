package com.uisrael.inventario.presentacion.dto.response;

import lombok.Data;

@Data
public class OficinaResponseDto {

	private int idOficina;
	private String nombre;
	private String direccion;
	private boolean activo;

}
