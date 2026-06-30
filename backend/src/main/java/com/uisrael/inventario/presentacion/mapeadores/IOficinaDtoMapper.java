package com.uisrael.inventario.presentacion.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.presentacion.dto.request.OficinaRequestDto;
import com.uisrael.inventario.presentacion.dto.response.OficinaResponseDto;

@Mapper(componentModel = "spring")
public interface IOficinaDtoMapper {

	Oficina toDomain(OficinaRequestDto dto);

	OficinaResponseDto toResponseDto(Oficina oficinaPojo);

}
