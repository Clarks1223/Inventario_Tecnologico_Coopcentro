package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

import com.uisrael.inventario.dominio.entidades.AsignacionActivo;

public interface IAsignacionActivoUseCase {
	AsignacionActivo guardar(AsignacionActivo nuevaAsignacion);
	AsignacionActivo buscarPorId(int idAsignacion);
	List<AsignacionActivo> listarTodos();
	void eliminar(int idAsignacion);
}
