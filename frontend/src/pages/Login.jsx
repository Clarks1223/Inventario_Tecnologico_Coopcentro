import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Link,
} from '@mui/material';
import { useSession } from '../hooks/useSession';
import { useSnackbar } from '../hooks/useSnackbar';
import SolicitarRecuperacionDialog from '../components/ui/SolicitarRecuperacionDialog';

const Login = () => {
  const [correo, setCorreo] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [recuperacionAbierta, setRecuperacionAbierta] = useState(false);
  const session = useSession();
  const showSnackbar = useSnackbar();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const sesion = await session.login(correo, contrasena);
      showSnackbar(`Bienvenido, ${sesion.nombre} ${sesion.apellido}`, 'success');
      navigate('/');
    } catch (error) {
      showSnackbar(
        error.response?.data?.message || 'Usuario o contraseña incorrectos',
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
      }}
    >
      <Paper sx={{ p: 4, width: 360 }} component="form" onSubmit={handleSubmit}>
        <Typography variant="h5" gutterBottom textAlign="center">
          Sistema de Inventario
        </Typography>
        <Typography variant="body2" color="text.secondary" textAlign="center" sx={{ mb: 3 }}>
          Inicia sesión con tu cuenta de TI
        </Typography>
        <TextField
          label="Correo"
          type="email"
          fullWidth
          margin="normal"
          value={correo}
          onChange={(e) => setCorreo(e.target.value)}
          autoFocus
          required
        />
        <TextField
          label="Contraseña"
          type="password"
          fullWidth
          margin="normal"
          value={contrasena}
          onChange={(e) => setContrasena(e.target.value)}
          required
        />
        <Button
          type="submit"
          variant="contained"
          fullWidth
          sx={{ mt: 3 }}
          disabled={isLoading}
        >
          {isLoading ? 'Ingresando...' : 'Iniciar sesión'}
        </Button>
        <Typography textAlign="center" sx={{ mt: 2 }}>
          <Link
            component="button"
            type="button"
            variant="body2"
            onClick={() => setRecuperacionAbierta(true)}
          >
            ¿Olvidaste tu contraseña?
          </Link>
        </Typography>
      </Paper>
      <SolicitarRecuperacionDialog
        open={recuperacionAbierta}
        onClose={() => setRecuperacionAbierta(false)}
      />
    </Box>
  );
};

export default Login;
