package com.uisrael.inventario.infraestructura.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.ActaEntregaRecepcionEntity;

public interface IActaEntregaRecepcionJpaRepositorio extends JpaRepository<ActaEntregaRecepcionEntity, Integer> {
	List<ActaEntregaRecepcionEntity> findByEstadoAsignacion(String estadoAsignacion);
}
