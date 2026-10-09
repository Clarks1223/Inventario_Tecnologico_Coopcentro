import api from './api';

export const dashboardService = {
  getActivos: async (config = {}) => {
    return await api.get('activos', config);
  },
};
