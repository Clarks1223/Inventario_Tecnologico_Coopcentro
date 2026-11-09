package com.uisrael.inventario.presentacion.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class GrupoActivosResponseDto {

	private int idGrupo;
	private String nombre;
	private String descripcion;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	/** VACIO, EN_BODEGA, ASIGNADO o MIXTO; derivado de las actas abiertas. */
	private String estado;
	private Integer idEmpleadoAsignado;
	private String nombreEmpleadoAsignado;
	private List<ActivoGrupoResponseDto> activos;

}
