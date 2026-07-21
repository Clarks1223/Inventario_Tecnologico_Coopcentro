package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DevolverTodoEmpleadoRequestDto {

	@NotNull
	private Integer idEmpleado;

	private String motivo;

}
