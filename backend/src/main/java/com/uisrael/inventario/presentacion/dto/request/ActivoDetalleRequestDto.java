package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivoDetalleRequestDto {

	@NotNull
	private Integer idActivo;

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
