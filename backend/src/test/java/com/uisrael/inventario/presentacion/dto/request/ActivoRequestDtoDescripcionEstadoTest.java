package com.uisrael.inventario.presentacion.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ActivoRequestDtoDescripcionEstadoTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeAll
	static void crearValidador() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterAll
	static void cerrar() {
		factory.close();
	}

	private Set<ConstraintViolation<ActivoRequestDto>> violacionesDe(String descripcionEstado) {
		ActivoRequestDto dto = new ActivoRequestDto();
		dto.setDescripcionEstado(descripcionEstado);
		return validator.validateProperty(dto, "descripcionEstado");
	}

	@Test
	void esOpcional() {
		assertTrue(violacionesDe(null).isEmpty());
	}

	@Test
	void textoVacioOEnBlancoSeGuardaComoSinValor() {
		ActivoRequestDto dto = new ActivoRequestDto();
		dto.setDescripcionEstado("   ");
		assertNull(dto.getDescripcionEstado());
		assertTrue(violacionesDe("").isEmpty());
	}

	@Test
	void rechazaMenosDeTresCaracteres() {
		Set<ConstraintViolation<ActivoRequestDto>> violaciones = violacionesDe("ok");
		assertEquals(1, violaciones.size());
		assertEquals("debe tener entre 3 y 250 caracteres", violaciones.iterator().next().getMessage());
	}

	@Test
	void aceptaTresYDoscientosCincuentaCaracteres() {
		assertTrue(violacionesDe("abc").isEmpty());
		assertTrue(violacionesDe("a".repeat(250)).isEmpty());
	}

	@Test
	void rechazaMasDeDoscientosCincuentaCaracteres() {
		assertEquals(1, violacionesDe("a".repeat(251)).size());
	}

}
