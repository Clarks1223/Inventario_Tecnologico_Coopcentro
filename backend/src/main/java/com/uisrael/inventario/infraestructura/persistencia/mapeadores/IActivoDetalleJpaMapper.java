package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uisrael.inventario.dominio.entidades.ActivoDetalle;
import com.uisrael.inventario.infraestructura.persistencia.jpa.ActivoDetalleEntity;

@Mapper(componentModel = "spring", uses = IReferenciaJpaMapper.class)
public interface IActivoDetalleJpaMapper {

	@Mapping(source = "activo.idActivo", target = "idActivo")
	ActivoDetalle toDomain(ActivoDetalleEntity entity);

	@Mapping(source = "idActivo", target = "activo")
	ActivoDetalleEntity toEntity(ActivoDetalle activoDetallePojo);

}
