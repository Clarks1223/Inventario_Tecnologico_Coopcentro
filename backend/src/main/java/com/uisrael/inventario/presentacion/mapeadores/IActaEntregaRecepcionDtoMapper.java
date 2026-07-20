package com.uisrael.inventario.presentacion.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.presentacion.dto.request.ActaEntregaRecepcionRequestDto;
import com.uisrael.inventario.presentacion.dto.response.ActaEntregaRecepcionResponseDto;

@Mapper(componentModel = "spring")
public interface IActaEntregaRecepcionDtoMapper {

	ActaEntregaRecepcion toDomain(ActaEntregaRecepcionRequestDto dto);

	ActaEntregaRecepcionResponseDto toResponseDto(ActaEntregaRecepcion actaPojo);

}
