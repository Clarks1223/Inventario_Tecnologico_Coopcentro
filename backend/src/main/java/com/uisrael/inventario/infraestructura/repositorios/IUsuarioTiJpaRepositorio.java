package com.uisrael.inventario.infraestructura.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.UsuarioTiEntity;

public interface IUsuarioTiJpaRepositorio extends JpaRepository<UsuarioTiEntity, Integer> {
	Optional<UsuarioTiEntity> findByCorreo(String correo);
}
