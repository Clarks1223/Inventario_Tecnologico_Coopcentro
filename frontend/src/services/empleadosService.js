import api from './api';

export const empleadosService = {
  getEmpleados: async (config = {}) => {
    return await api.get('empleados', config);
  },
  getOficinas: async () => {
    return await api.get('oficinas');
  },
  getCargos: async () => {
    return await api.get('cargos');
  },
  createEmpleado: async (data) => {
    return await api.post('empleados', data);
  },
  updateEmpleado: async (id, data) => {
    return await api.put(`empleados/${id}`, data);
  },
  deleteEmpleado: async (id) => {
    return await api.delete(`empleados/${id}`);
  },
};
