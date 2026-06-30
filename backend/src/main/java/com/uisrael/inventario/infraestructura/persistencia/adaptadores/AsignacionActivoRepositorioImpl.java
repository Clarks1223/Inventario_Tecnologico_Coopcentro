package com.uisrael.inventario.infraestructura.persistencia.adaptadores;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.AsignacionActivo;
import com.uisrael.inventario.dominio.repositorios.IAsignacionActivoRepositorio;
import com.uisrael.inventario.infraestructura.persistencia.jpa.AsignacionActivoEntity;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IAsignacionActivoJpaMapper;
import com.uisrael.inventario.infraestructura.repositorios.IAsignacionActivoJpaRepositorio;

public class AsignacionActivoRepositorioImpl implements IAsignacionActivoRepositorio {

	private final IAsignacionActivoJpaRepositorio jpaRepositorio;
	private final IAsignacionActivoJpaMapper entityMapper;

	public AsignacionActivoRepositorioImpl(IAsignacionActivoJpaRepositorio jpaRepositorio, IAsignacionActivoJpaMapper entityMapper) {
		this.jpaRepositorio = jpaRepositorio;
		this.entityMapper = entityMapper;
	}

	@Override
	public AsignacionActivo guardar(AsignacionActivo nuevaAsignacion) {
		AsignacionActivoEntity entity = entityMapper.toEntity(nuevaAsignacion);
		AsignacionActivoEntity guardado = jpaRepositorio.save(entity);
		return entityMapper.toDomain(guardado);
	}

	@Override
	public Optional<AsignacionActivo> buscarPorId(int idAsignacion) {
		return jpaRepositorio.findById(idAsignacion).map(entityMapper::toDomain);
	}

	@Override
	public List<AsignacionActivo> listarTodos() {
		return jpaRepositorio.findAll().stream().map(entityMapper::toDomain).toList();
	}

	@Override
	public void eliminar(int idAsignacion) {
		jpaRepositorio.deleteById(idAsignacion);
	}

}
