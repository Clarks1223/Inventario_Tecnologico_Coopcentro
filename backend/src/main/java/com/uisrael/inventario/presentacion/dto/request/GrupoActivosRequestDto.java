package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GrupoActivosRequestDto {

	@NotBlank(message = "es obligatorio")
	@Size(max = 100, message = "no debe superar 100 caracteres")
	private String nombre;

	private String descripcion;

}
