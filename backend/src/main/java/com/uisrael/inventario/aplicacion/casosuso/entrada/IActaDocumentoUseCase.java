package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

import com.uisrael.inventario.dominio.documentos.ActasArchivadas;

/**
 * Genera las actas y las archiva en disco. No devuelve el PDF: las actas no se
 * abren en el navegador, solo se guardan en su carpeta y se informa la ruta.
 */
public interface IActaDocumentoUseCase {

	/**
	 * Archiva un acta individual: cubre unicamente su propio activo.
	 *
	 * @param observacion comentario del tecnico para el documento. Nulo deja el
	 *                    que el acta ya tuviera.
	 */
	ActasArchivadas archivarActa(int idActa, String observacion);

	/**
	 * Archiva todas las actas imprimibles del empleado, agrupadas entre si (la
	 * PC con sus perifericos, el movil con su impresora).
	 */
	ActasArchivadas archivarActasDelEmpleado(int idEmpleado, String observacion);

	/**
	 * Archiva un lote de actas concretas, agrupadas entre si. Lo usa la
	 * devolucion masiva, cuyas actas ya estan cerradas cuando se emiten.
	 */
	ActasArchivadas archivarLote(List<Integer> idsActas);

	List<Integer> listarActasImprimibles(int idEmpleado);

}
