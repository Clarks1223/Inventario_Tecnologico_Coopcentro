import { useState } from 'react';
import {
  Box,
  Button,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  TextField,
  Autocomplete,
  Typography,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import AssignmentReturnIcon from '@mui/icons-material/AssignmentReturn';
import PrintIcon from '@mui/icons-material/Print';
import { useAsignaciones } from '../hooks/useAsignaciones';
import { useSession } from '../hooks/useSession';
import PageHeader from '../components/ui/PageHeader';
import PageToolbar from '../components/ui/PageToolbar';
import DataTable from '../components/ui/DataTable';

const columns = [
  { key: 'activo_label', label: 'Activo' },
  { key: 'nombre_empleado', label: 'Empleado' },
  { key: 'nombre_tecnico', label: 'Procesado por' },
  {
    key: 'fecha_asignacion',
    label: 'Fecha Asignación',
    render: (row) =>
      row.fecha_asignacion
        ? new Date(row.fecha_asignacion).toLocaleString()
        : '-',
  },
  {
    key: 'fecha_devolucion',
    label: 'Fecha Devolución',
    render: (row) =>
      row.fecha_devolucion
        ? new Date(row.fecha_devolucion).toLocaleString()
        : '-',
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

const emptyForm = { id_empleado: '', id_activo: '', motivo: '' };

const Asignaciones = () => {
  const {
    asignaciones,
    isLoading,
    page,
    setPage,
    count,
    rowsPerPage,
    empleados,
    activosDisponibles,
    oficinas,
    showHistorial,
    setShowHistorial,
    open,
    setOpen,
    empleadoFiltro,
    setEmpleadoFiltro,
    oficinaFiltro,
    setOficinaFiltro,
    confirmDialog,
    handleAssign,
    initiateReturn,
    initiateReturnAll,
    handleConfirmAction,
    handleCloseConfirm,
    handlePrint,
    handlePrintAllForEmpleado,
  } = useAsignaciones();

  const { sesion } = useSession();
  const [form, setForm] = useState(emptyForm);

  const empleadosDeOficina = oficinaFiltro
    ? empleados.filter((e) => e.id_oficina === oficinaFiltro)
    : empleados;

  const handleOficinaFiltroChange = (idOficina) => {
    setOficinaFiltro(idOficina || null);
    const empleadoActual = empleados.find(
      (e) => e.id_empleado === empleadoFiltro,
    );
    if (
      idOficina &&
      empleadoActual &&
      empleadoActual.id_oficina !== idOficina
    ) {
      setEmpleadoFiltro(null);
    }
  };

  const openDialog = () => {
    setForm(emptyForm);
    setOpen(true);
  };

  const submitAssign = () => {
    handleAssign(form);
  };

  return (
    <Box>
      <PageHeader title="Asignaciones">
        <PageToolbar>
          <Button
            onClick={() => setShowHistorial(!showHistorial)}
            color={showHistorial ? 'warning' : 'primary'}
            variant="outlined"
          >
            {showHistorial ? 'Ver Activas' : 'Ver Historial Completo'}
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openDialog}
          >
            Nueva Asignación
          </Button>
        </PageToolbar>
      </PageHeader>

      <Box
        sx={{
          display: 'flex',
          gap: 2,
          alignItems: 'center',
          mb: 2,
          flexWrap: 'wrap',
        }}
      >
        <FormControl size="small" sx={{ width: 240 }}>
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
          sx={{ width: 320 }}
          options={empleadosDeOficina}
          getOptionLabel={(option) => `${option.nombre} ${option.apellido}`}
          isOptionEqualToValue={(option, value) =>
            option.id_empleado === value.id_empleado
          }
          value={
            empleados.find((e) => e.id_empleado === empleadoFiltro) || null
          }
          onChange={(event, newValue) =>
            setEmpleadoFiltro(newValue ? newValue.id_empleado : null)
          }
          renderInput={(params) => (
            <TextField {...params} label="Filtrar por empleado" size="small" />
          )}
        />
        {empleadoFiltro && (
          <>
            <Button
              color="primary"
              variant="outlined"
              startIcon={<PrintIcon />}
              onClick={() => handlePrintAllForEmpleado(empleadoFiltro)}
            >
              Imprimir actas de entrega
            </Button>
            <Button
              color="error"
              variant="outlined"
              startIcon={<AssignmentReturnIcon />}
              onClick={() => initiateReturnAll(empleadoFiltro)}
            >
              Devolver activos
            </Button>
          </>
        )}
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
          <>
            {row.estado_asignacion === 'activa' ? (
              <>
                <IconButton
                  aria-label="Imprimir acta de entrega"
                  onClick={() => handlePrint(row.id_acta)}
                  title="Imprimir acta de entrega"
                >
                  <PrintIcon />
                </IconButton>
                <IconButton
                  aria-label="Imprimir acta de devolución"
                  onClick={() => initiateReturn(row.id_acta)}
                  color="error"
                  title="Imprimir acta de devolución"
                >
                  <AssignmentReturnIcon />
                </IconButton>
              </>
            ) : (
              <IconButton
                aria-label="Imprimir acta de devolución"
                onClick={() => handlePrint(row.id_acta)}
                title="Imprimir acta de devolución"
              >
                <PrintIcon />
              </IconButton>
            )}
          </>
        )}
        emptyMessage="No existen asignaciones registradas."
      />

      <Dialog
        open={open}
        onClose={() => setOpen(false)}
        maxWidth="sm"
        fullWidth
        aria-labelledby="asignacion-form-dialog-title"
      >
        <DialogTitle id="asignacion-form-dialog-title">
          Nueva Asignación
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Procesado por: {sesion?.nombre} {sesion?.apellido}
          </Typography>
          <Autocomplete
            options={empleadosDeOficina}
            getOptionLabel={(option) => `${option.nombre} ${option.apellido}`}
            isOptionEqualToValue={(option, value) =>
              option.id_empleado === value.id_empleado
            }
            value={
              empleados.find((e) => e.id_empleado === form.id_empleado) || null
            }
            onChange={(event, newValue) =>
              setForm({
                ...form,
                id_empleado: newValue ? newValue.id_empleado : '',
              })
            }
            renderInput={(params) => (
              <TextField
                {...params}
                label="Empleado"
                margin="dense"
                fullWidth
              />
            )}
          />
          <Autocomplete
            options={activosDisponibles}
            getOptionLabel={(option) =>
              `${option.codigo_inventario} · ${option.tipo_activo} · ${option.marca || ''} ${option.modelo || ''}`
            }
            isOptionEqualToValue={(option, value) =>
              option.id_activo === value.id_activo
            }
            value={
              activosDisponibles.find((a) => a.id_activo === form.id_activo) ||
              null
            }
            onChange={(event, newValue) =>
              setForm({
                ...form,
                id_activo: newValue ? newValue.id_activo : '',
              })
            }
            renderInput={(params) => (
              <TextField
                {...params}
                label="Activo disponible"
                margin="dense"
                fullWidth
              />
            )}
          />
          <TextField
            margin="dense"
            label="Motivo (opcional)"
            fullWidth
            multiline
            rows={2}
            value={form.motivo}
            onChange={(e) => setForm({ ...form, motivo: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button
            onClick={submitAssign}
            variant="contained"
            disabled={!form.id_empleado || !form.id_activo}
          >
            Asignar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={confirmDialog.open}
        onClose={handleCloseConfirm}
        aria-labelledby="asignaciones-confirm-dialog-title"
      >
        <DialogTitle id="asignaciones-confirm-dialog-title">
          Confirmación
        </DialogTitle>
        <DialogContent>
          <DialogContentText>{confirmDialog.message}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseConfirm}>Cancelar</Button>
          <Button
            onClick={handleConfirmAction}
            color="error"
            variant="contained"
          >
            Aceptar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Asignaciones;
