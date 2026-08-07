package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoUseCase;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.ActivoDetalle;
import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoDetalleRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IOficinaRepositorio;
import com.uisrael.inventario.dominio.repositorios.IUsuarioTiRepositorio;
import com.uisrael.inventario.dominio.valores.EstadoActivo;

public class ActivoUseCaseImpl implements IActivoUseCase {

	private static final String MOTIVO_REGISTRO = "Registro inicial en inventario";
	private static final String MOTIVO_REACTIVACION = "Reactivacion del activo";

	private final IActivoRepositorio repositorio;
	private final IActivoDetalleRepositorio detalleRepositorio;
	private final IOficinaRepositorio oficinaRepositorio;
	private final IActaEntregaRecepcionRepositorio actaRepositorio;
	private final IUsuarioTiRepositorio usuarioTiRepositorio;

	public ActivoUseCaseImpl(IActivoRepositorio repositorio, IActivoDetalleRepositorio detalleRepositorio,
			IOficinaRepositorio oficinaRepositorio, IActaEntregaRecepcionRepositorio actaRepositorio,
			IUsuarioTiRepositorio usuarioTiRepositorio) {
		this.repositorio = repositorio;
		this.detalleRepositorio = detalleRepositorio;
		this.oficinaRepositorio = oficinaRepositorio;
		this.actaRepositorio = actaRepositorio;
		this.usuarioTiRepositorio = usuarioTiRepositorio;
	}

	/**
	 * Guarda el activo, su detalle y mantiene la cadena de custodia en un solo
	 * bloque atomico: si algo falla no puede quedar un activo sin detalle ni,
	 * sobre todo, un activo operativo sin acta abierta (huerfano).
	 */
	@Override
	@Transactional
	public Activo guardar(Activo nuevoActivo, ActivoDetalle detalle, int idUsuarioTi) {
		Optional<Activo> activoExistente = repositorio.buscarPorId(nuevoActivo.getIdActivo());
		String estadoAnterior = activoExistente.map(Activo::getEstado).orElse(null);

		// Un activo nace siempre operativo y bajo la custodia de quien lo
		// registra; su situacion solo cambia despues, al editarlo.
		if (activoExistente.isEmpty()) {
			nuevoActivo.setEstado(EstadoActivo.OPERATIVO);
		}

		Oficina oficina = oficinaRepositorio.buscarPorId(nuevoActivo.getIdOficina())
				.orElseThrow(() -> new NegocioException("Oficina no encontrada"));

		boolean cambiaDeOficina = activoExistente
				.map(actual -> actual.getIdOficina() != nuevoActivo.getIdOficina())
				.orElse(true);
		if (cambiaDeOficina && !oficina.isActivo()) {
			throw new NegocioException("La oficina esta inactiva y no puede recibir nuevas asignaciones");
		}

		repositorio.buscarPorSerial(nuevoActivo.getSerial())
				.filter(existente -> existente.getIdActivo() != nuevoActivo.getIdActivo())
				.ifPresent(existente -> {
					throw new NegocioException("Ya existe un activo con ese serial");
				});

		repositorio.buscarPorCodigoInventario(nuevoActivo.getCodigoInventario())
				.filter(existente -> existente.getIdActivo() != nuevoActivo.getIdActivo())
				.ifPresent(existente -> {
					throw new NegocioException("Ya existe un activo con ese codigo de inventario");
				});

		ActivoDetalle detalleRelevante = filtrarDetallePorTipo(nuevoActivo.getTipoActivo(), detalle,
				nuevoActivo.getIdActivo());
		normalizarBlancosANull(detalleRelevante);
		validarUnicidadDetalle(detalleRelevante, nuevoActivo.getEstado());

		Activo guardado = repositorio.guardar(nuevoActivo);
		detalleRelevante.setIdActivo(guardado.getIdActivo());
		detalleRepositorio.guardar(detalleRelevante);

		sincronizarCustodia(guardado, estadoAnterior, idUsuarioTi);
		return guardado;
	}

	/**
	 * Mantiene el invariante "un activo OPERATIVO tiene siempre exactamente un
	 * acta abierta". Al nacer o al reactivarse queda bajo la custodia de quien
	 * hace la operacion; al darse de baja o reportarse robado se cierra el acta
	 * que tuviera abierta, sea de custodia o de entrega a un empleado. Esto
	 * ultimo permite reportar como robado un equipo que estaba en manos de un
	 * empleado, sin tener que simular antes una devolucion que nunca ocurrio.
	 */
	private void sincronizarCustodia(Activo activo, String estadoAnterior, int idUsuarioTi) {
		boolean eraOperativo = EstadoActivo.esOperativo(estadoAnterior);
		boolean esOperativo = EstadoActivo.esOperativo(activo.getEstado());
		if (eraOperativo == esOperativo) {
			return;
		}

		if (esOperativo) {
			UsuarioTi usuarioTi = usuarioTiRepositorio.buscarPorId(idUsuarioTi)
					.orElseThrow(() -> new NegocioException("Usuario TI no encontrado"));
			actaRepositorio.guardar(ActaEntregaRecepcion.custodiaDeTi(activo.getIdActivo(),
					usuarioTi.getIdEmpleado(), idUsuarioTi,
					estadoAnterior == null ? MOTIVO_REGISTRO : MOTIVO_REACTIVACION));
			return;
		}

		cerrarActasAbiertas(activo.getIdActivo(), "Cierre por cambio de situacion a " + activo.getEstado());
	}

