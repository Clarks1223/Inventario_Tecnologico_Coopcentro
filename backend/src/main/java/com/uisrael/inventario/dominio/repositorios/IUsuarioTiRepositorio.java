package com.uisrael.inventario.dominio.repositorios;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.UsuarioTi;

public interface IUsuarioTiRepositorio {
	UsuarioTi guardar(UsuarioTi nuevoUsuarioTi);
	Optional<UsuarioTi> buscarPorId(int idUsuarioTi);
	Optional<UsuarioTi> buscarPorCorreo(String correo);
	List<UsuarioTi> listarTodos();
	void eliminar(int idUsuarioTi);
}
