package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoUseCase;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.ActivoDetalle;
import com.uisrael.inventario.dominio.repositorios.IActivoDetalleRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;

public class ActivoUseCaseImpl implements IActivoUseCase {

	private final IActivoRepositorio repositorio;
	private final IActivoDetalleRepositorio detalleRepositorio;

	public ActivoUseCaseImpl(IActivoRepositorio repositorio, IActivoDetalleRepositorio detalleRepositorio) {
		this.repositorio = repositorio;
		this.detalleRepositorio = detalleRepositorio;
	}

	@Override
	public Activo guardar(Activo nuevoActivo, ActivoDetalle detalle) {
		Activo guardado = repositorio.guardar(nuevoActivo);
		if (!"periferico".equals(guardado.getTipoActivo())) {
			ActivoDetalle detalleRelevante = filtrarDetallePorTipo(guardado.getTipoActivo(), detalle, guardado.getIdActivo());
			detalleRepositorio.guardar(detalleRelevante);
		}
		return guardado;
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
