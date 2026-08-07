package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaDocumentoUseCase;
import com.uisrael.inventario.dominio.documentos.ActasArchivadas;
import com.uisrael.inventario.dominio.documentos.IActaPdfGenerador;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.ActivoDetalle;
import com.uisrael.inventario.dominio.entidades.Cargo;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.excepciones.NegocioException;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoDetalleRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.ICargoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IOficinaRepositorio;
import com.uisrael.inventario.dominio.repositorios.IUsuarioTiRepositorio;
import com.uisrael.inventario.dominio.valores.EstadoActa;

/**
 * Arma los datos de un acta (activo, empleado, oficina, cargo, TI) y decide
 * que plantilla y que campos le corresponden segun tipoActivo. Ademas de
 * devolver el PDF, lo guarda en disco en:
 * {directorioActasGeneradas}/{oficina}/{nombre apellido} -{cedula}/{entrega|recepcion}/{cedula}-{entrega|recepcion}-{Pc|DispositivoMovil}-{idActa}.pdf
 */
public class ActaDocumentoUseCaseImpl implements IActaDocumentoUseCase {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final String PLANTILLA_PCS = "ACTA ENTREGA - RECEPCION - PCs.pdf";
	private static final String PLANTILLA_MOVILES = "ACTA ENTREGA - RECEPCION - Dispositivos_Moviles.pdf";
	private static final String[] MARCAS_PROCESADOR_CONOCIDAS = { "Intel", "AMD", "Apple", "Qualcomm" };

	/**
	 * Perifericos que tienen fila propia en la plantilla de PCs, por su
	 * tipoDispositivo. Los que no estan aqui (diadema, webcam, ...) caen en las
	 * filas genericas "Periferico_AdicionalN".
	 */
	private static final Map<String, String> SLOTS_DEDICADOS = Map.of(
			"teclado", "Teclado",
			"mouse", "Mouse",
			"monitor", "Monitor");

	/** Fila de la plantilla que le corresponde a un periferico, o null si no tiene una propia. */
	private String slotDedicadoDe(String tipoDispositivo) {
		if (tipoDispositivo == null) {
			return null;
		}
		return SLOTS_DEDICADOS.get(tipoDispositivo.trim().toLowerCase());
	}

	private final IActaEntregaRecepcionRepositorio actaRepositorio;
	private final IActivoRepositorio activoRepositorio;
	private final IActivoDetalleRepositorio activoDetalleRepositorio;
	private final IEmpleadoRepositorio empleadoRepositorio;
	private final ICargoRepositorio cargoRepositorio;
	private final IOficinaRepositorio oficinaRepositorio;
	private final IUsuarioTiRepositorio usuarioTiRepositorio;
	private final IActaPdfGenerador pdfGenerador;
	private final String directorioActasGeneradas;

	public ActaDocumentoUseCaseImpl(IActaEntregaRecepcionRepositorio actaRepositorio, IActivoRepositorio activoRepositorio,
			IActivoDetalleRepositorio activoDetalleRepositorio, IEmpleadoRepositorio empleadoRepositorio,
			ICargoRepositorio cargoRepositorio, IOficinaRepositorio oficinaRepositorio,
			IUsuarioTiRepositorio usuarioTiRepositorio, IActaPdfGenerador pdfGenerador, String directorioActasGeneradas) {
		this.actaRepositorio = actaRepositorio;
		this.activoRepositorio = activoRepositorio;
		this.activoDetalleRepositorio = activoDetalleRepositorio;
		this.empleadoRepositorio = empleadoRepositorio;
		this.cargoRepositorio = cargoRepositorio;
		this.oficinaRepositorio = oficinaRepositorio;
		this.usuarioTiRepositorio = usuarioTiRepositorio;
		this.pdfGenerador = pdfGenerador;
		this.directorioActasGeneradas = directorioActasGeneradas;
	}

