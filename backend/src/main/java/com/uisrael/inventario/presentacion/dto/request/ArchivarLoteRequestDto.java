package com.uisrael.inventario.presentacion.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/** Actas concretas a archivar, tras una devolucion masiva. */
@Data
public class ArchivarLoteRequestDto {

	@NotEmpty(message = "es obligatorio")
	private List<Integer> idsActas;

}
