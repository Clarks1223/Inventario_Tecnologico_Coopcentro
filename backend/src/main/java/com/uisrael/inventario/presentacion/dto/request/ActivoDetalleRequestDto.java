package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivoDetalleRequestDto {

	private Integer idActivo;

	@Size(max = 50, message = "no debe superar 50 caracteres")
	private String tipoConexion;

	@Size(max = 50, message = "no debe superar 50 caracteres")
	private String estadoBateria;

	@Size(max = 100, message = "no debe superar 100 caracteres")
	private String modeloCabezal;

	@Size(max = 50, message = "no debe superar 50 caracteres")
	private String tipoDispositivo;

	@Size(max = 100, message = "no debe superar 100 caracteres")
	private String sistemaOperativo;

	@Pattern(regexp = "|[0-9]{15}", message = "debe tener exactamente 15 dígitos numéricos")
	private String imei;

	@Pattern(regexp = "|[0-9]{10}", message = "debe tener exactamente 10 dígitos numéricos")
	private String numeroLinea;

	@Size(max = 100, message = "no debe superar 100 caracteres")
	private String procesador;

	@Min(value = 1, message = "debe ser al menos 1")
	@Max(value = 512, message = "no debe superar 512")
	private Integer ramGb;

	@Size(max = 20, message = "no debe superar 20 caracteres")
	private String tipoAlmacenamiento;

	@Pattern(
			regexp = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$",
			message = "debe ser una dirección IPv4 válida"
	)
	private String ip;

	@Size(max = 150, message = "no debe superar 150 caracteres")
	private String dominio;

	@Min(value = 1, message = "debe ser al menos 1")
	@Max(value = 65536, message = "no debe superar 65536")
	private Integer almacenamientoGb;

}
