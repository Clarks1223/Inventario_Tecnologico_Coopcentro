package com.uisrael.inventario.dominio.repositorios;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.AsignacionActivo;

public interface IAsignacionActivoRepositorio {
	AsignacionActivo guardar(AsignacionActivo nuevaAsignacion);
	Optional<AsignacionActivo> buscarPorId(int idAsignacion);
	List<AsignacionActivo> listarTodos();
	void eliminar(int idAsignacion);
}
