package com.uisrael.inventario.infraestructura.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.CargoEntity;

public interface ICargoJpaRepositorio extends JpaRepository<CargoEntity, Integer> {
	List<CargoEntity> findByNombre(String nombre);
}
