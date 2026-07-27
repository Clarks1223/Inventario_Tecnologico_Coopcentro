package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpleadoRequestDto {

	@NotBlank
	@Size(min = 3, max = 60)
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]+$",
			message = "Solo se permiten letras y espacios."
	)
	private String nombre;

	@NotBlank
	@Size(min = 3, max = 60)
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]+$",
			message = "Solo se permiten letras y espacios."
	)
	private String apellido;

	@NotBlank
	@Pattern(regexp = "[0-9]{10}", message = "cedula debe tener exactamente 10 digitos numericos")
	private String cedula;

	@NotBlank
	@Email
	@Size(min = 11, max = 150)
	private String correo;

	@Pattern(regexp = "|[0-9]{4,4}", message = "extensionTelefonica debe tener entre 4 digitos numericos")
	private String extensionTelefonica;

	@NotNull
	@Positive
	private Integer idOficina;

	@NotNull
	@Positive
	private Integer idCargo;

	@Pattern(regexp = "administrador|usuario", message = "rol debe ser 'administrador' o 'usuario'")
	private String rol;

	@NotNull
	private Boolean activo;

}
