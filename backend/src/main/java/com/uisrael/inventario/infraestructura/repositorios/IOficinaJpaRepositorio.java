package com.uisrael.inventario.infraestructura.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.OficinaEntity;

public interface IOficinaJpaRepositorio extends JpaRepository<OficinaEntity, Integer> {
	List<OficinaEntity> findByNombre(String nombre);
}
