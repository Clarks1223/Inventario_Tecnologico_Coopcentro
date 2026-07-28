import { useState } from 'react';
import {
  Box,
  Paper,
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
  Avatar,
  List,
  ListItemButton,
  ListItemAvatar,
  ListItemText,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import AssignmentReturnIcon from '@mui/icons-material/AssignmentReturn';
import PrintIcon from '@mui/icons-material/Print';
import PersonSearchIcon from '@mui/icons-material/PersonSearch';
import { useAsignaciones } from '../hooks/useAsignaciones';
import { useDialogFullScreen } from '../hooks/useDialogFullScreen';
import { useSession } from '../hooks/useSession';
import PageHeader from '../components/ui/PageHeader';
import { TIPO_ACTIVO_OPTIONS } from '../constants/activosConstants';

const MAX_EMPLEADOS_VISIBLES = 100;

const emptyForm = { id_activo: '', motivo: '' };

const iniciales = (empleado) =>
  `${empleado.nombre?.[0] || ''}${empleado.apellido?.[0] || ''}`.toUpperCase();

const Asignaciones = () => {
  const {
    isLoading,
    oficinas,
    busquedaEmpleado,
    setBusquedaEmpleado,
    oficinaFiltro,
    setOficinaFiltro,
    empleadosFiltrados,
    empleadoSeleccionado,
    setEmpleadoSeleccionado,
    empleadoActual,
    asignacionesDelEmpleado,
    conteoActivasPorEmpleado,
    activosDisponibles,
    oficinaNombre,
    cargoNombre,
    open,
    setOpen,
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
  const [tipoActivoDialogo, setTipoActivoDialogo] = useState('');
  const dialogoPantallaCompleta = useDialogFullScreen();

  const empleadosVisibles = empleadosFiltrados.slice(0, MAX_EMPLEADOS_VISIBLES);
  const empleadosOcultos = empleadosFiltrados.length - empleadosVisibles.length;

  const activosParaAsignar = tipoActivoDialogo
    ? activosDisponibles.filter((a) => a.tipo_activo === tipoActivoDialogo)
    : activosDisponibles;

  const activoElegido =
    activosDisponibles.find((a) => a.id_activo === form.id_activo) || null;

  const openDialog = () => {
    setForm(emptyForm);
    setTipoActivoDialogo('');
    setOpen(true);
  };

  return (
    <Box>
      <PageHeader title="Asignaciones" />

      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: { xs: '1fr', md: '300px minmax(0, 1fr)' },
          gap: 2,
          alignItems: 'start',
        }}
      >
        <Paper sx={{ p: 1.5, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          <TextField
            size="small"
            placeholder="Nombre, cédula o correo..."
            value={busquedaEmpleado}
            onChange={(e) => setBusquedaEmpleado(e.target.value)}
            fullWidth
          />
          <FormControl size="small" fullWidth>
            <InputLabel>Oficina</InputLabel>
            <Select
              value={oficinaFiltro || ''}
              label="Oficina"
              onChange={(e) => setOficinaFiltro(e.target.value || null)}
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
          <List dense disablePadding sx={{ maxHeight: 520, overflowY: 'auto' }}>
            {empleadosVisibles.map((emp) => (
              <ListItemButton
                key={emp.id_empleado}
                selected={emp.id_empleado === empleadoSeleccionado}
                onClick={() => setEmpleadoSeleccionado(emp.id_empleado)}
                sx={{ borderRadius: 1 }}
              >
                <ListItemAvatar sx={{ minWidth: 44 }}>
                  <Avatar sx={{ width: 32, height: 32, fontSize: 13 }}>
                    {iniciales(emp)}
                  </Avatar>
                </ListItemAvatar>
                <ListItemText
                  primary={`${emp.nombre} ${emp.apellido}`}
                  secondary={`${emp.cedula || ''} · ${oficinaNombre(emp.id_oficina)}`}
                  primaryTypographyProps={{ fontSize: 14, noWrap: true }}
                  secondaryTypographyProps={{ fontSize: 12, noWrap: true }}
                />
                {conteoActivasPorEmpleado[emp.id_empleado] ? (
                  <Chip
                    size="small"
                    color="primary"
                    label={conteoActivasPorEmpleado[emp.id_empleado]}
                  />
                ) : null}
              </ListItemButton>
            ))}
            {empleadosVisibles.length === 0 && (
              <Typography variant="body2" color="text.secondary" sx={{ p: 2, textAlign: 'center' }}>
                {isLoading ? 'Cargando empleados...' : 'Sin resultados para la búsqueda.'}
              </Typography>
            )}
          </List>
          {empleadosOcultos > 0 && (
            <Typography variant="caption" color="text.secondary" textAlign="center">
              {empleadosOcultos} empleados más — refina la búsqueda
            </Typography>
          )}
        </Paper>

        {empleadoActual ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, minWidth: 0 }}>
            <Paper sx={{ p: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
                <Avatar sx={{ width: 48, height: 48 }}>{iniciales(empleadoActual)}</Avatar>
                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="h6" noWrap>
                    {empleadoActual.nombre} {empleadoActual.apellido}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Cédula {empleadoActual.cedula} · {cargoNombre(empleadoActual.id_cargo)} ·{' '}
                    {oficinaNombre(empleadoActual.id_oficina)}
                  </Typography>
                </Box>
                <Box sx={{ ml: 'auto', display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                  <Button variant="contained" startIcon={<AddIcon />} onClick={openDialog}>
                    Asignar activo
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<PrintIcon />}
                    onClick={() => handlePrintAllForEmpleado(empleadoActual.id_empleado)}
                    disabled={asignacionesDelEmpleado.length === 0}
                  >
                    Imprimir actas
                  </Button>
                  <Button
                    variant="outlined"
                    color="error"
                    startIcon={<AssignmentReturnIcon />}
                    onClick={() => initiateReturnAll(empleadoActual.id_empleado)}
                    disabled={asignacionesDelEmpleado.length === 0}
                  >
                    Devolver todo
                  </Button>
                </Box>
              </Box>
            </Paper>

            <Paper sx={{ p: 2 }}>
              <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
                Activos asignados ({asignacionesDelEmpleado.length})
              </Typography>
              {asignacionesDelEmpleado.length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                  Este empleado no tiene activos asignados actualmente.
                </Typography>
              ) : (
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Número de Serie</TableCell>
                        <TableCell>Tipo</TableCell>
                        <TableCell>Fecha Asignación</TableCell>
                        <TableCell>Procesado por</TableCell>
                        <TableCell
                          align="right"
                          sx={{
                            position: 'sticky',
                            right: 0,
                            bgcolor: 'background.paper',
                            boxShadow: '-2px 0 4px rgba(0,0,0,0.08)',
                          }}
                        >
                          Acciones
                        </TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {asignacionesDelEmpleado.map((acta) => (
                        <TableRow key={acta.id_acta}>
                          <TableCell>{acta.serial}</TableCell>
                          <TableCell>{acta.tipo_activo}</TableCell>
                          <TableCell>
                            {acta.fecha_asignacion
                              ? new Date(acta.fecha_asignacion).toLocaleString()
                              : '-'}
                          </TableCell>
                          <TableCell>{acta.nombre_tecnico}</TableCell>
                          <TableCell
                            align="right"
                            sx={{
                              position: 'sticky',
                              right: 0,
                              bgcolor: 'background.paper',
                              boxShadow: '-2px 0 4px rgba(0,0,0,0.08)',
                              whiteSpace: 'nowrap',
                            }}
                          >
                            <IconButton
                              aria-label="Imprimir acta de entrega"
                              title="Imprimir acta de entrega"
                              onClick={() => handlePrint(acta.id_acta)}
                            >
                              <PrintIcon />
                            </IconButton>
                            <IconButton
                              aria-label="Registrar devolución"
                              title="Registrar devolución"
                              color="error"
                              onClick={() => initiateReturn(acta.id_acta)}
                            >
                              <AssignmentReturnIcon />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Paper>
          </Box>
        ) : (
          <Paper
            sx={{
              p: 6,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 1,
              color: 'text.secondary',
            }}
          >
            <PersonSearchIcon sx={{ fontSize: 48 }} />
            <Typography variant="h6">Selecciona un empleado</Typography>
            <Typography variant="body2" textAlign="center">
              Busca por nombre, cédula o correo en el panel izquierdo para ver y gestionar sus
              activos asignados.
            </Typography>
          </Paper>
        )}
      </Box>

      <Dialog
        open={open}
        onClose={() => setOpen(false)}
        fullScreen={dialogoPantallaCompleta}
        maxWidth="sm"
        fullWidth
        aria-labelledby="asignacion-form-dialog-title"
      >
        <DialogTitle id="asignacion-form-dialog-title">Nueva Asignación</DialogTitle>
        <DialogContent>
          {empleadoActual && (
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Empleado: {empleadoActual.nombre} {empleadoActual.apellido} (cédula{' '}
              {empleadoActual.cedula}) · Procesado por: {sesion?.nombre} {sesion?.apellido}
            </Typography>
          )}
          <FormControl size="small" fullWidth sx={{ mb: 1 }}>
            <InputLabel>Tipo de activo</InputLabel>
            <Select
              value={tipoActivoDialogo}
              label="Tipo de activo"
              onChange={(e) => {
                setTipoActivoDialogo(e.target.value);
                setForm({ ...form, id_activo: '' });
              }}
            >
              <MenuItem value="">
                <em>Todos</em>
              </MenuItem>
              {TIPO_ACTIVO_OPTIONS.map((tipo) => (
                <MenuItem key={tipo.value} value={tipo.value}>
                  {tipo.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Autocomplete
            options={activosParaAsignar}
            getOptionLabel={(option) =>
              `${option.serial} · ${option.tipo_activo} · ${option.marca || ''} ${option.modelo || ''} (${option.codigo_inventario})`
            }
            isOptionEqualToValue={(option, value) => option.id_activo === value.id_activo}
            value={activoElegido}
            onChange={(event, newValue) =>
              setForm({ ...form, id_activo: newValue ? newValue.id_activo : '' })
            }
            renderInput={(params) => (
              <TextField
                {...params}
                label="Activo disponible (busca por serial)"
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
          {activoElegido && empleadoActual && (
            <Typography variant="body2" sx={{ mt: 1 }}>
              Se asignará <b>{activoElegido.serial}</b> ({activoElegido.tipo_activo}) a{' '}
              <b>
                {empleadoActual.nombre} {empleadoActual.apellido}
              </b>
              .
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button
            onClick={() => handleAssign(form)}
            variant="contained"
            disabled={!form.id_activo}
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
        <DialogTitle id="asignaciones-confirm-dialog-title">Confirmación</DialogTitle>
        <DialogContent>
          <DialogContentText>{confirmDialog.message}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseConfirm}>Cancelar</Button>
          <Button onClick={handleConfirmAction} color="error" variant="contained">
            Aceptar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Asignaciones;
