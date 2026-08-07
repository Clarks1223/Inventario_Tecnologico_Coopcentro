package com.uisrael.inventario.presentacion.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Comentario opcional que el tecnico agrega al acta justo antes de imprimirla.
 * Se guarda en el acta y sale en el campo Observacion_General del documento.
 */
@Data
public class ObservacionActaRequestDto {

	@Size(max = 100, message = "no debe superar 100 caracteres")
	private String observacion;

}
