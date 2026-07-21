import api from './api';

export const activosService = {
  getActivos: async (config = {}) => {
    return await api.get('activos', config);
  },
  getActivo: async (id) => {
    return await api.get(`activos/${id}`);
  },
  createActivo: async (data) => {
    return await api.post('activos', data);
  },
  updateActivo: async (id, data) => {
    return await api.put(`activos/${id}`, data);
  },
  deleteActivo: async (id) => {
    return await api.delete(`activos/${id}`);
  },
};
