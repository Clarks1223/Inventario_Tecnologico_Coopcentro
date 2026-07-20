package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uisrael.inventario.dominio.entidades.Activo;
import com.uisrael.inventario.infraestructura.persistencia.jpa.ActivoEntity;

@Mapper(componentModel = "spring", uses = IReferenciaJpaMapper.class)
public interface IActivoJpaMapper {

	@Mapping(source = "oficina.idOficina", target = "idOficina")
	Activo toDomain(ActivoEntity entity);

	@Mapping(source = "idOficina", target = "oficina")
	ActivoEntity toEntity(Activo activoPojo);

}
