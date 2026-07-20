package com.uisrael.inventario.presentacion.controladores;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IAuthUseCase;
import com.uisrael.inventario.dominio.entidades.SesionUsuario;
import com.uisrael.inventario.presentacion.dto.request.LoginRequestDto;
import com.uisrael.inventario.presentacion.dto.response.LoginResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.IAuthDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final IAuthUseCase authUseCase;
	private final IAuthDtoMapper mapper;

	public AuthController(IAuthUseCase authUseCase, IAuthDtoMapper mapper) {
		this.authUseCase = authUseCase;
		this.mapper = mapper;
	}

	@PostMapping("/login")
	public LoginResponseDto login(@Valid @RequestBody LoginRequestDto requestDto) {
		SesionUsuario sesion = authUseCase.iniciarSesion(requestDto.getCorreo(), requestDto.getContrasena());
		return mapper.toResponseDto(sesion);
	}

}
