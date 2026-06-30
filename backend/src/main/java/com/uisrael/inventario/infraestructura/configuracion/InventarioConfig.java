package com.uisrael.inventario.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IAsignacionActivoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.ICargoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IEmpleadoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IOficinaUseCase;
import com.uisrael.inventario.aplicacion.casosuso.impl.ActivoUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.AsignacionActivoUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.CargoUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.EmpleadoUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.OficinaUseCaseImpl;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IAsignacionActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.ICargoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IOficinaRepositorio;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.ActivoRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.AsignacionActivoRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.CargoRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.EmpleadoRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.OficinaRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IActivoJpaMapper;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IAsignacionActivoJpaMapper;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.ICargoJpaMapper;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IEmpleadoJpaMapper;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IOficinaJpaMapper;
import com.uisrael.inventario.infraestructura.repositorios.IActivoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IAsignacionActivoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.ICargoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IEmpleadoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IOficinaJpaRepositorio;

@Configuration
public class InventarioConfig {

	@Bean
	IOficinaRepositorio oficinaRepositorio(IOficinaJpaRepositorio jpaRepositorio, IOficinaJpaMapper mapper) {
		return new OficinaRepositorioImpl(jpaRepositorio, mapper);
	}

	@Bean
	IOficinaUseCase oficinaUseCase(IOficinaRepositorio repositorio) {
		return new OficinaUseCaseImpl(repositorio);
	}

	@Bean
	ICargoRepositorio cargoRepositorio(ICargoJpaRepositorio jpaRepositorio, ICargoJpaMapper mapper) {
		return new CargoRepositorioImpl(jpaRepositorio, mapper);
	}

	@Bean
	ICargoUseCase cargoUseCase(ICargoRepositorio repositorio) {
		return new CargoUseCaseImpl(repositorio);
	}

	@Bean
	IEmpleadoRepositorio empleadoRepositorio(IEmpleadoJpaRepositorio jpaRepositorio, IEmpleadoJpaMapper mapper) {
		return new EmpleadoRepositorioImpl(jpaRepositorio, mapper);
	}

	@Bean
	IEmpleadoUseCase empleadoUseCase(IEmpleadoRepositorio repositorio) {
		return new EmpleadoUseCaseImpl(repositorio);
	}

	@Bean
	IActivoRepositorio activoRepositorio(IActivoJpaRepositorio jpaRepositorio, IActivoJpaMapper mapper) {
		return new ActivoRepositorioImpl(jpaRepositorio, mapper);
	}

	@Bean
	IActivoUseCase activoUseCase(IActivoRepositorio repositorio) {
		return new ActivoUseCaseImpl(repositorio);
	}

	@Bean
	IAsignacionActivoRepositorio asignacionActivoRepositorio(IAsignacionActivoJpaRepositorio jpaRepositorio, IAsignacionActivoJpaMapper mapper) {
		return new AsignacionActivoRepositorioImpl(jpaRepositorio, mapper);
	}

	@Bean
	IAsignacionActivoUseCase asignacionActivoUseCase(IAsignacionActivoRepositorio repositorio) {
		return new AsignacionActivoUseCaseImpl(repositorio);
	}

}
