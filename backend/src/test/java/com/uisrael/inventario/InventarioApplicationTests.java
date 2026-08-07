package com.uisrael.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaEntregaRecepcionUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IActivoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.ICargoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IEmpleadoUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IOficinaUseCase;
import com.uisrael.inventario.aplicacion.casosuso.entrada.IUsuarioTiUseCase;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.Cargo;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.valores.EstadoActa;
import com.uisrael.inventario.dominio.valores.EstadoActivo;
import com.uisrael.inventario.infraestructura.persistencia.jpa.ActaEntregaRecepcionEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.ActivoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.CargoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.EmpleadoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.OficinaEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.UsuarioTiEntity;
import com.uisrael.inventario.infraestructura.repositorios.IActaEntregaRecepcionJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IActivoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.ICargoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IEmpleadoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IOficinaJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IUsuarioTiJpaRepositorio;

/**
 * Corre contra la base de datos configurada en application.properties (no hay
 * H2/Testcontainers). @Transactional hace que cada @Test se revierta al
 * terminar, para que los datos de prueba no queden pegados en la BD real y
 * los tests se puedan correr mas de una vez sin chocar con las restricciones
 * unicas (cedula, correo, serial, codigo_inventario).
 */
@SpringBootTest
@Transactional
class InventarioApplicationTests {

	@Autowired
	IOficinaJpaRepositorio repoOficina;

	@Autowired
	ICargoJpaRepositorio repoCargo;

	@Autowired
	IEmpleadoJpaRepositorio repoEmpleado;

	@Autowired
	IUsuarioTiJpaRepositorio repoUsuarioTi;

	@Autowired
	IActivoJpaRepositorio repoActivo;

	@Autowired
	IActaEntregaRecepcionJpaRepositorio repoActa;

	@Autowired
	IOficinaUseCase oficinaUseCase;

	@Autowired
	ICargoUseCase cargoUseCase;

	@Autowired
	IEmpleadoUseCase empleadoUseCase;

	@Autowired
	IUsuarioTiUseCase usuarioTiUseCase;

	@Autowired
	IActivoUseCase activoUseCase;

	@Autowired
	IActaEntregaRecepcionUseCase actaUseCase;

	@Test
	void contextLoads() {

		//**Oficina
		OficinaEntity oficina = new OficinaEntity();
		oficina.setNombre("Oficina Central");
		oficina.setDireccion("Av. Republica E7-123");
		oficina.setActivo(true);
		repoOficina.save(oficina);

		//**Cargo
		CargoEntity cargo = new CargoEntity();
		cargo.setNombre("Analista de Sistemas");
		cargo.setActivo(true);
		repoCargo.save(cargo);

		//**Empleado (relacion con Oficina y Cargo)
		EmpleadoEntity empleado = new EmpleadoEntity();
		empleado.setNombre("Mario");
		empleado.setApellido("Perez");
		empleado.setCedula("1712345678");
		empleado.setCorreo("mario.perez@empresa.com");
		empleado.setExtensionTelefonica("1234");
		empleado.setOficina(oficina);
		empleado.setCargo(cargo);
		empleado.setRol("usuario");
		empleado.setActivo(true);
		repoEmpleado.save(empleado);

		//**UsuarioTi (relacion 1:1 con Empleado, requerido por Acta)
		UsuarioTiEntity usuarioTi = new UsuarioTiEntity();
		usuarioTi.setEmpleado(empleado);
		usuarioTi.setCorreo("mario.perez@empresa.com");
		usuarioTi.setContrasena("hash-de-prueba");
		usuarioTi.setFechaCreacion(LocalDateTime.now());
		usuarioTi.setFechaActualizacion(LocalDateTime.now());
		repoUsuarioTi.save(usuarioTi);

		//**Activo (relacion con Oficina)
		ActivoEntity activo = new ActivoEntity();
		activo.setCodigoInventario("INV-2026-001");
		activo.setTipoActivo("laptop");
		activo.setMarca("Dell");
		activo.setModelo("Latitude 5540");
		activo.setSerial("SN-ABC123456");
		activo.setEstado(EstadoActivo.OPERATIVO);
		activo.setOficina(oficina);
		activo.setObservaciones("Equipo nuevo");
		activo.setCreatedAt(LocalDateTime.now());
		activo.setUpdatedAt(LocalDateTime.now());
		repoActivo.save(activo);

		//**Acta de entrega/recepcion (relacion con Activo, Empleado y UsuarioTi)
		ActaEntregaRecepcionEntity acta = new ActaEntregaRecepcionEntity();
		acta.setActivo(activo);
		acta.setEmpleado(empleado);
		acta.setUsuarioTi(usuarioTi);
		acta.setFechaAsignacion(LocalDateTime.now());
		acta.setEstadoAsignacion(EstadoActa.ACTIVA);
		acta.setMotivo("Dotacion inicial de equipo");
		repoActa.save(acta);

		//**Consultas con relaciones
		List<EmpleadoEntity> empleados = repoEmpleado.findByApellido("Perez");
		System.out.println("** Empleados con apellido Perez: " + empleados.size());

		List<ActivoEntity> laptops = repoActivo.findByTipoActivo("laptop");
		System.out.println("** Activos tipo laptop: " + laptops.size());

		List<ActaEntregaRecepcionEntity> asignados = repoActa.findByEstadoAsignacion("activa");
		System.out.println("** Actas activas: " + asignados.size());

		List<OficinaEntity> oficinas = repoOficina.findByNombre("Oficina Central");
		System.out.println("** Oficinas encontradas: " + oficinas.size());

		List<CargoEntity> cargos = repoCargo.findByNombre("Analista de Sistemas");
		System.out.println("** Cargos encontrados: " + cargos.size());
	}

