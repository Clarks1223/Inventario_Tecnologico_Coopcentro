import api from './api';

export const gruposService = {
  getGrupos: async (config = {}) => {
    return await api.get('grupos', config);
  },
  getGrupo: async (id) => {
    return await api.get(`grupos/${id}`);
  },
  createGrupo: async ({ nombre, descripcion }) => {
    return await api.post('grupos', { nombre, descripcion });
  },
  updateGrupo: async (id, { nombre, descripcion }) => {
    return await api.put(`grupos/${id}`, { nombre, descripcion });
  },
  deleteGrupo: async (id) => {
    return await api.delete(`grupos/${id}`);
  },
  agregarActivos: async (id, idsActivos) => {
    return await api.post(`grupos/${id}/activos`, { ids_activos: idsActivos });
  },
  quitarActivo: async (id, idActivo) => {
    return await api.delete(`grupos/${id}/activos/${idActivo}`);
  },
  // Entrega el grupo completo al empleado; devuelve las actas de entrega creadas.
  asignarGrupo: async (id, { id_empleado, id_usuario_ti, motivo, observacion }) => {
    return await api.post(`grupos/${id}/asignar`, { id_empleado, id_usuario_ti, motivo, observacion });
  },
};
