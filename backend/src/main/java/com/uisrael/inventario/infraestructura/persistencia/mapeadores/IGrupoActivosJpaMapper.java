package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.GrupoActivos;
import com.uisrael.inventario.infraestructura.persistencia.jpa.GrupoActivosEntity;

@Mapper(componentModel = "spring")
public interface IGrupoActivosJpaMapper {

	GrupoActivos toDomain(GrupoActivosEntity entity);

	GrupoActivosEntity toEntity(GrupoActivos grupo);

}
