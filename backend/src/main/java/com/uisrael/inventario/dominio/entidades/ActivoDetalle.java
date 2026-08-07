package com.uisrael.inventario.dominio.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivoDetalle {

	private int idActivo;
	private String tipoConexion;
	private String estadoBateria;
	private String modeloCabezal;
	private String tipoDispositivo;
	private String sistemaOperativo;
	private String imei;
	private String numeroLinea;
	private String procesador;
	private Integer ramGb;
	private String tipoAlmacenamiento;
	private String ip;
	private String dominio;
	private Integer almacenamientoGb;

	// Accesorios que acompanan al equipo. Solo aplican a dispositivo_movil e
	// impresora_termica, y se marcan al registrar el activo.
	private Boolean incluyeCargador;
	private Boolean incluyeCableUsb;

}
