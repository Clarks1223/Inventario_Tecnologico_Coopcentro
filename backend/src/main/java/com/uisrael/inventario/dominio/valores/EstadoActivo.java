package com.uisrael.inventario.dominio.valores;

/**
 * Situacion del activo dentro de su ciclo de vida. NO indica a quien esta
 * entregado: eso se deriva siempre del acta abierta que tenga (ver
 * {@link EstadoActa}), para que no existan dos fuentes de verdad que puedan
 * contradecirse.
 */
public final class EstadoActivo {

	/** En uso o disponible. Tiene siempre exactamente un acta abierta. */
	public static final String OPERATIVO = "OPERATIVO";

	/** Retirado del inventario. Sin acta abierta. */
	public static final String DADO_DE_BAJA = "DADO_DE_BAJA";

	/** Extraviado o sustraido. Sin acta abierta. */
	public static final String ROBADO_PERDIDO = "ROBADO_PERDIDO";

	private EstadoActivo() {
	}

	public static boolean esOperativo(String estado) {
		return OPERATIVO.equals(estado);
	}

}
