import { useState, useEffect, useCallback, useMemo } from 'react';
import { cargosService } from '../services/cargosService';
import { useSnackbar } from './useSnackbar';
import axios from 'axios';

export const useCargos = () => {
  const [allCargos, setAllCargos] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage] = useState(10);
  const [open, setOpen] = useState(false);
  const [currentCargo, setCurrentCargo] = useState({ nombre: '' });
  const [isEdit, setIsEdit] = useState(false);
  const [showInactivos, setShowInactivos] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState({ open: false, type: '', id: null, message: '' });
  const showSnackbar = useSnackbar();

  const fetchCargos = useCallback(async (signal) => {
    setIsLoading(true);
    try {
      const response = await cargosService.getCargos({ signal });
      setAllCargos(response.data || []);
    } catch (error) {
      if (!axios.isCancel(error)) console.error(error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    fetchCargos(controller.signal);
    return () => controller.abort();
  }, [fetchCargos]);

  const filteredCargos = useMemo(
    () => allCargos.filter((c) => c.activo === !showInactivos),
    [allCargos, showInactivos]
  );

  const cargos = useMemo(
    () => filteredCargos.slice(page * rowsPerPage, (page + 1) * rowsPerPage),
    [filteredCargos, page, rowsPerPage]
  );

  const handleSave = useCallback(async () => {
    try {
      if (isEdit) {
        await cargosService.updateCargo(currentCargo.id_cargo, currentCargo);
      } else {
        await cargosService.createCargo({ ...currentCargo, activo: true });
      }
      fetchCargos();
      setOpen(false);
    } catch (error) {
      const msg = error.response?.data?.nombre || error.message;
      showSnackbar('Error al guardar: ' + msg, 'error');
    }
  }, [isEdit, currentCargo, fetchCargos, showSnackbar]);

  const setActivo = useCallback(async (id, activo) => {
    try {
      const cargo = allCargos.find((c) => c.id_cargo === id);
      if (!cargo) return;
      await cargosService.updateCargo(id, { ...cargo, activo });
      fetchCargos();
    } catch (error) {
      showSnackbar(
        `Error al ${activo ? 'reactivar' : 'desactivar'} cargo: ` + (error.response?.data?.nombre || error.message),
        'error'
      );
    }
  }, [allCargos, fetchCargos, showSnackbar]);

  const initiateDeactivate = useCallback((id) => {
    setConfirmDialog({ open: true, type: 'deactivate', id, message: '¿Deseas desactivar este cargo?' });
  }, []);

  const initiateReactivate = useCallback((id) => {
    setConfirmDialog({ open: true, type: 'reactivate', id, message: '¿Deseas reactivar este cargo?' });
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

  const openDialog = useCallback((cargo = { nombre: '' }) => {
    setCurrentCargo(cargo);
    setIsEdit(!!cargo.id_cargo);
    setOpen(true);
  }, []);

  return {
    cargos,
    isLoading,
    page,
    setPage,
    count: filteredCargos.length,
    rowsPerPage,
    open,
    setOpen,
    currentCargo,
    setCurrentCargo,
    isEdit,
    showInactivos,
    setShowInactivos,
    confirmDialog,
    fetchCargos,
    handleSave,
    initiateDeactivate,
    initiateReactivate,
    handleConfirmAction,
    handleCloseConfirm,
    openDialog,
  };
};
