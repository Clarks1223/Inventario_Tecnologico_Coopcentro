package com.uisrael.inventario.dominio.documentos;

import java.util.List;

/**
 * Resultado de archivar una o varias actas en disco.
 *
 * @param rutas   ruta completa de cada archivo guardado
 * @param carpeta carpeta comun de todas ellas, que es lo que se muestra en la
 *                notificacion cuando son varias
 */
public record ActasArchivadas(List<String> rutas, String carpeta) {

	public int cantidad() {
		return rutas.size();
	}

}
