package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarActivoRequestDto {

	@NotNull
	private Integer idActivo;

	@NotNull
	private Integer idEmpleado;

	@NotNull
	private Integer idUsuarioTi;

	private String motivo;

}
