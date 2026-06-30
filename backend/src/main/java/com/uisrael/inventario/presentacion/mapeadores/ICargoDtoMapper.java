package com.uisrael.inventario.presentacion.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.Cargo;
import com.uisrael.inventario.presentacion.dto.request.CargoRequestDto;
import com.uisrael.inventario.presentacion.dto.response.CargoResponseDto;

@Mapper(componentModel = "spring")
public interface ICargoDtoMapper {

	Cargo toDomain(CargoRequestDto dto);

	CargoResponseDto toResponseDto(Cargo cargoPojo);

}