	@Override
	@Transactional
	public ActasArchivadas archivarActa(int idActa, String observacion) {
		guardarObservacion(idActa, observacion);
		// Acta individual: cubre unicamente su propio activo. Es lo que debe
		// pasar al imprimir o devolver una fila concreta, porque reemplazar un
		// mouse danado no puede generar un documento que liste tambien el
		// equipo y los demas perifericos que el empleado conserva.
		return comoArchivadas(List.of(generarPdf(idActa, null)));
	}

	/** Carpeta comun de un conjunto de rutas, para mostrarla en la notificacion. */
	private ActasArchivadas comoArchivadas(List<String> rutas) {
		String carpeta = rutas.isEmpty() ? "" : Path.of(rutas.get(0)).getParent().toString();
		return new ActasArchivadas(rutas, carpeta);
	}

	/**
	 * @param paquete conjunto de actas que se emiten juntas. Si no es nulo, el
	 *                acta de una PC embebe los perifericos del paquete y la de
	 *                un movil su impresora, para cubrir todo en menos hojas.
	 *                Nulo = acta individual, solo su propio activo.
	 *                <p>
	 *                Se pasa explicitamente en vez de deducirlo de las actas
	 *                activas del empleado porque en una devolucion masiva las
	 *                actas del paquete ya estan cerradas cuando se imprimen.
	 */
	private String generarPdf(int idActa, List<ActaEntregaRecepcion> paquete) {
		ActaEntregaRecepcion acta = actaRepositorio.buscarPorId(idActa)
				.orElseThrow(() -> new NegocioException("Acta no encontrada"));
		Activo activo = activoRepositorio.buscarPorId(acta.getIdActivo())
				.orElseThrow(() -> new NegocioException("Activo no encontrado"));
		Empleado empleado = empleadoRepositorio.buscarPorId(acta.getIdEmpleado())
				.orElseThrow(() -> new NegocioException("Empleado no encontrado"));
		Cargo cargoEmpleado = cargoRepositorio.buscarPorId(empleado.getIdCargo())
				.orElseThrow(() -> new NegocioException("Cargo no encontrado"));
		Oficina oficina = oficinaRepositorio.buscarPorId(empleado.getIdOficina())
				.orElseThrow(() -> new NegocioException("Oficina no encontrada"));
		UsuarioTi usuarioTi = usuarioTiRepositorio.buscarPorId(acta.getIdUsuarioTi())
				.orElseThrow(() -> new NegocioException("Usuario TI no encontrado"));
		Empleado empleadoTi = empleadoRepositorio.buscarPorId(usuarioTi.getIdEmpleado())
				.orElseThrow(() -> new NegocioException("Empleado de TI no encontrado"));
		Cargo cargoTi = cargoRepositorio.buscarPorId(empleadoTi.getIdCargo())
				.orElseThrow(() -> new NegocioException("Cargo de TI no encontrado"));
		ActivoDetalle detalle = activoDetalleRepositorio.buscarPorId(activo.getIdActivo()).orElse(null);

		// Las actas de custodia son un registro interno de bodega: nadie las firma
		// y no tienen plantilla asociada.
		if (EstadoActa.esCustodiaDeTi(acta.getEstadoAsignacion())) {
			throw new NegocioException("Las actas de custodia interna de TI no generan documento");
		}

		boolean esEntrega = EstadoActa.esEntregaAEmpleado(acta.getEstadoAsignacion());
		LocalDateTime fecha = esEntrega ? acta.getFechaAsignacion() : acta.getFechaDevolucion();

		String nombreEmpleadoCompleto = empleado.getNombre() + " " + empleado.getApellido();
		String nombreTiCompleto = empleadoTi.getNombre() + " " + empleadoTi.getApellido();

		Map<String, String> valores = new HashMap<>();
		valores.put("Nombre_Empleado", nombreEmpleadoCompleto);
		valores.put("Cedula_Empleado", empleado.getCedula());
		valores.put("Oficina_Empleado", oficina.getNombre());
		valores.put("Cargo_Empleado", cargoEmpleado.getNombre());
		valores.put("Fecha", fecha == null ? "" : fecha.format(FORMATO_FECHA));

		if (esEntrega) {
			valores.put("Nombre_Entrega", nombreTiCompleto);
			valores.put("Cargo_Entrega", cargoTi.getNombre());
			valores.put("Nombre_Recibe", nombreEmpleadoCompleto);
			valores.put("Cargo_Recibe", cargoEmpleado.getNombre());
		} else {
			valores.put("Nombre_Entrega", nombreEmpleadoCompleto);
			valores.put("Cargo_Entrega", cargoEmpleado.getNombre());
			valores.put("Nombre_Recibe", nombreTiCompleto);
			valores.put("Cargo_Recibe", cargoTi.getNombre());
		}

		// Comentario que escribio el tecnico para este documento. No se usa el
		// motivo: ese es trazabilidad interna y ademas se acumula con cada
		// movimiento, asi que acabaria imprimiendo el historial completo.
		valores.put("Observacion_General", textoOVacio(acta.getObservacion()));

		Set<String> casillas = new HashSet<>();
		casillas.add(esEntrega ? "Entrega" : "Devolucion");

		String nombrePlantilla = completarCamposPorTipo(activo, detalle, empleado, valores, casillas, paquete);

		byte[] pdf = pdfGenerador.generar(nombrePlantilla, valores, casillas);
		return guardarEnCarpeta(pdf, oficina.getNombre(), nombreEmpleadoCompleto, empleado.getCedula(), esEntrega,
				activo.getTipoActivo(), idActa);
	}

