package com.uisrael.inventario.dominio.entidades;

import java.util.List;

/**
 * Grupo con sus activos y su estado derivado de las actas abiertas. El estado
 * nunca se guarda, para no tener una segunda fuente de verdad.
 *
 * @param idEmpleadoAsignado     solo cuando el estado es ASIGNADO
 * @param nombreEmpleadoAsignado solo cuando el estado es ASIGNADO
 */
public record ResumenGrupo(GrupoActivos grupo, List<Activo> activos, String estado, Integer idEmpleadoAsignado,
		String nombreEmpleadoAsignado) {
}
