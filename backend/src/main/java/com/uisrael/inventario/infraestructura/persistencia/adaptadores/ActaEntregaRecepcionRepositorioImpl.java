package com.uisrael.inventario.infraestructura.persistencia.adaptadores;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.infraestructura.persistencia.jpa.ActaEntregaRecepcionEntity;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IActaEntregaRecepcionJpaMapper;
import com.uisrael.inventario.infraestructura.repositorios.IActaEntregaRecepcionJpaRepositorio;

public class ActaEntregaRecepcionRepositorioImpl implements IActaEntregaRecepcionRepositorio {

	private final IActaEntregaRecepcionJpaRepositorio jpaRepositorio;
	private final IActaEntregaRecepcionJpaMapper entityMapper;

	public ActaEntregaRecepcionRepositorioImpl(IActaEntregaRecepcionJpaRepositorio jpaRepositorio, IActaEntregaRecepcionJpaMapper entityMapper) {
		this.jpaRepositorio = jpaRepositorio;
		this.entityMapper = entityMapper;
	}

	@Override
	public ActaEntregaRecepcion guardar(ActaEntregaRecepcion nuevaActa) {
		ActaEntregaRecepcionEntity entity = entityMapper.toEntity(nuevaActa);
		ActaEntregaRecepcionEntity guardado = jpaRepositorio.save(entity);
		return entityMapper.toDomain(guardado);
	}

	@Override
	public Optional<ActaEntregaRecepcion> buscarPorId(int idActa) {
		return jpaRepositorio.findById(idActa).map(entityMapper::toDomain);
	}

	@Override
	public List<ActaEntregaRecepcion> listarTodos() {
		return jpaRepositorio.findAll().stream().map(entityMapper::toDomain).toList();
	}

	@Override
	public void eliminar(int idActa) {
		jpaRepositorio.deleteById(idActa);
	}

}
