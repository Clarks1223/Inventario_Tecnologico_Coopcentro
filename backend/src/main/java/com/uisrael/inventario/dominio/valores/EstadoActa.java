package com.uisrael.inventario.dominio.valores;

/**
 * Estado de un acta dentro de la cadena de custodia. En todo momento un activo
 * operativo tiene exactamente un acta abierta (CUSTODIA o ACTIVA), de modo que
 * siempre hay alguien que responde por el y nunca queda huerfano.
 */
public final class EstadoActa {

	/**
	 * El activo esta en bodega, a cargo del area de TI. Es un registro interno:
	 * no se firma ni genera PDF, y no cuenta como activo entregado.
	 */
	public static final String CUSTODIA = "custodia";

	/** El activo esta entregado a un empleado. Es el acta que se firma. */
	public static final String ACTIVA = "activa";

	/** Acta cerrada: el ciclo que documenta ya termino. */
	public static final String DEVUELTA = "devuelta";

	private EstadoActa() {
	}

	public static boolean estaAbierta(String estadoAsignacion) {
		return CUSTODIA.equals(estadoAsignacion) || ACTIVA.equals(estadoAsignacion);
	}

	public static boolean esEntregaAEmpleado(String estadoAsignacion) {
		return ACTIVA.equals(estadoAsignacion);
	}

	public static boolean esCustodiaDeTi(String estadoAsignacion) {
		return CUSTODIA.equals(estadoAsignacion);
	}

}
