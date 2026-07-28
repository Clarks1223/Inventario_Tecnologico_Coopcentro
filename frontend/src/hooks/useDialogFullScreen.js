import { useMediaQuery, useTheme } from '@mui/material';

/**
 * Los formularios largos (empleados, activos, asignaciones) quedan muy
 * estrechos dentro de un Dialog normal en pantallas de teléfono. Este hook
 * indica cuándo conviene abrirlos a pantalla completa.
 */
export const useDialogFullScreen = () => {
  const theme = useTheme();
  return useMediaQuery(theme.breakpoints.down('sm'));
};
