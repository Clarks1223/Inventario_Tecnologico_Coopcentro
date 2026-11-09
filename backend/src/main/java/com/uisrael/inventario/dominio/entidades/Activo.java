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
	/** Descripcion libre y opcional del estado fisico del equipo. */
	private String descripcionEstado;
	/** Grupo al que pertenece; null si no esta en ninguno. */
	private Integer idGrupo;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
