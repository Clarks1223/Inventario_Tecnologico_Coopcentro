package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

import com.uisrael.inventario.dominio.entidades.Oficina;

public interface IOficinaUseCase {
	Oficina guardar(Oficina nuevaOficina);
	Oficina buscarPorId(int idOficina);
	List<Oficina> listarTodos();
	void eliminar(int idOficina);
}
