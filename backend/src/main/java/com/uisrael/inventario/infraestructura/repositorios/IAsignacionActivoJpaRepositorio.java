package com.uisrael.inventario.infraestructura.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.AsignacionActivoEntity;

public interface IAsignacionActivoJpaRepositorio extends JpaRepository<AsignacionActivoEntity, Integer> {

}
