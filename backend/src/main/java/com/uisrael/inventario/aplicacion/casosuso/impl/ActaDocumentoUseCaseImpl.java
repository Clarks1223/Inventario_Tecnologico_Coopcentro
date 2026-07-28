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
import java.util.Set;

import com.uisrael.inventario.aplicacion.casosuso.entrada.IActaDocumentoUseCase;
import com.uisrael.inventario.dominio.documentos.IActaPdfGenerador;
import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.ActivoDetalle;
import com.uisrael.inventario.dominio.entidades.Cargo;
import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.dominio.repositorios.IActaEntregaRecepcionRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoDetalleRepositorio;
import com.uisrael.inventario.dominio.repositorios.IActivoRepositorio;
import com.uisrael.inventario.dominio.repositorios.ICargoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IEmpleadoRepositorio;
import com.uisrael.inventario.dominio.repositorios.IOficinaRepositorio;
import com.uisrael.inventario.dominio.repositorios.IUsuarioTiRepositorio;

/**
 * Arma los datos de un acta (activo, empleado, oficina, cargo, TI) y decide
 * que plantilla y que campos le corresponden segun tipoActivo. Ademas de
 * devolver el PDF, lo guarda en disco en:
 * {directorioActasGeneradas}/{oficina}/{nombre apellido} -{cedula}/{entrega|recepcion}/{cedula}-{entrega|recepcion}-{Pc|DispositivoMovil}-{idActa}.pdf
 */
public class ActaDocumentoUseCaseImpl implements IActaDocumentoUseCase {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private static final String PLANTILLA_PCS = "ACTA ENTREGA - RECEPCION - PCs.pdf";
	private static final String PLANTILLA_MOVILES = "ACTA ENTREGA - RECEPCION - Dispositivos_Moviles.pdf";
	private static final String[] MARCAS_PROCESADOR_CONOCIDAS = { "Intel", "AMD", "Apple", "Qualcomm" };

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
	public byte[] generarPdf(int idActa) {
		ActaEntregaRecepcion acta = actaRepositorio.buscarPorId(idActa)
				.orElseThrow(() -> new RuntimeException("Acta no encontrada"));
		Activo activo = activoRepositorio.buscarPorId(acta.getIdActivo())
				.orElseThrow(() -> new RuntimeException("Activo no encontrado"));
		Empleado empleado = empleadoRepositorio.buscarPorId(acta.getIdEmpleado())
				.orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
		Cargo cargoEmpleado = cargoRepositorio.buscarPorId(empleado.getIdCargo())
				.orElseThrow(() -> new RuntimeException("Cargo no encontrado"));
		Oficina oficina = oficinaRepositorio.buscarPorId(empleado.getIdOficina())
				.orElseThrow(() -> new RuntimeException("Oficina no encontrada"));
		UsuarioTi usuarioTi = usuarioTiRepositorio.buscarPorId(acta.getIdUsuarioTi())
				.orElseThrow(() -> new RuntimeException("Usuario TI no encontrado"));
		Empleado empleadoTi = empleadoRepositorio.buscarPorId(usuarioTi.getIdEmpleado())
				.orElseThrow(() -> new RuntimeException("Empleado de TI no encontrado"));
		Cargo cargoTi = cargoRepositorio.buscarPorId(empleadoTi.getIdCargo())
				.orElseThrow(() -> new RuntimeException("Cargo de TI no encontrado"));
		ActivoDetalle detalle = activoDetalleRepositorio.buscarPorId(activo.getIdActivo()).orElse(null);

		boolean esEntrega = "activa".equals(acta.getEstadoAsignacion());
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

		Set<String> casillas = new HashSet<>();
		casillas.add(esEntrega ? "Entrega" : "Devolucion");

		String nombrePlantilla = completarCamposPorTipo(activo, detalle, empleado, valores, casillas);

		byte[] pdf = pdfGenerador.generar(nombrePlantilla, valores, casillas);
		guardarEnCarpeta(pdf, oficina.getNombre(), nombreEmpleadoCompleto, empleado.getCedula(), esEntrega, activo.getTipoActivo(), idActa);
		return pdf;
	}

