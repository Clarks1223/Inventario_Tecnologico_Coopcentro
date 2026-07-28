package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OficinaRequestDto {

	@NotBlank(message = "es obligatorio")
	@Size(min = 3, max = 20, message = "debe tener entre 3 y 20 caracteres")
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]+$",
			message = "solo puede contener letras y espacios"
	)
	private String nombre;

	@NotBlank(message = "es obligatorio")
	@Size(min = 3, max = 100, message = "debe tener entre 3 y 100 caracteres")
	private String direccion;

	@NotNull(message = "es obligatorio")
	private Boolean activo;

}
