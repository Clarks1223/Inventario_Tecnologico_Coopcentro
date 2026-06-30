package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.infraestructura.persistencia.jpa.CargoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.EmpleadoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.OficinaEntity;

@Mapper(componentModel = "spring")
public interface IEmpleadoJpaMapper {

	@Mapping(source = "oficina.idOficina", target = "idOficina")
	@Mapping(source = "cargo.idCargo", target = "idCargo")
	Empleado toDomain(EmpleadoEntity entity);

	@Mapping(source = "idOficina", target = "oficina")
	@Mapping(source = "idCargo", target = "cargo")
	EmpleadoEntity toEntity(Empleado empleadoPojo);

	default OficinaEntity mapOficina(int idOficina) {
		OficinaEntity entity = new OficinaEntity();
		entity.setIdOficina(idOficina);
		return entity;
	}

	default CargoEntity mapCargo(int idCargo) {
		CargoEntity entity = new CargoEntity();
		entity.setIdCargo(idCargo);
		return entity;
	}

}
