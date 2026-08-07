package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IEmpleadoUseCase;
import com.uisrael.inventario.dominio.entidades.Cargo;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.ICargoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IOficinaRepositorio;
import com.uisrael.inventario.dominio.valores.EstadoActa;

public class EmpleadoUseCaseImpl implements IEmpleadoUseCase {

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
	@Transactional
	public Empleado guardar(Empleado nuevoEmpleado) {
		repositorio.buscarPorCedula(nuevoEmpleado.getCedula())
				.filter(existente -> existente.getIdEmpleado() != nuevoEmpleado.getIdEmpleado())
				.ifPresent(existente -> {
					throw new NegocioException("Ya existe un empleado con esa cedula");
				});

		repositorio.buscarPorCorreo(nuevoEmpleado.getCorreo())
				.filter(existente -> existente.getIdEmpleado() != nuevoEmpleado.getIdEmpleado())
				.ifPresent(existente -> {
					throw new NegocioException("Ya existe un empleado con ese correo");
				});

		Oficina oficina = oficinaRepositorio.buscarPorId(nuevoEmpleado.getIdOficina())
				.orElseThrow(() -> new NegocioException("Oficina no encontrada"));

		Cargo cargo = cargoRepositorio.buscarPorId(nuevoEmpleado.getIdCargo())
				.orElseThrow(() -> new NegocioException("Cargo no encontrado"));

		Empleado empleadoActual = repositorio.buscarPorId(nuevoEmpleado.getIdEmpleado()).orElse(null);

		boolean seDesactiva = empleadoActual != null && empleadoActual.isActivo() && !nuevoEmpleado.isActivo();
		if (seDesactiva) {
			// Solo cuentan las entregas reales: la custodia de bodega de un tecnico
			// de TI no debe impedir nunca que se lo desactive.
			boolean tieneActivosAsignados = actaEntregaRecepcionRepositorio.listarTodos().stream()
					.anyMatch(acta -> acta.getIdEmpleado() == nuevoEmpleado.getIdEmpleado()
							&& EstadoActa.esEntregaAEmpleado(acta.getEstadoAsignacion()));
			if (tieneActivosAsignados) {
				throw new NegocioException(
						"No se puede desactivar el empleado: aun tiene activos asignados. Debe devolverlos primero");
			}
		}

		boolean cambiaDeOficina = empleadoActual == null || empleadoActual.getIdOficina() != nuevoEmpleado.getIdOficina();
		if (cambiaDeOficina && !oficina.isActivo()) {
			throw new NegocioException("La oficina esta inactiva y no puede recibir nuevas asignaciones");
		}

		boolean cambiaDeCargo = empleadoActual == null || empleadoActual.getIdCargo() != nuevoEmpleado.getIdCargo();
		if (cambiaDeCargo && !cargo.isActivo()) {
			throw new NegocioException("El cargo esta inactivo y no puede recibir nuevas asignaciones");
		}

		return repositorio.guardar(nuevoEmpleado);
	}

	@Override
	public Empleado buscarPorId(int idEmpleado) {
		return repositorio.buscarPorId(idEmpleado)
				.orElseThrow(() -> new NegocioException("Empleado no encontrado"));
	}

	@Override
	public List<Empleado> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	@Transactional
	public void eliminar(int idEmpleado) {
		repositorio.eliminar(idEmpleado);
	}

}
