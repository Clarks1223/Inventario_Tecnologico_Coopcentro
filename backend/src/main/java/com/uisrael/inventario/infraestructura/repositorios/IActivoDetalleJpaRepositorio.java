package com.uisrael.inventario.infraestructura.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.ActivoDetalleEntity;

public interface IActivoDetalleJpaRepositorio extends JpaRepository<ActivoDetalleEntity, Integer> {
	Optional<ActivoDetalleEntity> findByImei(String imei);
	Optional<ActivoDetalleEntity> findByIp(String ip);
	Optional<ActivoDetalleEntity> findByDominio(String dominio);
}
