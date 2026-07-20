package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmpleadoRequestDto {

	@NotBlank
	private String nombre;

	@NotBlank
	private String apellido;

	@NotBlank
	private String cedula;

	@NotBlank
	private String correo;

	private String extensionTelefonica;

	@NotNull
	private Integer idOficina;

	@NotNull
	private Integer idCargo;

	@Pattern(regexp = "administrador|usuario", message = "rol debe ser 'administrador' o 'usuario'")
	private String rol;

	@NotNull
	private Boolean activo;

}
