package com.uisrael.inventario.presentacion.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Donde quedaron guardadas las actas. Las actas no se abren en el navegador:
 * se archivan en disco y el frontend informa la ruta al usuario.
 */
@Data
@AllArgsConstructor
public class ActasArchivadasResponseDto {

	private List<String> rutas;
	private String carpeta;
	private int cantidad;

}
