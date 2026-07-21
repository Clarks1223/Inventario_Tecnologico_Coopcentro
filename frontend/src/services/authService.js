import api from './api';

export const authService = {
  login: async (correo, contrasena) => {
    return await api.post('auth/login', { correo, contrasena });
  },
};
