package com.uisrael.inventario.presentacion.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.AsignacionActivo;
import com.uisrael.inventario.presentacion.dto.request.AsignacionActivoRequestDto;
import com.uisrael.inventario.presentacion.dto.response.AsignacionActivoResponseDto;

@Mapper(componentModel = "spring")
public interface IAsignacionActivoDtoMapper {

	AsignacionActivo toDomain(AsignacionActivoRequestDto dto);

	AsignacionActivoResponseDto toResponseDto(AsignacionActivo asignacionPojo);

}
