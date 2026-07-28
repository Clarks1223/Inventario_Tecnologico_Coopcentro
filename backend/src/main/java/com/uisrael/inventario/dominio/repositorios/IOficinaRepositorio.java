package com.uisrael.inventario.dominio.repositorios;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.Oficina;

public interface IOficinaRepositorio {
	Oficina guardar(Oficina nuevaOficina);
	Optional<Oficina> buscarPorId(int idOficina);
	List<Oficina> listarTodos();
	List<Oficina> buscarPorNombre(String nombre);
	void eliminar(int idOficina);
}
