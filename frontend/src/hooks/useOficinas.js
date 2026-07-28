import { useState, useEffect, useCallback, useMemo } from 'react';
import { oficinasService } from '../services/oficinasService';
import { useSnackbar } from './useSnackbar';
import axios from 'axios';

export const useOficinas = () => {
  const [allOficinas, setAllOficinas] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage] = useState(10);
  const [open, setOpen] = useState(false);
  const [currentOficina, setCurrentOficina] = useState({ nombre: '' });
  const [isEdit, setIsEdit] = useState(false);
  const [showInactivos, setShowInactivos] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState({ open: false, type: '', id: null, message: '' });
  const showSnackbar = useSnackbar();

  const fetchOficinas = useCallback(async (signal) => {
    setIsLoading(true);
    try {
      const response = await oficinasService.getOficinas({ signal });
      setAllOficinas(response.data || []);
    } catch (error) {
      if (!axios.isCancel(error)) console.error(error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    fetchOficinas(controller.signal);
    return () => controller.abort();
  }, [fetchOficinas]);

  const filteredOficinas = useMemo(
    () => allOficinas.filter((o) => o.activo === !showInactivos),
    [allOficinas, showInactivos]
  );

  const oficinas = useMemo(
    () => filteredOficinas.slice(page * rowsPerPage, (page + 1) * rowsPerPage),
    [filteredOficinas, page, rowsPerPage]
  );

  const handleSave = useCallback(async () => {
    try {
      if (isEdit) {
        await oficinasService.updateOficina(currentOficina.id_oficina, currentOficina);
        showSnackbar('Oficina actualizada correctamente', 'success');
      } else {
        await oficinasService.createOficina({ ...currentOficina, activo: true });
        showSnackbar('Oficina registrada exitosamente', 'success');
      }
      fetchOficinas();
      setOpen(false);
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al guardar la oficina', 'error');
    }
  }, [isEdit, currentOficina, fetchOficinas, showSnackbar]);

  const setActivo = useCallback(async (id, activo) => {
    try {
      const oficina = allOficinas.find((o) => o.id_oficina === id);
      if (!oficina) return;
      await oficinasService.updateOficina(id, { ...oficina, activo });
      showSnackbar(activo ? 'Oficina reactivada correctamente' : 'Oficina desactivada correctamente', 'success');
      fetchOficinas();
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al actualizar la oficina', 'error');
    }
  }, [allOficinas, fetchOficinas, showSnackbar]);

  const initiateDeactivate = useCallback((id) => {
    setConfirmDialog({ open: true, type: 'deactivate', id, message: '¿Deseas desactivar esta oficina?' });
  }, []);

  const initiateReactivate = useCallback((id) => {
    setConfirmDialog({ open: true, type: 'reactivate', id, message: '¿Deseas reactivar esta oficina?' });
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

  const openDialog = useCallback((oficina = { nombre: '', direccion: '' }) => {
    setCurrentOficina(oficina);
    setIsEdit(!!oficina.id_oficina);
    setOpen(true);
  }, []);

  return {
    oficinas,
    isLoading,
    page,
    setPage,
    count: filteredOficinas.length,
    rowsPerPage,
    open,
    setOpen,
    currentOficina,
    setCurrentOficina,
    isEdit,
    showInactivos,
    setShowInactivos,
    confirmDialog,
    fetchOficinas,
    handleSave,
    initiateDeactivate,
    initiateReactivate,
    handleConfirmAction,
    handleCloseConfirm,
    openDialog,
  };
};
