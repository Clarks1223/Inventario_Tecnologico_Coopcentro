import axios from 'axios';
import { keysToCamel, keysToSnake } from '../utils/caseConverter';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  if (config.data && !(config.data instanceof FormData)) {
    config.data = keysToCamel(config.data);
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    if (response.data && response.config.responseType !== 'blob') {
      response.data = keysToSnake(response.data);
    }
    return response;
  },
  (error) => {
    if (axios.isCancel(error)) return Promise.reject(error);
    if (error.response) {
      if (error.response.data && error.config?.responseType !== 'blob') {
        error.response.data = keysToSnake(error.response.data);
      }
      const { status } = error.response;
      if (status === 401) {
        console.error('Sesión no autorizada');
      } else if (status === 403) {
        console.error('Acceso denegado');
      } else if (status === 500) {
        console.error('Error interno del servidor');
      }
    } else if (error.request) {
      console.error('Error de red - sin respuesta del servidor');
    }
    return Promise.reject(error);
  }
);

export default api;
