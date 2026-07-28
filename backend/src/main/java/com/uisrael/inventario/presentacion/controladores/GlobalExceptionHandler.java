package com.uisrael.inventario.presentacion.controladores;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.uisrael.inventario.presentacion.dto.response.ErrorResponseDto;

/**
 * Traduce las excepciones de negocio (RuntimeException) y de validacion
 * (@Valid) a una respuesta 400 con un mensaje claro en espanol, en vez del
 * 500 generico o el volcado tecnico que Spring Boot devuelve por defecto.
 * Los mensajes de validacion se componen como "El campo X <regla>", por lo
 * que los message de las anotaciones de los DTOs deben redactarse para leerse
 * a continuacion de ese prefijo (ej. "debe tener entre 3 y 20 caracteres").
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String MENSAJE_OBLIGATORIO = "es obligatorio";

	private static final Map<String, String> NOMBRES_CAMPOS = Map.ofEntries(
			Map.entry("idOficina", "oficina"),
			Map.entry("idCargo", "cargo"),
			Map.entry("idEmpleado", "empleado"),
			Map.entry("idActivo", "activo"),
			Map.entry("idUsuarioTi", "usuario TI"),
			Map.entry("codigoInventario", "código de inventario"),
			Map.entry("tipoActivo", "tipo de activo"),
			Map.entry("extensionTelefonica", "extensión telefónica"),
			Map.entry("contrasena", "contraseña"),
			Map.entry("contrasenaActual", "contraseña actual"),
			Map.entry("contrasenaNueva", "nueva contraseña"),
			Map.entry("confirmarContrasenaNueva", "confirmación de la nueva contraseña"),
			Map.entry("numeroLinea", "número de línea"),
			Map.entry("tipoConexion", "tipo de conexión"),
			Map.entry("estadoBateria", "estado de batería"),
			Map.entry("modeloCabezal", "modelo de cabezal"),
			Map.entry("tipoDispositivo", "tipo de dispositivo"),
			Map.entry("sistemaOperativo", "sistema operativo"),
			Map.entry("ramGb", "RAM (GB)"),
			Map.entry("tipoAlmacenamiento", "tipo de almacenamiento"),
			Map.entry("almacenamientoGb", "almacenamiento (GB)"),
			Map.entry("direccion", "dirección"),
			Map.entry("cedula", "cédula"));

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponseDto> manejarRuntimeException(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto(e.getMessage()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponseDto> manejarIntegridadDatos(DataIntegrityViolationException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto(
				"No se pudo guardar: los datos entran en conflicto con registros ya existentes"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> manejarValidacion(MethodArgumentNotValidException e) {
		Map<String, List<String>> erroresPorCampo = new LinkedHashMap<>();
		for (FieldError error : e.getBindingResult().getFieldErrors()) {
			erroresPorCampo.computeIfAbsent(error.getField(), campo -> new ArrayList<>())
					.add(error.getDefaultMessage());
		}
		String mensaje = erroresPorCampo.entrySet().stream()
				.map(entrada -> "El campo " + nombreAmigable(entrada.getKey()) + " " + resumenDeErrores(entrada.getValue()))
				.collect(Collectors.joining(". "));
		if (mensaje.isBlank()) {
			mensaje = "Hay datos inválidos en el formulario";
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto(mensaje));
	}

	/**
	 * Si el campo esta vacio suelen dispararse varias reglas a la vez (@NotBlank,
	 * @Size, @Pattern); en ese caso "es obligatorio" es el unico mensaje util.
	 */
	private String resumenDeErrores(List<String> mensajes) {
		if (mensajes.contains(MENSAJE_OBLIGATORIO)) {
			return MENSAJE_OBLIGATORIO;
		}
		return String.join(" y ", new LinkedHashSet<>(mensajes));
	}

	private String nombreAmigable(String campo) {
		String nombreSimple = campo.substring(campo.lastIndexOf('.') + 1);
		return NOMBRES_CAMPOS.getOrDefault(nombreSimple, nombreSimple);
	}

}
