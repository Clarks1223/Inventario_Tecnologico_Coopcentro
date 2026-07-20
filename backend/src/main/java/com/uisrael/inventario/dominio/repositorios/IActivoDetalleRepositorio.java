package com.uisrael.inventario.dominio.repositorios;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.ActivoDetalle;

public interface IActivoDetalleRepositorio {
	ActivoDetalle guardar(ActivoDetalle nuevoActivoDetalle);
	Optional<ActivoDetalle> buscarPorId(int idActivo);
	List<ActivoDetalle> listarTodos();
	void eliminar(int idActivo);
}
