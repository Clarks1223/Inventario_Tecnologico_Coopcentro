package com.uisrael.inventario.presentacion.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AgregarActivosGrupoRequestDto {

	@NotEmpty(message = "debe incluir al menos un activo")
	private List<@NotNull @Positive Integer> idsActivos;

}
