package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

	private String numeroTelefono;

	@NotNull
	private Boolean activo;

}
