package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IOficinaUseCase;
import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
import com.uisrael.inventario.dominio.repositorios.IOficinaRepositorio;

public class OficinaUseCaseImpl implements IOficinaUseCase {

	private final IOficinaRepositorio repositorio;

	public OficinaUseCaseImpl(IOficinaRepositorio repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	@Transactional
	public Oficina guardar(Oficina nuevaOficina) {
		repositorio.buscarPorNombre(nuevaOficina.getNombre()).stream()
				.filter(existente -> existente.getIdOficina() != nuevaOficina.getIdOficina())
				.findFirst()
				.ifPresent(existente -> {
					throw new NegocioException("Ya existe una oficina con ese nombre");
				});
		return repositorio.guardar(nuevaOficina);
	}

	@Override
	public Oficina buscarPorId(int idOficina) {
		return repositorio.buscarPorId(idOficina)
				.orElseThrow(() -> new NegocioException("Oficina no encontrada"));
	}

	@Override
	public List<Oficina> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	@Transactional
	public void eliminar(int idOficina) {
		repositorio.eliminar(idOficina);
	}

}
