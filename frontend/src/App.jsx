import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ErrorBoundary } from 'react-error-boundary';
import { Box, Typography, Button } from '@mui/material';
import { SnackbarProvider } from './context/SnackbarContext';
import { SessionProvider } from './context/SessionContext';
import Layout from './layout/Layout';
import LoadingSpinner from './components/ui/LoadingSpinner';
import RequireAuth from './components/ui/RequireAuth';

const Dashboard = lazy(() => import('./pages/Dashboard'));
const Oficinas = lazy(() => import('./pages/Oficinas'));
const Cargos = lazy(() => import('./pages/Cargos'));
const Activos = lazy(() => import('./pages/Activos'));
const Empleados = lazy(() => import('./pages/Empleados'));
const Asignaciones = lazy(() => import('./pages/Asignaciones'));
const Login = lazy(() => import('./pages/Login'));
const NotFound = lazy(() => import('./pages/NotFound'));

function ErrorFallback({ error, resetErrorBoundary }) {
  return (
    <Box sx={{ p: 4, textAlign: 'center' }}>
      <Typography variant="h5" gutterBottom>Algo salió mal</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        {error.message}
      </Typography>
      <Button variant="contained" onClick={resetErrorBoundary}>
        Reintentar
      </Button>
    </Box>
  );
}

function App() {
  return (
    <BrowserRouter>
      <SnackbarProvider>
        <SessionProvider>
          <ErrorBoundary FallbackComponent={ErrorFallback}>
            <Suspense fallback={<LoadingSpinner message="Cargando..." />}>
              <Routes>
                <Route path="login" element={<Login />} />
                <Route element={<RequireAuth />}>
                  <Route path="/" element={<Layout />}>
                    <Route index element={<Dashboard />} />
                    <Route path="oficinas" element={<Oficinas />} />
                    <Route path="cargos" element={<Cargos />} />
                    <Route path="empleados" element={<Empleados />} />
                    <Route path="activos" element={<Activos />} />
                    <Route path="asignaciones" element={<Asignaciones />} />
                  </Route>
                </Route>
                <Route path="*" element={<NotFound />} />
              </Routes>
            </Suspense>
          </ErrorBoundary>
        </SessionProvider>
      </SnackbarProvider>
    </BrowserRouter>
  );
}

export default App;