	private String completarCamposPorTipo(Activo activo, ActivoDetalle detalle, Empleado empleado,
			Map<String, String> valores, Set<String> casillas, List<ActaEntregaRecepcion> paquete) {
		switch (activo.getTipoActivo()) {
			case "laptop", "desktop" -> {
				valores.put("Marca_Equipo", activo.getMarca());
				valores.put("Modelo_Equipo", activo.getModelo());
				valores.put("Serie_Equipo", activo.getSerial());
				valores.put("Observacion_Equipo", textoOVacio(activo.getObservaciones()));
				valores.put("Numero_Extencion", empleado.getExtensionTelefonica());
				if (detalle != null) {
					String[] procesador = separarMarcaModeloProcesador(detalle.getProcesador());
					valores.put("Marca_Procesador", procesador[0]);
					valores.put("Modelo Procesador", procesador[1]);
					valores.put("Memoria_RAM", detalle.getRamGb() == null ? "" : detalle.getRamGb() + " GB");
					valores.put("Cantidad_DiscoDuro", concatenarNoNulos(detalle.getTipoAlmacenamiento(),
							detalle.getAlmacenamientoGb() == null ? null : detalle.getAlmacenamientoGb() + " GB"));
				}
				casillas.add("laptop".equals(activo.getTipoActivo()) ? "Laptop_Equipo" : "Desktop_Equipo");
				if (paquete != null) {
					completarPerifericosDelPaquete(paquete, activo.getIdActivo(), valores);
				}
				return PLANTILLA_PCS;
			}
			case "dispositivo_movil" -> {
				valores.put("Marca_DispositivoMovil", activo.getMarca());
				valores.put("Modelo_DispositivoMovil", activo.getModelo());
				valores.put("Serie_DispositivoMovil", activo.getSerial());
				valores.put("Observacion_DispositivoMovil", textoOVacio(activo.getObservaciones()));
				if (detalle != null) {
					valores.put("NumeroTelefonico__Tablet", detalle.getNumeroLinea());
					marcarAccesorios(detalle, casillas, "Dispositivo_Movil");
				}
				if (paquete != null) {
					completarImpresoraDelPaquete(paquete, activo.getIdActivo(), valores, casillas);
				}
				return PLANTILLA_MOVILES;
			}
			case "impresora_termica" -> {
				valores.put("Marca_ImpresoraTermica", activo.getMarca());
				valores.put("Modelo_ImpresoraTermica", activo.getModelo());
				valores.put("Serie_ImpresoraTermica", activo.getSerial());
				valores.put("Observacion_Impresora", textoOVacio(activo.getObservaciones()));
				if (detalle != null) {
					marcarAccesorios(detalle, casillas, "Impresora");
				}
				return PLANTILLA_MOVILES;
			}
			case "periferico" -> {
				// Cada periferico va en SU fila (Teclado / Mouse / Monitor), no en
				// la de EQUIPO: esa es la del computador y dejarla ocupada por un
				// mouse hace ilegible el acta.
				String tipoDispositivo = detalle == null ? null : detalle.getTipoDispositivo();
				String slot = slotDedicadoDe(tipoDispositivo);
				if (slot != null) {
					valores.put("Marca_" + slot, activo.getMarca());
					valores.put("Modelo_" + slot, activo.getModelo());
					valores.put("Serie_" + slot, activo.getSerial());
					valores.put("Observacion_" + slot, textoOVacio(activo.getObservaciones()));
				} else {
					// Periferico sin fila propia (diadema, webcam...): usa la
					// primera fila generica, rotulada con su tipo.
					valores.put("NombrePeriferico_Adicional1",
							tipoDispositivo == null || tipoDispositivo.isBlank() ? "Periferico" : tipoDispositivo);
					valores.put("MarcaPeriferico_Adicional1", activo.getMarca());
					valores.put("ModeloPeriferico_Adicional1", activo.getModelo());
					valores.put("SeriePeriferico_Adicional1", activo.getSerial());
					valores.put("Observacion_Periferico_Adicional1", textoOVacio(activo.getObservaciones()));
				}
				return PLANTILLA_PCS;
			}
			default -> throw new NegocioException(
					"No hay plantilla de acta disponible para el tipo de activo: " + activo.getTipoActivo());
		}
	}

