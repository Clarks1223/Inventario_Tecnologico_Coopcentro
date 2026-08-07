package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.ActivoDetalle;

public interface IActivoUseCase {
	/**
	 * @param idUsuarioTi usuario en sesion que realiza la operacion; queda como
	 *                    custodio del activo si este nace o se reactiva.
	 */
	Activo guardar(Activo nuevoActivo, ActivoDetalle detalle, int idUsuarioTi);
	Activo buscarPorId(int idActivo);
	Optional<ActivoDetalle> buscarDetalle(int idActivo);
	List<Activo> listarTodos();
	void eliminar(int idActivo);
}
