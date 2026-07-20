package com.uisrael.inventario.presentacion.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.UsuarioTi;
import com.uisrael.inventario.presentacion.dto.request.UsuarioTiRequestDto;
import com.uisrael.inventario.presentacion.dto.response.UsuarioTiResponseDto;

@Mapper(componentModel = "spring")
public interface IUsuarioTiDtoMapper {

	UsuarioTi toDomain(UsuarioTiRequestDto dto);

	UsuarioTiResponseDto toResponseDto(UsuarioTi usuarioTiPojo);

}
