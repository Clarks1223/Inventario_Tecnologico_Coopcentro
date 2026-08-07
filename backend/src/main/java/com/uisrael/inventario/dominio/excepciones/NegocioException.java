package com.uisrael.inventario.dominio.excepciones;

/**
 * Regla de negocio incumplida por el usuario: dato duplicado, transicion de
 * estado invalida, registro inexistente, etc. El GlobalExceptionHandler la
 * traduce a un 400 mostrando su mensaje tal cual, porque esta redactado para
 * leerse en pantalla. Cualquier otra RuntimeException es un bug o un fallo de
 * infraestructura y sale como 500 generico, sin exponer detalles internos.
 */
public class NegocioException extends RuntimeException {

	public NegocioException(String mensaje) {
		super(mensaje);
	}

	public NegocioException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}

}
