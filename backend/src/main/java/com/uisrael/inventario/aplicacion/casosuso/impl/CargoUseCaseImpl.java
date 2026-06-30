package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;

import com.uisrael.inventario.aplicacion.casosuso.entrada.ICargoUseCase;
import com.uisrael.inventario.dominio.entidades.Cargo;
import com.uisrael.inventario.dominio.repositorios.ICargoRepositorio;

public class CargoUseCaseImpl implements ICargoUseCase {

	private final ICargoRepositorio repositorio;

	public CargoUseCaseImpl(ICargoRepositorio repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	public Cargo guardar(Cargo nuevoCargo) {
		return repositorio.guardar(nuevoCargo);
	}

	@Override
	public Cargo buscarPorId(int idCargo) {
		return repositorio.buscarPorId(idCargo)
				.orElseThrow(() -> new RuntimeException("Cargo no encontrado"));
	}

	@Override
	public List<Cargo> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	public void eliminar(int idCargo) {
		repositorio.eliminar(idCargo);
	}

}
