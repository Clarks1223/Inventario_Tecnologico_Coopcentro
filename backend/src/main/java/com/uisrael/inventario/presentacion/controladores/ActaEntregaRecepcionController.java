package com.uisrael.inventario.presentacion.controladores;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaDocumentoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaEntregaRecepcionUseCase;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.presentacion.dto.request.ActaEntregaRecepcionRequestDto;
import com.uisrael.inventario.presentacion.dto.request.AsignarActivoRequestDto;
import com.uisrael.inventario.presentacion.dto.request.DevolverActivoRequestDto;
import com.uisrael.inventario.presentacion.dto.request.DevolverTodoEmpleadoRequestDto;
import com.uisrael.inventario.presentacion.dto.response.ActaEntregaRecepcionResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.IActaEntregaRecepcionDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/actas")
public class ActaEntregaRecepcionController {

	private final IActaEntregaRecepcionUseCase actaUseCase;
	private final IActaDocumentoUseCase actaDocumentoUseCase;
	private final IActaEntregaRecepcionDtoMapper mapper;

	public ActaEntregaRecepcionController(IActaEntregaRecepcionUseCase actaUseCase, IActaDocumentoUseCase actaDocumentoUseCase,
			IActaEntregaRecepcionDtoMapper mapper) {
		this.actaUseCase = actaUseCase;
		this.actaDocumentoUseCase = actaDocumentoUseCase;
		this.mapper = mapper;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ActaEntregaRecepcionResponseDto guardar(@Valid @RequestBody ActaEntregaRecepcionRequestDto requestDto) {
		ActaEntregaRecepcion acta = mapper.toDomain(requestDto);
		return mapper.toResponseDto(actaUseCase.guardar(acta));
	}

	@GetMapping
	public List<ActaEntregaRecepcionResponseDto> listarTodos() {
		return actaUseCase.listarTodos().stream().map(mapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public ActaEntregaRecepcionResponseDto buscarPorId(@PathVariable("id") int id) {
		return mapper.toResponseDto(actaUseCase.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public ActaEntregaRecepcionResponseDto actualizar(@PathVariable("id") int id, @Valid @RequestBody ActaEntregaRecepcionRequestDto requestDto) {
		ActaEntregaRecepcion acta = mapper.toDomain(requestDto);
		acta.setIdActa(id);
		return mapper.toResponseDto(actaUseCase.guardar(acta));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		actaUseCase.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/imprimir")
	public ResponseEntity<byte[]> imprimir(@PathVariable("id") int id) {
		byte[] pdf = actaDocumentoUseCase.generarPdf(id);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=acta-" + id + ".pdf")
				.body(pdf);
	}

	@PostMapping("/asignar")
	@ResponseStatus(HttpStatus.CREATED)
	public ActaEntregaRecepcionResponseDto asignar(@Valid @RequestBody AsignarActivoRequestDto requestDto) {
		ActaEntregaRecepcion acta = actaUseCase.asignar(requestDto.getIdActivo(), requestDto.getIdEmpleado(),
				requestDto.getIdUsuarioTi(), requestDto.getMotivo());
		return mapper.toResponseDto(acta);
	}

	@PostMapping("/{id}/devolver")
	public ActaEntregaRecepcionResponseDto devolver(@PathVariable("id") int id, @RequestBody DevolverActivoRequestDto requestDto) {
		ActaEntregaRecepcion acta = actaUseCase.devolver(id, requestDto.getMotivo());
		return mapper.toResponseDto(acta);
	}

	@PostMapping("/devolver-todo-empleado")
	public List<ActaEntregaRecepcionResponseDto> devolverTodoEmpleado(@Valid @RequestBody DevolverTodoEmpleadoRequestDto requestDto) {
		return actaUseCase.devolverTodoEmpleado(requestDto.getIdEmpleado(), requestDto.getMotivo()).stream()
				.map(mapper::toResponseDto).toList();
	}

}
