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

import com.uisrael.inventario.aplicacion.casosuso.entrada.IAsignacionActivoUseCase;
import com.uisrael.inventario.dominio.entidades.AsignacionActivo;
import com.uisrael.inventario.presentacion.dto.request.AsignacionActivoRequestDto;
import com.uisrael.inventario.presentacion.dto.response.AsignacionActivoResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.IAsignacionActivoDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionActivoController {

	private final IAsignacionActivoUseCase asignacionUseCase;
	private final IAsignacionActivoDtoMapper mapper;

	public AsignacionActivoController(IAsignacionActivoUseCase asignacionUseCase, IAsignacionActivoDtoMapper mapper) {
		this.asignacionUseCase = asignacionUseCase;
		this.mapper = mapper;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AsignacionActivoResponseDto guardar(@Valid @RequestBody AsignacionActivoRequestDto requestDto) {
		AsignacionActivo asignacion = mapper.toDomain(requestDto);
		return mapper.toResponseDto(asignacionUseCase.guardar(asignacion));
	}

	@GetMapping
	public List<AsignacionActivoResponseDto> listarTodos() {
		return asignacionUseCase.listarTodos().stream().map(mapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public AsignacionActivoResponseDto buscarPorId(@PathVariable("id") int id) {
		return mapper.toResponseDto(asignacionUseCase.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public AsignacionActivoResponseDto actualizar(@PathVariable("id") int id, @Valid @RequestBody AsignacionActivoRequestDto requestDto) {
		AsignacionActivo asignacion = mapper.toDomain(requestDto);
		asignacion.setIdAsignacion(id);
		return mapper.toResponseDto(asignacionUseCase.guardar(asignacion));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		asignacionUseCase.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}
