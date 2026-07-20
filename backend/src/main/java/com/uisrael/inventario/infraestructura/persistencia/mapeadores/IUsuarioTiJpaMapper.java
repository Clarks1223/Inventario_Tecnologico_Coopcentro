package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.infraestructura.persistencia.jpa.UsuarioTiEntity;

@Mapper(componentModel = "spring", uses = IReferenciaJpaMapper.class)
public interface IUsuarioTiJpaMapper {

	@Mapping(source = "empleado.idEmpleado", target = "idEmpleado")
	UsuarioTi toDomain(UsuarioTiEntity entity);

	@Mapping(source = "idEmpleado", target = "empleado")
	UsuarioTiEntity toEntity(UsuarioTi usuarioTiPojo);

}
