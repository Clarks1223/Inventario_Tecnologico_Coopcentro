package com.uisrael.inventario.infraestructura.persistencia.adaptadores;

import java.util.List;
import java.util.Optional;

import com.uisrael.inventario.dominio.entidades.Cargo;
import com.uisrael.inventario.dominio.repositorios.ICargoRepositorio;
import com.uisrael.inventario.infraestructura.persistencia.jpa.CargoEntity;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.ICargoJpaMapper;
import com.uisrael.inventario.infraestructura.repositorios.ICargoJpaRepositorio;

public class CargoRepositorioImpl implements ICargoRepositorio {

	private final ICargoJpaRepositorio jpaRepositorio;
	private final ICargoJpaMapper entityMapper;

	public CargoRepositorioImpl(ICargoJpaRepositorio jpaRepositorio, ICargoJpaMapper entityMapper) {
		this.jpaRepositorio = jpaRepositorio;
		this.entityMapper = entityMapper;
	}

	@Override
	public Cargo guardar(Cargo nuevoCargo) {
		CargoEntity entity = entityMapper.toEntity(nuevoCargo);
		CargoEntity guardado = jpaRepositorio.save(entity);
		return entityMapper.toDomain(guardado);
	}

	@Override
	public Optional<Cargo> buscarPorId(int idCargo) {
		return jpaRepositorio.findById(idCargo).map(entityMapper::toDomain);
	}

	@Override
	public List<Cargo> listarTodos() {
		return jpaRepositorio.findAll().stream().map(entityMapper::toDomain).toList();
	}

	@Override
	public void eliminar(int idCargo) {
		jpaRepositorio.deleteById(idCargo);
	}

}
