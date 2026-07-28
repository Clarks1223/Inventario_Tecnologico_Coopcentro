package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CargoRequestDto {

	@NotBlank(message = "es obligatorio")
	@Size(min = 3, max = 40, message = "debe tener entre 3 y 40 caracteres")
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]+$",
			message = "solo puede contener letras y espacios"
	)
	private String nombre;

	@NotNull(message = "es obligatorio")
	private Boolean activo;

}
