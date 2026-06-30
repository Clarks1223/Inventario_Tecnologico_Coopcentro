package com.uisrael.inventario.presentacion.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AsignacionActivoResponseDto {

	private int idAsignacion;
	private int idActivo;
	private int idEmpleado;
	private LocalDateTime fechaAsignacion;
	private LocalDateTime fechaDevolucion;
	private String estadoAsignacion;
	private String motivo;

}
