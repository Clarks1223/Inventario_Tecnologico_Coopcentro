import api from './api';

export const asignacionesService = {
  getAsignaciones: async (config = {}) => {
    return await api.get('actas', config);
  },
  getUsuariosTi: async () => {
    return await api.get('usuarios-ti');
  },
  assignActivo: async ({ id_activo, id_empleado, id_usuario_ti, motivo }) => {
    return await api.post('actas/asignar', { id_activo, id_empleado, id_usuario_ti, motivo });
  },
  returnActivo: async (idActa, motivo) => {
    return await api.post(`actas/${idActa}/devolver`, { motivo });
  },
  returnAllForEmpleado: async (idEmpleado, motivo) => {
    return await api.post('actas/devolver-todo-empleado', { id_empleado: idEmpleado, motivo });
  },
  printActa: async (idActa) => {
    return await api.get(`actas/${idActa}/imprimir`, { responseType: 'blob' });
  },
};
