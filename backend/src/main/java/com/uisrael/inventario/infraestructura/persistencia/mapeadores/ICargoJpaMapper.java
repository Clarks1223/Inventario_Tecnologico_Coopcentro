package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.Cargo;
import com.uisrael.inventario.infraestructura.persistencia.jpa.CargoEntity;

@Mapper(componentModel = "spring")
public interface ICargoJpaMapper {

	Cargo toDomain(CargoEntity entity);

	CargoEntity toEntity(Cargo cargoPojo);

}
