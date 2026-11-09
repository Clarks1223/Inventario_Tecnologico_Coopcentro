package com.uisrael.inventario.infraestructura.persistencia.adaptadores;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.GrupoActivos;
import com.uisrael.inventario.dominio.repositorios.IGrupoActivosRepositorio;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IGrupoActivosJpaMapper;
import com.uisrael.inventario.infraestructura.repositorios.IGrupoActivosJpaRepositorio;

public class GrupoActivosRepositorioImpl implements IGrupoActivosRepositorio {

	private final IGrupoActivosJpaRepositorio jpaRepositorio;
	private final IGrupoActivosJpaMapper entityMapper;

	public GrupoActivosRepositorioImpl(IGrupoActivosJpaRepositorio jpaRepositorio, IGrupoActivosJpaMapper entityMapper) {
		this.jpaRepositorio = jpaRepositorio;
		this.entityMapper = entityMapper;
	}

	@Override
	public GrupoActivos guardar(GrupoActivos grupo) {
		return entityMapper.toDomain(jpaRepositorio.save(entityMapper.toEntity(grupo)));
	}

	@Override
	public Optional<GrupoActivos> buscarPorId(int idGrupo) {
		return jpaRepositorio.findById(idGrupo).map(entityMapper::toDomain);
	}

	@Override
	public List<GrupoActivos> listarTodos() {
		return jpaRepositorio.findAll().stream().map(entityMapper::toDomain).toList();
	}

	@Override
	public void eliminar(int idGrupo) {
		jpaRepositorio.deleteById(idGrupo);
	}

}
