package com.uisrael.inventario.dominio.entidades;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Conjunto fijo de activos (p. ej. un puesto de trabajo) que se entrega
 * completo a un empleado. El grupo no tiene custodia propia: quien responde por
 * cada activo lo sigue diciendo su acta abierta.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrupoActivos {

	private int idGrupo;
	private String nombre;
	private String descripcion;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
