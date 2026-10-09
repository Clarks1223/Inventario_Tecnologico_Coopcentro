import { useState } from 'react';
import { Alert, Box, FormControlLabel, Switch, Typography } from '@mui/material';
import { useDashboard } from '../hooks/useDashboard';
import PageHeader from '../components/ui/PageHeader';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import BarraKpis from '../components/dashboard/BarraKpis';
import ResumenOficinaTable from '../components/dashboard/ResumenOficinaTable';
import GraficoPcsLaptops from '../components/dashboard/GraficoPcsLaptops';
import DetalleOficinaTable from '../components/dashboard/DetalleOficinaTable';

const Dashboard = () => {
  const [incluirFueraDeServicio, setIncluirFueraDeServicio] = useState(false);
  const { activos, resumen, loading, error } = useDashboard(incluirFueraDeServicio);

  if (loading) return <LoadingSpinner message="Cargando dashboard..." />;
  if (error) return <Alert severity="error">{error}</Alert>;

  const { kpis, subtipos, filas, totales, sin_oficina_listada: sinOficinaListada } = resumen;
  const fecha = new Date().toLocaleDateString('es-EC');

  // Los subtipos de periférico (diademas, teclados, mouse...) van a continuación del total de periféricos.
  const items = [
    { id: 'total', label: 'Total equipos', value: kpis.total },
    { id: 'pcs', label: 'PCs (desktop)', value: kpis.pcs },
    { id: 'laptops', label: 'Laptops', value: kpis.laptops },
    { id: 'pcs_laptops', label: 'PCs + Laptops', value: kpis.pcs_laptops },
    { id: 'perifericos', label: 'Periféricos', value: kpis.perifericos },
    ...subtipos.map((s) => ({
      id: `subtipo-${s.clave}`, label: s.etiqueta, value: s.cantidad, secundario: true,
    })),
    { id: 'en_bodega', label: 'En bodega (TI)', value: kpis.en_bodega },
    { id: 'fuera_de_servicio', label: 'Baja / Robado', value: kpis.fuera_de_servicio },
  ];

  return (
    <Box sx={{ minWidth: 0, maxWidth: '100%' }}>
      <PageHeader title="Dashboard de Activos por Oficina">
        <FormControlLabel
          control={
            <Switch
              checked={incluirFueraDeServicio}
              onChange={(e) => setIncluirFueraDeServicio(e.target.checked)}
            />
          }
          label="Incluir dados de baja y robados/perdidos"
        />
      </PageHeader>
      <Typography variant="body2" color="text.secondary" sx={{ mt: -2, mb: 2, fontStyle: 'italic' }}>
        Inventario al {fecha}. Los valores se calculan en vivo con los activos registrados.{' '}
        {incluirFueraDeServicio
          ? 'Los totales incluyen los equipos dados de baja o robados/perdidos, que no tienen custodio.'
          : 'Los equipos dados de baja o robados/perdidos no cuentan en los totales.'}
      </Typography>

      <Box sx={{ mb: { xs: 2, md: 3 } }}>
        <BarraKpis items={items} />
      </Box>

      {/* minmax(0, 1fr): las tablas anchas se desplazan dentro de su tarjeta y no ensanchan la página. */}
      <Box sx={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr)', gap: { xs: 2, md: 3 } }}>
        <ResumenOficinaTable
          filas={filas}
          totales={totales}
          subtipos={subtipos}
          sinOficinaListada={sinOficinaListada}
          incluirFueraDeServicio={incluirFueraDeServicio}
        />
        <GraficoPcsLaptops filas={filas} />
        <DetalleOficinaTable
          activos={activos}
          filasOficina={filas}
          subtipos={subtipos}
          incluirFueraDeServicio={incluirFueraDeServicio}
        />
      </Box>
    </Box>
  );
};

export default Dashboard;
