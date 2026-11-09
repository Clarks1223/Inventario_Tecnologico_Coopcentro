package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IGrupoActivosUseCase;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.entidades.Activo;
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

/**
 * Administra los grupos de activos y su pertenencia. Un activo esta como
 * maximo en un grupo, y el estado del grupo se calcula siempre a partir de las
 * actas abiertas de sus miembros.
 */
public class GrupoActivosUseCaseImpl implements IGrupoActivosUseCase {

	private final IGrupoActivosRepositorio repositorio;
	private final IActivoRepositorio activoRepositorio;
	private final IActaEntregaRecepcionRepositorio actaRepositorio;
	private final IEmpleadoRepositorio empleadoRepositorio;

	public GrupoActivosUseCaseImpl(IGrupoActivosRepositorio repositorio, IActivoRepositorio activoRepositorio,
			IActaEntregaRecepcionRepositorio actaRepositorio, IEmpleadoRepositorio empleadoRepositorio) {
		this.repositorio = repositorio;
		this.activoRepositorio = activoRepositorio;
		this.actaRepositorio = actaRepositorio;
		this.empleadoRepositorio = empleadoRepositorio;
	}

	@Override
	@Transactional
	public GrupoActivos guardar(GrupoActivos grupo) {
		String nombre = grupo.getNombre() == null ? "" : grupo.getNombre().trim();
		if (nombre.isEmpty()) {
			throw new NegocioException("El nombre del grupo es obligatorio");
		}
		boolean duplicado = repositorio.listarTodos().stream()
				.filter(otro -> otro.getIdGrupo() != grupo.getIdGrupo())
				.anyMatch(otro -> otro.getNombre() != null && otro.getNombre().trim().equalsIgnoreCase(nombre));
		if (duplicado) {
			throw new NegocioException("Ya existe un grupo con ese nombre");
		}

		LocalDateTime ahora = LocalDateTime.now();
		grupo.setNombre(nombre);
		grupo.setCreatedAt(grupo.getIdGrupo() == 0 ? ahora : buscarPorId(grupo.getIdGrupo()).getCreatedAt());
		grupo.setUpdatedAt(ahora);
		return repositorio.guardar(grupo);
	}

	@Override
	public GrupoActivos buscarPorId(int idGrupo) {
		return repositorio.buscarPorId(idGrupo).orElseThrow(() -> new NegocioException("Grupo no encontrado"));
	}

	@Override
	public ResumenGrupo resumen(int idGrupo) {
		return resumir(buscarPorId(idGrupo), actasAbiertasPorActivo());
	}

	@Override
	public List<ResumenGrupo> listarResumenes() {
		Map<Integer, ActaEntregaRecepcion> actasAbiertas = actasAbiertasPorActivo();
		return repositorio.listarTodos().stream().map(grupo -> resumir(grupo, actasAbiertas)).toList();
	}

	@Override
	@Transactional
	public void eliminar(int idGrupo) {
		buscarPorId(idGrupo);
		for (Activo activo : activoRepositorio.listarPorGrupo(idGrupo)) {
			activo.setIdGrupo(null);
			activoRepositorio.guardar(activo);
		}
		repositorio.eliminar(idGrupo);
	}

