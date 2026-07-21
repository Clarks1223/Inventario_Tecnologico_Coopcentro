import { useState, useCallback } from 'react';
import { activosService } from '../services/activosService';
import { useSnackbar } from './useSnackbar';

const emptyBaseData = {
  codigo_inventario: '',
  tipo_activo: 'periferico',
  marca: '',
  modelo: '',
  serial: '',
  estado: 'NO_ASIGNADO',
  id_oficina: '',
  observaciones: '',
};

export const useActivoEditor = (onSaveSuccess) => {
  const [open, setOpen] = useState(false);
  const [tipo, setTipo] = useState('periferico');
  const [baseData, setBaseData] = useState(emptyBaseData);
  const [detailData, setDetailData] = useState({});

  const [isEditing, setIsEditing] = useState(false);
  const [editId, setEditId] = useState(null);

  const showSnackbar = useSnackbar();

  const resetForm = useCallback(() => {
    setBaseData(emptyBaseData);
    setDetailData({});
    setTipo('periferico');
    setIsEditing(false);
    setEditId(null);
  }, []);

  const handleSave = useCallback(async () => {
    const payload = { ...baseData, tipo_activo: tipo };
    payload.detalle = { ...detailData, id_activo: editId || 0 };

    try {
      if (isEditing) {
        await activosService.updateActivo(editId, payload);
        showSnackbar('Activo actualizado correctamente', 'success');
      } else {
        await activosService.createActivo(payload);
        showSnackbar('Activo creado correctamente', 'success');
      }
      setOpen(false);
      resetForm();
      if (onSaveSuccess) onSaveSuccess();
    } catch (error) {
      let errorMessage = 'Error al guardar el activo.';

      const extractErrorMessage = (data) => {
        if (typeof data === 'string') return data;
        if (Array.isArray(data)) return extractErrorMessage(data[0]);
        if (typeof data === 'object' && data !== null) {
          const values = Object.values(data);
          if (values.length > 0) return extractErrorMessage(values[0]);
        }
        return 'Error desconocido del servidor.';
      };

      if (error.response?.data) {
        errorMessage = extractErrorMessage(error.response.data);
      } else if (error.message) {
        errorMessage = error.message;
      }

      showSnackbar(errorMessage, 'error');
    }
  }, [baseData, tipo, detailData, isEditing, editId, onSaveSuccess, resetForm, showSnackbar]);

  const handleEdit = useCallback(async (row) => {
    setIsEditing(true);
    setEditId(row.id_activo);
    setTipo(row.tipo_activo);

    setBaseData({
      codigo_inventario: row.codigo_inventario,
      tipo_activo: row.tipo_activo,
      marca: row.marca,
      modelo: row.modelo,
      serial: row.serial,
      estado: row.estado,
      id_oficina: row.id_oficina || '',
      observaciones: row.observaciones || '',
    });
    setDetailData(row.detalle || {});
    setOpen(true);

    try {
      const response = await activosService.getActivo(row.id_activo);
      setDetailData(response.data.detalle || {});
    } catch (error) {
      console.error('Error al obtener el detalle del activo', error);
    }
  }, []);

  const handleOpenNew = useCallback(() => {
    resetForm();
    setOpen(true);
  }, [resetForm]);

  return {
    open,
    setOpen,
    tipo,
    setTipo,
    baseData,
    setBaseData,
    detailData,
    setDetailData,
    isEditing,
    handleSave,
    handleEdit,
    handleOpenNew,
  };
};
