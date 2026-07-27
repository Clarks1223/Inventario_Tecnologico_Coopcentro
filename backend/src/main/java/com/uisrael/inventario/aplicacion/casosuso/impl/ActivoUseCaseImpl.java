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

		boolean esAsignacionNueva = repositorio.buscarPorId(nuevoActivo.getIdActivo())
				.map(actual -> actual.getIdOficina() != nuevoActivo.getIdOficina())
				.orElse(true);
		if (esAsignacionNueva && !oficina.isActivo()) {
			throw new RuntimeException("La oficina esta inactiva y no puede recibir nuevas asignaciones");
		}

		repositorio.buscarPorSerial(nuevoActivo.getSerial())
				.filter(existente -> existente.getIdActivo() != nuevoActivo.getIdActivo())
				.ifPresent(existente -> {
					throw new RuntimeException("Ya existe un activo registrado con ese serial");
				});

		repositorio.buscarPorCodigoInventario(nuevoActivo.getCodigoInventario())
				.filter(existente -> existente.getIdActivo() != nuevoActivo.getIdActivo())
				.ifPresent(existente -> {
					throw new RuntimeException("Ya existe un activo registrado con ese codigo de inventario");
				});

		ActivoDetalle detalleRelevante = filtrarDetallePorTipo(nuevoActivo.getTipoActivo(), detalle, nuevoActivo.getIdActivo());
		normalizarBlancosANull(detalleRelevante);
		validarUnicidadDetalle(detalleRelevante);

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

	private void validarUnicidadDetalle(ActivoDetalle detalle) {
		if (detalle.getImei() != null) {
			detalleRepositorio.buscarPorImei(detalle.getImei())
					.filter(existente -> existente.getIdActivo() != detalle.getIdActivo())
					.ifPresent(existente -> {
						throw new RuntimeException("Ya existe un activo registrado con ese IMEI");
					});
		}
		if (detalle.getIp() != null) {
			detalleRepositorio.buscarPorIp(detalle.getIp())
					.filter(existente -> existente.getIdActivo() != detalle.getIdActivo())
					.ifPresent(existente -> {
						throw new RuntimeException("Ya existe un activo registrado con esa IP");
					});
		}
		if (detalle.getDominio() != null) {
			detalleRepositorio.buscarPorDominio(detalle.getDominio())
					.filter(existente -> existente.getIdActivo() != detalle.getIdActivo())
					.ifPresent(existente -> {
						throw new RuntimeException("Ya existe un activo registrado con ese dominio");
					});
		}
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
