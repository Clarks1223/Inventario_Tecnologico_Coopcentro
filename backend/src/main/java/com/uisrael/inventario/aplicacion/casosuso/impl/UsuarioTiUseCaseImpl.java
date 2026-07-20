package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IUsuarioTiUseCase;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IUsuarioTiRepositorio;

public class UsuarioTiUseCaseImpl implements IUsuarioTiUseCase {

	private final IUsuarioTiRepositorio repositorio;
	private final IEmpleadoRepositorio empleadoRepositorio;
	private final PasswordEncoder passwordEncoder;

	public UsuarioTiUseCaseImpl(IUsuarioTiRepositorio repositorio, IEmpleadoRepositorio empleadoRepositorio,
			PasswordEncoder passwordEncoder) {
		this.repositorio = repositorio;
		this.empleadoRepositorio = empleadoRepositorio;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UsuarioTi guardar(UsuarioTi usuarioTi) {
		LocalDateTime ahora = LocalDateTime.now();
		if (usuarioTi.getIdUsuarioTi() == 0) {
			Empleado empleado = empleadoRepositorio.buscarPorId(usuarioTi.getIdEmpleado())
					.orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
			usuarioTi.setContrasena(passwordEncoder.encode(empleado.getCedula()));
			usuarioTi.setFechaCreacion(ahora);
			usuarioTi.setFechaActualizacion(ahora);
		} else {
			UsuarioTi existente = repositorio.buscarPorId(usuarioTi.getIdUsuarioTi())
					.orElseThrow(() -> new RuntimeException("UsuarioTi no encontrado"));
			usuarioTi.setContrasena(existente.getContrasena());
			usuarioTi.setFechaCreacion(existente.getFechaCreacion());
			usuarioTi.setFechaActualizacion(ahora);
		}
		return repositorio.guardar(usuarioTi);
	}

	@Override
	public UsuarioTi buscarPorId(int idUsuarioTi) {
		return repositorio.buscarPorId(idUsuarioTi)
				.orElseThrow(() -> new RuntimeException("UsuarioTi no encontrado"));
	}

	@Override
	public List<UsuarioTi> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	public void eliminar(int idUsuarioTi) {
		repositorio.eliminar(idUsuarioTi);
	}

}
