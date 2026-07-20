package com.uisrael.inventario.aplicacion.casosuso.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
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
 * {directorioActasGeneradas}/{oficina}/{nombre apellido}/{entrega|devolucion}/acta-{idActa}.pdf
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
		guardarEnCarpeta(pdf, oficina.getNombre(), nombreEmpleadoCompleto, esEntrega ? "entrega" : "devolucion", idActa);
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
			default -> throw new RuntimeException(
					"No hay plantilla de acta disponible para el tipo de activo: " + activo.getTipoActivo());
		}
	}

	private void guardarEnCarpeta(byte[] pdf, String oficina, String nombreCompleto, String tipo, int idActa) {
		try {
			Path carpeta = Path.of(directorioActasGeneradas, sanear(oficina), sanear(nombreCompleto), tipo);
			Files.createDirectories(carpeta);
			Files.write(carpeta.resolve("acta-" + idActa + ".pdf"), pdf);
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
