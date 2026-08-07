package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DevolverTodoEmpleadoRequestDto {

	@NotNull(message = "es obligatorio")
	@Positive(message = "debe ser un número positivo")
	private Integer idEmpleado;

	private String motivo;

	/** Comentario opcional que sale impreso en todas las actas de recepcion. */
	@Size(max = 100, message = "no debe superar 100 caracteres")
	private String observacion;

	@NotNull(message = "es obligatorio")
	@Positive(message = "debe ser un número positivo")
	private Integer idUsuarioTi;

}
