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

	@NotBlank(message = "es obligatorio")
	@Size(min = 3, max = 60, message = "debe tener entre 3 y 60 caracteres")
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]+$",
			message = "solo puede contener letras y espacios"
	)
	private String nombre;

	@NotBlank(message = "es obligatorio")
	@Size(min = 3, max = 60, message = "debe tener entre 3 y 60 caracteres")
	@Pattern(
			regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]+$",
			message = "solo puede contener letras y espacios"
	)
	private String apellido;

	@NotBlank(message = "es obligatorio")
	@Pattern(regexp = "[0-9]{10}", message = "debe tener exactamente 10 dígitos numéricos")
	private String cedula;

	@NotBlank(message = "es obligatorio")
	@Email(message = "debe ser un correo electrónico válido")
	@Size(min = 11, max = 150, message = "debe tener entre 11 y 150 caracteres")
	private String correo;

	@Pattern(regexp = "|[0-9]{4,4}", message = "debe tener exactamente 4 dígitos numéricos")
	private String extensionTelefonica;

	@NotNull(message = "es obligatorio")
	@Positive(message = "debe ser un número positivo")
	private Integer idOficina;

	@NotNull(message = "es obligatorio")
	@Positive(message = "debe ser un número positivo")
	private Integer idCargo;

	@Pattern(regexp = "administrador|usuario", message = "debe ser 'administrador' o 'usuario'")
	private String rol;

	@NotNull(message = "es obligatorio")
	private Boolean activo;

}