	private void cerrarActasAbiertas(int idActivo, String motivoCierre) {
		actaRepositorio.listarTodos().stream()
				.filter(acta -> acta.getIdActivo() == idActivo)
				.filter(ActaEntregaRecepcion::estaAbierta)
				.forEach(acta -> {
					acta.cerrar(motivoCierre);
					actaRepositorio.guardar(acta);
				});
	}

	private void normalizarBlancosANull(ActivoDetalle detalle) {
		detalle.setImei(blancoANull(detalle.getImei()));
		detalle.setIp(blancoANull(detalle.getIp()));
		detalle.setDominio(blancoANull(detalle.getDominio()));
	}

	private String blancoANull(String valor) {
		return (valor == null || valor.isBlank()) ? null : valor;
	}

	private void validarUnicidadDetalle(ActivoDetalle detalle, String estadoDelActivo) {
		if (detalle.getImei() != null) {
			detalleRepositorio.buscarPorImei(detalle.getImei())
					.filter(existente -> existente.getIdActivo() != detalle.getIdActivo())
					.ifPresent(existente -> {
						throw new NegocioException("Ya existe un activo con ese IMEI");
					});
		}
		// IP y dominio solo deben ser unicos entre activos operativos: los valores
		// de un activo dado de baja o robado/perdido quedan libres para reutilizarse,
		// y un activo fuera de servicio tampoco compite por ellos al editarse.
		if (!EstadoActivo.esOperativo(estadoDelActivo)) {
			return;
		}
		if (detalle.getIp() != null && existeConflictoOperativo(detalleRepositorio.buscarPorIp(detalle.getIp()), detalle)) {
			throw new NegocioException("Ya existe un activo operativo con esa IP");
		}
		if (detalle.getDominio() != null
				&& existeConflictoOperativo(detalleRepositorio.buscarPorDominio(detalle.getDominio()), detalle)) {
			throw new NegocioException("Ya existe un activo operativo con ese dominio");
		}
	}

	private boolean existeConflictoOperativo(List<ActivoDetalle> coincidencias, ActivoDetalle detalle) {
		return coincidencias.stream()
				.filter(existente -> existente.getIdActivo() != detalle.getIdActivo())
				.anyMatch(existente -> estaOperativo(existente.getIdActivo()));
	}

	private boolean estaOperativo(int idActivo) {
		return repositorio.buscarPorId(idActivo)
				.map(activo -> EstadoActivo.esOperativo(activo.getEstado()))
				.orElse(false);
	}

	@Override
	public Activo buscarPorId(int idActivo) {
		return repositorio.buscarPorId(idActivo)
				.orElseThrow(() -> new NegocioException("Activo no encontrado"));
	}

	@Override
	public Optional<ActivoDetalle> buscarDetalle(int idActivo) {
		return detalleRepositorio.buscarPorId(idActivo);
	}

	@Override
	public List<Activo> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	@Transactional
	public void eliminar(int idActivo) {
		repositorio.eliminar(idActivo);
	}

	private ActivoDetalle filtrarDetallePorTipo(String tipoActivo, ActivoDetalle detalle, int idActivo) {
		ActivoDetalle resultado = new ActivoDetalle();
		resultado.setIdActivo(idActivo);
		if (detalle == null) {
			return resultado;
		}
		switch (tipoActivo) {
			case "periferico" -> resultado.setTipoDispositivo(detalle.getTipoDispositivo());
			case "impresora_termica" -> {
				resultado.setTipoConexion(detalle.getTipoConexion());
				resultado.setEstadoBateria(detalle.getEstadoBateria());
				resultado.setModeloCabezal(detalle.getModeloCabezal());
				resultado.setIncluyeCargador(detalle.getIncluyeCargador());
				resultado.setIncluyeCableUsb(detalle.getIncluyeCableUsb());
			}
			case "dispositivo_movil" -> {
				resultado.setTipoDispositivo(detalle.getTipoDispositivo());
				resultado.setSistemaOperativo(detalle.getSistemaOperativo());
				resultado.setImei(detalle.getImei());
				resultado.setNumeroLinea(detalle.getNumeroLinea());
				resultado.setAlmacenamientoGb(detalle.getAlmacenamientoGb());
				resultado.setIncluyeCargador(detalle.getIncluyeCargador());
				resultado.setIncluyeCableUsb(detalle.getIncluyeCableUsb());
			}
			case "desktop", "laptop" -> {
				resultado.setProcesador(detalle.getProcesador());
				resultado.setRamGb(detalle.getRamGb());
				resultado.setTipoAlmacenamiento(detalle.getTipoAlmacenamiento());
				resultado.setIp(detalle.getIp());
				resultado.setDominio(detalle.getDominio());
				resultado.setAlmacenamientoGb(detalle.getAlmacenamientoGb());
			}
			default -> {
			}
		}
		return resultado;
	}

}
