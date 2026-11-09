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

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaEntregaRecepcionUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IGrupoActivosUseCase;
import com.uisrael.inventario.dominio.entidades.GrupoActivos;
import com.uisrael.inventario.presentacion.dto.request.AgregarActivosGrupoRequestDto;
import com.uisrael.inventario.presentacion.dto.request.AsignarGrupoRequestDto;
import com.uisrael.inventario.presentacion.dto.request.GrupoActivosRequestDto;
import com.uisrael.inventario.presentacion.dto.response.ActaEntregaRecepcionResponseDto;
import com.uisrael.inventario.presentacion.dto.response.GrupoActivosResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.IActaEntregaRecepcionDtoMapper;
import com.uisrael.inventario.presentacion.mapeadores.IGrupoActivosDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/grupos")
public class GrupoActivosController {

	private final IGrupoActivosUseCase grupoUseCase;
	private final IActaEntregaRecepcionUseCase actaUseCase;
	private final IGrupoActivosDtoMapper mapper;
	private final IActaEntregaRecepcionDtoMapper actaMapper;

	public GrupoActivosController(IGrupoActivosUseCase grupoUseCase, IActaEntregaRecepcionUseCase actaUseCase,
			IGrupoActivosDtoMapper mapper, IActaEntregaRecepcionDtoMapper actaMapper) {
		this.grupoUseCase = grupoUseCase;
		this.actaUseCase = actaUseCase;
		this.mapper = mapper;
		this.actaMapper = actaMapper;
	}

	@GetMapping
	public List<GrupoActivosResponseDto> listarTodos() {
		return grupoUseCase.listarResumenes().stream().map(mapper::toResponseDto).toList();
	}

	@GetMapping("/{id}")
	public GrupoActivosResponseDto buscarPorId(@PathVariable("id") int id) {
		return mapper.toResponseDto(grupoUseCase.resumen(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public GrupoActivosResponseDto crear(@Valid @RequestBody GrupoActivosRequestDto requestDto) {
		GrupoActivos guardado = grupoUseCase.guardar(mapper.toDomain(requestDto));
		return mapper.toResponseDto(grupoUseCase.resumen(guardado.getIdGrupo()));
	}

	@PutMapping("/{id}")
	public GrupoActivosResponseDto actualizar(@PathVariable("id") int id,
			@Valid @RequestBody GrupoActivosRequestDto requestDto) {
		GrupoActivos grupo = mapper.toDomain(requestDto);
		grupo.setIdGrupo(id);
		grupoUseCase.guardar(grupo);
		return mapper.toResponseDto(grupoUseCase.resumen(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable("id") int id) {
		grupoUseCase.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/activos")
	public GrupoActivosResponseDto agregarActivos(@PathVariable("id") int id,
			@Valid @RequestBody AgregarActivosGrupoRequestDto requestDto) {
		return mapper.toResponseDto(grupoUseCase.agregarActivos(id, requestDto.getIdsActivos()));
	}

	@DeleteMapping("/{id}/activos/{idActivo}")
	public GrupoActivosResponseDto quitarActivo(@PathVariable("id") int id, @PathVariable("idActivo") int idActivo) {
		return mapper.toResponseDto(grupoUseCase.quitarActivo(id, idActivo));
	}

	@PostMapping("/{id}/asignar")
	@ResponseStatus(HttpStatus.CREATED)
	public List<ActaEntregaRecepcionResponseDto> asignar(@PathVariable("id") int id,
			@Valid @RequestBody AsignarGrupoRequestDto requestDto) {
		return actaUseCase
				.asignarGrupo(id, requestDto.getIdEmpleado(), requestDto.getIdUsuarioTi(), requestDto.getMotivo(),
						requestDto.getObservacion())
				.stream().map(actaMapper::toResponseDto).toList();
	}

}