	/**
	 * Ubicacion que le toco a un periferico en el reparto entre las PCs del
	 * empleado: indiceEquipo -1 significa que no cupo en ninguna acta.
	 */
	private record UbicacionPeriferico(int indiceEquipo, String slotDedicado, int numeroAdicional,
			String tipoDispositivo) {
	}

	/**
	 * Solo las entregas reales a empleados: las actas de custodia de bodega no
	 * representan equipo en uso y no deben entrar al reparto de perifericos ni
	 * a las actas imprimibles.
	 */
	private List<ActaEntregaRecepcion> actasActivasDelEmpleado(int idEmpleado) {
		return actaRepositorio.listarTodos().stream()
				.filter(acta -> acta.getIdEmpleado() == idEmpleado)
				.filter(acta -> EstadoActa.esEntregaAEmpleado(acta.getEstadoAsignacion()))
				.sorted(Comparator.comparing(ActaEntregaRecepcion::getFechaAsignacion)
						.thenComparing(ActaEntregaRecepcion::getIdActa))
				.toList();
	}

	/**
	 * Reparte los perifericos entre los equipos (en orden de asignacion) de modo
	 * que cada periferico aparezca en UNA sola acta: primero intenta el slot
	 * dedicado (teclado/mouse/monitor) de cada PC en orden, y si no hay libre
	 * usa los 2 slots genericos "Periferico_AdicionalN". El reparto es
	 * deterministico: mismo resultado sin importar cual acta se imprima.
	 */
	private Map<Integer, UbicacionPeriferico> repartirPerifericos(int totalEquipos, List<Activo> perifericos) {
		Map<Integer, UbicacionPeriferico> reparto = new HashMap<>();
		Map<String, boolean[]> ocupacion = new HashMap<>();
		SLOTS_DEDICADOS.values().forEach(slot -> ocupacion.put(slot, new boolean[totalEquipos]));
		int[] adicionalesOcupados = new int[totalEquipos];

		for (Activo periferico : perifericos) {
			ActivoDetalle detallePeriferico = activoDetalleRepositorio.buscarPorId(periferico.getIdActivo()).orElse(null);
			String tipoDispositivo = detallePeriferico == null ? null : detallePeriferico.getTipoDispositivo();
			String slotDeseado = slotDedicadoDe(tipoDispositivo);

			int destino = -1;
			String slotDedicado = null;
			if (slotDeseado != null) {
				boolean[] ocupado = ocupacion.get(slotDeseado);
				for (int i = 0; i < totalEquipos && destino == -1; i++) {
					if (!ocupado[i]) {
						ocupado[i] = true;
						destino = i;
						slotDedicado = slotDeseado;
					}
				}
			}
			int numeroAdicional = 0;
			if (destino == -1) {
				for (int i = 0; i < totalEquipos && destino == -1; i++) {
					if (adicionalesOcupados[i] < 2) {
						adicionalesOcupados[i]++;
						numeroAdicional = adicionalesOcupados[i];
						destino = i;
					}
				}
			}
			reparto.put(periferico.getIdActivo(),
					new UbicacionPeriferico(destino, slotDedicado, numeroAdicional, tipoDispositivo));
		}
		return reparto;
	}

