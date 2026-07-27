package com.uisrael.inventario.presentacion.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.presentacion.dto.response.ActaEntregaRecepcionResponseDto;

@Mapper(componentModel = "spring")
public interface IActaEntregaRecepcionDtoMapper {

	ActaEntregaRecepcionResponseDto toResponseDto(ActaEntregaRecepcion actaPojo);

}
