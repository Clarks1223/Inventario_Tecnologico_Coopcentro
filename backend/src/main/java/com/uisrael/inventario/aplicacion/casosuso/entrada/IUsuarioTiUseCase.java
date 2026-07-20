package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

import com.uisrael.inventario.dominio.entidades.UsuarioTi;

public interface IUsuarioTiUseCase {
	UsuarioTi guardar(UsuarioTi nuevoUsuarioTi);
	UsuarioTi buscarPorId(int idUsuarioTi);
	List<UsuarioTi> listarTodos();
	void eliminar(int idUsuarioTi);
}
