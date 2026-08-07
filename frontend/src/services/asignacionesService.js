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
  returnActivo: async (idActa, motivo, idUsuarioTi, observacion) => {
    return await api.post(`actas/${idActa}/devolver`, {
      motivo,
      id_usuario_ti: idUsuarioTi,
      observacion,
    });
  },
  returnAllForEmpleado: async (idEmpleado, motivo, idUsuarioTi, observacion) => {
    return await api.post('actas/devolver-todo-empleado', {
      id_empleado: idEmpleado,
      motivo,
      id_usuario_ti: idUsuarioTi,
      observacion,
    });
  },
  // Las actas no se abren en el navegador: se archivan en disco y estos
  // endpoints devuelven la ruta donde quedaron guardadas.
  // Un observacion null deja intacto el comentario que el acta ya tuviera.
  archivarActa: async (idActa, observacion = null) => {
    return await api.post(`actas/${idActa}/archivar`, { observacion });
  },
  archivarLote: async (idsActas) => {
    return await api.post('actas/archivar-lote', { ids_actas: idsActas });
  },
  getActasImprimibles: async (idEmpleado) => {
    return await api.get(`actas/imprimibles-empleado/${idEmpleado}`);
  },
  archivarActasEmpleado: async (idEmpleado, observacion = null) => {
    return await api.post(`actas/imprimibles-empleado/${idEmpleado}/archivar`, { observacion });
  },
};
