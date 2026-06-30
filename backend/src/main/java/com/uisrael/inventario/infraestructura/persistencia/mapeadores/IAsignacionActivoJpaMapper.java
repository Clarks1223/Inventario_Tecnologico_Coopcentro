package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uisrael.inventario.dominio.entidades.AsignacionActivo;
import com.uisrael.inventario.infraestructura.persistencia.jpa.ActivoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.AsignacionActivoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.EmpleadoEntity;

@Mapper(componentModel = "spring")
public interface IAsignacionActivoJpaMapper {

	@Mapping(source = "activo.idActivo", target = "idActivo")
	@Mapping(source = "empleado.idEmpleado", target = "idEmpleado")
	AsignacionActivo toDomain(AsignacionActivoEntity entity);

	@Mapping(source = "idActivo", target = "activo")
	@Mapping(source = "idEmpleado", target = "empleado")
	AsignacionActivoEntity toEntity(AsignacionActivo asignacionPojo);

	default ActivoEntity mapActivo(int idActivo) {
		ActivoEntity entity = new ActivoEntity();
		entity.setIdActivo(idActivo);
		return entity;
	}

	default EmpleadoEntity mapEmpleado(int idEmpleado) {
		EmpleadoEntity entity = new EmpleadoEntity();
		entity.setIdEmpleado(idEmpleado);
		return entity;
	}

}
