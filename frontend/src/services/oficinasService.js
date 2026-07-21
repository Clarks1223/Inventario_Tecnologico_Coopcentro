import api from './api';

export const oficinasService = {
  getOficinas: async (config = {}) => {
    return await api.get('oficinas', config);
  },
  createOficina: async (data) => {
    return await api.post('oficinas', data);
  },
  updateOficina: async (id, data) => {
    return await api.put(`oficinas/${id}`, data);
  },
  deleteOficina: async (id) => {
    return await api.delete(`oficinas/${id}`);
  },
};
