package com.uisrael.inventario.aplicacion.casosuso.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaDocumentoUseCase;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.GrupoActivos;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IGrupoActivosRepositorio;
import com.uisrael.inventario.dominio.repositorios.IUsuarioTiRepositorio;
import com.uisrael.inventario.dominio.valores.EstadoActa;
import com.uisrael.inventario.dominio.valores.EstadoActivo;

class AsignarGrupoTest {

	private static final int ID_GRUPO = 1;
	private static final int ID_EMPLEADO = 7;
	private static final int ID_USUARIO_TI = 3;
	private static final int ID_EMPLEADO_TI = 50;

	private IActaEntregaRecepcionRepositorio actaRepositorio;
	private IActivoRepositorio activoRepositorio;
	private IEmpleadoRepositorio empleadoRepositorio;
	private IActaDocumentoUseCase actaDocumentoUseCase;
	private IGrupoActivosRepositorio grupoRepositorio;
	private ActaEntregaRecepcionUseCaseImpl useCase;

	/** Base de actas en memoria: el caso de uso busca el acta abierta en listarTodos. */
	private final List<ActaEntregaRecepcion> actas = new ArrayList<>();
	private final List<Activo> miembros = new ArrayList<>();
	private int siguienteIdActa = 100;

	@BeforeEach
	void setUp() {
		actaRepositorio = mock(IActaEntregaRecepcionRepositorio.class);
		activoRepositorio = mock(IActivoRepositorio.class);
		empleadoRepositorio = mock(IEmpleadoRepositorio.class);
		IUsuarioTiRepositorio usuarioTiRepositorio = mock(IUsuarioTiRepositorio.class);
		actaDocumentoUseCase = mock(IActaDocumentoUseCase.class);
		grupoRepositorio = mock(IGrupoActivosRepositorio.class);
		useCase = new ActaEntregaRecepcionUseCaseImpl(actaRepositorio, activoRepositorio, empleadoRepositorio,
				usuarioTiRepositorio, actaDocumentoUseCase, grupoRepositorio);

		when(actaRepositorio.listarTodos()).thenAnswer(inv -> List.copyOf(actas));
		when(actaRepositorio.guardar(any())).thenAnswer(inv -> {
			ActaEntregaRecepcion acta = inv.getArgument(0);
			if (acta.getIdActa() == 0) {
				acta.setIdActa(siguienteIdActa++);
				actas.add(acta);
			}
			return acta;
		});
		when(actaRepositorio.buscarPorId(anyInt())).thenAnswer(inv -> actas.stream()
				.filter(a -> a.getIdActa() == (int) inv.getArgument(0)).findFirst());

		GrupoActivos grupo = new GrupoActivos();
		grupo.setIdGrupo(ID_GRUPO);
		grupo.setNombre("Caja 1");
		when(grupoRepositorio.buscarPorId(ID_GRUPO)).thenReturn(Optional.of(grupo));
		when(activoRepositorio.listarPorGrupo(ID_GRUPO)).thenAnswer(inv -> List.copyOf(miembros));

		empleado(ID_EMPLEADO, "Ana", "Paz", true);
		empleado(ID_EMPLEADO_TI, "Tec", "Nico", true);
		empleado(8, "Juan", "Perez", true);
		UsuarioTi usuarioTi = new UsuarioTi();
		usuarioTi.setIdUsuarioTi(ID_USUARIO_TI);
		usuarioTi.setIdEmpleado(ID_EMPLEADO_TI);
		when(usuarioTiRepositorio.buscarPorId(ID_USUARIO_TI)).thenReturn(Optional.of(usuarioTi));
	}

	@Test
	void asignaTodosYEmiteUnSoloLote() {
		enBodega(10, "SN-10");
		enBodega(11, "SN-11");
		enBodega(12, "SN-12");

		List<ActaEntregaRecepcion> entregadas = useCase.asignarGrupo(ID_GRUPO, ID_EMPLEADO, ID_USUARIO_TI, null,
				"Puesto completo");

		assertEquals(3, entregadas.size());
		entregadas.forEach(acta -> {
			assertEquals(EstadoActa.ACTIVA, acta.getEstadoAsignacion());
			assertEquals(ID_EMPLEADO, acta.getIdEmpleado());
			assertEquals("Puesto completo", acta.getObservacion());
		});
		assertEquals(3, actas.stream().filter(a -> EstadoActa.DEVUELTA.equals(a.getEstadoAsignacion())).count());
		assertEquals(0, actas.stream().filter(a -> EstadoActa.CUSTODIA.equals(a.getEstadoAsignacion())).count());
	}

	/**
	 * archivarLote/archivarActa son @Transactional en un bean con proxy: si
	 * fallaran dentro de esta transaccion la marcarian rollback-only y toda la
	 * asignacion se revertiria. Por eso el PDF del grupo se archiva despues, en
	 * su propia peticion (POST /actas/archivar-lote).
	 */
	@Test
	void noGeneraPdfDentroDeLaTransaccion() {
		enBodega(10, "SN-10");
		enBodega(11, "SN-11");

		useCase.asignarGrupo(ID_GRUPO, ID_EMPLEADO, ID_USUARIO_TI, null, null);

		verifyNoInteractions(actaDocumentoUseCase);
	}

	@Test
	void motivoIncluyeNombreDelGrupo() {
		enBodega(10, "SN-10");
		List<ActaEntregaRecepcion> entregadas = useCase.asignarGrupo(ID_GRUPO, ID_EMPLEADO, ID_USUARIO_TI,
				"Reemplazo", null);
		assertTrue(entregadas.get(0).getMotivo().startsWith("Asignacion del grupo Caja 1"),
				entregadas.get(0).getMotivo());
		assertTrue(entregadas.get(0).getMotivo().contains("Reemplazo"), entregadas.get(0).getMotivo());
	}

