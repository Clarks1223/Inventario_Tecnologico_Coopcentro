package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uisrael.inventario.dominio.entidades.ActaEntregaRecepcion;
import com.uisrael.inventario.infraestructura.persistencia.jpa.ActaEntregaRecepcionEntity;

@Mapper(componentModel = "spring", uses = IReferenciaJpaMapper.class)
public interface IActaEntregaRecepcionJpaMapper {

	@Mapping(source = "activo.idActivo", target = "idActivo")
	@Mapping(source = "empleado.idEmpleado", target = "idEmpleado")
	@Mapping(source = "usuarioTi.idUsuarioTi", target = "idUsuarioTi")
	ActaEntregaRecepcion toDomain(ActaEntregaRecepcionEntity entity);

	@Mapping(source = "idActivo", target = "activo")
	@Mapping(source = "idEmpleado", target = "empleado")
	@Mapping(source = "idUsuarioTi", target = "usuarioTi")
	ActaEntregaRecepcionEntity toEntity(ActaEntregaRecepcion actaPojo);

}
