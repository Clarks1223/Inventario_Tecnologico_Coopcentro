package com.uisrael.inventario.infraestructura.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uisrael.inventario.infraestructura.persistencia.jpa.EmpleadoEntity;

public interface IEmpleadoJpaRepositorio extends JpaRepository<EmpleadoEntity, Integer> {
	List<EmpleadoEntity> findByApellido(String apellido);
	List<EmpleadoEntity> findByCedula(String cedula);
}
