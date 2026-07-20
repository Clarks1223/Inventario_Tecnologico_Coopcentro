package com.uisrael.inventario.aplicacion.casosuso.entrada;

import com.uisrael.inventario.dominio.entidades.SesionUsuario;

public interface IAuthUseCase {
	SesionUsuario iniciarSesion(String correo, String contrasena);
}
