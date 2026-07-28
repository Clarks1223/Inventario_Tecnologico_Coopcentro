package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

public interface IActaDocumentoUseCase {
	byte[] generarPdf(int idActa);
	List<Integer> listarActasImprimibles(int idEmpleado);
}
