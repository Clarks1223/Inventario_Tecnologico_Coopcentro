package com.uisrael.inventario.dominio.repositorios;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.GrupoActivos;

public interface IGrupoActivosRepositorio {
	GrupoActivos guardar(GrupoActivos grupo);
	Optional<GrupoActivos> buscarPorId(int idGrupo);
	List<GrupoActivos> listarTodos();
	void eliminar(int idGrupo);
}
