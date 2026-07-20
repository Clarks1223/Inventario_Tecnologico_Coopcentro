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

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoDetalleUseCase;
import com.uisrael.inventario.dominio.entidades.ActivoDetalle;
import com.uisrael.inventario.presentacion.dto.request.ActivoDetalleRequestDto;
import com.uisrael.inventario.presentacion.dto.response.ActivoDetalleResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.IActivoDetalleDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/activo-detalles")
public class ActivoDetalleController {

	private final IActivoDetalleUseCase activoDetalleUseCase;
	private final IActivoDetalleDtoMapper mapper;

	public ActivoDetalleController(IActivoDetalleUseCase activoDetalleUseCase, IActivoDetalleDtoMapper mapper) {
		this.activoDetalleUseCase = activoDetalleUseCase;
		this.mapper = mapper;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ActivoDetalleResponseDto guardar(@Valid @RequestBody ActivoDetalleRequestDto requestDto) {
		ActivoDetalle activoDetalle = mapper.toDomain(requestDto);
		return mapper.toResponseDto(activoDetalleUseCase.guardar(activoDetalle));
	}

	@GetMapping
	public List<ActivoDetalleResponseDto> listarTodos() {
		return activoDetalleUseCase.listarTodos().stream().map(mapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public ActivoDetalleResponseDto buscarPorId(@PathVariable("id") int id) {
		return mapper.toResponseDto(activoDetalleUseCase.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public ActivoDetalleResponseDto actualizar(@PathVariable("id") int id, @Valid @RequestBody ActivoDetalleRequestDto requestDto) {
		ActivoDetalle activoDetalle = mapper.toDomain(requestDto);
		activoDetalle.setIdActivo(id);
		return mapper.toResponseDto(activoDetalleUseCase.guardar(activoDetalle));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		activoDetalleUseCase.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}
