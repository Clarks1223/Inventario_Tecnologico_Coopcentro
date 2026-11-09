package com.uisrael.inventario.presentacion.mapeadores;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.dominio.entidades.GrupoActivos;
import com.uisrael.inventario.dominio.entidades.ResumenGrupo;
import com.uisrael.inventario.presentacion.dto.request.GrupoActivosRequestDto;
import com.uisrael.inventario.presentacion.dto.response.ActivoGrupoResponseDto;
import com.uisrael.inventario.presentacion.dto.response.GrupoActivosResponseDto;

@Mapper(componentModel = "spring")
public interface IGrupoActivosDtoMapper {

	@Mapping(target = "idGrupo", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	GrupoActivos toDomain(GrupoActivosRequestDto dto);

	ActivoGrupoResponseDto toActivoDto(Activo activo);

	@Mapping(source = "grupo.idGrupo", target = "idGrupo")
	@Mapping(source = "grupo.nombre", target = "nombre")
	@Mapping(source = "grupo.descripcion", target = "descripcion")
	@Mapping(source = "grupo.createdAt", target = "createdAt")
	@Mapping(source = "grupo.updatedAt", target = "updatedAt")
	GrupoActivosResponseDto toResponseDto(ResumenGrupo resumen);

}
