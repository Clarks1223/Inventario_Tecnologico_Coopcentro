package com.uisrael.inventario.dominio.repositorios;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.ActivoDetalle;

public interface IActivoDetalleRepositorio {
	ActivoDetalle guardar(ActivoDetalle nuevoActivoDetalle);
	Optional<ActivoDetalle> buscarPorId(int idActivo);
	Optional<ActivoDetalle> buscarPorImei(String imei);
	Optional<ActivoDetalle> buscarPorIp(String ip);
	Optional<ActivoDetalle> buscarPorDominio(String dominio);
	List<ActivoDetalle> listarTodos();
	void eliminar(int idActivo);
}
