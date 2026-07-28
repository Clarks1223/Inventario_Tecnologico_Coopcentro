import { useState, useId } from 'react';
import PropTypes from 'prop-types';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Stack,
  Typography,
} from '@mui/material';
import { useDialogFullScreen } from '../../hooks/useDialogFullScreen';
import { useSession } from '../../hooks/useSession';
import { useSnackbar } from '../../hooks/useSnackbar';
import { usuariosTiService } from '../../services/usuariosTiService';

const CambiarContrasenaDialog = ({ open, forced = false, onClose }) => {
  const { sesion, updateSesion } = useSession();
  const showSnackbar = useSnackbar();
  const titleId = useId();
  const dialogoPantallaCompleta = useDialogFullScreen();
  const [contrasenaActual, setContrasenaActual] = useState('');
  const [contrasenaNueva, setContrasenaNueva] = useState('');
  const [confirmarContrasenaNueva, setConfirmarContrasenaNueva] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const limpiar = () => {
    setContrasenaActual('');
    setContrasenaNueva('');
    setConfirmarContrasenaNueva('');
  };

  const handleClose = () => {
    if (forced || isLoading) return;
    limpiar();
    onClose?.();
  };

  const handleSubmit = async () => {
    if (!contrasenaActual || !contrasenaNueva || !confirmarContrasenaNueva) {
      showSnackbar('Completa los 3 campos', 'error');
      return;
    }
    if (contrasenaNueva !== confirmarContrasenaNueva) {
      showSnackbar('La nueva contraseña y su confirmación no coinciden', 'error');
      return;
    }
    if (contrasenaNueva.length < 8) {
      showSnackbar('La nueva contraseña debe tener al menos 8 caracteres', 'error');
      return;
    }
    if (contrasenaNueva === contrasenaActual) {
      showSnackbar('La nueva contraseña debe ser diferente a la actual', 'error');
      return;
    }

    setIsLoading(true);
    try {
      await usuariosTiService.cambiarContrasena(sesion.id_usuario_ti, {
        contrasenaActual,
        contrasenaNueva,
        confirmarContrasenaNueva,
      });
      updateSesion({ debe_cambiar_contrasena: false });
      showSnackbar('Contraseña actualizada correctamente', 'success');
      limpiar();
      onClose?.();
    } catch (error) {
      showSnackbar(
        'Error al cambiar la contraseña: ' + (error.response?.data?.message || error.message),
        'error'
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      fullScreen={dialogoPantallaCompleta}
      maxWidth="sm"
      fullWidth
      aria-labelledby={titleId}
      disableEscapeKeyDown={forced}
    >
      <DialogTitle id={titleId}>Cambiar contraseña</DialogTitle>
      <DialogContent>
        {forced && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Debes actualizar tu contraseña antes de continuar.
          </Typography>
        )}
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            label="Contraseña actual"
            type="password"
            value={contrasenaActual}
            onChange={(e) => setContrasenaActual(e.target.value)}
            required
            fullWidth
            autoFocus
          />
          <TextField
            label="Nueva contraseña"
            type="password"
            value={contrasenaNueva}
            onChange={(e) => setContrasenaNueva(e.target.value)}
            required
            fullWidth
            helperText="Mínimo 8 caracteres, distinta a la actual"
          />
          <TextField
            label="Repetir nueva contraseña"
            type="password"
            value={confirmarContrasenaNueva}
            onChange={(e) => setConfirmarContrasenaNueva(e.target.value)}
            required
            fullWidth
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        {!forced && (
          <Button onClick={handleClose} disabled={isLoading}>
            Cancelar
          </Button>
        )}
        <Button onClick={handleSubmit} variant="contained" disabled={isLoading}>
          Guardar
        </Button>
      </DialogActions>
    </Dialog>
  );
};

CambiarContrasenaDialog.propTypes = {
  open: PropTypes.bool.isRequired,
  forced: PropTypes.bool,
  onClose: PropTypes.func,
};

export default CambiarContrasenaDialog;
