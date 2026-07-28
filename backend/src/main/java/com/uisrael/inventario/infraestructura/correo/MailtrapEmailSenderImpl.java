package com.uisrael.inventario.infraestructura.correo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.uisrael.inventario.dominio.correo.IEmailSender;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;

public class MailtrapEmailSenderImpl implements IEmailSender {

	private static final String PLANTILLA_RECUPERACION = "recuperacion-contrasena.html";

	private final MailtrapClient client;
	private final String fromEmail;
	private final String fromName;
	private final String directorioPlantillas;

	public MailtrapEmailSenderImpl(String apiToken, String fromEmail, String fromName, boolean sandboxHabilitado,
			Long inboxId, String directorioPlantillas) {
		MailtrapConfig.Builder configBuilder = new MailtrapConfig.Builder().token(apiToken);
		if (sandboxHabilitado) {
			configBuilder.sandbox(true).inboxId(inboxId);
		}
		this.client = MailtrapClientFactory.createMailtrapClient(configBuilder.build());
		this.fromEmail = fromEmail;
		this.fromName = fromName;
		this.directorioPlantillas = directorioPlantillas;
	}

	@Override
	public void enviarCorreoRecuperacion(String correoDestino, String nombreDestino, String enlaceRecuperacion) {
		String html = cargarPlantilla(PLANTILLA_RECUPERACION)
				.replace("{{nombre}}", nombreDestino)
				.replace("{{enlace}}", enlaceRecuperacion);

		MailtrapMail mail = MailtrapMail.builder()
				.from(new Address(fromEmail, fromName))
				.to(List.of(new Address(correoDestino)))
				.subject("Recuperacion de contrasena")
				.html(html)
				.category("Password Recovery")
				.build();

		try {
			client.send(mail);
		} catch (Exception e) {
			throw new RuntimeException("No se pudo enviar el correo de recuperacion", e);
		}
	}

	private String cargarPlantilla(String nombrePlantilla) {
		File archivoPlantilla = new File(directorioPlantillas, nombrePlantilla);
		if (!archivoPlantilla.exists()) {
			throw new RuntimeException("No se encontro la plantilla de correo: " + archivoPlantilla.getAbsolutePath());
		}
		try {
			return Files.readString(archivoPlantilla.toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Error leyendo la plantilla de correo " + nombrePlantilla, e);
		}
	}

}
