package com.uisrael.inventario.infraestructura.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.ActivoEntity;

public interface IActivoJpaRepositorio extends JpaRepository<ActivoEntity, Integer> {
	List<ActivoEntity> findByTipoActivo(String tipoActivo);
	List<ActivoEntity> findByEstado(String estado);
	Optional<ActivoEntity> findBySerial(String serial);
	Optional<ActivoEntity> findByCodigoInventario(String codigoInventario);
}
