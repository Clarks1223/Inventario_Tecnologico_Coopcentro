package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;

public interface IActaEntregaRecepcionUseCase {
	ActaEntregaRecepcion guardar(ActaEntregaRecepcion nuevaActa);
	ActaEntregaRecepcion buscarPorId(int idActa);
	List<ActaEntregaRecepcion> listarTodos();
	void eliminar(int idActa);
	ActaEntregaRecepcion asignar(int idActivo, int idEmpleado, int idUsuarioTi, String motivo);
	ActaEntregaRecepcion devolver(int idActa, String motivo);
	List<ActaEntregaRecepcion> devolverTodoEmpleado(int idEmpleado, String motivo);
}
