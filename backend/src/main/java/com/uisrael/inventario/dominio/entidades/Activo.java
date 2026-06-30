package com.uisrael.inventario.dominio.entidades;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Activo {

	private int idActivo;
	private String codigoInventario;
	private String tipoActivo;
	private String marca;
	private String modelo;
	private String serial;
	private String estado;
	private int idOficina;
	private String observaciones;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
