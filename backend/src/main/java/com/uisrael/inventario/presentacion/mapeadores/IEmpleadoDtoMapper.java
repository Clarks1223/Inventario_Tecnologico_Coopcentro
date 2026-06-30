package com.uisrael.inventario.presentacion.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.Empleado;
import com.uisrael.inventario.presentacion.dto.request.EmpleadoRequestDto;
import com.uisrael.inventario.presentacion.dto.response.EmpleadoResponseDto;

@Mapper(componentModel = "spring")
public interface IEmpleadoDtoMapper {

	Empleado toDomain(EmpleadoRequestDto dto);

	EmpleadoResponseDto toResponseDto(Empleado empleadoPojo);

}
