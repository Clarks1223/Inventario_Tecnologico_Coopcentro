package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.infraestructura.persistencia.jpa.EmpleadoEntity;

@Mapper(componentModel = "spring", uses = IReferenciaJpaMapper.class)
public interface IEmpleadoJpaMapper {

	@Mapping(source = "oficina.idOficina", target = "idOficina")
	@Mapping(source = "cargo.idCargo", target = "idCargo")
	Empleado toDomain(EmpleadoEntity entity);

	@Mapping(source = "idOficina", target = "oficina")
	@Mapping(source = "idCargo", target = "cargo")
	EmpleadoEntity toEntity(Empleado empleadoPojo);

}
