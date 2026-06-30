package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoUseCase;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;

public class ActivoUseCaseImpl implements IActivoUseCase {

	private final IActivoRepositorio repositorio;

	public ActivoUseCaseImpl(IActivoRepositorio repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	public Activo guardar(Activo nuevoActivo) {
		return repositorio.guardar(nuevoActivo);
	}

	@Override
	public Activo buscarPorId(int idActivo) {
		return repositorio.buscarPorId(idActivo)
				.orElseThrow(() -> new RuntimeException("Activo no encontrado"));
	}

	@Override
	public List<Activo> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	public void eliminar(int idActivo) {
		repositorio.eliminar(idActivo);
	}

}
