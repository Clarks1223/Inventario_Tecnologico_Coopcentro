import { useState, useEffect, useCallback, useMemo } from 'react';
import * as XLSX from 'xlsx';
import { empleadosService } from '../services/empleadosService';
import { usuariosTiService } from '../services/usuariosTiService';
import { useSnackbar } from './useSnackbar';
import axios from 'axios';

const emptyEmpleado = {
  nombre: '',
  apellido: '',
  cedula: '',
  correo: '',
  extension_telefonica: '',
  id_cargo: '',
  id_oficina: '',
  rol: 'usuario',
};

export const useEmpleados = () => {
  const [allEmpleados, setAllEmpleados] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage] = useState(10);
  const [oficinas, setOficinas] = useState([]);
  const [cargos, setCargos] = useState([]);
  const [usuariosTi, setUsuariosTi] = useState([]);
  const [open, setOpen] = useState(false);
  const [currentEmpleado, setCurrentEmpleado] = useState(emptyEmpleado);
  const [isEdit, setIsEdit] = useState(false);

  const [filters, setFilters] = useState({
    nombre: '',
    cedula: '',
    correo: '',
    extension_telefonica: '',
    id_cargo: '',
    id_oficina: '',
  });
  const [showInactivos, setShowInactivos] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState({ open: false, type: '', id: null, message: '' });
  const showSnackbar = useSnackbar();

  const fetchEmpleados = useCallback(async (signal) => {
    setIsLoading(true);
    try {
      const response = await empleadosService.getEmpleados({ signal });
      setAllEmpleados(response.data || []);
    } catch (error) {
      if (!axios.isCancel(error)) console.error(error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const fetchOficinas = useCallback(async () => {
    try {
      const response = await empleadosService.getOficinas();
      setOficinas(response.data || []);
    } catch (error) {
      console.error(error);
    }
  }, []);

  const fetchCargos = useCallback(async () => {
    try {
      const response = await empleadosService.getCargos();
      setCargos(response.data || []);
    } catch (error) {
      console.error(error);
    }
  }, []);

  const fetchUsuariosTi = useCallback(async () => {
    try {
      const response = await usuariosTiService.getUsuariosTi();
      setUsuariosTi(response.data || []);
    } catch (error) {
      console.error(error);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    fetchEmpleados(controller.signal);
    return () => controller.abort();
  }, [fetchEmpleados]);

  useEffect(() => {
    fetchOficinas();
    fetchCargos();
    fetchUsuariosTi();
  }, [fetchOficinas, fetchCargos, fetchUsuariosTi]);

  const cargoNombre = useCallback((id) => cargos.find((c) => c.id_cargo === id)?.nombre || '', [cargos]);
  const oficinaNombre = useCallback((id) => oficinas.find((o) => o.id_oficina === id)?.nombre || '', [oficinas]);
  const usuarioTiDeEmpleado = useCallback(
    (idEmpleado) => usuariosTi.find((u) => u.id_empleado === idEmpleado),
    [usuariosTi]
  );

  const filteredEmpleados = useMemo(() => {
    return allEmpleados
      .filter((e) => e.activo === !showInactivos)
      .filter((e) => !filters.nombre || `${e.nombre} ${e.apellido}`.toLowerCase().includes(filters.nombre.toLowerCase()))
      .filter((e) => !filters.cedula || e.cedula?.includes(filters.cedula))
      .filter((e) => !filters.correo || e.correo?.toLowerCase().includes(filters.correo.toLowerCase()))
      .filter((e) => !filters.extension_telefonica || e.extension_telefonica?.includes(filters.extension_telefonica))
      .filter((e) => !filters.id_cargo || cargoNombre(e.id_cargo).toLowerCase().includes(filters.id_cargo.toLowerCase()))
      .filter((e) => !filters.id_oficina || e.id_oficina === filters.id_oficina);
  }, [allEmpleados, showInactivos, filters, cargoNombre]);

  const empleados = useMemo(
    () => filteredEmpleados.slice(page * rowsPerPage, (page + 1) * rowsPerPage)
      .map((e) => ({ ...e, nombre_cargo: cargoNombre(e.id_cargo), nombre_oficina: oficinaNombre(e.id_oficina) })),
    [filteredEmpleados, page, rowsPerPage, cargoNombre, oficinaNombre]
  );

  const handleSave = useCallback(async () => {
    try {
      let idEmpleado = currentEmpleado.id_empleado;
      if (isEdit) {
        await empleadosService.updateEmpleado(idEmpleado, currentEmpleado);
      } else {
        const creado = await empleadosService.createEmpleado({ ...currentEmpleado, activo: true });
        idEmpleado = creado.data.id_empleado;
      }

      const usuarioTiActual = usuarioTiDeEmpleado(idEmpleado);
      if (currentEmpleado.rol === 'administrador' && !usuarioTiActual) {
        try {
          await usuariosTiService.createUsuarioTi({ id_empleado: idEmpleado, correo: currentEmpleado.correo });
        } catch (accesoError) {
          showSnackbar(
            'Empleado guardado, pero no se pudo otorgar acceso a la plataforma: ' +
              (accesoError.response?.data?.message || accesoError.message),
            'warning'
          );
        }
      } else if (currentEmpleado.rol !== 'administrador' && usuarioTiActual) {
        try {
          await usuariosTiService.deleteUsuarioTi(usuarioTiActual.id_usuario_ti);
        } catch (accesoError) {
          showSnackbar(
            'Empleado guardado, pero no se pudo revocar el acceso a la plataforma: ' +
              (accesoError.response?.data?.message || accesoError.message),
            'warning'
          );
        }
      }

      fetchEmpleados();
      fetchUsuariosTi();
      setOpen(false);
      showSnackbar(isEdit ? 'Empleado actualizado correctamente' : 'Empleado registrado exitosamente', 'success');
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al guardar el empleado', 'error');
    }
  }, [isEdit, currentEmpleado, usuarioTiDeEmpleado, fetchEmpleados, fetchUsuariosTi, showSnackbar]);

  const setActivo = useCallback(async (id, activo) => {
    try {
      const empleado = allEmpleados.find((e) => e.id_empleado === id);
      if (!empleado) return;
      await empleadosService.updateEmpleado(id, { ...empleado, activo });
      showSnackbar(activo ? 'Empleado reactivado correctamente' : 'Empleado desactivado correctamente', 'success');
      fetchEmpleados();
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al actualizar el empleado', 'error');
    }
  }, [allEmpleados, fetchEmpleados, showSnackbar]);

  const initiateDeactivate = useCallback((id) => {
    setConfirmDialog({ open: true, type: 'deactivate', id, message: '¿Deseas desactivar este empleado?' });
  }, []);

  const initiateReactivate = useCallback((id) => {
    setConfirmDialog({ open: true, type: 'reactivate', id, message: '¿Deseas reactivar este empleado?' });
  }, []);

  const handleConfirmAction = useCallback(() => {
    const { type, id } = confirmDialog;
    setConfirmDialog({ open: false, type: '', id: null, message: '' });
    if (type === 'deactivate') setActivo(id, false);
    else if (type === 'reactivate') setActivo(id, true);
  }, [confirmDialog, setActivo]);

  const handleCloseConfirm = useCallback(() => {
    setConfirmDialog({ open: false, type: '', id: null, message: '' });
  }, []);

  const openDialog = useCallback((empleado = emptyEmpleado) => {
    setCurrentEmpleado(empleado);
    setIsEdit(!!empleado.id_empleado);
    setOpen(true);
  }, []);

  const handleExportCsv = useCallback(() => {
    if (filteredEmpleados.length === 0) {
      showSnackbar('No hay datos para exportar', 'warning');
      return;
    }

    const csvRows = [];
    const headers = ['Nombre Completo', 'Cédula', 'Correo', 'Extensión', 'Cargo', 'Oficina', 'Está Activo'];
    csvRows.push(headers.join(','));

    filteredEmpleados.forEach((row) => {
      const nombreCompleto = `${row.nombre || ''} ${row.apellido || ''}`.trim().replace(/,/g, '');
      const cedula = (row.cedula || '').replace(/,/g, '');
      const correo = (row.correo || '').replace(/,/g, '');
      const extension = (row.extension_telefonica || '').replace(/,/g, '');
      const cargo = cargoNombre(row.id_cargo).replace(/,/g, '');
      const oficina = oficinaNombre(row.id_oficina).replace(/,/g, '');
      const activo = row.activo ? 'Sí' : 'No';

      csvRows.push([nombreCompleto, cedula, correo, extension, cargo, oficina, activo].join(','));
    });

    const blob = new Blob([`\uFEFF${csvRows.join('\n')}`], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `Empleados_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }, [filteredEmpleados, cargoNombre, oficinaNombre, showSnackbar]);

  const handleExportExcel = useCallback(() => {
    if (filteredEmpleados.length === 0) {
      showSnackbar('No hay datos para exportar', 'warning');
      return;
    }

    const rows = filteredEmpleados.map((row) => ({
      ID: row.id_empleado,
      'Nombre Completo': `${row.nombre || ''} ${row.apellido || ''}`.trim(),
      Cédula: row.cedula || '',
      Correo: row.correo || '',
      Extensión: row.extension_telefonica || '',
      Cargo: cargoNombre(row.id_cargo),
      Oficina: oficinaNombre(row.id_oficina),
      'Está Activo': row.activo ? 'Sí' : 'No',
    }));

    const worksheet = XLSX.utils.json_to_sheet(rows);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Empleados');
    XLSX.writeFile(workbook, `Empleados_${new Date().toISOString().split('T')[0]}.xlsx`);
  }, [filteredEmpleados, cargoNombre, oficinaNombre, showSnackbar]);

  return {
    empleados,
    isLoading,
    page,
    setPage,
    count: filteredEmpleados.length,
    rowsPerPage,
    oficinas,
    cargos,
    usuarioTiDeEmpleado,
    open,
    setOpen,
    currentEmpleado,
    setCurrentEmpleado,
    isEdit,
    filters,
    setFilters,
    showInactivos,
    setShowInactivos,
    confirmDialog,
    fetchEmpleados,
    handleSave,
    initiateDeactivate,
    initiateReactivate,
    handleConfirmAction,
    handleCloseConfirm,
    openDialog,
    handleExportCsv,
    handleExportExcel,
  };
};
