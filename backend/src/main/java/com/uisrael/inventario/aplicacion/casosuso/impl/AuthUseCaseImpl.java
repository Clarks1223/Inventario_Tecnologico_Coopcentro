package com.uisrael.inventario.aplicacion.casosuso.impl;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IAuthUseCase;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.SesionUsuario;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IUsuarioTiRepositorio;

public class AuthUseCaseImpl implements IAuthUseCase {

	private final IUsuarioTiRepositorio usuarioTiRepositorio;
	private final IEmpleadoRepositorio empleadoRepositorio;
	private final PasswordEncoder passwordEncoder;

	public AuthUseCaseImpl(IUsuarioTiRepositorio usuarioTiRepositorio, IEmpleadoRepositorio empleadoRepositorio,
			PasswordEncoder passwordEncoder) {
		this.usuarioTiRepositorio = usuarioTiRepositorio;
		this.empleadoRepositorio = empleadoRepositorio;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public SesionUsuario iniciarSesion(String correo, String contrasena) {
		UsuarioTi usuarioTi = usuarioTiRepositorio.buscarPorCorreo(correo)
				.orElseThrow(() -> new RuntimeException("Credenciales invalidas"));

		if (!passwordEncoder.matches(contrasena, usuarioTi.getContrasena())) {
			throw new RuntimeException("Credenciales invalidas");
		}

		Empleado empleado = empleadoRepositorio.buscarPorId(usuarioTi.getIdEmpleado())
				.orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

		if (!empleado.isActivo()) {
			throw new RuntimeException("Empleado sin acceso a la plataforma");
		}

		return new SesionUsuario(usuarioTi.getIdUsuarioTi(), empleado.getIdEmpleado(), empleado.getNombre(),
				empleado.getApellido(), usuarioTi.getCorreo(), empleado.getRol());
	}

}
