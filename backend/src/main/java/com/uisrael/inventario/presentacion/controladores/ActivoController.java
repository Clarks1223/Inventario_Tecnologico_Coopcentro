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
import com.uisrael.inventario.dominio.entidades.ActivoDetalle;
import com.uisrael.inventario.presentacion.dto.request.ActivoRequestDto;
import com.uisrael.inventario.presentacion.dto.response.ActivoResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.IActivoDetalleDtoMapper;
import com.uisrael.inventario.presentacion.mapeadores.IActivoDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/activos")
public class ActivoController {

	private final IActivoUseCase activoUseCase;
	private final IActivoDtoMapper mapper;
	private final IActivoDetalleDtoMapper detalleMapper;

	public ActivoController(IActivoUseCase activoUseCase, IActivoDtoMapper mapper, IActivoDetalleDtoMapper detalleMapper) {
		this.activoUseCase = activoUseCase;
		this.mapper = mapper;
		this.detalleMapper = detalleMapper;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ActivoResponseDto guardar(@Valid @RequestBody ActivoRequestDto requestDto) {
		Activo activo = mapper.toDomain(requestDto);
		activo.setCreatedAt(LocalDateTime.now());
		activo.setUpdatedAt(LocalDateTime.now());
		ActivoDetalle detalle = requestDto.getDetalle() == null ? null : detalleMapper.toDomain(requestDto.getDetalle());
		Activo guardado = activoUseCase.guardar(activo, detalle);
		return enriquecerConDetalle(mapper.toResponseDto(guardado));
	}

	@GetMapping
	public List<ActivoResponseDto> listarTodos() {
		return activoUseCase.listarTodos().stream().map(mapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public ActivoResponseDto buscarPorId(@PathVariable("id") int id) {
		return enriquecerConDetalle(mapper.toResponseDto(activoUseCase.buscarPorId(id)));
	}

	@PutMapping("/{id}")
	public ActivoResponseDto actualizar(@PathVariable("id") int id, @Valid @RequestBody ActivoRequestDto requestDto) {
		Activo existente = activoUseCase.buscarPorId(id);
		Activo activo = mapper.toDomain(requestDto);
		activo.setIdActivo(id);
		activo.setCreatedAt(existente.getCreatedAt());
		activo.setUpdatedAt(LocalDateTime.now());
		ActivoDetalle detalle = requestDto.getDetalle() == null ? null : detalleMapper.toDomain(requestDto.getDetalle());
		Activo guardado = activoUseCase.guardar(activo, detalle);
		return enriquecerConDetalle(mapper.toResponseDto(guardado));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		activoUseCase.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	private ActivoResponseDto enriquecerConDetalle(ActivoResponseDto responseDto) {
		activoUseCase.buscarDetalle(responseDto.getIdActivo())
				.ifPresent(detalle -> responseDto.setDetalle(detalleMapper.toResponseDto(detalle)));
		return responseDto;
	}

}
