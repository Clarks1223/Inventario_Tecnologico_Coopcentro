package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

import com.uisrael.inventario.dominio.entidades.Cargo;

public interface ICargoUseCase {
	Cargo guardar(Cargo nuevoCargo);
	Cargo buscarPorId(int idCargo);
	List<Cargo> listarTodos();
	void eliminar(int idCargo);
}
