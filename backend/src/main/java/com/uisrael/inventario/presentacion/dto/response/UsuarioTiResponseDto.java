package com.uisrael.inventario.presentacion.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UsuarioTiResponseDto {

	private int idUsuarioTi;
	private int idEmpleado;
	private String correo;
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaActualizacion;

}
