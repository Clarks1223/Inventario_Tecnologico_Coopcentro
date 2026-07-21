import { Grid, Paper, Typography, Box } from '@mui/material';
import { useDashboard } from '../hooks/useDashboard';
import PageHeader from '../components/ui/PageHeader';
import LoadingSpinner from '../components/ui/LoadingSpinner';

const Dashboard = () => {
    const { stats, loading } = useDashboard();

    if (loading) return <LoadingSpinner message="Cargando dashboard..." />;

    return (
        <Box>
            <PageHeader title="Dashboard Inventario" />

            <Grid container spacing={3}>
                <Grid size={{ xs: 12, md: 6 }}>
                    <Paper sx={{ p: 2 }}>
                        <Typography variant="h6">Activos por Tipo</Typography>
                        {stats.por_tipo.map((item) => (
                            <Box key={item.tipo_activo} sx={{ display: 'flex', justifyContent: 'space-between', my: 1 }}>
                                <Typography>{item.tipo_activo}</Typography>
                                <Typography fontWeight="bold">{item.count}</Typography>
                            </Box>
                        ))}
                    </Paper>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                    <Paper sx={{ p: 2 }}>
                        <Typography variant="h6">Activos por Estado</Typography>
                        {stats.por_estado.map((item) => (
                            <Box key={item.estado} sx={{ display: 'flex', justifyContent: 'space-between', my: 1 }}>
                                <Typography>{item.estado}</Typography>
                                <Typography fontWeight="bold">{item.count}</Typography>
                            </Box>
                        ))}
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );
};

export default Dashboard;
