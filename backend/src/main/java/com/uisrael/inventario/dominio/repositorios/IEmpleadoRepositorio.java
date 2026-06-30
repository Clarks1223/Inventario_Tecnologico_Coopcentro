package com.uisrael.inventario.dominio.repositorios;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.Empleado;

public interface IEmpleadoRepositorio {
	Empleado guardar(Empleado nuevoEmpleado);
	Optional<Empleado> buscarPorId(int idEmpleado);
	List<Empleado> listarTodos();
	void eliminar(int idEmpleado);
}
