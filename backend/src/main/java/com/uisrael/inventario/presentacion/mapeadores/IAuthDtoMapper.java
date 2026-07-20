package com.uisrael.inventario.presentacion.mapeadores;

import org.mapstruct.Mapper;

import com.uisrael.inventario.dominio.entidades.SesionUsuario;
import com.uisrael.inventario.presentacion.dto.response.LoginResponseDto;

@Mapper(componentModel = "spring")
public interface IAuthDtoMapper {

	LoginResponseDto toResponseDto(SesionUsuario sesionUsuario);

}
