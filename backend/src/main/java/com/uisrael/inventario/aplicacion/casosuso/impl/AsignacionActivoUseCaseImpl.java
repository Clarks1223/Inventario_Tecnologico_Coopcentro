package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IAsignacionActivoUseCase;
import com.uisrael.inventario.dominio.entidades.AsignacionActivo;
import com.uisrael.inventario.dominio.repositorios.IAsignacionActivoRepositorio;

public class AsignacionActivoUseCaseImpl implements IAsignacionActivoUseCase {

	private final IAsignacionActivoRepositorio repositorio;

	public AsignacionActivoUseCaseImpl(IAsignacionActivoRepositorio repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	public AsignacionActivo guardar(AsignacionActivo nuevaAsignacion) {
		return repositorio.guardar(nuevaAsignacion);
	}

	@Override
	public AsignacionActivo buscarPorId(int idAsignacion) {
		return repositorio.buscarPorId(idAsignacion)
				.orElseThrow(() -> new RuntimeException("Asignacion no encontrada"));
	}

	@Override
	public List<AsignacionActivo> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	public void eliminar(int idAsignacion) {
		repositorio.eliminar(idAsignacion);
	}

}
