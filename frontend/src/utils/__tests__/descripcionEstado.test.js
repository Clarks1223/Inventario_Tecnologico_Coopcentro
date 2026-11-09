import { describe, it, expect } from 'vitest';
import {
  MAX_DESCRIPCION_ESTADO, errorDescripcionEstado, normalizarDescripcionEstado,
} from '../descripcionEstado';

describe('errorDescripcionEstado', () => {
  it('es opcional: vacío o en blanco no es error', () => {
    expect(errorDescripcionEstado('')).toBeNull();
    expect(errorDescripcionEstado('   ')).toBeNull();
    expect(errorDescripcionEstado(null)).toBeNull();
  });

  it('pide al menos 3 caracteres sin contar espacios de los extremos', () => {
    expect(errorDescripcionEstado(' ok ')).toBe('Debe tener al menos 3 caracteres');
    expect(errorDescripcionEstado('abc')).toBeNull();
  });

  it('no permite más de 250 caracteres', () => {
    expect(MAX_DESCRIPCION_ESTADO).toBe(250);
    expect(errorDescripcionEstado('a'.repeat(250))).toBeNull();
    expect(errorDescripcionEstado('a'.repeat(251))).toBe('No debe superar 250 caracteres');
  });
});

describe('normalizarDescripcionEstado', () => {
  it('envía null cuando no hay texto y recorta los extremos', () => {
    expect(normalizarDescripcionEstado('  ')).toBeNull();
    expect(normalizarDescripcionEstado(undefined)).toBeNull();
    expect(normalizarDescripcionEstado('  Pantalla rayada ')).toBe('Pantalla rayada');
  });
});
