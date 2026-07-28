package com.uisrael.inventario.infraestructura.persistencia.adaptadores;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.ActivoDetalle;
import com.uisrael.inventario.dominio.repositorios.IActivoDetalleRepositorio;
import com.uisrael.inventario.infraestructura.persistencia.jpa.ActivoDetalleEntity;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IActivoDetalleJpaMapper;
import com.uisrael.inventario.infraestructura.repositorios.IActivoDetalleJpaRepositorio;

public class ActivoDetalleRepositorioImpl implements IActivoDetalleRepositorio {

	private final IActivoDetalleJpaRepositorio jpaRepositorio;
	private final IActivoDetalleJpaMapper entityMapper;

	public ActivoDetalleRepositorioImpl(IActivoDetalleJpaRepositorio jpaRepositorio, IActivoDetalleJpaMapper entityMapper) {
		this.jpaRepositorio = jpaRepositorio;
		this.entityMapper = entityMapper;
	}

	@Override
	public ActivoDetalle guardar(ActivoDetalle nuevoActivoDetalle) {
		ActivoDetalleEntity entity = entityMapper.toEntity(nuevoActivoDetalle);
		// El mapper crea la entidad con nuevo=true; si la fila ya existe hay que
		// marcarla como existente para que save() haga UPDATE y no INSERT.
		if (jpaRepositorio.existsById(nuevoActivoDetalle.getIdActivo())) {
			entity.setNuevo(false);
		}
		ActivoDetalleEntity guardado = jpaRepositorio.save(entity);
		return entityMapper.toDomain(guardado);
	}

	@Override
	public Optional<ActivoDetalle> buscarPorId(int idActivo) {
		return jpaRepositorio.findById(idActivo).map(entityMapper::toDomain);
	}

	@Override
	public Optional<ActivoDetalle> buscarPorImei(String imei) {
		return jpaRepositorio.findByImei(imei).map(entityMapper::toDomain);
	}

	@Override
	public List<ActivoDetalle> buscarPorIp(String ip) {
		return jpaRepositorio.findByIp(ip).stream().map(entityMapper::toDomain).toList();
	}

	@Override
	public List<ActivoDetalle> buscarPorDominio(String dominio) {
		return jpaRepositorio.findByDominio(dominio).stream().map(entityMapper::toDomain).toList();
	}

	@Override
	public List<ActivoDetalle> listarTodos() {
		return jpaRepositorio.findAll().stream().map(entityMapper::toDomain).toList();
	}

	@Override
	public void eliminar(int idActivo) {
		jpaRepositorio.deleteById(idActivo);
	}

}
