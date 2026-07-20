package com.uisrael.inventario.infraestructura.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.ActivoDetalleEntity;

public interface IActivoDetalleJpaRepositorio extends JpaRepository<ActivoDetalleEntity, Integer> {
}
