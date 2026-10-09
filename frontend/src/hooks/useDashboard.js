import { useEffect, useMemo, useState } from 'react';
import { dashboardService } from '../services/dashboardService';
import { oficinasService } from '../services/oficinasService';
import { empleadosService } from '../services/empleadosService';
import { asignacionesService } from '../services/asignacionesService';
import { enriquecerActivos, calcularResumen } from '../utils/dashboardStats';

export const useDashboard = (incluirFueraDeServicio = false) => {
  const [datos, setDatos] = useState({ activos: [], oficinas: [], empleados: [], actas: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const controller = new AbortController();
    const cargar = async () => {
      try {
        const config = { signal: controller.signal };
        const [activos, oficinas, empleados, actas] = await Promise.all([
          dashboardService.getActivos(config),
          oficinasService.getOficinas(config),
          empleadosService.getEmpleados(config),
          asignacionesService.getAsignaciones(config),
        ]);
        setDatos({
          activos: activos.data || [],
          oficinas: oficinas.data || [],
          empleados: empleados.data || [],
          actas: actas.data || [],
        });
      } catch (err) {
        if (controller.signal.aborted) return;
        console.error('Error al cargar el dashboard', err);
        setError('No se pudo cargar la información del dashboard.');
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    };
    cargar();
    return () => controller.abort();
  }, []);

  const activos = useMemo(
    () => enriquecerActivos(datos.activos, datos.oficinas, datos.empleados, datos.actas),
    [datos]
  );
  const resumen = useMemo(
    () => calcularResumen(activos, datos.oficinas, incluirFueraDeServicio),
    [activos, datos.oficinas, incluirFueraDeServicio]
  );

  return { activos, oficinas: datos.oficinas, resumen, loading, error };
};
