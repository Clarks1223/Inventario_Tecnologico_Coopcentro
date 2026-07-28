package com.uisrael.inventario.dominio.correo;

/**
 * Puerto para el envio de correos transaccionales. La logica de que
 * contenido enviar vive en el caso de uso (aplicacion); este puerto solo
 * sabe entregar un correo de recuperacion de contrasena.
 */
public interface IEmailSender {
	void enviarCorreoRecuperacion(String correoDestino, String nombreDestino, String enlaceRecuperacion);
}
