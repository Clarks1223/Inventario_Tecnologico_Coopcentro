package com.uisrael.inventario.presentacion.controladores;

import java.time.LocalDateTime;
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

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoUseCase;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.presentacion.dto.request.ActivoRequestDto;
import com.uisrael.inventario.presentacion.dto.response.ActivoResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.IActivoDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/activos")
public class ActivoController {

	private final IActivoUseCase activoUseCase;
	private final IActivoDtoMapper mapper;

	public ActivoController(IActivoUseCase activoUseCase, IActivoDtoMapper mapper) {
		this.activoUseCase = activoUseCase;
		this.mapper = mapper;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ActivoResponseDto guardar(@Valid @RequestBody ActivoRequestDto requestDto) {
		Activo activo = mapper.toDomain(requestDto);
		activo.setCreatedAt(LocalDateTime.now());
		activo.setUpdatedAt(LocalDateTime.now());
		return mapper.toResponseDto(activoUseCase.guardar(activo));
	}

	@GetMapping
	public List<ActivoResponseDto> listarTodos() {
		return activoUseCase.listarTodos().stream().map(mapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public ActivoResponseDto buscarPorId(@PathVariable("id") int id) {
		return mapper.toResponseDto(activoUseCase.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public ActivoResponseDto actualizar(@PathVariable("id") int id, @Valid @RequestBody ActivoRequestDto requestDto) {
		Activo existente = activoUseCase.buscarPorId(id);
		Activo activo = mapper.toDomain(requestDto);
		activo.setIdActivo(id);
		activo.setCreatedAt(existente.getCreatedAt());
		activo.setUpdatedAt(LocalDateTime.now());
		return mapper.toResponseDto(activoUseCase.guardar(activo));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		activoUseCase.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}
