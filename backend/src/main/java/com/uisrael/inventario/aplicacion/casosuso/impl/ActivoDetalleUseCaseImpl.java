package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoDetalleUseCase;
import com.uisrael.inventario.dominio.entidades.ActivoDetalle;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
import com.uisrael.inventario.dominio.repositorios.IActivoDetalleRepositorio;

public class ActivoDetalleUseCaseImpl implements IActivoDetalleUseCase {

	private final IActivoDetalleRepositorio repositorio;

	public ActivoDetalleUseCaseImpl(IActivoDetalleRepositorio repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	@Transactional
	public ActivoDetalle guardar(ActivoDetalle nuevoActivoDetalle) {
		return repositorio.guardar(nuevoActivoDetalle);
	}

	@Override
	public ActivoDetalle buscarPorId(int idActivo) {
		return repositorio.buscarPorId(idActivo)
				.orElseThrow(() -> new NegocioException("ActivoDetalle no encontrado"));
	}

	@Override
	public List<ActivoDetalle> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	@Transactional
	public void eliminar(int idActivo) {
		repositorio.eliminar(idActivo);
	}

}
