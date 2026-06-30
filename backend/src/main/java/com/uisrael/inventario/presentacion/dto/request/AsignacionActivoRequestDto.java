package com.uisrael.inventario.presentacion.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignacionActivoRequestDto {

	@NotNull
	private Integer idActivo;

	@NotNull
	private Integer idEmpleado;

	@NotNull
	private LocalDateTime fechaAsignacion;

	private LocalDateTime fechaDevolucion;

	@NotBlank
	private String estadoAsignacion;

	private String motivo;

}
