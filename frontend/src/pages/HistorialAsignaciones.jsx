import {
  Box,
  IconButton,
  TextField,
  Autocomplete,
  Typography,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import PrintIcon from '@mui/icons-material/Print';
import { useHistorialAsignaciones } from '../hooks/useHistorialAsignaciones';
import PageHeader from '../components/ui/PageHeader';
import DataTable from '../components/ui/DataTable';

const HistorialAsignaciones = () => {
  const {
    asignaciones,
    isLoading,
    page,
    setPage,
    count,
    rowsPerPage,
    empleados,
    oficinas,
    empleadoFiltro,
    setEmpleadoFiltro,
    oficinaFiltro,
    setOficinaFiltro,
    serialFiltro,
    setSerialFiltro,
    estadoFiltro,
    setEstadoFiltro,
    handlePrint,
  } = useHistorialAsignaciones();

  const empleadosDeOficina = oficinaFiltro
    ? empleados.filter((e) => e.id_oficina === oficinaFiltro)
    : empleados;

  const handleOficinaFiltroChange = (idOficina) => {
    setOficinaFiltro(idOficina || null);
    const empleadoActual = empleados.find((e) => e.id_empleado === empleadoFiltro);
    if (idOficina && empleadoActual && empleadoActual.id_oficina !== idOficina) {
      setEmpleadoFiltro(null);
    }
  };

  const columns = [
    {
      key: 'serial',
      label: (
        <>
          <Typography variant="subtitle2">Número de Serie</Typography>
          <TextField
            size="small"
            variant="standard"
            placeholder="Filtrar..."
            value={serialFiltro}
            onChange={(e) => setSerialFiltro(e.target.value)}
          />
        </>
      ),
    },
    { key: 'tipo_activo', label: 'Tipo de Activo' },
    { key: 'nombre_empleado', label: 'Empleado' },
    { key: 'nombre_tecnico', label: 'Procesado por' },
    {
      key: 'fecha_asignacion',
      label: 'Fecha Asignación',
      render: (row) =>
        row.fecha_asignacion ? new Date(row.fecha_asignacion).toLocaleString() : '-',
    },
    {
      key: 'fecha_devolucion',
      label: 'Fecha Devolución',
      render: (row) =>
        row.fecha_devolucion ? new Date(row.fecha_devolucion).toLocaleString() : '-',
    },
    {
      key: 'estado_asignacion',
      label: 'Estado',
      render: (row) => (
        <Chip
          label={row.estado_asignacion === 'activa' ? 'Activa' : 'Devuelta'}
          color={row.estado_asignacion === 'activa' ? 'success' : 'default'}
          size="small"
        />
      ),
    },
  ];

  return (
    <Box>
      <PageHeader title="Historial de Asignaciones" />

      <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2, flexWrap: 'wrap' }}>
        <FormControl size="small" sx={{ width: 220 }}>
          <InputLabel>Filtrar por oficina</InputLabel>
          <Select
            value={oficinaFiltro || ''}
            label="Filtrar por oficina"
            onChange={(e) => handleOficinaFiltroChange(e.target.value)}
          >
            <MenuItem value="">
              <em>Todas</em>
            </MenuItem>
            {oficinas.map((of) => (
              <MenuItem key={of.id_oficina} value={of.id_oficina}>
                {of.nombre}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <Autocomplete
          sx={{ width: 300 }}
          options={empleadosDeOficina}
          getOptionLabel={(option) => `${option.nombre} ${option.apellido}`}
          isOptionEqualToValue={(option, value) => option.id_empleado === value.id_empleado}
          value={empleados.find((e) => e.id_empleado === empleadoFiltro) || null}
          onChange={(event, newValue) =>
            setEmpleadoFiltro(newValue ? newValue.id_empleado : null)
          }
          renderInput={(params) => (
            <TextField {...params} label="Filtrar por empleado" size="small" />
          )}
        />
        <FormControl size="small" sx={{ width: 180 }}>
          <InputLabel>Estado</InputLabel>
          <Select
            value={estadoFiltro}
            label="Estado"
            onChange={(e) => setEstadoFiltro(e.target.value)}
          >
            <MenuItem value="">
              <em>Todas</em>
            </MenuItem>
            <MenuItem value="activa">Activa</MenuItem>
            <MenuItem value="devuelta">Devuelta</MenuItem>
          </Select>
        </FormControl>
      </Box>

      <DataTable
        columns={columns}
        data={asignaciones}
        isLoading={isLoading}
        page={page}
        count={count}
        rowsPerPage={rowsPerPage}
        onPageChange={(newPage) => setPage(newPage)}
        keyExtractor={(row) => row.id_acta}
        renderActions={(row) => (
          <IconButton
            aria-label="Imprimir acta"
            title={row.estado_asignacion === 'activa' ? 'Imprimir acta de entrega' : 'Imprimir acta de devolución'}
            onClick={() => handlePrint(row.id_acta)}
          >
            <PrintIcon />
          </IconButton>
        )}
        emptyMessage="No existen asignaciones registradas."
      />
    </Box>
  );
};

export default HistorialAsignaciones;
