package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AsignarActivoRequestDto {

	@NotNull
	@Positive
	private Integer idActivo;

	@NotNull
	@Positive
	private Integer idEmpleado;

	@NotNull
	@Positive
	private Integer idUsuarioTi;

	private String motivo;

}
