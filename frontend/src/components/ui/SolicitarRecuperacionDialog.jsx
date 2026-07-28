import { useState, useId } from 'react';
import PropTypes from 'prop-types';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Typography,
} from '@mui/material';
import { useSnackbar } from '../../hooks/useSnackbar';
import { authService } from '../../services/authService';

const SolicitarRecuperacionDialog = ({ open, onClose }) => {
  const showSnackbar = useSnackbar();
  const titleId = useId();
  const [correo, setCorreo] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleClose = () => {
    if (isLoading) return;
    setCorreo('');
    onClose?.();
  };

  const handleSubmit = async () => {
    if (!correo) {
      showSnackbar('Ingresa tu correo', 'error');
      return;
    }

    setIsLoading(true);
    try {
      await authService.solicitarRecuperacion(correo);
      showSnackbar(
        'Si el correo esta registrado, recibiras un enlace para restablecer tu contraseña',
        'success'
      );
      setCorreo('');
      onClose?.();
    } catch (error) {
      showSnackbar(
        error.response?.data?.message || 'No se pudo enviar la solicitud, intenta nuevamente',
        'error'
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth aria-labelledby={titleId}>
      <DialogTitle id={titleId}>Recuperar contraseña</DialogTitle>
      <DialogContent>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Ingresa el correo de tu cuenta de TI. Si está registrado, te enviaremos un enlace para
          establecer una nueva contraseña.
        </Typography>
        <TextField
          label="Correo"
          type="email"
          value={correo}
          onChange={(e) => setCorreo(e.target.value)}
          required
          fullWidth
          autoFocus
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={isLoading}>
          Cancelar
        </Button>
        <Button onClick={handleSubmit} variant="contained" disabled={isLoading}>
          Enviar enlace
        </Button>
      </DialogActions>
    </Dialog>
  );
};

SolicitarRecuperacionDialog.propTypes = {
  open: PropTypes.bool.isRequired,
  onClose: PropTypes.func,
};

export default SolicitarRecuperacionDialog;
