package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoUseCase;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.ActivoDetalle;
import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.dominio.repositorios.IActivoDetalleRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IOficinaRepositorio;

public class ActivoUseCaseImpl implements IActivoUseCase {

	private final IActivoRepositorio repositorio;
	private final IActivoDetalleRepositorio detalleRepositorio;
	private final IOficinaRepositorio oficinaRepositorio;

	public ActivoUseCaseImpl(IActivoRepositorio repositorio, IActivoDetalleRepositorio detalleRepositorio,
			IOficinaRepositorio oficinaRepositorio) {
		this.repositorio = repositorio;
		this.detalleRepositorio = detalleRepositorio;
		this.oficinaRepositorio = oficinaRepositorio;
	}

	@Override
	public Activo guardar(Activo nuevoActivo, ActivoDetalle detalle) {
		Oficina oficina = oficinaRepositorio.buscarPorId(nuevoActivo.getIdOficina())
				.orElseThrow(() -> new RuntimeException("Oficina no encontrada"));

		Optional<Activo> activoExistente = repositorio.buscarPorId(nuevoActivo.getIdActivo());
		String estadoAnterior = activoExistente.map(Activo::getEstado).orElse(null);

		// El estado ASIGNADO entra y sale unicamente por el modulo de asignaciones
		// (actas de entrega/recepcion), para no romper la cadena de custodia.
		if ("ASIGNADO".equals(estadoAnterior) && !"ASIGNADO".equals(nuevoActivo.getEstado())) {
			throw new RuntimeException(
					"El activo esta asignado a un empleado; registre la devolucion antes de cambiar su estado");
		}
		if (!"ASIGNADO".equals(estadoAnterior) && "ASIGNADO".equals(nuevoActivo.getEstado())) {
			throw new RuntimeException("El estado Asignado solo se establece al registrar una asignacion");
		}

		boolean esAsignacionNueva = activoExistente
				.map(actual -> actual.getIdOficina() != nuevoActivo.getIdOficina())
				.orElse(true);
		if (esAsignacionNueva && !oficina.isActivo()) {
			throw new RuntimeException("La oficina esta inactiva y no puede recibir nuevas asignaciones");
		}

		repositorio.buscarPorSerial(nuevoActivo.getSerial())
				.filter(existente -> existente.getIdActivo() != nuevoActivo.getIdActivo())
				.ifPresent(existente -> {
					throw new RuntimeException("Ya existe un activo con ese serial");
				});

		repositorio.buscarPorCodigoInventario(nuevoActivo.getCodigoInventario())
				.filter(existente -> existente.getIdActivo() != nuevoActivo.getIdActivo())
				.ifPresent(existente -> {
					throw new RuntimeException("Ya existe un activo con ese codigo de inventario");
				});

		ActivoDetalle detalleRelevante = filtrarDetallePorTipo(nuevoActivo.getTipoActivo(), detalle, nuevoActivo.getIdActivo());
		normalizarBlancosANull(detalleRelevante);
		validarUnicidadDetalle(detalleRelevante, nuevoActivo.getEstado());

		Activo guardado = repositorio.guardar(nuevoActivo);
		detalleRelevante.setIdActivo(guardado.getIdActivo());
		detalleRepositorio.guardar(detalleRelevante);
		return guardado;
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
						throw new RuntimeException("Ya existe un activo con ese IMEI");
					});
		}
		// IP y dominio solo deben ser unicos entre activos operativos: los valores
		// de un activo dado de baja o robado/perdido quedan libres para reutilizarse,
		// y un activo fuera de servicio tampoco compite por ellos al editarse.
		boolean fueraDeServicio = "DADO_DE_BAJA".equals(estadoDelActivo) || "ROBADO_PERDIDO".equals(estadoDelActivo);
		if (fueraDeServicio) {
			return;
		}
		if (detalle.getIp() != null && existeConflictoOperativo(detalleRepositorio.buscarPorIp(detalle.getIp()), detalle)) {
			throw new RuntimeException("Ya existe un activo operativo con esa IP");
		}
		if (detalle.getDominio() != null
				&& existeConflictoOperativo(detalleRepositorio.buscarPorDominio(detalle.getDominio()), detalle)) {
			throw new RuntimeException("Ya existe un activo operativo con ese dominio");
		}
	}

	private boolean existeConflictoOperativo(List<ActivoDetalle> coincidencias, ActivoDetalle detalle) {
		return coincidencias.stream()
				.filter(existente -> existente.getIdActivo() != detalle.getIdActivo())
				.anyMatch(existente -> estaOperativo(existente.getIdActivo()));
	}

	private boolean estaOperativo(int idActivo) {
		return repositorio.buscarPorId(idActivo)
				.map(activo -> !"DADO_DE_BAJA".equals(activo.getEstado()) && !"ROBADO_PERDIDO".equals(activo.getEstado()))
				.orElse(false);
	}

	@Override
	public Activo buscarPorId(int idActivo) {
		return repositorio.buscarPorId(idActivo)
				.orElseThrow(() -> new RuntimeException("Activo no encontrado"));
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
			}
			case "dispositivo_movil" -> {
				resultado.setTipoDispositivo(detalle.getTipoDispositivo());
				resultado.setSistemaOperativo(detalle.getSistemaOperativo());
				resultado.setImei(detalle.getImei());
				resultado.setNumeroLinea(detalle.getNumeroLinea());
				resultado.setAlmacenamientoGb(detalle.getAlmacenamientoGb());
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
