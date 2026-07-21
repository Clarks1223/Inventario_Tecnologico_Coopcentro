import api from './api';

export const cargosService = {
  getCargos: async (config = {}) => {
    return await api.get('cargos', config);
  },
  createCargo: async (data) => {
    return await api.post('cargos', data);
  },
  updateCargo: async (id, data) => {
    return await api.put(`cargos/${id}`, data);
  },
  deleteCargo: async (id) => {
    return await api.delete(`cargos/${id}`);
  },
};
