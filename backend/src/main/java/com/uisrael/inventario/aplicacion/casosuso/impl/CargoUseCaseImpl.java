package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.ICargoUseCase;
import com.uisrael.inventario.dominio.entidades.Cargo;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
import com.uisrael.inventario.dominio.repositorios.ICargoRepositorio;

public class CargoUseCaseImpl implements ICargoUseCase {

	private final ICargoRepositorio repositorio;

	public CargoUseCaseImpl(ICargoRepositorio repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	@Transactional
	public Cargo guardar(Cargo nuevoCargo) {
		nuevoCargo.setNombre(nuevoCargo.getNombre().toUpperCase());
		repositorio.buscarPorNombre(nuevoCargo.getNombre()).stream()
				.filter(existente -> existente.getIdCargo() != nuevoCargo.getIdCargo())
				.findFirst()
				.ifPresent(existente -> {
					throw new NegocioException("Ya existe un cargo con ese nombre");
				});
		return repositorio.guardar(nuevoCargo);
	}

	@Override
	public Cargo buscarPorId(int idCargo) {
		return repositorio.buscarPorId(idCargo)
				.orElseThrow(() -> new NegocioException("Cargo no encontrado"));
	}

	@Override
	public List<Cargo> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	@Transactional
	public void eliminar(int idCargo) {
		repositorio.eliminar(idCargo);
	}

}
