package com.uisrael.inventario.aplicacion.casosuso.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.GrupoActivos;
import com.uisrael.inventario.dominio.entidades.ResumenGrupo;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IGrupoActivosRepositorio;
import com.uisrael.inventario.dominio.valores.EstadoActa;
import com.uisrael.inventario.dominio.valores.EstadoActivo;
import com.uisrael.inventario.dominio.valores.EstadoGrupo;

class GrupoActivosUseCaseImplTest {

	private IGrupoActivosRepositorio grupoRepositorio;
	private IActivoRepositorio activoRepositorio;
	private IActaEntregaRecepcionRepositorio actaRepositorio;
	private IEmpleadoRepositorio empleadoRepositorio;
	private GrupoActivosUseCaseImpl useCase;

	private final List<ActaEntregaRecepcion> actas = new ArrayList<>();

	@BeforeEach
	void setUp() {
		grupoRepositorio = mock(IGrupoActivosRepositorio.class);
		activoRepositorio = mock(IActivoRepositorio.class);
		actaRepositorio = mock(IActaEntregaRecepcionRepositorio.class);
		empleadoRepositorio = mock(IEmpleadoRepositorio.class);
		useCase = new GrupoActivosUseCaseImpl(grupoRepositorio, activoRepositorio, actaRepositorio, empleadoRepositorio);

		when(actaRepositorio.listarTodos()).thenReturn(actas);
		when(grupoRepositorio.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(activoRepositorio.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(grupoRepositorio.buscarPorId(1)).thenReturn(Optional.of(grupo(1, "Caja 1")));
		when(grupoRepositorio.buscarPorId(2)).thenReturn(Optional.of(grupo(2, "Caja 2")));
		when(grupoRepositorio.listarTodos()).thenReturn(List.of(grupo(1, "Caja 1"), grupo(2, "Caja 2")));
		when(activoRepositorio.listarPorGrupo(any(Integer.class))).thenReturn(List.of());
	}

	// ---- guardar ----

	@Test
	void guardarRechazaNombreVacio() {
		NegocioException e = assertThrows(NegocioException.class, () -> useCase.guardar(grupo(0, "   ")));
		assertEquals("El nombre del grupo es obligatorio", e.getMessage());
	}

	@Test
	void guardarRechazaNombreDuplicadoIgnorandoMayusculasYEspacios() {
		NegocioException e = assertThrows(NegocioException.class, () -> useCase.guardar(grupo(0, " caja 1 ")));
		assertEquals("Ya existe un grupo con ese nombre", e.getMessage());
	}

	@Test
	void guardarRecortaElNombre() {
		GrupoActivos guardado = useCase.guardar(grupo(0, "  Puesto Nuevo  "));
		assertEquals("Puesto Nuevo", guardado.getNombre());
	}

	@Test
	void actualizarConElMismoNombreNoChocaConsigoMismo() {
		assertDoesNotThrow(() -> useCase.guardar(grupo(1, "Caja 1")));
	}

	// ---- agregarActivos ----

	@Test
	void agregarActivoDeOtroGrupoFalla() {
		activo(10, "SN-10", EstadoActivo.OPERATIVO, 2);
		NegocioException e = assertThrows(NegocioException.class, () -> useCase.agregarActivos(1, List.of(10)));
		assertTrue(e.getMessage().contains("SN-10"), e.getMessage());
		assertTrue(e.getMessage().contains("Caja 2"), e.getMessage());
	}

	@Test
	void agregarActivoDadoDeBajaFalla() {
		activo(10, "SN-10", EstadoActivo.DADO_DE_BAJA, null);
		NegocioException e = assertThrows(NegocioException.class, () -> useCase.agregarActivos(1, List.of(10)));
		assertTrue(e.getMessage().contains("SN-10"), e.getMessage());
	}

	@Test
	void agregarActivoInexistenteFalla() {
		when(activoRepositorio.buscarPorId(99)).thenReturn(Optional.empty());
		assertThrows(NegocioException.class, () -> useCase.agregarActivos(1, List.of(99)));
	}

	@Test
	void agregarActivoYaEnElMismoGrupoSeIgnora() {
		activo(10, "SN-10", EstadoActivo.OPERATIVO, 1);
		assertDoesNotThrow(() -> useCase.agregarActivos(1, List.of(10)));
		verify(activoRepositorio, never()).guardar(any());
	}

	@Test
	void agregarAsignaElGrupoAlActivo() {
		activo(10, "SN-10", EstadoActivo.OPERATIVO, null);
		useCase.agregarActivos(1, List.of(10));
		ArgumentCaptor<Activo> captor = ArgumentCaptor.forClass(Activo.class);
		verify(activoRepositorio).guardar(captor.capture());
		assertEquals(1, captor.getValue().getIdGrupo());
	}

	@Test
	void agregarEsTodoONada() {
		activo(10, "SN-10", EstadoActivo.OPERATIVO, null);
		activo(11, "SN-11", EstadoActivo.ROBADO_PERDIDO, null);
		assertThrows(NegocioException.class, () -> useCase.agregarActivos(1, List.of(10, 11)));
		verify(activoRepositorio, never()).guardar(any());
	}

	@Test
	void agregarAGrupoInexistenteFalla() {
		when(grupoRepositorio.buscarPorId(5)).thenReturn(Optional.empty());
		NegocioException e = assertThrows(NegocioException.class, () -> useCase.agregarActivos(5, List.of(10)));
		assertEquals("Grupo no encontrado", e.getMessage());
	}

	// ---- quitarActivo ----

	@Test
	void quitarActivoQueNoPerteneceFalla() {
		activo(10, "SN-10", EstadoActivo.OPERATIVO, 2);
		NegocioException e = assertThrows(NegocioException.class, () -> useCase.quitarActivo(1, 10));
		assertEquals("El activo no pertenece a este grupo", e.getMessage());
	}

	@Test
	void quitarActivoLoDesagrupa() {
		activo(10, "SN-10", EstadoActivo.OPERATIVO, 1);
		useCase.quitarActivo(1, 10);
		ArgumentCaptor<Activo> captor = ArgumentCaptor.forClass(Activo.class);
		verify(activoRepositorio).guardar(captor.capture());
		assertNull(captor.getValue().getIdGrupo());
	}

	// ---- eliminar ----

	@Test
	void eliminarDesagrupaMiembros() {
		Activo a = activo(10, "SN-10", EstadoActivo.OPERATIVO, 1);
		Activo b = activo(11, "SN-11", EstadoActivo.OPERATIVO, 1);
		when(activoRepositorio.listarPorGrupo(1)).thenReturn(List.of(a, b));

		useCase.eliminar(1);

		ArgumentCaptor<Activo> captor = ArgumentCaptor.forClass(Activo.class);
		InOrder orden = inOrder(activoRepositorio, grupoRepositorio);
		orden.verify(activoRepositorio, org.mockito.Mockito.times(2)).guardar(captor.capture());
		orden.verify(grupoRepositorio).eliminar(1);
		captor.getAllValues().forEach(activo -> assertNull(activo.getIdGrupo()));
	}

	// ---- resumen ----

	@Test
	void resumenVacio() {
		assertEquals(EstadoGrupo.VACIO, useCase.resumen(1).estado());
	}

	@Test
	void resumenEnBodega() {
		Activo a = activo(10, "SN-10", EstadoActivo.OPERATIVO, 1);
		actas.add(acta(10, 99, EstadoActa.CUSTODIA));
		when(activoRepositorio.listarPorGrupo(1)).thenReturn(List.of(a));

		ResumenGrupo resumen = useCase.resumen(1);
		assertEquals(EstadoGrupo.EN_BODEGA, resumen.estado());
		assertNull(resumen.idEmpleadoAsignado());
	}

	@Test
	void resumenAsignado() {
		Activo a = activo(10, "SN-10", EstadoActivo.OPERATIVO, 1);
		Activo b = activo(11, "SN-11", EstadoActivo.OPERATIVO, 1);
		actas.add(acta(10, 7, EstadoActa.ACTIVA));
		actas.add(acta(11, 7, EstadoActa.ACTIVA));
		actas.add(acta(11, 99, EstadoActa.DEVUELTA));
		when(activoRepositorio.listarPorGrupo(1)).thenReturn(List.of(a, b));
		when(empleadoRepositorio.buscarPorId(7)).thenReturn(Optional.of(empleado(7, "Ana", "Paz")));

		ResumenGrupo resumen = useCase.resumen(1);
		assertEquals(EstadoGrupo.ASIGNADO, resumen.estado());
		assertEquals(7, resumen.idEmpleadoAsignado());
		assertEquals("Ana Paz", resumen.nombreEmpleadoAsignado());
	}

	@Test
	void resumenMixto() {
		Activo a = activo(10, "SN-10", EstadoActivo.OPERATIVO, 1);
		Activo b = activo(11, "SN-11", EstadoActivo.OPERATIVO, 1);
		actas.add(acta(10, 99, EstadoActa.CUSTODIA));
		actas.add(acta(11, 7, EstadoActa.ACTIVA));
		when(activoRepositorio.listarPorGrupo(1)).thenReturn(List.of(a, b));

		assertEquals(EstadoGrupo.MIXTO, useCase.resumen(1).estado());
	}

	@Test
	void resumenConMiembroDadoDeBajaEsMixto() {
		Activo a = activo(10, "SN-10", EstadoActivo.OPERATIVO, 1);
		Activo b = activo(11, "SN-11", EstadoActivo.DADO_DE_BAJA, 1);
		actas.add(acta(10, 99, EstadoActa.CUSTODIA));
		when(activoRepositorio.listarPorGrupo(1)).thenReturn(List.of(a, b));

		assertEquals(EstadoGrupo.MIXTO, useCase.resumen(1).estado());
	}

	// ---- helpers ----

	private static GrupoActivos grupo(int id, String nombre) {
		GrupoActivos grupo = new GrupoActivos();
		grupo.setIdGrupo(id);
		grupo.setNombre(nombre);
		return grupo;
	}

	private Activo activo(int id, String serial, String estado, Integer idGrupo) {
		Activo activo = new Activo();
		activo.setIdActivo(id);
		activo.setSerial(serial);
		activo.setEstado(estado);
		activo.setIdGrupo(idGrupo);
		when(activoRepositorio.buscarPorId(id)).thenReturn(Optional.of(activo));
		return activo;
	}

	private static ActaEntregaRecepcion acta(int idActivo, int idEmpleado, String estado) {
		ActaEntregaRecepcion acta = new ActaEntregaRecepcion();
		acta.setIdActivo(idActivo);
		acta.setIdEmpleado(idEmpleado);
		acta.setEstadoAsignacion(estado);
		return acta;
	}

	private static Empleado empleado(int id, String nombre, String apellido) {
		Empleado empleado = new Empleado();
		empleado.setIdEmpleado(id);
		empleado.setNombre(nombre);
		empleado.setApellido(apellido);
		return empleado;
	}

}