	/**
	 * Agrega al acta del equipo idActivoEquipo solo los perifericos que le
	 * tocaron en el reparto.
	 */
	private void completarPerifericosDelPaquete(List<ActaEntregaRecepcion> paquete, int idActivoEquipo,
			Map<String, String> valores) {
		List<Integer> equiposPc = new ArrayList<>();
		List<Activo> perifericos = new ArrayList<>();
		for (ActaEntregaRecepcion actaActiva : paquete) {
			Activo candidato = activoRepositorio.buscarPorId(actaActiva.getIdActivo()).orElse(null);
			if (candidato == null) {
				continue;
			}
			boolean esPc = "desktop".equals(candidato.getTipoActivo()) || "laptop".equals(candidato.getTipoActivo());
			if (esPc && !equiposPc.contains(candidato.getIdActivo())) {
				equiposPc.add(candidato.getIdActivo());
			} else if ("periferico".equals(candidato.getTipoActivo())) {
				perifericos.add(candidato);
			}
		}
		// Si el equipo no viene en el paquete (p. ej. su acta ya se cerro),
		// participa al final del reparto para seguir mostrando los perifericos
		// que le correspondan sin quitarselos a las PCs que si estan.
		if (!equiposPc.contains(idActivoEquipo)) {
			equiposPc.add(idActivoEquipo);
		}

		int indiceEquipoActa = equiposPc.indexOf(idActivoEquipo);
		Map<Integer, UbicacionPeriferico> reparto = repartirPerifericos(equiposPc.size(), perifericos);

		for (Activo periferico : perifericos) {
			UbicacionPeriferico ubicacion = reparto.get(periferico.getIdActivo());
			if (ubicacion == null || ubicacion.indiceEquipo() != indiceEquipoActa) {
				continue;
			}
			if (ubicacion.slotDedicado() != null) {
				valores.put("Marca_" + ubicacion.slotDedicado(), periferico.getMarca());
				valores.put("Modelo_" + ubicacion.slotDedicado(), periferico.getModelo());
				valores.put("Serie_" + ubicacion.slotDedicado(), periferico.getSerial());
				valores.put("Observacion_" + ubicacion.slotDedicado(), textoOVacio(periferico.getObservaciones()));
			} else if (ubicacion.numeroAdicional() > 0) {
				String tipoDispositivo = ubicacion.tipoDispositivo();
				valores.put("NombrePeriferico_Adicional" + ubicacion.numeroAdicional(),
						tipoDispositivo == null || tipoDispositivo.isBlank() ? "Periferico" : tipoDispositivo);
				valores.put("MarcaPeriferico_Adicional" + ubicacion.numeroAdicional(), periferico.getMarca());
				valores.put("ModeloPeriferico_Adicional" + ubicacion.numeroAdicional(), periferico.getModelo());
				valores.put("SeriePeriferico_Adicional" + ubicacion.numeroAdicional(), periferico.getSerial());
				valores.put("Observacion_Periferico_Adicional" + ubicacion.numeroAdicional(),
						textoOVacio(periferico.getObservaciones()));
			}
		}
	}

