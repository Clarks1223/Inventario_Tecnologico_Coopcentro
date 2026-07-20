package com.uisrael.inventario;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

@SpringBootTest
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
		activo.setEstado("disponible");
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
		acta.setEstadoAsignacion("activa");
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

}
