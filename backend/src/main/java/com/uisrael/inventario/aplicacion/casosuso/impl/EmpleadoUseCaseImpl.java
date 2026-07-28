package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IEmpleadoUseCase;
import com.uisrael.inventario.dominio.entidades.Cargo;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.ICargoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IOficinaRepositorio;

public class EmpleadoUseCaseImpl implements IEmpleadoUseCase {

	private static final String ESTADO_ASIGNACION_ACTIVA = "activa";

	private final IEmpleadoRepositorio repositorio;
	private final IOficinaRepositorio oficinaRepositorio;
	private final ICargoRepositorio cargoRepositorio;
	private final IActaEntregaRecepcionRepositorio actaEntregaRecepcionRepositorio;

	public EmpleadoUseCaseImpl(IEmpleadoRepositorio repositorio, IOficinaRepositorio oficinaRepositorio,
			ICargoRepositorio cargoRepositorio, IActaEntregaRecepcionRepositorio actaEntregaRecepcionRepositorio) {
		this.repositorio = repositorio;
		this.oficinaRepositorio = oficinaRepositorio;
		this.cargoRepositorio = cargoRepositorio;
		this.actaEntregaRecepcionRepositorio = actaEntregaRecepcionRepositorio;
	}

	@Override
	public Empleado guardar(Empleado nuevoEmpleado) {
		repositorio.buscarPorCedula(nuevoEmpleado.getCedula())
				.filter(existente -> existente.getIdEmpleado() != nuevoEmpleado.getIdEmpleado())
				.ifPresent(existente -> {
					throw new RuntimeException("Ya existe un empleado con esa cedula");
				});

		repositorio.buscarPorCorreo(nuevoEmpleado.getCorreo())
				.filter(existente -> existente.getIdEmpleado() != nuevoEmpleado.getIdEmpleado())
				.ifPresent(existente -> {
					throw new RuntimeException("Ya existe un empleado con ese correo");
				});

		Oficina oficina = oficinaRepositorio.buscarPorId(nuevoEmpleado.getIdOficina())
				.orElseThrow(() -> new RuntimeException("Oficina no encontrada"));

		Cargo cargo = cargoRepositorio.buscarPorId(nuevoEmpleado.getIdCargo())
				.orElseThrow(() -> new RuntimeException("Cargo no encontrado"));

		Empleado empleadoActual = repositorio.buscarPorId(nuevoEmpleado.getIdEmpleado()).orElse(null);

		boolean seDesactiva = empleadoActual != null && empleadoActual.isActivo() && !nuevoEmpleado.isActivo();
		if (seDesactiva) {
			boolean tieneActivosAsignados = actaEntregaRecepcionRepositorio.listarTodos().stream()
					.anyMatch(acta -> acta.getIdEmpleado() == nuevoEmpleado.getIdEmpleado()
							&& ESTADO_ASIGNACION_ACTIVA.equals(acta.getEstadoAsignacion()));
			if (tieneActivosAsignados) {
				throw new RuntimeException(
						"No se puede desactivar el empleado: aun tiene activos asignados. Debe devolverlos primero");
			}
		}

		boolean cambiaDeOficina = empleadoActual == null || empleadoActual.getIdOficina() != nuevoEmpleado.getIdOficina();
		if (cambiaDeOficina && !oficina.isActivo()) {
			throw new RuntimeException("La oficina esta inactiva y no puede recibir nuevas asignaciones");
		}

		boolean cambiaDeCargo = empleadoActual == null || empleadoActual.getIdCargo() != nuevoEmpleado.getIdCargo();
		if (cambiaDeCargo && !cargo.isActivo()) {
			throw new RuntimeException("El cargo esta inactivo y no puede recibir nuevas asignaciones");
		}

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
