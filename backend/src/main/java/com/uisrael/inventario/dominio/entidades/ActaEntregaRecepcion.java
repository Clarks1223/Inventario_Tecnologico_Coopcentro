package com.uisrael.inventario.dominio.entidades;

import java.time.LocalDateTime;

import com.uisrael.inventario.dominio.valores.EstadoActa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActaEntregaRecepcion {

	private int idActa;
	private int idActivo;
	private int idEmpleado;
	private int idUsuarioTi;
	private LocalDateTime fechaAsignacion;
	private LocalDateTime fechaDevolucion;
	private String estadoAsignacion;

	/** Trazabilidad interna: por que se movio el activo. No se imprime. */
	private String motivo;

	/**
	 * Comentario libre que escribe el tecnico y que sale en el acta impresa
	 * (campo Observacion_General). Es opcional y propio de cada acta, por eso
	 * se pide en cada accion que genera un documento.
	 */
	private String observacion;

	/**
	 * Acta interna que deja el activo en bodega a cargo del area de TI. Se abre
	 * al registrar el activo, al reactivarlo y al recibir una devolucion, para
	 * que nunca exista un momento en el que nadie responda por el.
	 */
	public static ActaEntregaRecepcion custodiaDeTi(int idActivo, int idEmpleado, int idUsuarioTi, String motivo) {
		ActaEntregaRecepcion acta = new ActaEntregaRecepcion();
		acta.setIdActivo(idActivo);
		acta.setIdEmpleado(idEmpleado);
		acta.setIdUsuarioTi(idUsuarioTi);
		acta.setFechaAsignacion(LocalDateTime.now());
		acta.setEstadoAsignacion(EstadoActa.CUSTODIA);
		acta.setMotivo(motivo);
		return acta;
	}

	/** Acta de entrega a un empleado: es la que se firma e imprime. */
	public static ActaEntregaRecepcion entregaAEmpleado(int idActivo, int idEmpleado, int idUsuarioTi, String motivo) {
		ActaEntregaRecepcion acta = new ActaEntregaRecepcion();
		acta.setIdActivo(idActivo);
		acta.setIdEmpleado(idEmpleado);
		acta.setIdUsuarioTi(idUsuarioTi);
		acta.setFechaAsignacion(LocalDateTime.now());
		acta.setEstadoAsignacion(EstadoActa.ACTIVA);
		acta.setMotivo(motivo);
		return acta;
	}

	public boolean estaAbierta() {
		return EstadoActa.estaAbierta(estadoAsignacion);
	}

	/** Cierra el ciclo documentado por el acta, conservando su motivo previo. */
	public void cerrar(String motivoCierre) {
		this.fechaDevolucion = LocalDateTime.now();
		this.estadoAsignacion = EstadoActa.DEVUELTA;
		agregarMotivo(motivoCierre);
	}

	/**
	 * Acumula texto en el motivo en vez de reemplazarlo, para que el acta
	 * conserve el historial completo de por que se movio el activo.
	 */
	public void agregarMotivo(String textoAdicional) {
		if (textoAdicional == null || textoAdicional.isBlank()) {
			return;
		}
		this.motivo = (motivo == null || motivo.isBlank()) ? textoAdicional : motivo + "\n" + textoAdicional;
	}

}
