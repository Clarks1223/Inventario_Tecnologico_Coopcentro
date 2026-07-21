import { useState, useEffect, useCallback, useMemo } from 'react';
import * as XLSX from 'xlsx';
import { activosService } from '../services/activosService';
import { oficinasService } from '../services/oficinasService';
import { useSnackbar } from './useSnackbar';
import axios from 'axios';

export const useActivos = () => {
  const [allActivos, setAllActivos] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage] = useState(10);
  const [oficinas, setOficinas] = useState([]);
  const [filters, setFilters] = useState({
    tipo_activo: '',
    marca: '',
    modelo: '',
    serial: '',
    estado: '',
    id_oficina: '',
    ip: '',
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

  useEffect(() => {
    fetchOficinas();
  }, [fetchOficinas]);

  useEffect(() => {
    const controller = new AbortController();
    fetchActivos(controller.signal);
    return () => controller.abort();
  }, [fetchActivos]);

  const oficinaNombre = useCallback((id) => oficinas.find((o) => o.id_oficina === id)?.nombre || '', [oficinas]);

  const filteredActivos = useMemo(() => {
    return allActivos
      .filter((a) => !filters.tipo_activo || a.tipo_activo === filters.tipo_activo)
      .filter((a) => !filters.marca || a.marca === filters.marca)
      .filter((a) => !filters.modelo || a.modelo?.toLowerCase().includes(filters.modelo.toLowerCase()))
      .filter((a) => !filters.serial || a.serial?.toLowerCase().includes(filters.serial.toLowerCase()))
      .filter((a) => !filters.estado || a.estado === filters.estado)
      .filter((a) => !filters.id_oficina || a.id_oficina === filters.id_oficina)
      .filter((a) => !filters.ip || a.detalle?.ip?.includes(filters.ip));
  }, [allActivos, filters]);

  const activos = useMemo(
    () => filteredActivos.slice(page * rowsPerPage, (page + 1) * rowsPerPage)
      .map((a) => ({ ...a, oficina_nombre: oficinaNombre(a.id_oficina) })),
    [filteredActivos, page, rowsPerPage, oficinaNombre]
  );

  const handleExportExcel = useCallback(() => {
    if (filteredActivos.length === 0) {
      showSnackbar('No hay datos para exportar', 'warning');
      return;
    }

    const rows = filteredActivos.map((row) => ({
      Código: row.codigo_inventario,
      Tipo: row.tipo_activo,
      Marca: row.marca,
      Modelo: row.modelo,
      Serial: row.serial,
      Estado: row.estado,
      Oficina: oficinaNombre(row.id_oficina),
      IP: row.detalle?.ip || '',
    }));

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
    handleExportExcel,
  };
};
