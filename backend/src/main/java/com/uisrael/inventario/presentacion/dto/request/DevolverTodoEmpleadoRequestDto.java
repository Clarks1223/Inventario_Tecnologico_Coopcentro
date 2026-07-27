package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DevolverTodoEmpleadoRequestDto {

	@NotNull
	@Positive
	private Integer idEmpleado;

	private String motivo;

}
