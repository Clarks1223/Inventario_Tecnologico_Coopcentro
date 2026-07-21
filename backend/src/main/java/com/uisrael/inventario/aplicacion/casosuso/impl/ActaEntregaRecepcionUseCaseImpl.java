package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaDocumentoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaEntregaRecepcionUseCase;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;

public class ActaEntregaRecepcionUseCaseImpl implements IActaEntregaRecepcionUseCase {

	private static final String ESTADO_ACTIVA = "activa";
	private static final String ESTADO_DEVUELTA = "devuelta";

	private final IActaEntregaRecepcionRepositorio repositorio;
	private final IActivoRepositorio activoRepositorio;
	private final IActaDocumentoUseCase actaDocumentoUseCase;

	public ActaEntregaRecepcionUseCaseImpl(IActaEntregaRecepcionRepositorio repositorio, IActivoRepositorio activoRepositorio,
			IActaDocumentoUseCase actaDocumentoUseCase) {
		this.repositorio = repositorio;
		this.activoRepositorio = activoRepositorio;
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

	@Override
	public ActaEntregaRecepcion asignar(int idActivo, int idEmpleado, int idUsuarioTi, String motivo) {
		Activo activo = activoRepositorio.buscarPorId(idActivo)
				.orElseThrow(() -> new RuntimeException("Activo no encontrado"));

		if (!"NO_ASIGNADO".equals(activo.getEstado())) {
			throw new RuntimeException("El activo no esta disponible para asignar");
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
			throw new RuntimeException("Esta asignacion ya fue devuelta");
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
		return repositorio.listarTodos().stream()
				.filter(acta -> acta.getIdEmpleado() == idEmpleado)
				.filter(acta -> ESTADO_ACTIVA.equals(acta.getEstadoAsignacion()))
				.map(acta -> devolver(acta.getIdActa(), motivo))
				.toList();
	}

}
