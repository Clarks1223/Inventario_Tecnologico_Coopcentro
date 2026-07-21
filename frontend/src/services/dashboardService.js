import api from './api';

export const dashboardService = {
  getActivos: async () => {
    return await api.get('activos');
  },
};
