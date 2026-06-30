package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.Oficina;
import com.uisrael.inventario.infraestructura.persistencia.jpa.OficinaEntity;

@Mapper(componentModel = "spring")
public interface IOficinaJpaMapper {

	Oficina toDomain(OficinaEntity entity);

	OficinaEntity toEntity(Oficina oficinaPojo);

}