	/**
	 * La hoja de dispositivos moviles tiene un hueco de movil y otro de
	 * impresora termica, asi que cada acta de movil puede llevar una impresora
	 * embebida. Se emparejan en orden: el movil i-esimo carga la impresora
	 * i-esima. La impresora que no encuentra movil (indice -1) se imprime en su
	 * propia acta, con la mitad de movil en blanco.
	 */
	private Map<Integer, Integer> repartirImpresoras(int totalMoviles, List<Activo> impresoras) {
		Map<Integer, Integer> reparto = new HashMap<>();
		for (int i = 0; i < impresoras.size(); i++) {
			reparto.put(impresoras.get(i).getIdActivo(), i < totalMoviles ? i : -1);
		}
		return reparto;
	}

	/** Moviles e impresoras que forman parte del paquete de actas. */
	private void recolectarMovilesEImpresoras(List<ActaEntregaRecepcion> paquete, List<Integer> moviles,
			List<Activo> impresoras) {
		for (ActaEntregaRecepcion actaActiva : paquete) {
			Activo candidato = activoRepositorio.buscarPorId(actaActiva.getIdActivo()).orElse(null);
			if (candidato == null) {
				continue;
			}
			if ("dispositivo_movil".equals(candidato.getTipoActivo())) {
				if (!moviles.contains(candidato.getIdActivo())) {
					moviles.add(candidato.getIdActivo());
				}
			} else if ("impresora_termica".equals(candidato.getTipoActivo())) {
				impresoras.add(candidato);
			}
		}
	}

	/**
	 * Agrega al acta del movil idActivoMovil la impresora que le toco en el
	 * reparto, para que ambos equipos viajen en un unico documento en vez de
	 * generar dos actas con media hoja vacia cada una.
	 */
	private void completarImpresoraDelPaquete(List<ActaEntregaRecepcion> paquete, int idActivoMovil,
			Map<String, String> valores, Set<String> casillas) {
		List<Integer> moviles = new ArrayList<>();
		List<Activo> impresoras = new ArrayList<>();
		recolectarMovilesEImpresoras(paquete, moviles, impresoras);

		// Si el movil no viene en el paquete, participa al final del reparto
		// para seguir mostrando la impresora que le corresponda sin quitarsela
		// a los moviles que si estan.
		if (!moviles.contains(idActivoMovil)) {
			moviles.add(idActivoMovil);
		}

		int indiceActa = moviles.indexOf(idActivoMovil);
		Map<Integer, Integer> reparto = repartirImpresoras(moviles.size(), impresoras);

		for (Activo impresora : impresoras) {
			Integer destino = reparto.get(impresora.getIdActivo());
			if (destino == null || destino != indiceActa) {
				continue;
			}
			valores.put("Marca_ImpresoraTermica", impresora.getMarca());
			valores.put("Modelo_ImpresoraTermica", impresora.getModelo());
			valores.put("Serie_ImpresoraTermica", impresora.getSerial());
			valores.put("Observacion_Impresora", textoOVacio(impresora.getObservaciones()));
			activoDetalleRepositorio.buscarPorId(impresora.getIdActivo())
					.ifPresent(detalleImpresora -> marcarAccesorios(detalleImpresora, casillas, "Impresora"));
		}
	}

	/**
	 * Devuelve las actas que hay que imprimir para cubrir todo lo asignado al
	 * empleado sin duplicar papel: las actas de PCs y de moviles siempre, y las
	 * actas individuales solo de los perifericos e impresoras que NO cupieron
	 * embebidos en ninguna otra acta.
	 */
	@Override
	public List<Integer> listarActasImprimibles(int idEmpleado) {
		return actasImprimiblesDe(actasActivasDelEmpleado(idEmpleado));
	}

