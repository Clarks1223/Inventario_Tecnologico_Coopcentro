package com.uisrael.inventario.dominio.valores;

/**
 * Situacion de un grupo de activos, derivada de las actas abiertas de sus
 * miembros. Solo un grupo EN_BODEGA puede asignarse.
 */
public final class EstadoGrupo {

	/** El grupo no tiene activos. */
	public static final String VACIO = "VACIO";

	/** Todos sus activos estan operativos y en custodia de TI. */
	public static final String EN_BODEGA = "EN_BODEGA";

	/** Todos sus activos estan entregados al mismo empleado. */
	public static final String ASIGNADO = "ASIGNADO";

	/** Cualquier otra combinacion: no se puede asignar como grupo. */
	public static final String MIXTO = "MIXTO";

	private EstadoGrupo() {
	}

}
