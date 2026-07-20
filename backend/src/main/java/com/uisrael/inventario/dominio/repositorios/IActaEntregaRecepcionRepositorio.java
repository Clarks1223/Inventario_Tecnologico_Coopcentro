package com.uisrael.inventario.dominio.repositorios;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;

public interface IActaEntregaRecepcionRepositorio {
	ActaEntregaRecepcion guardar(ActaEntregaRecepcion nuevaActa);
	Optional<ActaEntregaRecepcion> buscarPorId(int idActa);
	List<ActaEntregaRecepcion> listarTodos();
	void eliminar(int idActa);
}