	/**
	 * Misma regla pero sobre un paquete cualquiera de actas, no solo sobre las
	 * activas del empleado: la devolucion masiva necesita filtrar un lote que ya
	 * esta cerrado.
	 */
	private List<Integer> actasImprimiblesDe(List<ActaEntregaRecepcion> paquete) {
		List<Integer> resultado = new ArrayList<>();
		List<Integer> equiposPc = new ArrayList<>();
		List<Integer> moviles = new ArrayList<>();
		List<Activo> perifericos = new ArrayList<>();
		List<Activo> impresoras = new ArrayList<>();
		Map<Integer, Integer> actaPorActivo = new HashMap<>();

		for (ActaEntregaRecepcion actaActiva : paquete) {
			Activo candidato = activoRepositorio.buscarPorId(actaActiva.getIdActivo()).orElse(null);
			if (candidato == null) {
				continue;
			}
			actaPorActivo.put(candidato.getIdActivo(), actaActiva.getIdActa());
			switch (candidato.getTipoActivo()) {
				case "desktop", "laptop" -> {
					if (!equiposPc.contains(candidato.getIdActivo())) {
						equiposPc.add(candidato.getIdActivo());
					}
					resultado.add(actaActiva.getIdActa());
				}
				case "dispositivo_movil" -> {
					if (!moviles.contains(candidato.getIdActivo())) {
						moviles.add(candidato.getIdActivo());
					}
					resultado.add(actaActiva.getIdActa());
				}
				case "periferico" -> perifericos.add(candidato);
				case "impresora_termica" -> impresoras.add(candidato);
				default -> resultado.add(actaActiva.getIdActa());
			}
		}

		Map<Integer, UbicacionPeriferico> reparto = repartirPerifericos(equiposPc.size(), perifericos);
		for (Activo periferico : perifericos) {
			UbicacionPeriferico ubicacion = reparto.get(periferico.getIdActivo());
			if (ubicacion == null || ubicacion.indiceEquipo() == -1) {
				resultado.add(actaPorActivo.get(periferico.getIdActivo()));
			}
		}

		// Las impresoras que viajan embebidas en el acta de un movil no generan
		// acta propia: evita las dos hojas a medio llenar por el mismo equipo.
		Map<Integer, Integer> repartoImpresoras = repartirImpresoras(moviles.size(), impresoras);
		for (Activo impresora : impresoras) {
			Integer destino = repartoImpresoras.get(impresora.getIdActivo());
			if (destino == null || destino == -1) {
				resultado.add(actaPorActivo.get(impresora.getIdActivo()));
			}
		}
		return resultado;
	}

	/**
	 * Un observacion nulo significa "no escribio nada": se respeta el comentario
	 * que el acta ya tuviera en vez de borrarlo. Una cadena vacia si lo limpia.
	 */
	private void guardarObservacion(int idActa, String observacion) {
		if (observacion == null) {
			return;
		}
		ActaEntregaRecepcion acta = actaRepositorio.buscarPorId(idActa)
				.orElseThrow(() -> new NegocioException("Acta no encontrada"));
		acta.setObservacion(observacion.isBlank() ? null : observacion);
		actaRepositorio.guardar(acta);
	}

	@Override
	@Transactional
	public ActasArchivadas archivarActasDelEmpleado(int idEmpleado, String observacion) {
		List<Integer> idsActas = listarActasImprimibles(idEmpleado);
		if (idsActas.isEmpty()) {
			throw new NegocioException("Este empleado no tiene actas para generar");
		}
		// El comentario es del paquete completo: cada acta del lote lo lleva,
		// porque cada una es un documento independiente que se firma por separado.
		idsActas.forEach(idActa -> guardarObservacion(idActa, observacion));
		// Aqui si se agrupa: el paquete cubre todo lo que el empleado tiene,
		// asi que la PC puede llevar sus perifericos y el movil su impresora.
		List<ActaEntregaRecepcion> paquete = actasActivasDelEmpleado(idEmpleado);
		return comoArchivadas(idsActas.stream().map(idActa -> generarPdf(idActa, paquete)).toList());
	}

