package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

import com.uisrael.inventario.dominio.entidades.Empleado;

public interface IEmpleadoUseCase {
	Empleado guardar(Empleado nuevoEmpleado);
	Empleado buscarPorId(int idEmpleado);
	List<Empleado> listarTodos();
	void eliminar(int idEmpleado);
}
