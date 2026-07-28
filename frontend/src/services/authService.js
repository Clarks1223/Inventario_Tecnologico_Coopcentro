import api from './api';

export const authService = {
  login: async (correo, contrasena) => {
    return await api.post('auth/login', { correo, contrasena });
  },
  solicitarRecuperacion: async (correo) => {
    return await api.post('auth/solicitar-recuperacion', { correo });
  },
  restablecerContrasena: async (data) => {
    return await api.post('auth/restablecer-contrasena', data);
  },
};