	/**
	 * Archiva las actas de una devolucion masiva agrupandolas entre si. No
	 * reutiliza archivarActasDelEmpleado porque ese parte de las actas activas
	 * del empleado, y aqui ya estan todas cerradas: el paquete son justamente
	 * las actas que se acaban de devolver.
	 */
	@Override
	@Transactional
	public ActasArchivadas archivarLote(List<Integer> idsActas) {
		List<ActaEntregaRecepcion> paquete = idsActas.stream()
				.map(idActa -> actaRepositorio.buscarPorId(idActa).orElse(null))
				.filter(Objects::nonNull)
				.toList();
		// Se filtra igual que al generar el paquete de entrega: un periferico que
		// viaja embebido en el acta de su PC no debe generar ademas su propia hoja.
		return comoArchivadas(actasImprimiblesDe(paquete).stream()
				.map(idActa -> generarPdf(idActa, paquete))
				.toList());
	}

	/** @return ruta absoluta del archivo guardado, que es lo que ve el usuario. */
	private String guardarEnCarpeta(byte[] pdf, String oficina, String nombreCompleto, String cedula, boolean esEntrega,
			String tipoActivo, int idActa) {
		String tipoProceso = esEntrega ? "entrega" : "recepcion";
		String etiquetaTipo = "impresora_termica".equals(tipoActivo) || "dispositivo_movil".equals(tipoActivo)
				? "DispositivoMovil"
				: "Pc";
		try {
			Path carpeta = Path.of(directorioActasGeneradas, sanear(oficina),
					sanear(nombreCompleto + " -" + cedula), tipoProceso);
			Files.createDirectories(carpeta);
			String nombreArchivo = sanear(cedula) + "-" + tipoProceso + "-" + etiquetaTipo + "-" + idActa + ".pdf";
			Path archivo = carpeta.resolve(nombreArchivo);
			Files.write(archivo, pdf);
			return archivo.toAbsolutePath().toString();
		} catch (IOException e) {
			throw new RuntimeException("Error guardando el acta generada en disco", e);
		}
	}

	private String sanear(String texto) {
		return texto == null ? "sin-dato" : texto.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
	}

	private String[] separarMarcaModeloProcesador(String procesador) {
		if (procesador == null || procesador.isBlank()) {
			return new String[] { "", "" };
		}
		for (String marca : MARCAS_PROCESADOR_CONOCIDAS) {
			if (procesador.toLowerCase().startsWith(marca.toLowerCase())) {
				return new String[] { marca, procesador.substring(marca.length()).trim() };
			}
		}
		return new String[] { "", procesador };
	}

	private String textoOVacio(String valor) {
		return valor == null ? "" : valor;
	}

	/**
	 * Marca las casillas de accesorios del equipo. Ambas plantillas de moviles
	 * comparten hoja pero tienen un par de casillas por tipo
	 * (Cargador_Dispositivo_Movil / Cargador_Impresora), asi que el sufijo lo
	 * decide el tipo de activo del acta.
	 */
	private void marcarAccesorios(ActivoDetalle detalle, Set<String> casillas, String sufijoTipo) {
		if (Boolean.TRUE.equals(detalle.getIncluyeCargador())) {
			casillas.add("Cargador_" + sufijoTipo);
		}
		if (Boolean.TRUE.equals(detalle.getIncluyeCableUsb())) {
			casillas.add("CableUSB_" + sufijoTipo);
		}
	}

	private String concatenarNoNulos(String a, String b) {
		if (a == null) {
			return b == null ? "" : b;
		}
		if (b == null) {
			return a;
		}
		return a + " " + b;
	}

}
