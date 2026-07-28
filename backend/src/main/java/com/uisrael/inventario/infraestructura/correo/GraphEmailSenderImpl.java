package com.uisrael.inventario.infraestructura.correo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.uisrael.inventario.dominio.correo.IEmailSender;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Envia correos usando Microsoft Graph (flujo client credentials: la app se
 * autentica con su propia identidad, no con la de un usuario). Se implementa
 * con HttpClient del JDK en vez del SDK oficial de Graph para no arrastrar
 * sus dependencias transitivas de Jackson 2, que chocan con Jackson 3 (usado
 * por Spring Boot 4 en este proyecto).
 */
public class GraphEmailSenderImpl implements IEmailSender {

	private static final String PLANTILLA_RECUPERACION = "recuperacion-contrasena.html";
	private static final String TOKEN_URL = "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
	private static final String SEND_MAIL_URL = "https://graph.microsoft.com/v1.0/users/%s/sendMail";
	private static final String GRAPH_SCOPE = "https://graph.microsoft.com/.default";

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper;

	private final String tenantId;
	private final String clientId;
	private final String clientSecret;
	private final String senderEmail;
	private final String directorioPlantillas;

	private volatile String tokenCacheado;
	private volatile Instant tokenExpiraEn = Instant.EPOCH;

	public GraphEmailSenderImpl(ObjectMapper objectMapper, String tenantId, String clientId, String clientSecret,
			String senderEmail, String directorioPlantillas) {
		this.objectMapper = objectMapper;
		this.tenantId = tenantId;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.senderEmail = senderEmail;
		this.directorioPlantillas = directorioPlantillas;
	}

	@Override
	public void enviarCorreoRecuperacion(String correoDestino, String nombreDestino, String enlaceRecuperacion) {
		String html = cargarPlantilla(PLANTILLA_RECUPERACION)
				.replace("{{nombre}}", nombreDestino)
				.replace("{{enlace}}", enlaceRecuperacion);

		Map<String, Object> peticion = Map.of(
				"message", Map.of(
						"subject", "Recuperacion de contrasena",
						"body", Map.of("contentType", "HTML", "content", html),
						"toRecipients", List.of(Map.of("emailAddress", Map.of("address", correoDestino)))),
				"saveToSentItems", false);

		try {
			String cuerpo = objectMapper.writeValueAsString(peticion);
			String token = obtenerTokenAcceso();

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(SEND_MAIL_URL.formatted(senderEmail)))
					.header("Authorization", "Bearer " + token)
					.header("Content-Type", "application/json")
					.POST(BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8))
					.build();

			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			if (response.statusCode() >= 300) {
				throw new RuntimeException(
						"Microsoft Graph respondio con estado " + response.statusCode() + ": " + response.body());
			}
		} catch (IOException e) {
			throw new RuntimeException("No se pudo enviar el correo de recuperacion", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("No se pudo enviar el correo de recuperacion", e);
		}
	}

	private synchronized String obtenerTokenAcceso() throws IOException, InterruptedException {
		if (tokenCacheado != null && Instant.now().isBefore(tokenExpiraEn)) {
			return tokenCacheado;
		}

		String cuerpoFormulario = "grant_type=client_credentials"
				+ "&client_id=" + clientId
				+ "&client_secret=" + clientSecret
				+ "&scope=" + GRAPH_SCOPE;

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(TOKEN_URL.formatted(tenantId)))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(BodyPublishers.ofString(cuerpoFormulario, StandardCharsets.UTF_8))
				.build();

		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		if (response.statusCode() >= 300) {
			throw new RuntimeException(
					"No se pudo obtener el token de Microsoft Graph (estado " + response.statusCode() + "): "
							+ response.body());
		}

		JsonNode json = objectMapper.readTree(response.body());
		String accessToken = json.get("access_token").asText();
		int expiraEnSegundos = json.get("expires_in").asInt();

		this.tokenCacheado = accessToken;
		this.tokenExpiraEn = Instant.now().plusSeconds(Math.max(0, expiraEnSegundos - 60));
		return accessToken;
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
