package com.uisrael.inventario.infraestructura.persistencia.adaptadores;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.repositorios.IUsuarioTiRepositorio;
import com.uisrael.inventario.infraestructura.persistencia.jpa.UsuarioTiEntity;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IUsuarioTiJpaMapper;
import com.uisrael.inventario.infraestructura.repositorios.IUsuarioTiJpaRepositorio;

public class UsuarioTiRepositorioImpl implements IUsuarioTiRepositorio {

	private final IUsuarioTiJpaRepositorio jpaRepositorio;
	private final IUsuarioTiJpaMapper entityMapper;

	public UsuarioTiRepositorioImpl(IUsuarioTiJpaRepositorio jpaRepositorio, IUsuarioTiJpaMapper entityMapper) {
		this.jpaRepositorio = jpaRepositorio;
		this.entityMapper = entityMapper;
	}

	@Override
	public UsuarioTi guardar(UsuarioTi nuevoUsuarioTi) {
		UsuarioTiEntity entity = entityMapper.toEntity(nuevoUsuarioTi);
		UsuarioTiEntity guardado = jpaRepositorio.save(entity);
		return entityMapper.toDomain(guardado);
	}

	@Override
	public Optional<UsuarioTi> buscarPorId(int idUsuarioTi) {
		return jpaRepositorio.findById(idUsuarioTi).map(entityMapper::toDomain);
	}

	@Override
	public Optional<UsuarioTi> buscarPorCorreo(String correo) {
		return jpaRepositorio.findByCorreo(correo).map(entityMapper::toDomain);
	}

	@Override
	public List<UsuarioTi> listarTodos() {
		return jpaRepositorio.findAll().stream().map(entityMapper::toDomain).toList();
	}

	@Override
	public void eliminar(int idUsuarioTi) {
		jpaRepositorio.deleteById(idUsuarioTi);
	}

}
