package com.uisrael.inventario.presentacion.dto.response;

import lombok.Data;

@Data
public class ActivoDetalleResponseDto {

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

}
