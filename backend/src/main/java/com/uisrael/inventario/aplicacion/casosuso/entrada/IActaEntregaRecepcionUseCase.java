package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;

public interface IActaEntregaRecepcionUseCase {
	ActaEntregaRecepcion buscarPorId(int idActa);
	List<ActaEntregaRecepcion> listarTodos();
	void eliminar(int idActa);
	ActaEntregaRecepcion asignar(int idActivo, int idEmpleado, int idUsuarioTi, String motivo);
	/**
	 * @param idUsuarioTi usuario en sesion que recibe la devolucion; firma el
	 *                    acta de recepcion y queda como custodio del activo.
	 */
	ActaEntregaRecepcion devolver(int idActa, String motivo, int idUsuarioTi, String observacion);
	List<ActaEntregaRecepcion> devolverTodoEmpleado(int idEmpleado, String motivo, int idUsuarioTi,
			String observacion);
}
