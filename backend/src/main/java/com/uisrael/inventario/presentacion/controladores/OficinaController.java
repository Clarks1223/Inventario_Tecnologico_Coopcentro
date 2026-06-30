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

import com.uisrael.inventario.aplicacion.casosuso.entrada.IOficinaUseCase;
import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.presentacion.dto.request.OficinaRequestDto;
import com.uisrael.inventario.presentacion.dto.response.OficinaResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.IOficinaDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/oficinas")
public class OficinaController {

	private final IOficinaUseCase oficinaUseCase;
	private final IOficinaDtoMapper mapper;

	public OficinaController(IOficinaUseCase oficinaUseCase, IOficinaDtoMapper mapper) {
		this.oficinaUseCase = oficinaUseCase;
		this.mapper = mapper;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OficinaResponseDto guardar(@Valid @RequestBody OficinaRequestDto requestDto) {
		Oficina oficina = mapper.toDomain(requestDto);
		return mapper.toResponseDto(oficinaUseCase.guardar(oficina));
	}

	@GetMapping
	public List<OficinaResponseDto> listarTodos() {
		return oficinaUseCase.listarTodos().stream().map(mapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public OficinaResponseDto buscarPorId(@PathVariable("id") int id) {
		return mapper.toResponseDto(oficinaUseCase.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public OficinaResponseDto actualizar(@PathVariable("id") int id, @Valid @RequestBody OficinaRequestDto requestDto) {
		Oficina oficina = mapper.toDomain(requestDto);
		oficina.setIdOficina(id);
		return mapper.toResponseDto(oficinaUseCase.guardar(oficina));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		oficinaUseCase.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}
