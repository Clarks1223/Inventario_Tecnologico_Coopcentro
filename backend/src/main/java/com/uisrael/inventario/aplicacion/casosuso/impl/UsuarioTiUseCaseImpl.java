package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IUsuarioTiUseCase;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
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
	@Transactional
	public UsuarioTi guardar(UsuarioTi usuarioTi) {
		LocalDateTime ahora = LocalDateTime.now();
		if (usuarioTi.getIdUsuarioTi() == 0) {
			Empleado empleado = empleadoRepositorio.buscarPorId(usuarioTi.getIdEmpleado())
					.orElseThrow(() -> new NegocioException("Empleado no encontrado"));
			usuarioTi.setContrasena(passwordEncoder.encode(empleado.getCedula()));
			usuarioTi.setDebeCambiarContrasena(true);
			usuarioTi.setFechaCreacion(ahora);
			usuarioTi.setFechaActualizacion(ahora);
		} else {
			empleadoRepositorio.buscarPorId(usuarioTi.getIdEmpleado())
					.orElseThrow(() -> new NegocioException("Empleado no encontrado"));
			UsuarioTi existente = repositorio.buscarPorId(usuarioTi.getIdUsuarioTi())
					.orElseThrow(() -> new NegocioException("UsuarioTi no encontrado"));
			usuarioTi.setContrasena(existente.getContrasena());
			usuarioTi.setDebeCambiarContrasena(existente.isDebeCambiarContrasena());
			usuarioTi.setTokenRecuperacion(existente.getTokenRecuperacion());
			usuarioTi.setTokenRecuperacionExpiracion(existente.getTokenRecuperacionExpiracion());
			usuarioTi.setFechaCreacion(existente.getFechaCreacion());
			usuarioTi.setFechaActualizacion(ahora);
		}
		return repositorio.guardar(usuarioTi);
	}

	@Override
	@Transactional
	public void cambiarContrasena(int idUsuarioTi, String contrasenaActual, String contrasenaNueva,
			String confirmarContrasenaNueva) {
		if (!contrasenaNueva.equals(confirmarContrasenaNueva)) {
			throw new NegocioException("La confirmacion no coincide con la nueva contrasena");
		}
		UsuarioTi existente = repositorio.buscarPorId(idUsuarioTi)
				.orElseThrow(() -> new NegocioException("UsuarioTi no encontrado"));
		if (!passwordEncoder.matches(contrasenaActual, existente.getContrasena())) {
			throw new NegocioException("La contrasena actual no es correcta");
		}
		if (contrasenaNueva.length() < 8) {
			throw new NegocioException("La nueva contrasena debe tener al menos 8 caracteres");
		}
		if (contrasenaNueva.equals(contrasenaActual)) {
			throw new NegocioException("La nueva contrasena debe ser diferente a la actual");
		}
		existente.setContrasena(passwordEncoder.encode(contrasenaNueva));
		existente.setDebeCambiarContrasena(false);
		existente.setTokenRecuperacion(null);
		existente.setTokenRecuperacionExpiracion(null);
		existente.setFechaActualizacion(LocalDateTime.now());
		repositorio.guardar(existente);
	}

	@Override
	public UsuarioTi buscarPorId(int idUsuarioTi) {
		return repositorio.buscarPorId(idUsuarioTi)
				.orElseThrow(() -> new NegocioException("UsuarioTi no encontrado"));
	}

	@Override
	public List<UsuarioTi> listarTodos() {
		return repositorio.listarTodos();
	}

	@Override
	@Transactional
	public void eliminar(int idUsuarioTi) {
		repositorio.eliminar(idUsuarioTi);
	}

}
