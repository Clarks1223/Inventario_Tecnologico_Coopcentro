package com.uisrael.inventario.presentacion.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ActivoResponseDto {

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
