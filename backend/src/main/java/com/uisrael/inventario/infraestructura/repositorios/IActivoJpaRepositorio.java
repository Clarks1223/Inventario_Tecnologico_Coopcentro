package com.uisrael.inventario.infraestructura.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.ActivoEntity;

public interface IActivoJpaRepositorio extends JpaRepository<ActivoEntity, Integer> {

}
