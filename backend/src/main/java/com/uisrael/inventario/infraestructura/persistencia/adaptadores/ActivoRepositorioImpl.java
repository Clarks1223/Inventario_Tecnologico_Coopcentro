package com.uisrael.inventario.infraestructura.persistencia.adaptadores;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.infraestructura.persistencia.jpa.ActivoEntity;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IActivoJpaMapper;
import com.uisrael.inventario.infraestructura.repositorios.IActivoJpaRepositorio;

public class ActivoRepositorioImpl implements IActivoRepositorio {

	private final IActivoJpaRepositorio jpaRepositorio;
	private final IActivoJpaMapper entityMapper;

	public ActivoRepositorioImpl(IActivoJpaRepositorio jpaRepositorio, IActivoJpaMapper entityMapper) {
		this.jpaRepositorio = jpaRepositorio;
		this.entityMapper = entityMapper;
	}

	@Override
	public Activo guardar(Activo nuevoActivo) {
		ActivoEntity entity = entityMapper.toEntity(nuevoActivo);
		ActivoEntity guardado = jpaRepositorio.save(entity);
		return entityMapper.toDomain(guardado);
	}

	@Override
	public Optional<Activo> buscarPorId(int idActivo) {
		return jpaRepositorio.findById(idActivo).map(entityMapper::toDomain);
	}

	@Override
	public List<Activo> listarTodos() {
		return jpaRepositorio.findAll().stream().map(entityMapper::toDomain).toList();
	}

	@Override
	public void eliminar(int idActivo) {
		jpaRepositorio.deleteById(idActivo);
	}

}
