package com.uisrael.inventario.infraestructura.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.GrupoActivosEntity;

public interface IGrupoActivosJpaRepositorio extends JpaRepository<GrupoActivosEntity, Integer> {
}
