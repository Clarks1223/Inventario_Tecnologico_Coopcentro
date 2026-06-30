package com.uisrael.inventario.presentacion.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.presentacion.dto.request.ActivoRequestDto;
import com.uisrael.inventario.presentacion.dto.response.ActivoResponseDto;

@Mapper(componentModel = "spring")
public interface IActivoDtoMapper {

	Activo toDomain(ActivoRequestDto dto);

	ActivoResponseDto toResponseDto(Activo activoPojo);

}
