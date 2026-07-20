package com.uisrael.inventario.presentacion.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ActaEntregaRecepcionResponseDto {

	private int idActa;
	private int idActivo;
	private int idEmpleado;
	private int idUsuarioTi;
	private LocalDateTime fechaAsignacion;
	private LocalDateTime fechaDevolucion;
	private String estadoAsignacion;
	private String motivo;

}
