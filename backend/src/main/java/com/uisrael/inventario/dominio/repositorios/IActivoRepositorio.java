package com.uisrael.inventario.dominio.repositorios;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.Activo;

public interface IActivoRepositorio {
	Activo guardar(Activo nuevoActivo);
	Optional<Activo> buscarPorId(int idActivo);
	Optional<Activo> buscarPorSerial(String serial);
	Optional<Activo> buscarPorCodigoInventario(String codigoInventario);
	List<Activo> listarTodos();
	void eliminar(int idActivo);
}
