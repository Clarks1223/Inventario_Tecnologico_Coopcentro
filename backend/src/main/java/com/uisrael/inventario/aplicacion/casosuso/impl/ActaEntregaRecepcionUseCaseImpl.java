package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaEntregaRecepcionUseCase;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;

public class ActaEntregaRecepcionUseCaseImpl implements IActaEntregaRecepcionUseCase {

	private final IActaEntregaRecepcionRepositorio repositorio;

	public ActaEntregaRecepcionUseCaseImpl(IActaEntregaRecepcionRepositorio repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	public ActaEntregaRecepcion guardar(ActaEntregaRecepcion nuevaActa) {
		return repositorio.guardar(nuevaActa);
	}

	@Override
	public ActaEntregaRecepcion buscarPorId(int idActa) {
		return repositorio.buscarPorId(idActa)
				.orElseThrow(() -> new RuntimeException("Acta no encontrada"));
	}

	@Override
	public List<ActaEntregaRecepcion> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	public void eliminar(int idActa) {
		repositorio.eliminar(idActa);
	}

}