	@Override
	@Transactional
	public ResumenGrupo agregarActivos(int idGrupo, List<Integer> idsActivos) {
		buscarPorId(idGrupo);

		// Se valida todo antes de escribir nada: o entran todos o ninguno.
		List<Activo> porAgregar = new ArrayList<>();
		List<String> rechazados = new ArrayList<>();
		for (Integer idActivo : new LinkedHashSet<>(idsActivos)) {
			Optional<Activo> encontrado = activoRepositorio.buscarPorId(idActivo);
			if (encontrado.isEmpty()) {
				rechazados.add("activo " + idActivo + " (no existe)");
				continue;
			}
			Activo activo = encontrado.get();
			Integer grupoActual = activo.getIdGrupo();
			if (grupoActual != null && grupoActual == idGrupo) {
				continue;
			}
			if (grupoActual != null) {
				String otroGrupo = repositorio.buscarPorId(grupoActual).map(GrupoActivos::getNombre)
						.orElse("otro grupo");
				rechazados.add(activo.getSerial() + " (ya pertenece al grupo " + otroGrupo + ")");
			} else if (!EstadoActivo.esOperativo(activo.getEstado())) {
				rechazados.add(activo.getSerial() + " (" + describirEstadoNoOperativo(activo.getEstado()) + ")");
			} else {
				porAgregar.add(activo);
			}
		}
		if (!rechazados.isEmpty()) {
			throw new NegocioException("No se pueden agregar al grupo: " + String.join(", ", rechazados));
		}

		for (Activo activo : porAgregar) {
			activo.setIdGrupo(idGrupo);
			activoRepositorio.guardar(activo);
		}
		return resumen(idGrupo);
	}

	@Override
	@Transactional
	public ResumenGrupo quitarActivo(int idGrupo, int idActivo) {
		buscarPorId(idGrupo);
		Activo activo = activoRepositorio.buscarPorId(idActivo)
				.orElseThrow(() -> new NegocioException("Activo no encontrado"));
		if (activo.getIdGrupo() == null || activo.getIdGrupo() != idGrupo) {
			throw new NegocioException("El activo no pertenece a este grupo");
		}
		activo.setIdGrupo(null);
		activoRepositorio.guardar(activo);
		return resumen(idGrupo);
	}

	/** Texto legible para un activo que ya no esta en servicio. */
	static String describirEstadoNoOperativo(String estado) {
		if (EstadoActivo.DADO_DE_BAJA.equals(estado)) {
			return "dado de baja";
		}
		if (EstadoActivo.ROBADO_PERDIDO.equals(estado)) {
			return "robado/perdido";
		}
		return "no operativo";
	}

	private Map<Integer, ActaEntregaRecepcion> actasAbiertasPorActivo() {
		return actaRepositorio.listarTodos().stream()
				.filter(ActaEntregaRecepcion::estaAbierta)
				.collect(Collectors.toMap(ActaEntregaRecepcion::getIdActivo, Function.identity(), (a, b) -> a));
	}

	private ResumenGrupo resumir(GrupoActivos grupo, Map<Integer, ActaEntregaRecepcion> actasAbiertas) {
		List<Activo> activos = activoRepositorio.listarPorGrupo(grupo.getIdGrupo());
		if (activos.isEmpty()) {
			return new ResumenGrupo(grupo, activos, EstadoGrupo.VACIO, null, null);
		}

		List<ActaEntregaRecepcion> actas = new ArrayList<>();
		for (Activo activo : activos) {
			ActaEntregaRecepcion acta = actasAbiertas.get(activo.getIdActivo());
			if (!EstadoActivo.esOperativo(activo.getEstado()) || acta == null) {
				return new ResumenGrupo(grupo, activos, EstadoGrupo.MIXTO, null, null);
			}
			actas.add(acta);
		}

		if (actas.stream().allMatch(acta -> EstadoActa.esCustodiaDeTi(acta.getEstadoAsignacion()))) {
			return new ResumenGrupo(grupo, activos, EstadoGrupo.EN_BODEGA, null, null);
		}
		Set<Integer> empleados = actas.stream().map(ActaEntregaRecepcion::getIdEmpleado).collect(Collectors.toSet());
		boolean todasEntregadas = actas.stream()
				.allMatch(acta -> EstadoActa.esEntregaAEmpleado(acta.getEstadoAsignacion()));
		if (todasEntregadas && empleados.size() == 1) {
			int idEmpleado = empleados.iterator().next();
			String nombre = empleadoRepositorio.buscarPorId(idEmpleado)
					.map(empleado -> empleado.getNombre() + " " + empleado.getApellido())
					.orElse(null);
			return new ResumenGrupo(grupo, activos, EstadoGrupo.ASIGNADO, idEmpleado, nombre);
		}
		return new ResumenGrupo(grupo, activos, EstadoGrupo.MIXTO, null, null);
	}

}
