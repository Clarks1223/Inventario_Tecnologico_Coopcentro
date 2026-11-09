import { describe, it, expect } from 'vitest';
import { filtrarActivosPorTexto } from '../filtrarActivos';

const activos = [
  { id_activo: 1, serial: 'SN-ABC', codigo_inventario: 'INV-1', marca: 'Dell', modelo: 'Latitude', tipo_activo: 'laptop' },
  { id_activo: 2, serial: 'XYZ-9', codigo_inventario: null, marca: 'Logitech', modelo: 'K120', tipo_activo: 'periferico' },
];

describe('filtrarActivosPorTexto', () => {
  it('sin texto devuelve todas las opciones', () => {
    expect(filtrarActivosPorTexto(activos, { inputValue: '  ' })).toHaveLength(2);
  });

  it('busca en serial, código, marca, modelo y tipo sin distinguir mayúsculas', () => {
    expect(filtrarActivosPorTexto(activos, { inputValue: 'abc' }).map((a) => a.id_activo)).toEqual([1]);
    expect(filtrarActivosPorTexto(activos, { inputValue: 'inv-1' }).map((a) => a.id_activo)).toEqual([1]);
    expect(filtrarActivosPorTexto(activos, { inputValue: 'k120' }).map((a) => a.id_activo)).toEqual([2]);
    expect(filtrarActivosPorTexto(activos, { inputValue: 'PERIF' }).map((a) => a.id_activo)).toEqual([2]);
  });
});
