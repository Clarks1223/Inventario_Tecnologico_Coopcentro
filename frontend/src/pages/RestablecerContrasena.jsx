import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
} from '@mui/material';
import { useSnackbar } from '../hooks/useSnackbar';
import { authService } from '../services/authService';

const RestablecerContrasena = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const [contrasenaNueva, setContrasenaNueva] = useState('');
  const [confirmarContrasenaNueva, setConfirmarContrasenaNueva] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const showSnackbar = useSnackbar();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!token) {
      showSnackbar('El enlace de recuperación no es válido', 'error');
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

    setIsLoading(true);
    try {
      await authService.restablecerContrasena({ token, contrasenaNueva, confirmarContrasenaNueva });
      showSnackbar('Contraseña actualizada correctamente, ya puedes iniciar sesión', 'success');
      navigate('/login');
    } catch (error) {
      showSnackbar(
        error.response?.data?.message || 'No se pudo restablecer la contraseña, intenta nuevamente',
        'error'
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        px: 2,
      }}
    >
      <Paper sx={{ p: 4, width: '100%', maxWidth: 360 }} component="form" onSubmit={handleSubmit}>
        <Typography variant="h5" gutterBottom textAlign="center">
          Restablecer contraseña
        </Typography>
        <Typography variant="body2" color="text.secondary" textAlign="center" sx={{ mb: 3 }}>
          Ingresa tu nueva contraseña
        </Typography>
        <TextField
          label="Nueva contraseña"
          type="password"
          fullWidth
          margin="normal"
          value={contrasenaNueva}
          onChange={(e) => setContrasenaNueva(e.target.value)}
          autoFocus
          required
          helperText="Mínimo 8 caracteres"
        />
        <TextField
          label="Repetir nueva contraseña"
          type="password"
          fullWidth
          margin="normal"
          value={confirmarContrasenaNueva}
          onChange={(e) => setConfirmarContrasenaNueva(e.target.value)}
          required
        />
        <Button
          type="submit"
          variant="contained"
          fullWidth
          sx={{ mt: 3 }}
          disabled={isLoading}
        >
          {isLoading ? 'Guardando...' : 'Restablecer contraseña'}
        </Button>
      </Paper>
    </Box>
  );
};

export default RestablecerContrasena;
