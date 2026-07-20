package com.uisrael.inventario.dominio.entidades;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActaEntregaRecepcion {

	private int idActa;
	private int idActivo;
	private int idEmpleado;
	private int idUsuarioTi;
	private LocalDateTime fechaAsignacion;
	private LocalDateTime fechaDevolucion;
	private String estadoAsignacion;
	private String motivo;

}
