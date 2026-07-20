package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

import com.uisrael.inventario.dominio.entidades.ActivoDetalle;

public interface IActivoDetalleUseCase {
	ActivoDetalle guardar(ActivoDetalle nuevoActivoDetalle);
	ActivoDetalle buscarPorId(int idActivo);
	List<ActivoDetalle> listarTodos();
	void eliminar(int idActivo);
}
