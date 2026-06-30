package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IEmpleadoUseCase;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;

public class EmpleadoUseCaseImpl implements IEmpleadoUseCase {

	private final IEmpleadoRepositorio repositorio;

	public EmpleadoUseCaseImpl(IEmpleadoRepositorio repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	public Empleado guardar(Empleado nuevoEmpleado) {
		return repositorio.guardar(nuevoEmpleado);
	}

	@Override
	public Empleado buscarPorId(int idEmpleado) {
		return repositorio.buscarPorId(idEmpleado)
				.orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
	}

	@Override
	public List<Empleado> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	public void eliminar(int idEmpleado) {
		repositorio.eliminar(idEmpleado);
	}

}