	@Test
	void bloqueaSiUnMiembroYaEstaEntregado() {
		enBodega(10, "SN-10");
		miembro(11, "SN-11", EstadoActivo.OPERATIVO);
		actas.add(acta(11, 8, EstadoActa.ACTIVA));
		int actasAntes = actas.size();

		NegocioException e = assertThrows(NegocioException.class,
				() -> useCase.asignarGrupo(ID_GRUPO, ID_EMPLEADO, ID_USUARIO_TI, null, null));

		assertTrue(e.getMessage().contains("SN-11"), e.getMessage());
		assertTrue(e.getMessage().contains("entregado a Juan Perez"), e.getMessage());
		verify(actaRepositorio, never()).guardar(any());
		assertEquals(actasAntes, actas.size());
	}

	@Test
	void bloqueaSiUnMiembroEstaDadoDeBaja() {
		enBodega(10, "SN-10");
		miembro(11, "SN-11", EstadoActivo.DADO_DE_BAJA);

		NegocioException e = assertThrows(NegocioException.class,
				() -> useCase.asignarGrupo(ID_GRUPO, ID_EMPLEADO, ID_USUARIO_TI, null, null));

		assertTrue(e.getMessage().contains("SN-11 (dado de baja)"), e.getMessage());
		verify(actaRepositorio, never()).guardar(any());
	}

	@Test
	void bloqueaSiUnMiembroEstaRobadoPerdido() {
		miembro(11, "SN-11", EstadoActivo.ROBADO_PERDIDO);

		NegocioException e = assertThrows(NegocioException.class,
				() -> useCase.asignarGrupo(ID_GRUPO, ID_EMPLEADO, ID_USUARIO_TI, null, null));

		assertTrue(e.getMessage().contains("SN-11 (robado/perdido)"), e.getMessage());
	}

	@Test
	void listaTodosLosBloqueantes() {
		miembro(10, "SN-10", EstadoActivo.DADO_DE_BAJA);
		miembro(11, "SN-11", EstadoActivo.OPERATIVO);
		actas.add(acta(11, 8, EstadoActa.ACTIVA));

		NegocioException e = assertThrows(NegocioException.class,
				() -> useCase.asignarGrupo(ID_GRUPO, ID_EMPLEADO, ID_USUARIO_TI, null, null));

		assertTrue(e.getMessage().startsWith("No se puede asignar el grupo: "), e.getMessage());
		assertTrue(e.getMessage().contains("SN-10") && e.getMessage().contains("SN-11"), e.getMessage());
	}

	@Test
	void grupoVacioFalla() {
		NegocioException e = assertThrows(NegocioException.class,
				() -> useCase.asignarGrupo(ID_GRUPO, ID_EMPLEADO, ID_USUARIO_TI, null, null));
		assertEquals("El grupo no tiene activos", e.getMessage());
	}

	@Test
	void grupoInexistenteFalla() {
		when(grupoRepositorio.buscarPorId(9)).thenReturn(Optional.empty());
		NegocioException e = assertThrows(NegocioException.class,
				() -> useCase.asignarGrupo(9, ID_EMPLEADO, ID_USUARIO_TI, null, null));
		assertEquals("Grupo no encontrado", e.getMessage());
	}

	@Test
	void empleadoInactivoFalla() {
		enBodega(10, "SN-10");
		empleado(ID_EMPLEADO, "Ana", "Paz", false);

		NegocioException e = assertThrows(NegocioException.class,
				() -> useCase.asignarGrupo(ID_GRUPO, ID_EMPLEADO, ID_USUARIO_TI, null, null));

		assertEquals("El empleado no esta activo", e.getMessage());
		verify(actaRepositorio, never()).guardar(any());
	}

	@Test
	void asignarIndividualSigueEmitiendoSuActa() {
		enBodega(10, "SN-10");

		ActaEntregaRecepcion acta = useCase.asignar(10, ID_EMPLEADO, ID_USUARIO_TI, "Entrega suelta");

		assertEquals(EstadoActa.ACTIVA, acta.getEstadoAsignacion());
		verify(actaDocumentoUseCase, times(1)).archivarActa(acta.getIdActa(), null);
		verify(actaDocumentoUseCase, never()).archivarLote(any());
	}

	// ---- helpers ----

	private Activo miembro(int id, String serial, String estado) {
		Activo activo = new Activo();
		activo.setIdActivo(id);
		activo.setSerial(serial);
		activo.setEstado(estado);
		activo.setIdGrupo(ID_GRUPO);
		miembros.add(activo);
		when(activoRepositorio.buscarPorId(id)).thenReturn(Optional.of(activo));
		return activo;
	}

	private void enBodega(int id, String serial) {
		miembro(id, serial, EstadoActivo.OPERATIVO);
		actas.add(acta(id, ID_EMPLEADO_TI, EstadoActa.CUSTODIA));
	}

	private ActaEntregaRecepcion acta(int idActivo, int idEmpleado, String estado) {
		ActaEntregaRecepcion acta = new ActaEntregaRecepcion();
		acta.setIdActa(siguienteIdActa++);
		acta.setIdActivo(idActivo);
		acta.setIdEmpleado(idEmpleado);
		acta.setIdUsuarioTi(ID_USUARIO_TI);
		acta.setEstadoAsignacion(estado);
		return acta;
	}

	private void empleado(int id, String nombre, String apellido, boolean activo) {
		Empleado empleado = new Empleado();
		empleado.setIdEmpleado(id);
		empleado.setNombre(nombre);
		empleado.setApellido(apellido);
		empleado.setActivo(activo);
		when(empleadoRepositorio.buscarPorId(id)).thenReturn(Optional.of(empleado));
	}

}
