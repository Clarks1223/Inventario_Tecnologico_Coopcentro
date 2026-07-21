import { useEffect, useState } from 'react';
import { dashboardService } from '../services/dashboardService';

const groupByCount = (activos, key) => {
  const counts = activos.reduce((acc, activo) => {
    const value = activo[key] || 'Sin especificar';
    acc[value] = (acc[value] || 0) + 1;
    return acc;
  }, {});
  return Object.entries(counts).map(([value, count]) => ({ [key]: value, count }));
};

export const useDashboard = () => {
    const [stats, setStats] = useState({ por_tipo: [], por_estado: [] });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        const fetchStats = async () => {
            try {
                const response = await dashboardService.getActivos();
                const activos = response.data || [];
                if (!cancelled) {
                    setStats({
                        por_tipo: groupByCount(activos, 'tipo_activo'),
                        por_estado: groupByCount(activos, 'estado'),
                    });
                }
            } catch (error) {
                console.error("Error fetching stats", error);
            } finally {
                if (!cancelled) setLoading(false);
            }
        };
        fetchStats();
        return () => { cancelled = true; };
    }, []);

    return {
        stats,
        loading,
    };
};
