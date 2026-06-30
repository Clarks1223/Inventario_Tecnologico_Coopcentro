package com.uisrael.inventario.infraestructura.persistencia.adaptadores;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.infraestructura.persistencia.jpa.EmpleadoEntity;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IEmpleadoJpaMapper;
import com.uisrael.inventario.infraestructura.repositorios.IEmpleadoJpaRepositorio;

public class EmpleadoRepositorioImpl implements IEmpleadoRepositorio {

	private final IEmpleadoJpaRepositorio jpaRepositorio;
	private final IEmpleadoJpaMapper entityMapper;

	public EmpleadoRepositorioImpl(IEmpleadoJpaRepositorio jpaRepositorio, IEmpleadoJpaMapper entityMapper) {
		this.jpaRepositorio = jpaRepositorio;
		this.entityMapper = entityMapper;
	}

	@Override
	public Empleado guardar(Empleado nuevoEmpleado) {
		EmpleadoEntity entity = entityMapper.toEntity(nuevoEmpleado);
		EmpleadoEntity guardado = jpaRepositorio.save(entity);
		return entityMapper.toDomain(guardado);
	}

	@Override
	public Optional<Empleado> buscarPorId(int idEmpleado) {
		return jpaRepositorio.findById(idEmpleado).map(entityMapper::toDomain);
	}

	@Override
	public List<Empleado> listarTodos() {
		return jpaRepositorio.findAll().stream().map(entityMapper::toDomain).toList();
	}

	@Override
	public void eliminar(int idEmpleado) {
		jpaRepositorio.deleteById(idEmpleado);
	}

}