	private String completarCamposPorTipo(Activo activo, ActivoDetalle detalle, Empleado empleado,
			Map<String, String> valores, Set<String> casillas) {
		switch (activo.getTipoActivo()) {
			case "laptop", "desktop" -> {
				valores.put("Marca_Equipo", activo.getMarca());
				valores.put("Modelo_Equipo", activo.getModelo());
				valores.put("Serie_Equipo", activo.getSerial());
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
				completarPerifericosDelEmpleado(empleado.getIdEmpleado(), activo.getIdActivo(), valores);
				return PLANTILLA_PCS;
			}
			case "dispositivo_movil" -> {
				valores.put("Marca_Tablet", activo.getMarca());
				valores.put("Model_Tablet", activo.getModelo());
				valores.put("Serie_Tablet", activo.getSerial());
				if (detalle != null) {
					valores.put("NumeroTelefonico__Tablet", detalle.getNumeroLinea());
				}
				return PLANTILLA_MOVILES;
			}
			case "impresora_termica" -> {
				valores.put("Marca_ImpresoraTermica", activo.getMarca());
				valores.put("Modelo_ImpresoraTermica", activo.getModelo());
				valores.put("Serie_ImpresoraTermica", activo.getSerial());
				return PLANTILLA_MOVILES;
			}
			case "periferico" -> {
				valores.put("Marca_Equipo", activo.getMarca());
				valores.put("Modelo_Equipo", activo.getModelo());
				valores.put("Serie_Equipo", activo.getSerial());
				return PLANTILLA_PCS;
			}
			default -> throw new RuntimeException(
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

	private List<ActaEntregaRecepcion> actasActivasDelEmpleado(int idEmpleado) {
		return actaRepositorio.listarTodos().stream()
				.filter(acta -> acta.getIdEmpleado() == idEmpleado)
				.filter(acta -> "activa".equals(acta.getEstadoAsignacion()))
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
		boolean[] tecladoOcupado = new boolean[totalEquipos];
		boolean[] mouseOcupado = new boolean[totalEquipos];
		boolean[] monitorOcupado = new boolean[totalEquipos];
		int[] adicionalesOcupados = new int[totalEquipos];

		for (Activo periferico : perifericos) {
			ActivoDetalle detallePeriferico = activoDetalleRepositorio.buscarPorId(periferico.getIdActivo()).orElse(null);
			String tipoDispositivo = detallePeriferico == null ? null : detallePeriferico.getTipoDispositivo();
			String tipoNormalizado = tipoDispositivo == null ? "" : tipoDispositivo.trim().toLowerCase();

			int destino = -1;
			String slotDedicado = null;
			for (int i = 0; i < totalEquipos && destino == -1; i++) {
				if ("teclado".equals(tipoNormalizado) && !tecladoOcupado[i]) {
					tecladoOcupado[i] = true;
					destino = i;
					slotDedicado = "Teclado";
				} else if ("mouse".equals(tipoNormalizado) && !mouseOcupado[i]) {
					mouseOcupado[i] = true;
					destino = i;
					slotDedicado = "Mouse";
				} else if ("monitor".equals(tipoNormalizado) && !monitorOcupado[i]) {
					monitorOcupado[i] = true;
					destino = i;
					slotDedicado = "Monitor";
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
	private void completarPerifericosDelEmpleado(int idEmpleado, int idActivoEquipo, Map<String, String> valores) {
		List<Integer> equiposPc = new ArrayList<>();
		List<Activo> perifericos = new ArrayList<>();
		for (ActaEntregaRecepcion actaActiva : actasActivasDelEmpleado(idEmpleado)) {
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
		// En un acta de devolucion el equipo ya no tiene asignacion "activa":
		// participa al final del reparto para seguir mostrando los perifericos
		// que le correspondan sin quitarselos a las PCs aun asignadas.
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
			} else if (ubicacion.numeroAdicional() > 0) {
				String tipoDispositivo = ubicacion.tipoDispositivo();
				valores.put("NombrePeriferico_Adicional" + ubicacion.numeroAdicional(),
						tipoDispositivo == null || tipoDispositivo.isBlank() ? "Periferico" : tipoDispositivo);
				valores.put("MarcaPeriferico_Adicional" + ubicacion.numeroAdicional(), periferico.getMarca());
				valores.put("ModeloPeriferico_Adicional" + ubicacion.numeroAdicional(), periferico.getModelo());
				valores.put("SeriePeriferico_Adicional" + ubicacion.numeroAdicional(), periferico.getSerial());
			}
		}
	}

	/**
	 * Devuelve las actas que hay que imprimir para cubrir todo lo asignado al
	 * empleado sin duplicar papel: las actas de PCs/moviles/impresoras siempre,
	 * y las actas individuales solo de los perifericos que NO cupieron
	 * embebidos en ninguna acta de PC.
	 */
	@Override
	public List<Integer> listarActasImprimibles(int idEmpleado) {
		List<Integer> resultado = new ArrayList<>();
		List<Integer> equiposPc = new ArrayList<>();
		List<Activo> perifericos = new ArrayList<>();
		Map<Integer, Integer> actaPorActivo = new HashMap<>();

		for (ActaEntregaRecepcion actaActiva : actasActivasDelEmpleado(idEmpleado)) {
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
				case "periferico" -> perifericos.add(candidato);
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
		return resultado;
	}

	private void guardarEnCarpeta(byte[] pdf, String oficina, String nombreCompleto, String cedula, boolean esEntrega,
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
			Files.write(carpeta.resolve(nombreArchivo), pdf);
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
