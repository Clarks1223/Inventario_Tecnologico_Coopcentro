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

import com.uisrael.inventario.aplicacion.casosuso.entrada.IEmpleadoUseCase;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.presentacion.dto.request.EmpleadoRequestDto;
import com.uisrael.inventario.presentacion.dto.response.EmpleadoResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.IEmpleadoDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

	private final IEmpleadoUseCase empleadoUseCase;
	private final IEmpleadoDtoMapper mapper;

	public EmpleadoController(IEmpleadoUseCase empleadoUseCase, IEmpleadoDtoMapper mapper) {
		this.empleadoUseCase = empleadoUseCase;
		this.mapper = mapper;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EmpleadoResponseDto guardar(@Valid @RequestBody EmpleadoRequestDto requestDto) {
		Empleado empleado = mapper.toDomain(requestDto);
		return mapper.toResponseDto(empleadoUseCase.guardar(empleado));
	}

	@GetMapping
	public List<EmpleadoResponseDto> listarTodos() {
		return empleadoUseCase.listarTodos().stream().map(mapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public EmpleadoResponseDto buscarPorId(@PathVariable("id") int id) {
		return mapper.toResponseDto(empleadoUseCase.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public EmpleadoResponseDto actualizar(@PathVariable("id") int id, @Valid @RequestBody EmpleadoRequestDto requestDto) {
		Empleado empleado = mapper.toDomain(requestDto);
		empleado.setIdEmpleado(id);
		return mapper.toResponseDto(empleadoUseCase.guardar(empleado));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		empleadoUseCase.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}
