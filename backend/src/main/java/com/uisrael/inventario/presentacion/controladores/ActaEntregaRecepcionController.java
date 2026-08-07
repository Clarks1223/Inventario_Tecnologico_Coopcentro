package com.uisrael.inventario.presentacion.controladores;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaDocumentoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaEntregaRecepcionUseCase;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.presentacion.dto.request.AsignarActivoRequestDto;
import com.uisrael.inventario.presentacion.dto.request.DevolverActivoRequestDto;
import com.uisrael.inventario.dominio.documentos.ActasArchivadas;
import com.uisrael.inventario.presentacion.dto.request.ArchivarLoteRequestDto;
import com.uisrael.inventario.presentacion.dto.request.DevolverTodoEmpleadoRequestDto;
import com.uisrael.inventario.presentacion.dto.request.ObservacionActaRequestDto;
import com.uisrael.inventario.presentacion.dto.response.ActaEntregaRecepcionResponseDto;
import com.uisrael.inventario.presentacion.dto.response.ActasArchivadasResponseDto;
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

	@GetMapping
	public List<ActaEntregaRecepcionResponseDto> listarTodos() {
		return actaUseCase.listarTodos().stream().map(mapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public ActaEntregaRecepcionResponseDto buscarPorId(@PathVariable("id") int id) {
		return mapper.toResponseDto(actaUseCase.buscarPorId(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		actaUseCase.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/imprimibles-empleado/{idEmpleado}")
	public List<Integer> listarActasImprimibles(@PathVariable("idEmpleado") int idEmpleado) {
		return actaDocumentoUseCase.listarActasImprimibles(idEmpleado);
	}

	private ActasArchivadasResponseDto aDto(ActasArchivadas archivadas) {
		return new ActasArchivadasResponseDto(archivadas.rutas(), archivadas.carpeta(), archivadas.cantidad());
	}

	private String observacionDe(ObservacionActaRequestDto requestDto) {
		return requestDto == null ? null : requestDto.getObservacion();
	}

	/**
	 * Generan el acta y la guardan en su carpeta; devuelven la ruta, no el PDF.
	 * Son POST porque archivan el documento y guardan el comentario del tecnico.
	 */
	@PostMapping("/imprimibles-empleado/{idEmpleado}/archivar")
	public ActasArchivadasResponseDto archivarTodasDelEmpleado(@PathVariable("idEmpleado") int idEmpleado,
			@Valid @RequestBody(required = false) ObservacionActaRequestDto requestDto) {
		return aDto(actaDocumentoUseCase.archivarActasDelEmpleado(idEmpleado, observacionDe(requestDto)));
	}

	@PostMapping("/{id}/archivar")
	public ActasArchivadasResponseDto archivar(@PathVariable("id") int id,
			@Valid @RequestBody(required = false) ObservacionActaRequestDto requestDto) {
		return aDto(actaDocumentoUseCase.archivarActa(id, observacionDe(requestDto)));
	}

	/** Archiva un lote de actas ya cerradas; lo usa la devolucion masiva. */
	@PostMapping("/archivar-lote")
	public ActasArchivadasResponseDto archivarLote(@Valid @RequestBody ArchivarLoteRequestDto requestDto) {
		return aDto(actaDocumentoUseCase.archivarLote(requestDto.getIdsActas()));
	}

	@PostMapping("/asignar")
	@ResponseStatus(HttpStatus.CREATED)
	public ActaEntregaRecepcionResponseDto asignar(@Valid @RequestBody AsignarActivoRequestDto requestDto) {
		ActaEntregaRecepcion acta = actaUseCase.asignar(requestDto.getIdActivo(), requestDto.getIdEmpleado(),
				requestDto.getIdUsuarioTi(), requestDto.getMotivo());
		return mapper.toResponseDto(acta);
	}

	@PostMapping("/{id}/devolver")
	public ActaEntregaRecepcionResponseDto devolver(@PathVariable("id") int id, @Valid @RequestBody DevolverActivoRequestDto requestDto) {
		ActaEntregaRecepcion acta = actaUseCase.devolver(id, requestDto.getMotivo(), requestDto.getIdUsuarioTi(),
				requestDto.getObservacion());
		return mapper.toResponseDto(acta);
	}

	@PostMapping("/devolver-todo-empleado")
	public List<ActaEntregaRecepcionResponseDto> devolverTodoEmpleado(@Valid @RequestBody DevolverTodoEmpleadoRequestDto requestDto) {
		return actaUseCase
				.devolverTodoEmpleado(requestDto.getIdEmpleado(), requestDto.getMotivo(), requestDto.getIdUsuarioTi(),
						requestDto.getObservacion())
				.stream().map(mapper::toResponseDto).toList();
	}

}
