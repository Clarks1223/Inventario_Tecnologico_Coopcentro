package com.uisrael.inventario.infraestructura.persistencia.adaptadores;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.dominio.repositorios.IOficinaRepositorio;
import com.uisrael.inventario.infraestructura.persistencia.jpa.OficinaEntity;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IOficinaJpaMapper;
import com.uisrael.inventario.infraestructura.repositorios.IOficinaJpaRepositorio;

public class OficinaRepositorioImpl implements IOficinaRepositorio {

	private final IOficinaJpaRepositorio jpaRepositorio;
	private final IOficinaJpaMapper entityMapper;

	public OficinaRepositorioImpl(IOficinaJpaRepositorio jpaRepositorio, IOficinaJpaMapper entityMapper) {
		this.jpaRepositorio = jpaRepositorio;
		this.entityMapper = entityMapper;
	}

	@Override
	public Oficina guardar(Oficina nuevaOficina) {
		OficinaEntity entity = entityMapper.toEntity(nuevaOficina);
		OficinaEntity guardado = jpaRepositorio.save(entity);
		return entityMapper.toDomain(guardado);
	}

	@Override
	public Optional<Oficina> buscarPorId(int idOficina) {
		return jpaRepositorio.findById(idOficina).map(entityMapper::toDomain);
	}

	@Override
	public List<Oficina> listarTodos() {
		return jpaRepositorio.findAll().stream().map(entityMapper::toDomain).toList();
	}

	@Override
	public void eliminar(int idOficina) {
		jpaRepositorio.deleteById(idOficina);
	}

}
