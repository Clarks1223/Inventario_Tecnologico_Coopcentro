import { useState, useEffect, useCallback, useMemo } from 'react';
import { asignacionesService } from '../services/asignacionesService';
import { empleadosService } from '../services/empleadosService';
import { activosService } from '../services/activosService';
import { oficinasService } from '../services/oficinasService';
import { useSession } from './useSession';
import { useSnackbar } from './useSnackbar';
import { useConfirmDialog } from './useConfirmDialog';

export const useAsignaciones = () => {
  const [allAsignaciones, setAllAsignaciones] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [activos, setActivos] = useState([]);
  const [usuariosTi, setUsuariosTi] = useState([]);
  const [oficinas, setOficinas] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage] = useState(10);
  const [showHistorial, setShowHistorial] = useState(false);
  const [open, setOpen] = useState(false);
  const [empleadoFiltro, setEmpleadoFiltro] = useState(null);
  const [oficinaFiltro, setOficinaFiltro] = useState(null);

  const { sesion } = useSession();
  const showSnackbar = useSnackbar();
  const { confirmDialog, openConfirm, closeConfirm } = useConfirmDialog();

  const fetchAll = useCallback(async (signal) => {
    setIsLoading(true);
    try {
      const [actasRes, empleadosRes, activosRes, usuariosTiRes, oficinasRes] = await Promise.all([
        asignacionesService.getAsignaciones({ signal }),
        empleadosService.getEmpleados({ signal }),
        activosService.getActivos({ signal }),
        asignacionesService.getUsuariosTi(),
        oficinasService.getOficinas(),
      ]);
      setAllAsignaciones(actasRes.data || []);
      setEmpleados(empleadosRes.data || []);
      setActivos(activosRes.data || []);
      setUsuariosTi(usuariosTiRes.data || []);
      setOficinas(oficinasRes.data || []);
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    fetchAll(controller.signal);
    return () => controller.abort();
  }, [fetchAll]);

  const empleadoNombre = useCallback(
    (id) => {
      const emp = empleados.find((e) => e.id_empleado === id);
      return emp ? `${emp.nombre} ${emp.apellido}` : '';
    },
    [empleados]
  );

  const tecnicoNombre = useCallback(
    (idUsuarioTi) => {
      const usuarioTi = usuariosTi.find((u) => u.id_usuario_ti === idUsuarioTi);
      return usuarioTi ? empleadoNombre(usuarioTi.id_empleado) : '';
    },
    [usuariosTi, empleadoNombre]
  );

  const activoLabel = useCallback(
    (id) => {
      const activo = activos.find((a) => a.id_activo === id);
      return activo ? `${activo.codigo_inventario} · ${activo.tipo_activo}` : '';
    },
    [activos]
  );

  const activosDisponibles = useMemo(
    () => activos.filter((a) => a.estado === 'NO_ASIGNADO'),
    [activos]
  );

  const empleadoOficina = useCallback(
    (idEmpleado) => empleados.find((e) => e.id_empleado === idEmpleado)?.id_oficina,
    [empleados]
  );

  const filteredAsignaciones = useMemo(() => {
    return allAsignaciones
      .filter((a) => (showHistorial ? true : a.estado_asignacion === 'activa'))
      .filter((a) => !empleadoFiltro || a.id_empleado === empleadoFiltro)
      .filter((a) => !oficinaFiltro || empleadoOficina(a.id_empleado) === oficinaFiltro)
      .sort((a, b) => new Date(b.fecha_asignacion) - new Date(a.fecha_asignacion));
  }, [allAsignaciones, showHistorial, empleadoFiltro, oficinaFiltro, empleadoOficina]);

  const asignaciones = useMemo(
    () => filteredAsignaciones.slice(page * rowsPerPage, (page + 1) * rowsPerPage)
      .map((a) => ({
        ...a,
        nombre_empleado: empleadoNombre(a.id_empleado),
        nombre_tecnico: tecnicoNombre(a.id_usuario_ti),
        activo_label: activoLabel(a.id_activo),
      })),
    [filteredAsignaciones, page, rowsPerPage, empleadoNombre, tecnicoNombre, activoLabel]
  );

  const handlePrint = useCallback(async (idActa) => {
    try {
      const response = await asignacionesService.printActa(idActa);
      const url = URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      window.open(url, '_blank');
    } catch (error) {
      if (error.response?.status === 500) {
        showSnackbar('No hay plantilla de acta disponible para este tipo de activo', 'warning');
      } else {
        showSnackbar('Error al generar el acta en PDF', 'error');
      }
    }
  }, [showSnackbar]);

  const handleReturn = useCallback(async (idActa, motivo) => {
    try {
      await asignacionesService.returnActivo(idActa, motivo);
      showSnackbar('Activo devuelto correctamente', 'success');
      fetchAll();
      handlePrint(idActa);
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al devolver el activo', 'error');
    }
  }, [fetchAll, showSnackbar, handlePrint]);

  const handleReturnAll = useCallback(async (idEmpleado, motivo) => {
    try {
      await asignacionesService.returnAllForEmpleado(idEmpleado, motivo);
      showSnackbar('Todos los activos del empleado fueron devueltos', 'success');
      fetchAll();
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al devolver los activos', 'error');
    }
  }, [fetchAll, showSnackbar]);

  const handleAssign = useCallback(async ({ id_activo, id_empleado, motivo }) => {
    try {
      await asignacionesService.assignActivo({
        id_activo,
        id_empleado,
        id_usuario_ti: sesion.id_usuario_ti,
        motivo,
      });
      showSnackbar('Activo asignado correctamente', 'success');
      setOpen(false);
      setEmpleadoFiltro(id_empleado);
      fetchAll();
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al asignar el activo', 'error');
    }
  }, [sesion, fetchAll, showSnackbar]);

  const handlePrintAllForEmpleado = useCallback((idEmpleado) => {
    const activasDelEmpleado = allAsignaciones.filter(
      (a) => a.id_empleado === idEmpleado && a.estado_asignacion === 'activa'
    );
    if (activasDelEmpleado.length === 0) {
      showSnackbar('Este empleado no tiene activos asignados actualmente', 'warning');
      return;
    }
    activasDelEmpleado.forEach((acta, indice) => {
      setTimeout(() => handlePrint(acta.id_acta), indice * 500);
    });
  }, [allAsignaciones, showSnackbar, handlePrint]);

  const initiateReturn = useCallback((idActa) => {
    openConfirm('devolver', idActa, '¿Deseas registrar la devolución de este activo?');
  }, [openConfirm]);

  const initiateReturnAll = useCallback((idEmpleado) => {
    openConfirm('devolverTodo', idEmpleado, '¿Deseas devolver todos los activos activos de este empleado?');
  }, [openConfirm]);

  const handleConfirmAction = useCallback(() => {
    const { type, id } = confirmDialog;
    closeConfirm();
    if (type === 'devolver') handleReturn(id, '');
    else if (type === 'devolverTodo') handleReturnAll(id, '');
  }, [confirmDialog, closeConfirm, handleReturn, handleReturnAll]);

  return {
    asignaciones,
    isLoading,
    page,
    setPage,
    count: filteredAsignaciones.length,
    rowsPerPage,
    empleados,
    activosDisponibles,
    oficinas,
    showHistorial,
    setShowHistorial,
    open,
    setOpen,
    empleadoFiltro,
    setEmpleadoFiltro,
    oficinaFiltro,
    setOficinaFiltro,
    confirmDialog,
    handleAssign,
    initiateReturn,
    initiateReturnAll,
    handleConfirmAction,
    handleCloseConfirm: closeConfirm,
    handlePrint,
    handlePrintAllForEmpleado,
  };
};
