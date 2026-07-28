package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaDocumentoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaEntregaRecepcionUseCase;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IUsuarioTiRepositorio;

public class ActaEntregaRecepcionUseCaseImpl implements IActaEntregaRecepcionUseCase {

	private static final String ESTADO_ACTIVA = "activa";
	private static final String ESTADO_DEVUELTA = "devuelta";

	private final IActaEntregaRecepcionRepositorio repositorio;
	private final IActivoRepositorio activoRepositorio;
	private final IEmpleadoRepositorio empleadoRepositorio;
	private final IUsuarioTiRepositorio usuarioTiRepositorio;
	private final IActaDocumentoUseCase actaDocumentoUseCase;

	public ActaEntregaRecepcionUseCaseImpl(IActaEntregaRecepcionRepositorio repositorio, IActivoRepositorio activoRepositorio,
			IEmpleadoRepositorio empleadoRepositorio, IUsuarioTiRepositorio usuarioTiRepositorio,
			IActaDocumentoUseCase actaDocumentoUseCase) {
		this.repositorio = repositorio;
		this.activoRepositorio = activoRepositorio;
		this.empleadoRepositorio = empleadoRepositorio;
		this.usuarioTiRepositorio = usuarioTiRepositorio;
		this.actaDocumentoUseCase = actaDocumentoUseCase;
	}

	/**
	 * Genera y guarda en disco el PDF del acta (entrega o recepcion segun su
	 * estado actual). No debe impedir que la asignacion/devolucion se complete
	 * si la generacion del PDF falla (ej. plantilla no disponible para el tipo
	 * de activo, como los perifericos sueltos sin equipo asociado).
	 */
	private void generarPdfSinFallar(int idActa) {
		try {
			actaDocumentoUseCase.generarPdf(idActa);
		} catch (RuntimeException e) {
			System.err.println("No se pudo generar el PDF del acta " + idActa + ": " + e.getMessage());
		}
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

	@Override
	public ActaEntregaRecepcion asignar(int idActivo, int idEmpleado, int idUsuarioTi, String motivo) {
		Activo activo = activoRepositorio.buscarPorId(idActivo)
				.orElseThrow(() -> new RuntimeException("Activo no encontrado"));

		if (!"NO_ASIGNADO".equals(activo.getEstado())) {
			throw new RuntimeException("El activo ya esta asignado");
		}

		Empleado empleado = empleadoRepositorio.buscarPorId(idEmpleado)
				.orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
		if (!empleado.isActivo()) {
			throw new RuntimeException("El empleado no esta activo");
		}

		UsuarioTi usuarioTi = usuarioTiRepositorio.buscarPorId(idUsuarioTi)
				.orElseThrow(() -> new RuntimeException("Usuario TI no encontrado"));
		Empleado empleadoTi = empleadoRepositorio.buscarPorId(usuarioTi.getIdEmpleado())
				.orElseThrow(() -> new RuntimeException("Empleado de TI no encontrado"));
		if (!empleadoTi.isActivo()) {
			throw new RuntimeException("El usuario TI no esta activo");
		}

		ActaEntregaRecepcion acta = new ActaEntregaRecepcion();
		acta.setIdActivo(idActivo);
		acta.setIdEmpleado(idEmpleado);
		acta.setIdUsuarioTi(idUsuarioTi);
		acta.setFechaAsignacion(LocalDateTime.now());
		acta.setFechaDevolucion(null);
		acta.setEstadoAsignacion(ESTADO_ACTIVA);
		acta.setMotivo(motivo);

		ActaEntregaRecepcion guardada = repositorio.guardar(acta);

		activo.setEstado("ASIGNADO");
		activoRepositorio.guardar(activo);

		generarPdfSinFallar(guardada.getIdActa());

		return guardada;
	}

	@Override
	public ActaEntregaRecepcion devolver(int idActa, String motivo) {
		ActaEntregaRecepcion acta = buscarPorId(idActa);

		if (!ESTADO_ACTIVA.equals(acta.getEstadoAsignacion())) {
			throw new RuntimeException("Este activo no tiene una asignacion activa");
		}

		acta.setFechaDevolucion(LocalDateTime.now());
		acta.setEstadoAsignacion(ESTADO_DEVUELTA);
		if (motivo != null && !motivo.isBlank()) {
			String motivoPrevio = acta.getMotivo();
			acta.setMotivo(motivoPrevio == null || motivoPrevio.isBlank()
					? motivo
					: motivoPrevio + "\nDevolucion: " + motivo);
		}

		ActaEntregaRecepcion actualizada = repositorio.guardar(acta);

		activoRepositorio.buscarPorId(acta.getIdActivo()).ifPresent(activo -> {
			activo.setEstado("NO_ASIGNADO");
			activoRepositorio.guardar(activo);
		});

		generarPdfSinFallar(actualizada.getIdActa());

		return actualizada;
	}

	@Override
	public List<ActaEntregaRecepcion> devolverTodoEmpleado(int idEmpleado, String motivo) {
		empleadoRepositorio.buscarPorId(idEmpleado)
				.orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
		return repositorio.listarTodos().stream()
				.filter(acta -> acta.getIdEmpleado() == idEmpleado)
				.filter(acta -> ESTADO_ACTIVA.equals(acta.getEstadoAsignacion()))
				.map(acta -> devolver(acta.getIdActa(), motivo))
				.toList();
	}

}
