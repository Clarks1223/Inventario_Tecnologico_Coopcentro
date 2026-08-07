package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaDocumentoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaEntregaRecepcionUseCase;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IUsuarioTiRepositorio;
import com.uisrael.inventario.dominio.valores.EstadoActa;
import com.uisrael.inventario.dominio.valores.EstadoActivo;

/**
 * Mueve el activo a lo largo de la cadena de custodia. El activo no guarda a
 * quien esta entregado: eso siempre lo dice su acta abierta, y este caso de uso
 * garantiza que haya exactamente una mientras el activo este operativo.
 */
public class ActaEntregaRecepcionUseCaseImpl implements IActaEntregaRecepcionUseCase {

	private static final String MOTIVO_RECEPCION = "Devolucion recibida en bodega";

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
			actaDocumentoUseCase.archivarActa(idActa, null);
		} catch (RuntimeException e) {
			System.err.println("No se pudo generar el PDF del acta " + idActa + ": " + e.getMessage());
		}
	}

	private void generarPdfsDelPaqueteSinFallar(List<Integer> idsActas) {
		try {
			actaDocumentoUseCase.archivarLote(idsActas);
		} catch (RuntimeException e) {
			System.err.println("No se pudieron generar las actas del paquete: " + e.getMessage());
		}
	}

	@Override
	public ActaEntregaRecepcion buscarPorId(int idActa) {
		return repositorio.buscarPorId(idActa)
				.orElseThrow(() -> new NegocioException("Acta no encontrada"));
	}

	@Override
	public List<ActaEntregaRecepcion> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	@Transactional
	public void eliminar(int idActa) {
		repositorio.eliminar(idActa);
	}

	/** El acta abierta (custodia o entrega) que tiene el activo ahora mismo. */
	private Optional<ActaEntregaRecepcion> buscarActaAbierta(int idActivo) {
		return repositorio.listarTodos().stream()
				.filter(acta -> acta.getIdActivo() == idActivo)
				.filter(ActaEntregaRecepcion::estaAbierta)
				.findFirst();
	}

	private UsuarioTi validarUsuarioTiActivo(int idUsuarioTi) {
		UsuarioTi usuarioTi = usuarioTiRepositorio.buscarPorId(idUsuarioTi)
				.orElseThrow(() -> new NegocioException("Usuario TI no encontrado"));
		Empleado empleadoTi = empleadoRepositorio.buscarPorId(usuarioTi.getIdEmpleado())
				.orElseThrow(() -> new NegocioException("Empleado de TI no encontrado"));
		if (!empleadoTi.isActivo()) {
			throw new NegocioException("El usuario TI no esta activo");
		}
		return usuarioTi;
	}

	@Override
	@Transactional
	public ActaEntregaRecepcion asignar(int idActivo, int idEmpleado, int idUsuarioTi, String motivo) {
		Activo activo = activoRepositorio.buscarPorId(idActivo)
				.orElseThrow(() -> new NegocioException("Activo no encontrado"));

		if (!EstadoActivo.esOperativo(activo.getEstado())) {
			throw new NegocioException("El activo no esta operativo y no puede entregarse");
		}

		ActaEntregaRecepcion actaAbierta = buscarActaAbierta(idActivo)
				.orElseThrow(() -> new NegocioException("El activo no tiene un acta abierta; revise su registro"));
		if (!EstadoActa.esCustodiaDeTi(actaAbierta.getEstadoAsignacion())) {
			throw new NegocioException("El activo ya esta entregado a un empleado; registre la devolucion primero");
		}

		Empleado empleado = empleadoRepositorio.buscarPorId(idEmpleado)
				.orElseThrow(() -> new NegocioException("Empleado no encontrado"));
		if (!empleado.isActivo()) {
			throw new NegocioException("El empleado no esta activo");
		}

		validarUsuarioTiActivo(idUsuarioTi);

		// Sale de la bodega para pasar al empleado: se cierra la custodia y se
		// abre la entrega, de modo que siempre haya exactamente un acta abierta.
		actaAbierta.cerrar("Entrega a " + empleado.getNombre() + " " + empleado.getApellido());
		repositorio.guardar(actaAbierta);

		ActaEntregaRecepcion guardada = repositorio
				.guardar(ActaEntregaRecepcion.entregaAEmpleado(idActivo, idEmpleado, idUsuarioTi, motivo));

		generarPdfSinFallar(guardada.getIdActa());

		return guardada;
	}

	@Override
	@Transactional
	public ActaEntregaRecepcion devolver(int idActa, String motivo, int idUsuarioTi, String observacion) {
		return devolver(idActa, motivo, idUsuarioTi, observacion, true);
	}

	/**
	 * @param emitirActa false cuando la devolucion forma parte de un lote: el
	 *                   PDF no se emite aqui sino al final, para que todas las
	 *                   actas del lote se agrupen entre si en vez de salir
	 *                   sueltas una por una.
	 */
	private ActaEntregaRecepcion devolver(int idActa, String motivo, int idUsuarioTi, String observacion,
			boolean emitirActa) {
		ActaEntregaRecepcion acta = buscarPorId(idActa);

		if (!EstadoActa.esEntregaAEmpleado(acta.getEstadoAsignacion())) {
			throw new NegocioException("Este activo no tiene una asignacion activa");
		}

		UsuarioTi usuarioTi = validarUsuarioTiActivo(idUsuarioTi);

		// Quien firma la recepcion es quien procesa la devolucion ahora, no el
		// tecnico que hizo la entrega original (puede ser otra persona).
		acta.setIdUsuarioTi(idUsuarioTi);
		if (observacion != null) {
			acta.setObservacion(observacion.isBlank() ? null : observacion);
		}
		acta.cerrar(motivo == null || motivo.isBlank() ? null : "Devolucion: " + motivo);
		ActaEntregaRecepcion actualizada = repositorio.guardar(acta);

		if (emitirActa) {
			generarPdfSinFallar(actualizada.getIdActa());
		}

		// El activo vuelve a la bodega a cargo de quien recibe: nunca queda sin
		// responsable entre una entrega y la siguiente.
		repositorio.guardar(ActaEntregaRecepcion.custodiaDeTi(acta.getIdActivo(), usuarioTi.getIdEmpleado(),
				idUsuarioTi, MOTIVO_RECEPCION));

		return actualizada;
	}

	@Override
	@Transactional
	public List<ActaEntregaRecepcion> devolverTodoEmpleado(int idEmpleado, String motivo, int idUsuarioTi,
			String observacion) {
		empleadoRepositorio.buscarPorId(idEmpleado)
				.orElseThrow(() -> new NegocioException("Empleado no encontrado"));

		// Se resuelven primero los ids: devolver() crea actas de custodia nuevas,
		// asi que iterar directamente sobre el listado seria iterar algo que la
		// propia operacion esta modificando.
		List<Integer> idsActasActivas = repositorio.listarTodos().stream()
				.filter(acta -> acta.getIdEmpleado() == idEmpleado)
				.filter(acta -> EstadoActa.esEntregaAEmpleado(acta.getEstadoAsignacion()))
				.map(ActaEntregaRecepcion::getIdActa)
				.toList();

		// Se cierran todas primero y recien despues se emiten las actas: asi el
		// paquete se agrupa entre si (la PC con sus perifericos, el movil con su
		// impresora) en vez de salir cada una suelta.
		List<ActaEntregaRecepcion> devueltas = idsActasActivas.stream()
				.map(idActaActiva -> devolver(idActaActiva, motivo, idUsuarioTi, observacion, false))
				.toList();

		generarPdfsDelPaqueteSinFallar(idsActasActivas);
		return devueltas;
	}

}
