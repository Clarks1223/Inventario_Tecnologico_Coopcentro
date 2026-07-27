package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CargoRequestDto {

	@NotBlank
	@Size(min = 3, max = 40)
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]+$",
			message = "Solo se permiten letras y espacios."
	)
	private String nombre;

	@NotNull
	private Boolean activo;

}
