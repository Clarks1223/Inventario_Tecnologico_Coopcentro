import { useState, useCallback } from 'react';

const initialState = { open: false, type: '', id: null, message: '' };

export const useConfirmDialog = () => {
  const [confirmDialog, setConfirmDialog] = useState(initialState);

  const openConfirm = useCallback((type, id, message) => {
    setConfirmDialog({ open: true, type, id, message });
  }, []);

  const closeConfirm = useCallback(() => {
    setConfirmDialog(initialState);
  }, []);

  return {
    confirmDialog,
    openConfirm,
    closeConfirm,
    setConfirmDialog,
  };
};
