import { useState, useEffect, useCallback, useMemo } from 'react';
import * as XLSX from 'xlsx';
import { activosService } from '../services/activosService';
import { oficinasService } from '../services/oficinasService';
import { empleadosService } from '../services/empleadosService';
import { asignacionesService } from '../services/asignacionesService';
import { useSnackbar } from './useSnackbar';
import axios from 'axios';

export const useActivos = () => {
  const [allActivos, setAllActivos] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage] = useState(10);
  const [oficinas, setOficinas] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [asignaciones, setAsignaciones] = useState([]);
  const [filters, setFilters] = useState({
    tipo_activo: '',
    marca: '',
    modelo: '',
    serial: '',
    estado: '',
    id_oficina: '',
    ip: '',
    dominio: '',
  });
  const showSnackbar = useSnackbar();

  const fetchActivos = useCallback(async (signal) => {
    setIsLoading(true);
    try {
      const response = await activosService.getActivos({ signal });
      setAllActivos(response.data || []);
    } catch (error) {
      if (!axios.isCancel(error)) console.error(error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const fetchOficinas = useCallback(async () => {
    try {
      const response = await oficinasService.getOficinas();
      setOficinas(response.data || []);
    } catch (error) {
      console.error(error);
    }
  }, []);

  const fetchEmpleados = useCallback(async () => {
    try {
      const response = await empleadosService.getEmpleados();
      setEmpleados(response.data || []);
    } catch (error) {
      console.error(error);
    }
  }, []);

  const fetchAsignaciones = useCallback(async () => {
    try {
      const response = await asignacionesService.getAsignaciones();
      setAsignaciones(response.data || []);
    } catch (error) {
      console.error(error);
    }
  }, []);

  useEffect(() => {
    fetchOficinas();
    fetchEmpleados();
    fetchAsignaciones();
  }, [fetchOficinas, fetchEmpleados, fetchAsignaciones]);

  useEffect(() => {
    const controller = new AbortController();
    fetchActivos(controller.signal);
    return () => controller.abort();
  }, [fetchActivos]);

  const oficinaNombre = useCallback((id) => oficinas.find((o) => o.id_oficina === id)?.nombre || '', [oficinas]);

  const empleadoAsignadoNombre = useCallback((idActivo) => {
    const acta = asignaciones.find(
      (a) => a.id_activo === idActivo && a.estado_asignacion === 'activa'
    );
    if (!acta) return '';
    const empleado = empleados.find((e) => e.id_empleado === acta.id_empleado);
    return empleado ? `${empleado.nombre} ${empleado.apellido}` : '';
  }, [asignaciones, empleados]);

  const filteredActivos = useMemo(() => {
    return allActivos
      .filter((a) => !filters.tipo_activo || a.tipo_activo === filters.tipo_activo)
      .filter((a) => !filters.marca || a.marca?.toLowerCase().includes(filters.marca.toLowerCase()))
      .filter((a) => !filters.modelo || a.modelo?.toLowerCase().includes(filters.modelo.toLowerCase()))
      .filter((a) => !filters.serial || a.serial?.toLowerCase().includes(filters.serial.toLowerCase()))
      .filter((a) => !filters.estado || a.estado === filters.estado)
      .filter((a) => !filters.id_oficina || a.id_oficina === filters.id_oficina)
      .filter((a) => !filters.ip || a.detalle?.ip?.includes(filters.ip))
      .filter((a) => !filters.dominio || a.detalle?.dominio?.toLowerCase().includes(filters.dominio.toLowerCase()));
  }, [allActivos, filters]);

  const activos = useMemo(
    () => filteredActivos.slice(page * rowsPerPage, (page + 1) * rowsPerPage)
      .map((a) => ({
        ...a,
        oficina_nombre: oficinaNombre(a.id_oficina),
        empleado_asignado: empleadoAsignadoNombre(a.id_activo),
      })),
    [filteredActivos, page, rowsPerPage, oficinaNombre, empleadoAsignadoNombre]
  );

  const handleExportExcel = useCallback(() => {
    if (filteredActivos.length === 0) {
      showSnackbar('No hay datos para exportar', 'warning');
      return;
    }

    const rows = filteredActivos.map((row) => {
      const detalle = row.detalle || {};
      return {
        Código: row.codigo_inventario || '',
        Tipo: row.tipo_activo || '',
        Marca: row.marca || '',
        Modelo: row.modelo || '',
        Serial: row.serial || '',
        Estado: row.estado || '',
        Oficina: oficinaNombre(row.id_oficina) || '',
        Observaciones: row.observaciones || '',
        'Tipo Conexión': detalle.tipo_conexion || '',
        'Estado Batería': detalle.estado_bateria || '',
        'Modelo Cabezal': detalle.modelo_cabezal || '',
        'Tipo Dispositivo': detalle.tipo_dispositivo || '',
        'Sistema Operativo': detalle.sistema_operativo || '',
        IMEI: detalle.imei || '',
        'Número de Línea': detalle.numero_linea || '',
        Procesador: detalle.procesador || '',
        'RAM (GB)': detalle.ram_gb ?? '',
        'Tipo Almacenamiento': detalle.tipo_almacenamiento || '',
        'Almacenamiento (GB)': detalle.almacenamiento_gb ?? '',
        IP: detalle.ip || '',
        Dominio: detalle.dominio || '',
        'Fecha Creación': row.created_at || '',
        'Última Actualización': row.updated_at || '',
      };
    });

    const worksheet = XLSX.utils.json_to_sheet(rows);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Activos');
    XLSX.writeFile(workbook, `Activos_${new Date().toISOString().split('T')[0]}.xlsx`);
  }, [filteredActivos, oficinaNombre, showSnackbar]);

  const handleDecommission = useCallback(async (id) => {
    try {
      const activo = allActivos.find((a) => a.id_activo === id);
      if (!activo) return;
      await activosService.updateActivo(id, { ...activo, estado: 'DADO_DE_BAJA' });
      fetchActivos();
      showSnackbar('Activo dado de baja correctamente.', 'success');
    } catch (error) {
      const msg = error.response?.data?.error || 'Error al dar de baja';
      showSnackbar(msg, 'error');
    }
  }, [allActivos, fetchActivos, showSnackbar]);

  const handleReportStolen = useCallback(async (id) => {
    try {
      const activo = allActivos.find((a) => a.id_activo === id);
      if (!activo) return;
      await activosService.updateActivo(id, { ...activo, estado: 'ROBADO_PERDIDO' });
      fetchActivos();
      showSnackbar('Activo reportado como robado/perdido.', 'success');
    } catch (error) {
      const msg = error.response?.data?.error || 'Error al reportar el activo';
      showSnackbar(msg, 'error');
    }
  }, [allActivos, fetchActivos, showSnackbar]);

  return {
    activos,
    isLoading,
    page,
    setPage,
    count: filteredActivos.length,
    rowsPerPage,
    oficinas,
    filters,
    setFilters,
    fetchActivos,
    handleDecommission,
    handleReportStolen,
    handleExportExcel,
  };
};