	/**
	 * Flujo completo de negocio, pasando por los casos de uso reales
	 */
	@Test
	void flujoCrudCompletoDeInventario() {

		// 1. Oficina
		Oficina oficina = new Oficina();
		oficina.setNombre("Oficina CRUD Test");
		oficina.setDireccion("Av. de Pruebas 100");
		oficina.setActivo(true);
		Oficina oficinaGuardada = oficinaUseCase.guardar(oficina);
		System.out.println("** Oficina creada: " + oficinaGuardada.getIdOficina());

		// 2. Cargo
		Cargo cargo = new Cargo();
		cargo.setNombre("Cargo CRUD Test");
		cargo.setActivo(true);
		Cargo cargoGuardado = cargoUseCase.guardar(cargo);
		System.out.println("** Cargo creado: " + cargoGuardado.getIdCargo());

		// 3. Empleado administrador (con acceso a la plataforma)
		Empleado admin = new Empleado();
		admin.setNombre("Ana");
		admin.setApellido("Administradora");
		admin.setCedula("1799999991");
		admin.setCorreo("ana.admin.crudtest@empresa.com");
		admin.setExtensionTelefonica("2001");
		admin.setIdOficina(oficinaGuardada.getIdOficina());
		admin.setIdCargo(cargoGuardado.getIdCargo());
		admin.setRol("administrador");
		admin.setActivo(true);
		Empleado adminGuardado = empleadoUseCase.guardar(admin);
		System.out.println("** Empleado administrador creado: " + adminGuardado.getIdEmpleado());

		UsuarioTi usuarioTi = new UsuarioTi();
		usuarioTi.setIdEmpleado(adminGuardado.getIdEmpleado());
		usuarioTi.setCorreo(adminGuardado.getCorreo());
		UsuarioTi usuarioTiGuardado = usuarioTiUseCase.guardar(usuarioTi);
		System.out.println("** Acceso a la plataforma otorgado: " + usuarioTiGuardado.getIdUsuarioTi());

		// 4. Empleado usuario (sin acceso a la plataforma)
		Empleado usuario = new Empleado();
		usuario.setNombre("Luis");
		usuario.setApellido("Usuario");
		usuario.setCedula("1799999992");
		usuario.setCorreo("luis.usuario.crudtest@empresa.com");
		usuario.setExtensionTelefonica("2002");
		usuario.setIdOficina(oficinaGuardada.getIdOficina());
		usuario.setIdCargo(cargoGuardado.getIdCargo());
		usuario.setRol("usuario");
		usuario.setActivo(true);
		Empleado usuarioGuardado = empleadoUseCase.guardar(usuario);
		System.out.println("** Empleado usuario creado: " + usuarioGuardado.getIdEmpleado());

		int idTi = usuarioTiGuardado.getIdUsuarioTi();

		// 5. Tres activos. Nacen OPERATIVO y bajo la custodia de quien los registra:
		// no debe existir en ningun momento un activo sin acta abierta.
		Activo activo1Guardado = activoUseCase.guardar(
				nuevoActivoLaptop("INV-CRUD-001", "SN-CRUD-001", oficinaGuardada.getIdOficina()), null, idTi);
		Activo activo2Guardado = activoUseCase.guardar(
				nuevoActivoLaptop("INV-CRUD-002", "SN-CRUD-002", oficinaGuardada.getIdOficina()), null, idTi);
		Activo activo3Guardado = activoUseCase.guardar(
				nuevoActivoLaptop("INV-CRUD-003", "SN-CRUD-003", oficinaGuardada.getIdOficina()), null, idTi);
		System.out.println("** Activos creados: " + activo1Guardado.getIdActivo() + ", "
				+ activo2Guardado.getIdActivo() + ", " + activo3Guardado.getIdActivo());

		assertEquals(EstadoActivo.OPERATIVO, activo1Guardado.getEstado());
		ActaEntregaRecepcion custodiaInicial = actaAbiertaDe(activo3Guardado.getIdActivo());
		assertEquals(EstadoActa.CUSTODIA, custodiaInicial.getEstadoAsignacion());
		assertEquals(adminGuardado.getIdEmpleado(), custodiaInicial.getIdEmpleado());
		System.out.println("** Los 3 activos quedaron en custodia de quien los registro");

		// 6. Dar de baja el primer activo: se cierra su acta de custodia
		activo1Guardado.setEstado(EstadoActivo.DADO_DE_BAJA);
		Activo activo1DadoDeBaja = activoUseCase.guardar(activo1Guardado, null, idTi);
		assertEquals(EstadoActivo.DADO_DE_BAJA, activo1DadoDeBaja.getEstado());
		assertEquals(0, actasAbiertasDe(activo1Guardado.getIdActivo()).size());
		System.out.println("** Activo 1 dado de baja y sin acta abierta");

		// 7. Reportar el segundo activo como robado/perdido
		activo2Guardado.setEstado(EstadoActivo.ROBADO_PERDIDO);
		Activo activo2Robado = activoUseCase.guardar(activo2Guardado, null, idTi);
		assertEquals(EstadoActivo.ROBADO_PERDIDO, activo2Robado.getEstado());
		assertEquals(0, actasAbiertasDe(activo2Guardado.getIdActivo()).size());
		System.out.println("** Activo 2 reportado como robado/perdido");

		// 8. Asignar el tercer activo: la custodia se cierra y se abre la entrega
		ActaEntregaRecepcion acta = actaUseCase.asignar(
				activo3Guardado.getIdActivo(),
				usuarioGuardado.getIdEmpleado(),
				idTi,
				"Entrega inicial de equipo (prueba)");
		assertEquals(EstadoActa.ACTIVA, acta.getEstadoAsignacion());
		assertEquals(EstadoActivo.OPERATIVO, activoUseCase.buscarPorId(activo3Guardado.getIdActivo()).getEstado());
		assertEquals(1, actasAbiertasDe(activo3Guardado.getIdActivo()).size());
		assertEquals(acta.getIdActa(), actaAbiertaDe(activo3Guardado.getIdActivo()).getIdActa());
		System.out.println("** Activo 3 asignado, acta " + acta.getIdActa());

		// 9. Devolver: el acta se cierra y el activo vuelve a la custodia de TI
		ActaEntregaRecepcion actaDevuelta = actaUseCase.devolver(acta.getIdActa(), "Devolucion de prueba", idTi,
				"Equipo recibido sin novedad");
		assertEquals(EstadoActa.DEVUELTA, actaDevuelta.getEstadoAsignacion());
		assertEquals(idTi, actaDevuelta.getIdUsuarioTi());
		// El comentario del tecnico es lo que se imprime en Observacion_General.
		assertEquals("Equipo recibido sin novedad", actaDevuelta.getObservacion());
		assertEquals(EstadoActivo.OPERATIVO, activoUseCase.buscarPorId(activo3Guardado.getIdActivo()).getEstado());

		ActaEntregaRecepcion custodiaTrasDevolucion = actaAbiertaDe(activo3Guardado.getIdActivo());
		assertEquals(EstadoActa.CUSTODIA, custodiaTrasDevolucion.getEstadoAsignacion());
		assertEquals(adminGuardado.getIdEmpleado(), custodiaTrasDevolucion.getIdEmpleado());
		System.out.println("** Activo 3 devuelto y de vuelta en bodega, sin quedar huerfano");
	}

