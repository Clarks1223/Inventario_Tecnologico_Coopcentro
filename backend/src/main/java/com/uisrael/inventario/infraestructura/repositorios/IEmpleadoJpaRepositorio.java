package com.uisrael.inventario.infraestructura.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.EmpleadoEntity;

public interface IEmpleadoJpaRepositorio extends JpaRepository<EmpleadoEntity, Integer> {

}
