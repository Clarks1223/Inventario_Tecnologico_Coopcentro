package com.uisrael.inventario.presentacion.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.ActivoDetalle;
import com.uisrael.inventario.presentacion.dto.request.ActivoDetalleRequestDto;
import com.uisrael.inventario.presentacion.dto.response.ActivoDetalleResponseDto;

@Mapper(componentModel = "spring")
public interface IActivoDetalleDtoMapper {

	ActivoDetalle toDomain(ActivoDetalleRequestDto dto);

	ActivoDetalleResponseDto toResponseDto(ActivoDetalle activoDetallePojo);

}
