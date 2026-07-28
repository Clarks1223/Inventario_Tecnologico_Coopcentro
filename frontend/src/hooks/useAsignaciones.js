import { useState, useEffect, useCallback, useMemo } from 'react';
import { asignacionesService } from '../services/asignacionesService';
import { empleadosService } from '../services/empleadosService';
import { activosService } from '../services/activosService';
import { oficinasService } from '../services/oficinasService';
import { useSession } from './useSession';
import { useSnackbar } from './useSnackbar';
import { useConfirmDialog } from './useConfirmDialog';
import axios from 'axios';

export const useAsignaciones = () => {
  const [allAsignaciones, setAllAsignaciones] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [activos, setActivos] = useState([]);
  const [usuariosTi, setUsuariosTi] = useState([]);
  const [oficinas, setOficinas] = useState([]);
  const [cargos, setCargos] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [busquedaEmpleado, setBusquedaEmpleado] = useState('');
  const [oficinaFiltro, setOficinaFiltro] = useState(null);
  const [empleadoSeleccionado, setEmpleadoSeleccionado] = useState(null);

  const { sesion } = useSession();
  const showSnackbar = useSnackbar();
  const { confirmDialog, openConfirm, closeConfirm } = useConfirmDialog();

  const fetchAll = useCallback(async (signal) => {
    setIsLoading(true);
    try {
      const [actasRes, empleadosRes, activosRes, usuariosTiRes, oficinasRes, cargosRes] = await Promise.all([
        asignacionesService.getAsignaciones({ signal }),
        empleadosService.getEmpleados({ signal }),
        activosService.getActivos({ signal }),
        asignacionesService.getUsuariosTi(),
        oficinasService.getOficinas(),
        empleadosService.getCargos(),
      ]);
      setAllAsignaciones(actasRes.data || []);
      setEmpleados(empleadosRes.data || []);
      setActivos(activosRes.data || []);
      setUsuariosTi(usuariosTiRes.data || []);
      setOficinas(oficinasRes.data || []);
      setCargos(cargosRes.data || []);
    } catch (error) {
      if (!axios.isCancel(error)) console.error(error);
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

  const activoDe = useCallback(
    (id) => activos.find((a) => a.id_activo === id),
    [activos]
  );

  const oficinaNombre = useCallback(
    (id) => oficinas.find((o) => o.id_oficina === id)?.nombre || '',
    [oficinas]
  );

  const cargoNombre = useCallback(
    (id) => cargos.find((c) => c.id_cargo === id)?.nombre || '',
    [cargos]
  );

  const conteoActivasPorEmpleado = useMemo(() => {
    const conteo = {};
    allAsignaciones.forEach((a) => {
      if (a.estado_asignacion === 'activa') {
        conteo[a.id_empleado] = (conteo[a.id_empleado] || 0) + 1;
      }
    });
    return conteo;
  }, [allAsignaciones]);

  const empleadosFiltrados = useMemo(() => {
    const texto = busquedaEmpleado.trim().toLowerCase();
    return empleados
      .filter((e) => e.activo)
      .filter((e) => !oficinaFiltro || e.id_oficina === oficinaFiltro)
      .filter(
        (e) =>
          !texto ||
          `${e.nombre} ${e.apellido}`.toLowerCase().includes(texto) ||
          e.cedula?.includes(texto) ||
          e.correo?.toLowerCase().includes(texto)
      )
      .sort((a, b) => `${a.nombre} ${a.apellido}`.localeCompare(`${b.nombre} ${b.apellido}`));
  }, [empleados, oficinaFiltro, busquedaEmpleado]);

  const empleadoActual = useMemo(
    () => empleados.find((e) => e.id_empleado === empleadoSeleccionado) || null,
    [empleados, empleadoSeleccionado]
  );

  const asignacionesDelEmpleado = useMemo(() => {
    if (!empleadoSeleccionado) return [];
    return allAsignaciones
      .filter((a) => a.id_empleado === empleadoSeleccionado && a.estado_asignacion === 'activa')
      .sort((a, b) => new Date(b.fecha_asignacion) - new Date(a.fecha_asignacion))
      .map((a) => ({
        ...a,
        serial: activoDe(a.id_activo)?.serial || '',
        tipo_activo: activoDe(a.id_activo)?.tipo_activo || '',
        nombre_tecnico: tecnicoNombre(a.id_usuario_ti),
      }));
  }, [allAsignaciones, empleadoSeleccionado, activoDe, tecnicoNombre]);

  const activosDisponibles = useMemo(
    () => activos.filter((a) => a.estado === 'NO_ASIGNADO'),
    [activos]
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

  const handleAssign = useCallback(async ({ id_activo, motivo }) => {
    try {
      await asignacionesService.assignActivo({
        id_activo,
        id_empleado: empleadoSeleccionado,
        id_usuario_ti: sesion.id_usuario_ti,
        motivo,
      });
      showSnackbar('Activo asignado correctamente', 'success');
      setOpen(false);
      fetchAll();
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al asignar el activo', 'error');
    }
  }, [empleadoSeleccionado, sesion, fetchAll, showSnackbar]);

  const handlePrintAllForEmpleado = useCallback(async (idEmpleado) => {
    try {
      const response = await asignacionesService.getActasImprimibles(idEmpleado);
      const idsActas = response.data || [];
      if (idsActas.length === 0) {
        showSnackbar('Este empleado no tiene activos asignados actualmente', 'warning');
        return;
      }
      idsActas.forEach((idActa, indice) => {
        setTimeout(() => handlePrint(idActa), indice * 500);
      });
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al obtener las actas del empleado', 'error');
    }
  }, [showSnackbar, handlePrint]);

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
    isLoading,
    oficinas,
    busquedaEmpleado,
    setBusquedaEmpleado,
    oficinaFiltro,
    setOficinaFiltro,
    empleadosFiltrados,
    empleadoSeleccionado,
    setEmpleadoSeleccionado,
    empleadoActual,
    asignacionesDelEmpleado,
    conteoActivasPorEmpleado,
    activosDisponibles,
    oficinaNombre,
    cargoNombre,
    open,
    setOpen,
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
