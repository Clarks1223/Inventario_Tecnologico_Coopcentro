package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IOficinaUseCase;
import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.dominio.repositorios.IOficinaRepositorio;

public class OficinaUseCaseImpl implements IOficinaUseCase {

	private final IOficinaRepositorio repositorio;

	public OficinaUseCaseImpl(IOficinaRepositorio repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	public Oficina guardar(Oficina nuevaOficina) {
		return repositorio.guardar(nuevaOficina);
	}

	@Override
	public Oficina buscarPorId(int idOficina) {
		return repositorio.buscarPorId(idOficina)
				.orElseThrow(() -> new RuntimeException("Oficina no encontrada"));
	}

	@Override
	public List<Oficina> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	public void eliminar(int idOficina) {
		repositorio.eliminar(idOficina);
	}

}
