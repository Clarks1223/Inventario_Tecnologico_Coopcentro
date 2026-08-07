package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IAuthUseCase;
import com.uisrael.inventario.dominio.correo.IEmailSender;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.SesionUsuario;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IUsuarioTiRepositorio;

public class AuthUseCaseImpl implements IAuthUseCase {

	private static final Logger log = LoggerFactory.getLogger(AuthUseCaseImpl.class);
	private static final int EXPIRACION_MINUTOS = 30;

	private final IUsuarioTiRepositorio usuarioTiRepositorio;
	private final IEmpleadoRepositorio empleadoRepositorio;
	private final PasswordEncoder passwordEncoder;
	private final IEmailSender emailSender;
	private final String frontendBaseUrl;

	public AuthUseCaseImpl(IUsuarioTiRepositorio usuarioTiRepositorio, IEmpleadoRepositorio empleadoRepositorio,
			PasswordEncoder passwordEncoder, IEmailSender emailSender, String frontendBaseUrl) {
		this.usuarioTiRepositorio = usuarioTiRepositorio;
		this.empleadoRepositorio = empleadoRepositorio;
		this.passwordEncoder = passwordEncoder;
		this.emailSender = emailSender;
		this.frontendBaseUrl = frontendBaseUrl;
	}

	@Override
	public SesionUsuario iniciarSesion(String correo, String contrasena) {
		UsuarioTi usuarioTi = usuarioTiRepositorio.buscarPorCorreo(correo)
				.orElseThrow(() -> new NegocioException("Usuario o contraseña incorrectos"));

		if (!passwordEncoder.matches(contrasena, usuarioTi.getContrasena())) {
			throw new NegocioException("Usuario o contraseña incorrectos");
		}

		Empleado empleado = empleadoRepositorio.buscarPorId(usuarioTi.getIdEmpleado())
				.orElseThrow(() -> new NegocioException("Empleado no encontrado"));

		if (!empleado.isActivo()) {
			throw new NegocioException("Empleado sin acceso a la plataforma");
		}

		return new SesionUsuario(usuarioTi.getIdUsuarioTi(), empleado.getIdEmpleado(), empleado.getNombre(),
				empleado.getApellido(), usuarioTi.getCorreo(), empleado.getRol(), usuarioTi.isDebeCambiarContrasena());
	}

	@Override
	@Transactional
	public void solicitarRecuperacion(String correo) {
		UsuarioTi usuarioTi = usuarioTiRepositorio.buscarPorCorreo(correo).orElse(null);
		if (usuarioTi == null) {
			return;
		}

		Empleado empleado = empleadoRepositorio.buscarPorId(usuarioTi.getIdEmpleado())
				.orElseThrow(() -> new NegocioException("Empleado no encontrado"));

		String token = generarToken();
		usuarioTi.setTokenRecuperacion(token);
		usuarioTi.setTokenRecuperacionExpiracion(LocalDateTime.now().plusMinutes(EXPIRACION_MINUTOS));
		usuarioTiRepositorio.guardar(usuarioTi);

		String enlace = frontendBaseUrl + "/restablecer-contrasena?token=" + token;
		try {
			emailSender.enviarCorreoRecuperacion(usuarioTi.getCorreo(), empleado.getNombre(), enlace);
		} catch (Exception e) {
			log.error("Error enviando el correo de recuperacion de contrasena", e);
		}
	}

	@Override
	@Transactional
	public void restablecerContrasena(String token, String contrasenaNueva, String confirmarContrasenaNueva) {
		if (!contrasenaNueva.equals(confirmarContrasenaNueva)) {
			throw new NegocioException("La confirmacion no coincide con la nueva contrasena");
		}
		if (contrasenaNueva.length() < 8) {
			throw new NegocioException("La nueva contrasena debe tener al menos 8 caracteres");
		}

		UsuarioTi usuarioTi = usuarioTiRepositorio.buscarPorTokenRecuperacion(token)
				.orElseThrow(() -> new NegocioException("El enlace de recuperacion no es valido"));

		if (usuarioTi.getTokenRecuperacionExpiracion() == null
				|| usuarioTi.getTokenRecuperacionExpiracion().isBefore(LocalDateTime.now())) {
			throw new NegocioException("El enlace de recuperacion ha expirado");
		}

		usuarioTi.setContrasena(passwordEncoder.encode(contrasenaNueva));
		usuarioTi.setDebeCambiarContrasena(false);
		usuarioTi.setTokenRecuperacion(null);
		usuarioTi.setTokenRecuperacionExpiracion(null);
		usuarioTi.setFechaActualizacion(LocalDateTime.now());
		usuarioTiRepositorio.guardar(usuarioTi);
	}

	private String generarToken() {
		byte[] bytes = new byte[32];
		new SecureRandom().nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}

}
