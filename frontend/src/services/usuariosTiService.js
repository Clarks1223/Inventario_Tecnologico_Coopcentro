import api from './api';

export const usuariosTiService = {
  getUsuariosTi: async (config = {}) => {
    return await api.get('usuarios-ti', config);
  },
  createUsuarioTi: async (data) => {
    return await api.post('usuarios-ti', data);
  },
  deleteUsuarioTi: async (id) => {
    return await api.delete(`usuarios-ti/${id}`);
  },
  cambiarContrasena: async (id, data) => {
    return await api.patch(`usuarios-ti/${id}/contrasena`, data);
  },
};
