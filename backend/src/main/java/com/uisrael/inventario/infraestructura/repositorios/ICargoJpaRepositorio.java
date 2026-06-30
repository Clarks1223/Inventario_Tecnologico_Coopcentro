package com.uisrael.inventario.infraestructura.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.CargoEntity;

public interface ICargoJpaRepositorio extends JpaRepository<CargoEntity, Integer> {

}
