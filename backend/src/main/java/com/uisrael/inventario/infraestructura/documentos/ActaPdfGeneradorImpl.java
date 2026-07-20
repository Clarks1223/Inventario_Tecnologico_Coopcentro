package com.uisrael.inventario.infraestructura.documentos;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import com.uisrael.inventario.dominio.documentos.IActaPdfGenerador;

public class ActaPdfGeneradorImpl implements IActaPdfGenerador {

	private final String directorioPlantillas;

	public ActaPdfGeneradorImpl(String directorioPlantillas) {
		this.directorioPlantillas = directorioPlantillas;
	}

	@Override
	public byte[] generar(String nombrePlantilla, Map<String, String> valoresCampos, Set<String> casillasMarcadas) {
		File archivoPlantilla = new File(directorioPlantillas, nombrePlantilla);
		if (!archivoPlantilla.exists()) {
			throw new RuntimeException("No se encontro la plantilla de acta: " + archivoPlantilla.getAbsolutePath());
		}

		try (PDDocument documento = Loader.loadPDF(archivoPlantilla)) {
			PDAcroForm acroForm = documento.getDocumentCatalog().getAcroForm();
			if (acroForm == null) {
				throw new RuntimeException("La plantilla no tiene campos de formulario: " + nombrePlantilla);
			}
			acroForm.setNeedAppearances(false);

			for (Map.Entry<String, String> entrada : valoresCampos.entrySet()) {
				PDField campo = acroForm.getField(entrada.getKey());
				if (campo != null && entrada.getValue() != null) {
					campo.setValue(entrada.getValue());
				}
			}

			for (String nombreCasilla : casillasMarcadas) {
				PDField campo = acroForm.getField(nombreCasilla);
				if (campo instanceof PDCheckBox casilla) {
					casilla.check();
				}
			}

			ByteArrayOutputStream salida = new ByteArrayOutputStream();
			documento.save(salida);
			return salida.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Error generando el PDF del acta a partir de " + nombrePlantilla, e);
		}
	}

}
