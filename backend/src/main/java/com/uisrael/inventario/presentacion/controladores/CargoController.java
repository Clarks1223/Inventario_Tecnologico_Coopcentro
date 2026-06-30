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

import com.uisrael.inventario.aplicacion.casosuso.entrada.ICargoUseCase;
import com.uisrael.inventario.dominio.entidades.Cargo;
import com.uisrael.inventario.presentacion.dto.request.CargoRequestDto;
import com.uisrael.inventario.presentacion.dto.response.CargoResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.ICargoDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cargos")
public class CargoController {

	private final ICargoUseCase cargoUseCase;
	private final ICargoDtoMapper mapper;

	public CargoController(ICargoUseCase cargoUseCase, ICargoDtoMapper mapper) {
		this.cargoUseCase = cargoUseCase;
		this.mapper = mapper;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CargoResponseDto guardar(@Valid @RequestBody CargoRequestDto requestDto) {
		Cargo cargo = mapper.toDomain(requestDto);
		return mapper.toResponseDto(cargoUseCase.guardar(cargo));
	}

	@GetMapping
	public List<CargoResponseDto> listarTodos() {
		return cargoUseCase.listarTodos().stream().map(mapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public CargoResponseDto buscarPorId(@PathVariable("id") int id) {
		return mapper.toResponseDto(cargoUseCase.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public CargoResponseDto actualizar(@PathVariable("id") int id, @Valid @RequestBody CargoRequestDto requestDto) {
		Cargo cargo = mapper.toDomain(requestDto);
		cargo.setIdCargo(id);
		return mapper.toResponseDto(cargoUseCase.guardar(cargo));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		cargoUseCase.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}
