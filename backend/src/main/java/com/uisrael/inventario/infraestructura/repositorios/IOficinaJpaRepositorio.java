package com.uisrael.inventario.infraestructura.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.OficinaEntity;

public interface IOficinaJpaRepositorio extends JpaRepository<OficinaEntity, Integer> {

}
