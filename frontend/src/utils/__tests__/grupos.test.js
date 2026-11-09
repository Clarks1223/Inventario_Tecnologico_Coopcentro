import { describe, it, expect } from 'vitest';
import {
  ESTADO_GRUPO, etiquetaEstadoGrupo, colorEstadoGrupo, esAsignable, activosAgrupables, asignadoA,
} from '../grupos';

describe('asignadoA', () => {
  it('muestra el empleado al que está entregado', () => {
    expect(asignadoA({ en_bodega: false, empleado_asignado: 'Erick Santillan' })).toBe('Erick Santillan');
  });

  it('dice "En bodega" aunque haya un técnico custodio', () => {
    expect(asignadoA({ en_bodega: true, empleado_asignado: 'Pablo Uchuari' })).toBe('En bodega');
  });

  it('sin acta abierta (dado de baja o robado) no hay a quién mostrar', () => {
    expect(asignadoA({ en_bodega: false, empleado_asignado: '' })).toBe('—');
  });
});

describe('etiquetaEstadoGrupo', () => {
  it('nombra al empleado cuando el grupo está asignado', () => {
    expect(
      etiquetaEstadoGrupo({ estado: ESTADO_GRUPO.ASIGNADO, nombre_empleado_asignado: 'Ana Paz' })
    ).toBe('Asignado a Ana Paz');
  });

  it('traduce los demás estados', () => {
    expect(etiquetaEstadoGrupo({ estado: ESTADO_GRUPO.EN_BODEGA })).toBe('En bodega');
    expect(etiquetaEstadoGrupo({ estado: ESTADO_GRUPO.MIXTO })).toBe('Mixto');
    expect(etiquetaEstadoGrupo({ estado: ESTADO_GRUPO.VACIO })).toBe('Vacío');
  });
});

describe('colorEstadoGrupo', () => {
  it('usa un color por estado', () => {
    expect(colorEstadoGrupo({ estado: ESTADO_GRUPO.EN_BODEGA })).toBe('success');
    expect(colorEstadoGrupo({ estado: ESTADO_GRUPO.ASIGNADO })).toBe('info');
    expect(colorEstadoGrupo({ estado: ESTADO_GRUPO.MIXTO })).toBe('warning');
    expect(colorEstadoGrupo({ estado: ESTADO_GRUPO.VACIO })).toBe('default');
  });
});

describe('esAsignable', () => {
  it('solo un grupo en bodega se puede asignar', () => {
    expect(esAsignable({ estado: ESTADO_GRUPO.EN_BODEGA })).toBe(true);
    expect(esAsignable({ estado: ESTADO_GRUPO.ASIGNADO })).toBe(false);
    expect(esAsignable({ estado: ESTADO_GRUPO.MIXTO })).toBe(false);
    expect(esAsignable({ estado: ESTADO_GRUPO.VACIO })).toBe(false);
    expect(esAsignable(null)).toBe(false);
  });
});

describe('activosAgrupables', () => {
  it('ofrece solo activos operativos que no están en ningún grupo', () => {
    const activos = [
      { id_activo: 1, estado: 'OPERATIVO', id_grupo: null },
      { id_activo: 2, estado: 'DADO_DE_BAJA', id_grupo: null },
      { id_activo: 3, estado: 'OPERATIVO', id_grupo: 9 },
      { id_activo: 4, estado: 'ROBADO_PERDIDO' },
      { id_activo: 5, estado: 'OPERATIVO' },
    ];
    expect(activosAgrupables(activos).map((a) => a.id_activo)).toEqual([1, 5]);
  });
});
