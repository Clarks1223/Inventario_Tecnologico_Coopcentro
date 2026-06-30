package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

import com.uisrael.inventario.dominio.entidades.Activo;

public interface IActivoUseCase {
	Activo guardar(Activo nuevoActivo);
	Activo buscarPorId(int idActivo);
	List<Activo> listarTodos();
	void eliminar(int idActivo);
}
