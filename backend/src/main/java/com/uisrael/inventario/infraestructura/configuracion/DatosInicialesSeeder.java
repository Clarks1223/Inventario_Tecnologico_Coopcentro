package com.uisrael.inventario.infraestructura.configuracion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.uisrael.inventario.infraestructura.persistencia.jpa.CargoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.EmpleadoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.OficinaEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.UsuarioTiEntity;
import com.uisrael.inventario.infraestructura.repositorios.ICargoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IEmpleadoJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IOficinaJpaRepositorio;
import com.uisrael.inventario.infraestructura.repositorios.IUsuarioTiJpaRepositorio;

/**
 * Garantiza que la oficina "Matriz", el cargo "Analista de Software", el
 * empleado Pablo Uchuari y su acceso (usuarios_ti) existan cada vez que
 * arranca la app. Idempotente: verifica por nombre/cedula/correo antes de
 * insertar, para no duplicar en cada reinicio (no hay Flyway/Liquibase que
 * controle esto).
 */
@Component
public class DatosInicialesSeeder implements CommandLineRunner {

	private static final String NOMBRE_OFICINA_MATRIZ = "Matriz";
	private static final String NOMBRE_CARGO_ANALISTA = "Analista de Software";
	private static final String CEDULA_PABLO = "1726603739";
	private static final String CORREO_PABLO = "pablo.uchuari@coopcentro.fin.ec";

	private final IOficinaJpaRepositorio oficinaRepositorio;
	private final ICargoJpaRepositorio cargoRepositorio;
	private final IEmpleadoJpaRepositorio empleadoRepositorio;
	private final IUsuarioTiJpaRepositorio usuarioTiRepositorio;
	private final PasswordEncoder passwordEncoder;

	public DatosInicialesSeeder(IOficinaJpaRepositorio oficinaRepositorio, ICargoJpaRepositorio cargoRepositorio,
			IEmpleadoJpaRepositorio empleadoRepositorio, IUsuarioTiJpaRepositorio usuarioTiRepositorio,
			PasswordEncoder passwordEncoder) {
		this.oficinaRepositorio = oficinaRepositorio;
		this.cargoRepositorio = cargoRepositorio;
		this.empleadoRepositorio = empleadoRepositorio;
		this.usuarioTiRepositorio = usuarioTiRepositorio;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		OficinaEntity oficinaMatriz = obtenerOCrearOficinaMatriz();
		CargoEntity cargoAnalista = obtenerOCrearCargoAnalista();
		EmpleadoEntity empleadoPablo = obtenerOCrearEmpleadoPablo(oficinaMatriz, cargoAnalista);
		obtenerOCrearUsuarioTiPablo(empleadoPablo);
	}

	private OficinaEntity obtenerOCrearOficinaMatriz() {
		List<OficinaEntity> existentes = oficinaRepositorio.findByNombre(NOMBRE_OFICINA_MATRIZ);
		if (!existentes.isEmpty()) {
			return existentes.get(0);
		}
		OficinaEntity oficina = new OficinaEntity();
		oficina.setNombre(NOMBRE_OFICINA_MATRIZ);
		oficina.setActivo(true);
		return oficinaRepositorio.save(oficina);
	}

	private CargoEntity obtenerOCrearCargoAnalista() {
		List<CargoEntity> existentes = cargoRepositorio.findByNombre(NOMBRE_CARGO_ANALISTA);
		if (!existentes.isEmpty()) {
			return existentes.get(0);
		}
		CargoEntity cargo = new CargoEntity();
		cargo.setNombre(NOMBRE_CARGO_ANALISTA);
		cargo.setActivo(true);
		return cargoRepositorio.save(cargo);
	}

	private EmpleadoEntity obtenerOCrearEmpleadoPablo(OficinaEntity oficina, CargoEntity cargo) {
		List<EmpleadoEntity> existentes = empleadoRepositorio.findByCedula(CEDULA_PABLO);
		if (!existentes.isEmpty()) {
			return existentes.get(0);
		}
		EmpleadoEntity empleado = new EmpleadoEntity();
		empleado.setNombre("Pablo");
		empleado.setApellido("Uchuari");
		empleado.setCedula(CEDULA_PABLO);
		empleado.setCorreo(CORREO_PABLO);
		empleado.setExtensionTelefonica("1043");
		empleado.setOficina(oficina);
		empleado.setCargo(cargo);
		empleado.setRol("administrador");
		empleado.setActivo(true);
		return empleadoRepositorio.save(empleado);
	}

	private void obtenerOCrearUsuarioTiPablo(EmpleadoEntity empleado) {
		if (usuarioTiRepositorio.findByCorreo(CORREO_PABLO).isPresent()) {
			return;
		}
		LocalDateTime ahora = LocalDateTime.now();
		UsuarioTiEntity usuarioTi = new UsuarioTiEntity();
		usuarioTi.setEmpleado(empleado);
		usuarioTi.setCorreo(CORREO_PABLO);
		usuarioTi.setContrasena(passwordEncoder.encode(empleado.getCedula()));
		usuarioTi.setFechaCreacion(ahora);
		usuarioTi.setFechaActualizacion(ahora);
		usuarioTiRepositorio.save(usuarioTi);
	}

}
