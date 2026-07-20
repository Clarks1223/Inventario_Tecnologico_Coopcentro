package com.uisrael.inventario.presentacion.controladores;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IUsuarioTiUseCase;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.presentacion.dto.request.UsuarioTiRequestDto;
import com.uisrael.inventario.presentacion.dto.response.UsuarioTiResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.IUsuarioTiDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios-ti")
public class UsuarioTiController {

	private final IUsuarioTiUseCase usuarioTiUseCase;
	private final IUsuarioTiDtoMapper mapper;

	public UsuarioTiController(IUsuarioTiUseCase usuarioTiUseCase, IUsuarioTiDtoMapper mapper) {
		this.usuarioTiUseCase = usuarioTiUseCase;
		this.mapper = mapper;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UsuarioTiResponseDto guardar(@Valid @RequestBody UsuarioTiRequestDto requestDto) {
		UsuarioTi usuarioTi = mapper.toDomain(requestDto);
		return mapper.toResponseDto(usuarioTiUseCase.guardar(usuarioTi));
	}

	@GetMapping
	public List<UsuarioTiResponseDto> listarTodos() {
		return usuarioTiUseCase.listarTodos().stream().map(mapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public UsuarioTiResponseDto buscarPorId(@PathVariable("id") int id) {
		return mapper.toResponseDto(usuarioTiUseCase.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public UsuarioTiResponseDto actualizar(@PathVariable("id") int id, @Valid @RequestBody UsuarioTiRequestDto requestDto) {
		UsuarioTi usuarioTi = mapper.toDomain(requestDto);
		usuarioTi.setIdUsuarioTi(id);
		return mapper.toResponseDto(usuarioTiUseCase.guardar(usuarioTi));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		usuarioTiUseCase.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}
