package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaDocumentoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaEntregaRecepcionUseCase;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.GrupoActivos;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IGrupoActivosRepositorio;
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
	private final IGrupoActivosRepositorio grupoRepositorio;

	public ActaEntregaRecepcionUseCaseImpl(IActaEntregaRecepcionRepositorio repositorio, IActivoRepositorio activoRepositorio,
			IEmpleadoRepositorio empleadoRepositorio, IUsuarioTiRepositorio usuarioTiRepositorio,
			IActaDocumentoUseCase actaDocumentoUseCase, IGrupoActivosRepositorio grupoRepositorio) {
		this.repositorio = repositorio;
		this.activoRepositorio = activoRepositorio;
		this.empleadoRepositorio = empleadoRepositorio;
		this.usuarioTiRepositorio = usuarioTiRepositorio;
		this.actaDocumentoUseCase = actaDocumentoUseCase;
		this.grupoRepositorio = grupoRepositorio;
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

	private Empleado validarEmpleadoActivo(int idEmpleado) {
		Empleado empleado = empleadoRepositorio.buscarPorId(idEmpleado)
				.orElseThrow(() -> new NegocioException("Empleado no encontrado"));
		if (!empleado.isActivo()) {
			throw new NegocioException("El empleado no esta activo");
		}
		return empleado;
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

		Empleado empleado = validarEmpleadoActivo(idEmpleado);
		validarUsuarioTiActivo(idUsuarioTi);

		return entregar(actaAbierta, empleado, idUsuarioTi, motivo, null, true);
	}

	/**
	 * Saca el activo de la bodega para pasarlo al empleado: se cierra la
	 * custodia y se abre la entrega, de modo que siempre haya exactamente un
	 * acta abierta. Las validaciones ya se hicieron antes de llamar.
	 *
	 * @param emitirActa false cuando la entrega forma parte de un lote: el PDF
	 *                   se emite al final, agrupado con el resto del paquete.
	 */
	private ActaEntregaRecepcion entregar(ActaEntregaRecepcion custodia, Empleado empleado, int idUsuarioTi,
			String motivo, String observacion, boolean emitirActa) {
		custodia.cerrar("Entrega a " + empleado.getNombre() + " " + empleado.getApellido());
		repositorio.guardar(custodia);

		ActaEntregaRecepcion entrega = ActaEntregaRecepcion.entregaAEmpleado(custodia.getIdActivo(),
				empleado.getIdEmpleado(), idUsuarioTi, motivo);
		if (observacion != null && !observacion.isBlank()) {
			entrega.setObservacion(observacion);
		}
		ActaEntregaRecepcion guardada = repositorio.guardar(entrega);

		if (emitirActa) {
			generarPdfSinFallar(guardada.getIdActa());
		}
		return guardada;
	}

	@Override
	@Transactional
	public List<ActaEntregaRecepcion> asignarGrupo(int idGrupo, int idEmpleado, int idUsuarioTi, String motivo,
			String observacion) {
		GrupoActivos grupo = grupoRepositorio.buscarPorId(idGrupo)
				.orElseThrow(() -> new NegocioException("Grupo no encontrado"));
		List<Activo> activos = activoRepositorio.listarPorGrupo(idGrupo);
		if (activos.isEmpty()) {
			throw new NegocioException("El grupo no tiene activos");
		}

		Empleado empleado = validarEmpleadoActivo(idEmpleado);
		validarUsuarioTiActivo(idUsuarioTi);

		// Se valida el grupo completo antes de escribir nada: o se entrega todo o
		// nada, y el mensaje dice exactamente que activos lo impiden.
		List<ActaEntregaRecepcion> custodias = new ArrayList<>();
		List<String> bloqueantes = new ArrayList<>();
		for (Activo activo : activos) {
			Optional<ActaEntregaRecepcion> actaAbierta = buscarActaAbierta(activo.getIdActivo());
			if (!EstadoActivo.esOperativo(activo.getEstado())) {
				bloqueantes.add(activo.getSerial() + " ("
						+ GrupoActivosUseCaseImpl.describirEstadoNoOperativo(activo.getEstado()) + ")");
			} else if (actaAbierta.isEmpty()) {
				bloqueantes.add(activo.getSerial() + " (sin acta abierta)");
			} else if (!EstadoActa.esCustodiaDeTi(actaAbierta.get().getEstadoAsignacion())) {
				bloqueantes.add(activo.getSerial() + " (entregado a "
						+ nombreDe(actaAbierta.get().getIdEmpleado()) + ")");
			} else {
				custodias.add(actaAbierta.get());
			}
		}
		if (!bloqueantes.isEmpty()) {
			throw new NegocioException("No se puede asignar el grupo: " + String.join(", ", bloqueantes));
		}

		String motivoGrupo = "Asignacion del grupo " + grupo.getNombre()
				+ (motivo == null || motivo.isBlank() ? "" : ": " + motivo);
		List<ActaEntregaRecepcion> entregadas = custodias.stream()
				.map(custodia -> entregar(custodia, empleado, idUsuarioTi, motivoGrupo, observacion, false))
				.toList();

		// Un solo paquete: la PC lleva embebidos sus perifericos en su acta.
		generarPdfsDelPaqueteSinFallar(entregadas.stream().map(ActaEntregaRecepcion::getIdActa).toList());
		return entregadas;
	}

	private String nombreDe(int idEmpleado) {
		return empleadoRepositorio.buscarPorId(idEmpleado)
				.map(empleado -> empleado.getNombre() + " " + empleado.getApellido())
				.orElse("otro empleado");
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
