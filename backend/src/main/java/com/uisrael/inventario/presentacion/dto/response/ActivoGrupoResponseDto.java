package com.uisrael.inventario.presentacion.dto.response;

import lombok.Data;

/** Vista resumida de un activo dentro de un grupo. */
@Data
public class ActivoGrupoResponseDto {

	private int idActivo;
	private String codigoInventario;
	private String tipoActivo;
	private String marca;
	private String modelo;
	private String serial;
	private String estado;

}
