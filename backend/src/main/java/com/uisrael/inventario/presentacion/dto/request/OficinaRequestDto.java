package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OficinaRequestDto {

	@NotBlank
	@Size(min = 3, max = 20)
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]+$",
			message = "Solo se permiten letras y espacios."
	)
	private String nombre;

	@NotBlank
	@Size(min =3, max = 100)
	private String direccion;

	@NotNull
	private Boolean activo;

}
