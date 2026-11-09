import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';

vi.mock('../../services/gruposService', () => ({
  gruposService: {
    getGrupos: vi.fn(),
    createGrupo: vi.fn(),
    updateGrupo: vi.fn(),
    agregarActivos: vi.fn(),
    quitarActivo: vi.fn(),
    deleteGrupo: vi.fn(),
  },
}));
vi.mock('../../services/activosService', () => ({
  activosService: { getActivos: vi.fn() },
}));
const showSnackbar = vi.fn();
vi.mock('../useSnackbar', () => ({ useSnackbar: () => showSnackbar }));

import { gruposService } from '../../services/gruposService';
import { activosService } from '../../services/activosService';
import { useGrupos } from '../useGrupos';

const error400 = (message) => Object.assign(new Error(message), { response: { status: 400, data: { message } } });

const montar = async () => {
  const hook = renderHook(() => useGrupos());
  await waitFor(() => expect(hook.result.current.isLoading).toBe(false));
  return hook;
};

beforeEach(() => {
  vi.clearAllMocks();
  gruposService.getGrupos.mockResolvedValue({ data: [] });
  activosService.getActivos.mockResolvedValue({ data: [] });
});

describe('useGrupos.handleSave tras un fallo parcial', () => {
  it('si el grupo se creó pero agregar activos falla, reintentar actualiza en vez de crear otro', async () => {
    gruposService.createGrupo.mockResolvedValue({ data: { id_grupo: 5, nombre: 'Caja 1', activos: [] } });
    gruposService.agregarActivos.mockRejectedValueOnce(error400('No se pueden agregar al grupo: SN-2 (dado de baja)'));
    const { result } = await montar();

    act(() => result.current.openDialog());
    act(() => result.current.setCurrentGrupo({ ...result.current.currentGrupo, nombre: 'Caja 1', ids_activos: [1, 2] }));
    await act(() => result.current.handleSave());

    expect(result.current.open).toBe(true);
    expect(result.current.currentGrupo.id_grupo).toBe(5);

    // El técnico quita el activo problemático y vuelve a guardar.
    gruposService.updateGrupo.mockResolvedValue({ data: { id_grupo: 5, nombre: 'Caja 1', activos: [] } });
    gruposService.agregarActivos.mockResolvedValue({ data: { id_grupo: 5, activos: [{ id_activo: 1 }] } });
    act(() => result.current.setCurrentGrupo({ ...result.current.currentGrupo, ids_activos: [1] }));
    await act(() => result.current.handleSave());

    expect(gruposService.createGrupo).toHaveBeenCalledTimes(1);
    expect(gruposService.updateGrupo).toHaveBeenCalledWith(5, { nombre: 'Caja 1', descripcion: '' });
    expect(gruposService.agregarActivos).toHaveBeenLastCalledWith(5, [1]);
    expect(result.current.open).toBe(false);
  });

  it('si una baja falla, las bajas ya hechas no se vuelven a pedir', async () => {
    const grupo = { id_grupo: 7, nombre: 'Caja 2', descripcion: '', activos: [{ id_activo: 1 }, { id_activo: 2 }, { id_activo: 3 }] };
    gruposService.updateGrupo.mockResolvedValue({ data: grupo });
    gruposService.quitarActivo
      .mockResolvedValueOnce({ data: { ...grupo, activos: [{ id_activo: 2 }, { id_activo: 3 }] } })
      .mockRejectedValueOnce(error400('Error de red'));
    const { result } = await montar();

    act(() => result.current.openDialog(grupo));
    act(() => result.current.setCurrentGrupo({ ...result.current.currentGrupo, ids_activos: [3] }));
    await act(() => result.current.handleSave());

    expect(result.current.open).toBe(true);
    gruposService.quitarActivo.mockReset();
    gruposService.quitarActivo.mockResolvedValue({ data: { ...grupo, activos: [{ id_activo: 3 }] } });
    await act(() => result.current.handleSave());

    expect(gruposService.quitarActivo).toHaveBeenCalledTimes(1);
    expect(gruposService.quitarActivo).toHaveBeenCalledWith(7, 2);
    expect(result.current.open).toBe(false);
  });
});
