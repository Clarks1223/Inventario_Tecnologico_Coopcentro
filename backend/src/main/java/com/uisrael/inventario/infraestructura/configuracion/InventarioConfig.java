package com.uisrael.inventario.infraestructura.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaDocumentoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaEntregaRecepcionUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoDetalleUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IAuthUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.ICargoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IEmpleadoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IOficinaUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IUsuarioTiUseCase;
import com.uisrael.inventario.aplicacion.casosuso.impl.ActaDocumentoUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.ActaEntregaRecepcionUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.ActivoDetalleUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.ActivoUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.AuthUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.CargoUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.EmpleadoUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.OficinaUseCaseImpl;
import com.uisrael.inventario.aplicacion.casosuso.impl.UsuarioTiUseCaseImpl;
import com.uisrael.inventario.dominio.documentos.IActaPdfGenerador;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoDetalleRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.ICargoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IOficinaRepositorio;
import com.uisrael.inventario.dominio.repositorios.IUsuarioTiRepositorio;
import com.uisrael.inventario.infraestructura.documentos.ActaPdfGeneradorImpl;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.ActaEntregaRecepcionRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.ActivoDetalleRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.ActivoRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.CargoRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.EmpleadoRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.OficinaRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.adaptadores.UsuarioTiRepositorioImpl;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IActaEntregaRecepcionJpaMapper;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IActivoDetalleJpaMapper;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IActivoJpaMapper;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.ICargoJpaMapper;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IEmpleadoJpaMapper;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IOficinaJpaMapper;
import com.uisrael.inventario.infraestructura.persistencia.mapeadores.IUsuarioTiJpaMapper;
import com.uisrael.inventario.infraestructura.repositorios.IActaEntregaRecepcionJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IActivoDetalleJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IActivoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.ICargoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IEmpleadoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IOficinaJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IUsuarioTiJpaRepositorio;

@Configuration
public class InventarioConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

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
	IActivoDetalleRepositorio activoDetalleRepositorio(IActivoDetalleJpaRepositorio jpaRepositorio, IActivoDetalleJpaMapper mapper) {
		return new ActivoDetalleRepositorioImpl(jpaRepositorio, mapper);
	}

	@Bean
	IActivoDetalleUseCase activoDetalleUseCase(IActivoDetalleRepositorio repositorio) {
		return new ActivoDetalleUseCaseImpl(repositorio);
	}

	@Bean
	IActivoUseCase activoUseCase(IActivoRepositorio repositorio, IActivoDetalleRepositorio detalleRepositorio) {
		return new ActivoUseCaseImpl(repositorio, detalleRepositorio);
	}

	@Bean
	IUsuarioTiRepositorio usuarioTiRepositorio(IUsuarioTiJpaRepositorio jpaRepositorio, IUsuarioTiJpaMapper mapper) {
		return new UsuarioTiRepositorioImpl(jpaRepositorio, mapper);
	}

	@Bean
	IUsuarioTiUseCase usuarioTiUseCase(IUsuarioTiRepositorio repositorio, IEmpleadoRepositorio empleadoRepositorio, PasswordEncoder passwordEncoder) {
		return new UsuarioTiUseCaseImpl(repositorio, empleadoRepositorio, passwordEncoder);
	}

	@Bean
	IAuthUseCase authUseCase(IUsuarioTiRepositorio usuarioTiRepositorio, IEmpleadoRepositorio empleadoRepositorio, PasswordEncoder passwordEncoder) {
		return new AuthUseCaseImpl(usuarioTiRepositorio, empleadoRepositorio, passwordEncoder);
	}

	@Bean
	IActaEntregaRecepcionRepositorio actaEntregaRecepcionRepositorio(IActaEntregaRecepcionJpaRepositorio jpaRepositorio, IActaEntregaRecepcionJpaMapper mapper) {
		return new ActaEntregaRecepcionRepositorioImpl(jpaRepositorio, mapper);
	}

	@Bean
	IActaEntregaRecepcionUseCase actaEntregaRecepcionUseCase(IActaEntregaRecepcionRepositorio repositorio) {
		return new ActaEntregaRecepcionUseCaseImpl(repositorio);
	}

	@Bean
	IActaPdfGenerador actaPdfGenerador(@Value("${app.actas.plantillas-dir:plantillas/actas}") String directorioPlantillas) {
		return new ActaPdfGeneradorImpl(directorioPlantillas);
	}

	@Bean
	IActaDocumentoUseCase actaDocumentoUseCase(IActaEntregaRecepcionRepositorio actaRepositorio, IActivoRepositorio activoRepositorio,
			IActivoDetalleRepositorio activoDetalleRepositorio, IEmpleadoRepositorio empleadoRepositorio,
			ICargoRepositorio cargoRepositorio, IOficinaRepositorio oficinaRepositorio, IUsuarioTiRepositorio usuarioTiRepositorio,
			IActaPdfGenerador pdfGenerador,
			@Value("${app.actas.generadas-dir:actas-generadas}") String directorioActasGeneradas) {
		return new ActaDocumentoUseCaseImpl(actaRepositorio, activoRepositorio, activoDetalleRepositorio, empleadoRepositorio,
				cargoRepositorio, oficinaRepositorio, usuarioTiRepositorio, pdfGenerador, directorioActasGeneradas);
	}

}
