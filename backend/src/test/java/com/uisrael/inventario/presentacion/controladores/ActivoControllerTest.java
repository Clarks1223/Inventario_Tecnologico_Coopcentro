package com.uisrael.inventario.presentacion.controladores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoDetalleUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IGrupoActivosUseCase;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.GrupoActivos;
import com.uisrael.inventario.presentacion.dto.request.ActivoRequestDto;
import com.uisrael.inventario.presentacion.dto.response.ActivoResponseDto;
import com.uisrael.inventario.presentacion.mapeadores.IActivoDetalleDtoMapperImpl;
import com.uisrael.inventario.presentacion.mapeadores.IActivoDtoMapperImpl;

class ActivoControllerTest {

	private IActivoUseCase activoUseCase;
	private IGrupoActivosUseCase grupoUseCase;
	private ActivoController controller;

	@BeforeEach
	void setUp() {
		activoUseCase = mock(IActivoUseCase.class);
		IActivoDetalleUseCase detalleUseCase = mock(IActivoDetalleUseCase.class);
		grupoUseCase = mock(IGrupoActivosUseCase.class);
		controller = new ActivoController(activoUseCase, detalleUseCase, new IActivoDtoMapperImpl(),
				new IActivoDetalleDtoMapperImpl(), grupoUseCase);

		when(activoUseCase.buscarDetalle(anyInt())).thenReturn(Optional.empty());
		when(activoUseCase.guardar(any(), any(), any(Integer.class))).thenAnswer(inv -> inv.getArgument(0));
		when(activoUseCase.guardar(any(), isNull(), any(Integer.class))).thenAnswer(inv -> inv.getArgument(0));
		when(detalleUseCase.listarTodos()).thenReturn(List.of());
		when(grupoUseCase.listarTodos()).thenReturn(List.of(new GrupoActivos(5, "Caja 1", null, null, null)));
	}

	@Test
	void actualizarConservaElGrupo() {
		Activo existente = activo(20, 5);
		when(activoUseCase.buscarPorId(20)).thenReturn(existente);
		ActivoRequestDto request = new ActivoRequestDto();
		request.setSerial("SN-20");
		request.setIdUsuarioTi(3);

		controller.actualizar(20, request);

		ArgumentCaptor<Activo> captor = ArgumentCaptor.forClass(Activo.class);
		verify(activoUseCase).guardar(captor.capture(), isNull(), any(Integer.class));
		assertEquals(5, captor.getValue().getIdGrupo());
	}

	@Test
	void listarIncluyeNombreDelGrupo() {
		when(activoUseCase.listarTodos()).thenReturn(List.of(activo(20, 5), activo(21, null)));

		List<ActivoResponseDto> respuesta = controller.listarTodos();

		assertEquals(5, respuesta.get(0).getIdGrupo());
		assertEquals("Caja 1", respuesta.get(0).getNombreGrupo());
		assertNull(respuesta.get(1).getIdGrupo());
		assertNull(respuesta.get(1).getNombreGrupo());
	}

	private static Activo activo(int id, Integer idGrupo) {
		Activo activo = new Activo();
		activo.setIdActivo(id);
		activo.setSerial("SN-" + id);
		activo.setIdGrupo(idGrupo);
		return activo;
	}

}
