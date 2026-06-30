package com.uisrael.inventario.dominio.repositorios;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.Cargo;

public interface ICargoRepositorio {
	Cargo guardar(Cargo nuevoCargo);
	Optional<Cargo> buscarPorId(int idCargo);
	List<Cargo> listarTodos();
	void eliminar(int idCargo);
}
