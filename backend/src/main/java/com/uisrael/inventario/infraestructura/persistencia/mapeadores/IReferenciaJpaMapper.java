package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.infraestructura.persistencia.jpa.ActivoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.CargoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.EmpleadoEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.OficinaEntity;
import com.uisrael.inventario.infraestructura.persistencia.jpa.UsuarioTiEntity;

/**
 * Construye entidades JPA "stub" (solo con el id) a partir de una FK plana.
 * Se referencia vía {@code @Mapper(uses = IReferenciaJpaMapper.class)} en los
 * demás mappers para no repetir estos métodos en cada uno.
 */
@Mapper(componentModel = "spring")
public interface IReferenciaJpaMapper {

	default ActivoEntity mapActivo(int idActivo) {
		ActivoEntity entity = new ActivoEntity();
		entity.setIdActivo(idActivo);
		return entity;
	}

	default CargoEntity mapCargo(int idCargo) {
		CargoEntity entity = new CargoEntity();
		entity.setIdCargo(idCargo);
		return entity;
	}

	default EmpleadoEntity mapEmpleado(int idEmpleado) {
		EmpleadoEntity entity = new EmpleadoEntity();
		entity.setIdEmpleado(idEmpleado);
		return entity;
	}

	default OficinaEntity mapOficina(int idOficina) {
		OficinaEntity entity = new OficinaEntity();
		entity.setIdOficina(idOficina);
		return entity;
	}

	default UsuarioTiEntity mapUsuarioTi(int idUsuarioTi) {
		UsuarioTiEntity entity = new UsuarioTiEntity();
		entity.setIdUsuarioTi(idUsuarioTi);
		return entity;
	}

}
