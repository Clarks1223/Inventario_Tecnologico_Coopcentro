package com.uisrael.inventario.infraestructura.persistencia.mapeadores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ReferenciaJpaMapperTest {

	private final IReferenciaJpaMapper mapper = new IReferenciaJpaMapper() {
	};

	@Test
	void mapGrupoConNullDevuelveNull() {
		// El grupo es la unica FK opcional del activo: un activo sin grupo no
		// debe generar una entidad stub con id 0 que rompa la FK.
		assertNull(mapper.mapGrupo(null));
	}

	@Test
	void mapGrupoConIdDevuelveStub() {
		assertEquals(7, mapper.mapGrupo(7).getIdGrupo());
	}

}
