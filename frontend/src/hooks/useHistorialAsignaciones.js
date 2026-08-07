import { useState, useEffect, useCallback, useMemo } from 'react';
import { asignacionesService } from '../services/asignacionesService';
import { empleadosService } from '../services/empleadosService';
import { activosService } from '../services/activosService';
import { oficinasService } from '../services/oficinasService';
import { useSnackbar } from './useSnackbar';
import { mensajeDeError } from '../utils/apiError';
import axios from 'axios';

export const useHistorialAsignaciones = () => {
  const [allAsignaciones, setAllAsignaciones] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [activos, setActivos] = useState([]);
  const [usuariosTi, setUsuariosTi] = useState([]);
  const [oficinas, setOficinas] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage] = useState(10);
  const [empleadoFiltro, setEmpleadoFiltro] = useState(null);
  const [oficinaFiltro, setOficinaFiltro] = useState(null);
  const [serialFiltro, setSerialFiltro] = useState('');
  const [estadoFiltro, setEstadoFiltro] = useState('');

  const showSnackbar = useSnackbar();

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

  const empleadoOficina = useCallback(
    (idEmpleado) => empleados.find((e) => e.id_empleado === idEmpleado)?.id_oficina,
    [empleados]
  );

  const filteredAsignaciones = useMemo(() => {
    return allAsignaciones
      .filter((a) => !estadoFiltro || a.estado_asignacion === estadoFiltro)
      .filter((a) => !empleadoFiltro || a.id_empleado === empleadoFiltro)
      .filter((a) => !oficinaFiltro || empleadoOficina(a.id_empleado) === oficinaFiltro)
      .filter((a) => !serialFiltro
        || activoDe(a.id_activo)?.serial?.toLowerCase().includes(serialFiltro.toLowerCase()))
      .sort((a, b) => new Date(b.fecha_asignacion) - new Date(a.fecha_asignacion));
  }, [allAsignaciones, estadoFiltro, empleadoFiltro, oficinaFiltro, serialFiltro, empleadoOficina, activoDe]);

  const asignaciones = useMemo(
    () => filteredAsignaciones.slice(page * rowsPerPage, (page + 1) * rowsPerPage)
      .map((a) => ({
        ...a,
        nombre_empleado: empleadoNombre(a.id_empleado),
        nombre_tecnico: tecnicoNombre(a.id_usuario_ti),
        serial: activoDe(a.id_activo)?.serial || '',
        tipo_activo: activoDe(a.id_activo)?.tipo_activo || '',
      })),
    [filteredAsignaciones, page, rowsPerPage, empleadoNombre, tecnicoNombre, activoDe]
  );

  // El acta no se abre en el navegador: se vuelve a archivar en su carpeta y
  // se informa la ruta. Sin comentario, para no pisar el que ya tenga.
  const handlePrint = useCallback(async (idActa) => {
    try {
      const { rutas = [] } = (await asignacionesService.archivarActa(idActa)).data || {};
      showSnackbar(`Acta guardada en: ${rutas[0]}`, 'success');
    } catch (error) {
      const esReglaDeNegocio = error.response?.status === 400;
      const mensaje = await mensajeDeError(
        error,
        esReglaDeNegocio
          ? 'No se puede generar el acta para este activo'
          : 'No se pudo generar el acta. Verifique que las plantillas estén instaladas en el servidor'
      );
      showSnackbar(mensaje, esReglaDeNegocio ? 'warning' : 'error');
    }
  }, [showSnackbar]);

  return {
    asignaciones,
    isLoading,
    page,
    setPage,
    count: filteredAsignaciones.length,
    rowsPerPage,
    empleados,
    oficinas,
    empleadoFiltro,
    setEmpleadoFiltro,
    oficinaFiltro,
    setOficinaFiltro,
    serialFiltro,
    setSerialFiltro,
    estadoFiltro,
    setEstadoFiltro,
    handlePrint,
  };
};
