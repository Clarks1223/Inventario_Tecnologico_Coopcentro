import { useState, useMemo } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  Typography,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  IconButton,
  Box,
  CssBaseline,
  Button,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/Dashboard';
import BusinessIcon from '@mui/icons-material/Business';
import PeopleIcon from '@mui/icons-material/People';
import DevicesIcon from '@mui/icons-material/Devices';
import AssignmentIcon from '@mui/icons-material/Assignment';
import HistoryIcon from '@mui/icons-material/History';
import WorkIcon from '@mui/icons-material/Work';
import LogoutIcon from '@mui/icons-material/Logout';
import LockResetIcon from '@mui/icons-material/LockReset';
import { useSession } from '../hooks/useSession';
import CambiarContrasenaDialog from '../components/ui/CambiarContrasenaDialog';

const drawerWidth = 240;

const menuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/' },
  { text: 'Oficinas', icon: <BusinessIcon />, path: '/oficinas' },
  { text: 'Cargos', icon: <WorkIcon />, path: '/cargos' },
  { text: 'Empleados', icon: <PeopleIcon />, path: '/empleados' },
  { text: 'Activos', icon: <DevicesIcon />, path: '/activos' },
  { text: 'Asignaciones', icon: <AssignmentIcon />, path: '/asignaciones' },
  { text: 'Historial de Asignaciones', icon: <HistoryIcon />, path: '/historial-asignaciones' },
];

const Layout = () => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [cambioContrasenaAbierto, setCambioContrasenaAbierto] = useState(false);
  const navigate = useNavigate();
  const { sesion, logout } = useSession();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const drawer = useMemo(() => (
    <div>
      <Toolbar>
        <Typography variant="h6" noWrap>
          Inventario IT
        </Typography>
      </Toolbar>
      <List>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              onClick={() => {
                navigate(item.path);
                setMobileOpen(false);
              }}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </div>
  ), [navigate]);

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <Box
        component="a"
        href="#main-content"
        sx={{
          position: 'absolute',
          top: -40,
          left: 0,
          zIndex: 9999,
          bgcolor: 'primary.main',
          color: 'white',
          p: 2,
          textDecoration: 'none',
          '&:focus': { top: 0 },
        }}
      >
        Saltar al contenido principal
      </Box>
      <AppBar
        position="fixed"
        sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="Abrir menú de navegación"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Sistema de Inventario
          </Typography>
          {sesion && (
            <>
              <Typography variant="body2" sx={{ mr: 2, display: { xs: 'none', sm: 'block' } }}>
                {sesion.nombre} {sesion.apellido}
              </Typography>
              <Button
                color="inherit"
                startIcon={<LockResetIcon />}
                onClick={() => setCambioContrasenaAbierto(true)}
              >
                Cambiar contraseña
              </Button>
              <Button
                color="inherit"
                startIcon={<LogoutIcon />}
                onClick={handleLogout}
              >
                Cerrar sesión
              </Button>
            </>
          )}
        </Toolbar>
      </AppBar>
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
        aria-label="Navegación principal"
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
            },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
            },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      <Box
        component="main"
        id="main-content"
        sx={{
          flexGrow: 1,
          p: { xs: 1, sm: 3 },
          width: { sm: `calc(100% - ${drawerWidth}px)` },
        }}
      >
        <Toolbar />
        <Outlet />
      </Box>
      {sesion && (
        <CambiarContrasenaDialog
          open={!!sesion.debe_cambiar_contrasena}
          forced
        />
      )}
      <CambiarContrasenaDialog
        open={cambioContrasenaAbierto}
        onClose={() => setCambioContrasenaAbierto(false)}
      />
    </Box>
  );
};

export default Layout;
