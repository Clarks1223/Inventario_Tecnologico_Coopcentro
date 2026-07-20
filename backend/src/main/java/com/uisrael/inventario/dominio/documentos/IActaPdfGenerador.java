package com.uisrael.inventario.dominio.documentos;

import java.util.Map;
import java.util.Set;

/**
 * Puerto para rellenar una plantilla PDF (AcroForm) con datos de un acta.
 * La logica de que campo corresponde a que dato vive en el caso de uso
 * (aplicacion); este puerto solo sabe llenar campos de texto y marcar
 * casillas de una plantilla dada.
 */
public interface IActaPdfGenerador {
	byte[] generar(String nombrePlantilla, Map<String, String> valoresCampos, Set<String> casillasMarcadas);
}