	/** Todas las actas sin cerrar de un activo: el invariante es que haya 0 o 1. */
	private List<ActaEntregaRecepcion> actasAbiertasDe(int idActivo) {
		return actaUseCase.listarTodos().stream()
				.filter(acta -> acta.getIdActivo() == idActivo)
				.filter(ActaEntregaRecepcion::estaAbierta)
				.toList();
	}

	private ActaEntregaRecepcion actaAbiertaDe(int idActivo) {
		List<ActaEntregaRecepcion> abiertas = actasAbiertasDe(idActivo);
		assertEquals(1, abiertas.size(), "un activo operativo debe tener exactamente un acta abierta");
		return abiertas.get(0);
	}

	private Activo nuevoActivoLaptop(String codigoInventario, String serial, int idOficina) {
		Activo activo = new Activo();
		activo.setCodigoInventario(codigoInventario);
		activo.setTipoActivo("laptop");
		activo.setMarca("Dell");
		activo.setModelo("Latitude 5540");
		activo.setSerial(serial);
		activo.setEstado(EstadoActivo.OPERATIVO);
		activo.setIdOficina(idOficina);
		activo.setObservaciones("Activo de prueba CRUD");
		activo.setCreatedAt(LocalDateTime.now());
		activo.setUpdatedAt(LocalDateTime.now());
		return activo;
	}

}
