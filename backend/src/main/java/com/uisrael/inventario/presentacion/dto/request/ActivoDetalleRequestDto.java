package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivoDetalleRequestDto {

	private Integer idActivo;

	@Size(max = 50)
	private String tipoConexion;

	@Size(max = 50)
	private String estadoBateria;

	@Size(max = 100)
	private String modeloCabezal;

	@Size(max = 50)
	private String tipoDispositivo;

	@Size(max = 100)
	private String sistemaOperativo;

	@Pattern(regexp = "|[0-9]{15}", message = "imei debe tener exactamente 15 digitos numericos")
	private String imei;

	@Pattern(regexp = "|[0-9]{10}", message = "numeroLinea debe tener exactamente 10 digitos numericos")
	private String numeroLinea;

	@Size(max = 100)
	private String procesador;

	@Min(1)
	@Max(512)
	private Integer ramGb;

	@Size(max = 20)
	private String tipoAlmacenamiento;

	@Pattern(
			regexp = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$",
			message = "ip debe ser una dirección IPv4 válida"
	)
	private String ip;

	@Size(max = 150)
	private String dominio;

	@Min(1)
	@Max(65536)
	private Integer almacenamientoGb;

}
