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
  Checkbox,
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
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import AssignmentReturnIcon from '@mui/icons-material/AssignmentReturn';
import PrintIcon from '@mui/icons-material/Print';
import PersonSearchIcon from '@mui/icons-material/PersonSearch';
import { useAsignaciones } from '../hooks/useAsignaciones';
import { useDialogFullScreen } from '../hooks/useDialogFullScreen';
import { useSession } from '../hooks/useSession';
import PageHeader from '../components/ui/PageHeader';
import ComentarioActaDialog from '../components/ui/ComentarioActaDialog';
import { TIPO_ACTIVO_OPTIONS } from '../constants/activosConstants';
import { esAsignable, etiquetaEstadoGrupo } from '../utils/grupos';
import { filtrarActivosPorTexto } from '../utils/filtrarActivos';

const MAX_EMPLEADOS_VISIBLES = 100;

// Modos del diálogo de asignación: activos sueltos o un grupo completo.
const MODO_ACTIVOS = 'activos';
const MODO_GRUPO = 'grupo';

// El comentario del acta ya no se pide aquí: se pide en cada acción que genera
// un documento (imprimir o devolver), que es donde se sabe qué acta lleva qué.
const emptyForm = { ids_activos: [] };

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
    handleAssign,
    grupos,
    handleAssignGrupo,
    accionActaAbierta,
    accionActaActual,
    iniciarAccionActa,
    confirmarAccionActa,
    cancelarAccionActa,
  } = useAsignaciones();

  const { sesion } = useSession();
  const [form, setForm] = useState(emptyForm);
  const [tipoActivoDialogo, setTipoActivoDialogo] = useState('');
  const [modoDialogo, setModoDialogo] = useState(MODO_ACTIVOS);
  const [idGrupoElegido, setIdGrupoElegido] = useState(null);
  const [asignandoGrupo, setAsignandoGrupo] = useState(false);
  const dialogoPantallaCompleta = useDialogFullScreen();

  const gruposOrdenados = [...grupos].sort((a, b) => a.nombre.localeCompare(b.nombre));
  const grupoElegido = grupos.find((g) => g.id_grupo === idGrupoElegido) || null;
  const activosDelGrupo = grupoElegido?.activos || [];

  const asignarGrupo = async () => {
    setAsignandoGrupo(true);
    try {
      await handleAssignGrupo({ id_grupo: idGrupoElegido });
    } finally {
      setAsignandoGrupo(false);
    }
  };

  const empleadosVisibles = empleadosFiltrados.slice(0, MAX_EMPLEADOS_VISIBLES);
  const empleadosOcultos = empleadosFiltrados.length - empleadosVisibles.length;

  const activosParaAsignar = tipoActivoDialogo
    ? activosDisponibles.filter((a) => a.tipo_activo === tipoActivoDialogo)
    : activosDisponibles;

  const activosElegidos = activosDisponibles.filter((a) =>
    form.ids_activos.includes(a.id_activo)
  );

  const openDialog = () => {
    setForm(emptyForm);
    setTipoActivoDialogo('');
    setModoDialogo(MODO_ACTIVOS);
    setIdGrupoElegido(null);
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
                    onClick={() => iniciarAccionActa('imprimirTodo', empleadoActual.id_empleado)}
                    disabled={asignacionesDelEmpleado.length === 0}
                  >
                    Generar actas
                  </Button>
                  <Button
                    variant="outlined"
                    color="error"
                    startIcon={<AssignmentReturnIcon />}
                    onClick={() => iniciarAccionActa('devolverTodo', empleadoActual.id_empleado)}
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
                        <TableCell>Código de Inventario</TableCell>
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
                          <TableCell>{acta.codigo_inventario}</TableCell>
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
                              aria-label="Generar acta de entrega"
                              title="Generar acta de entrega (se guarda en disco)"
                              onClick={() => iniciarAccionActa('imprimirActa', acta.id_acta)}
                            >
                              <PrintIcon />
                            </IconButton>
                            <IconButton
                              aria-label="Registrar devolución"
                              title="Registrar devolución"
                              color="error"
                              onClick={() => iniciarAccionActa('devolver', acta.id_acta)}
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
          <ToggleButtonGroup
            exclusive
            fullWidth
            size="small"
            color="primary"
            value={modoDialogo}
            onChange={(event, modo) => {
              if (modo) setModoDialogo(modo);
            }}
            aria-label="Forma de asignar"
            sx={{ mb: 2 }}
          >
            <ToggleButton value={MODO_ACTIVOS}>Por activos</ToggleButton>
            <ToggleButton value={MODO_GRUPO}>Por grupo</ToggleButton>
          </ToggleButtonGroup>
          {modoDialogo === MODO_ACTIVOS ? (
            <>
              <FormControl size="small" fullWidth sx={{ mb: 1 }}>
                <InputLabel>Tipo de activo</InputLabel>
                <Select
                  value={tipoActivoDialogo}
                  label="Tipo de activo"
                  onChange={(e) => {
                    setTipoActivoDialogo(e.target.value);
                    setForm({ ...form, ids_activos: [] });
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
                multiple
                disableCloseOnSelect
                options={activosParaAsignar}
                value={activosElegidos}
                getOptionLabel={(option) => option.serial || ''}
                isOptionEqualToValue={(option, value) => option.id_activo === value.id_activo}
                filterOptions={filtrarActivosPorTexto}
                onChange={(event, newValue) =>
                  setForm({ ...form, ids_activos: newValue.map((a) => a.id_activo) })
                }
                noOptionsText="Ningún activo disponible coincide"
                renderOption={(props, option, { selected }) => {
                  const { key, ...otros } = props;
                  return (
                    <li key={key} {...otros}>
                      <Checkbox size="small" checked={selected} sx={{ mr: 1 }} />
                      <Box sx={{ minWidth: 0 }}>
                        <Typography variant="body2" noWrap>
                          {option.serial} · {option.tipo_activo}
                        </Typography>
                        <Typography variant="caption" color="text.secondary" noWrap component="div">
                          {[option.marca, option.modelo].filter(Boolean).join(' ')}
                          {option.codigo_inventario ? ` · ${option.codigo_inventario}` : ''}
                        </Typography>
                      </Box>
                    </li>
                  );
                }}
                renderTags={(value, getTagProps) =>
                  value.map((option, index) => {
                    const { key, ...otros } = getTagProps({ index });
                    return <Chip key={key} size="small" label={option.serial} {...otros} />;
                  })
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Activos disponibles"
                    placeholder="Escribe el serial, código, marca o modelo"
                    margin="dense"
                    fullWidth
                  />
                )}
              />
              {activosElegidos.length > 0 && empleadoActual && (
                <Typography variant="body2" sx={{ mt: 1 }}>
                  Se asignarán <b>{activosElegidos.length}</b>{' '}
                  {activosElegidos.length === 1 ? 'activo' : 'activos'} a{' '}
                  <b>
                    {empleadoActual.nombre} {empleadoActual.apellido}
                  </b>
                  .
                </Typography>
              )}
            </>
          ) : (
            <>
              <Autocomplete
                options={gruposOrdenados}
                value={grupoElegido}
                getOptionLabel={(option) => option.nombre || ''}
                isOptionEqualToValue={(option, value) => option.id_grupo === value.id_grupo}
                getOptionDisabled={(option) => !esAsignable(option)}
                onChange={(event, grupo) => setIdGrupoElegido(grupo?.id_grupo ?? null)}
                noOptionsText="No hay grupos registrados"
                renderOption={(props, option) => {
                  const { key, ...otros } = props;
                  const cantidad = option.activos?.length || 0;
                  return (
                    <li key={key} {...otros}>
                      <Box sx={{ minWidth: 0 }}>
                        <Typography variant="body2" noWrap>
                          {option.nombre}
                        </Typography>
                        <Typography variant="caption" color="text.secondary" noWrap component="div">
                          {etiquetaEstadoGrupo(option)} · {cantidad} {cantidad === 1 ? 'activo' : 'activos'}
                        </Typography>
                      </Box>
                    </li>
                  );
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Grupo"
                    placeholder="Escribe el nombre del grupo"
                    margin="dense"
                    fullWidth
                  />
                )}
              />
              <Typography variant="caption" color="text.secondary" component="div">
                Solo se puede asignar un grupo con todos sus activos en bodega.
              </Typography>
              {grupoElegido && (
                <>
                  <List dense disablePadding sx={{ mt: 1 }}>
                    {activosDelGrupo.map((activo) => (
                      <ListItemText
                        key={activo.id_activo}
                        primary={`${activo.serial} · ${activo.tipo_activo}`}
                        secondary={[activo.marca, activo.modelo].filter(Boolean).join(' ') || null}
                        sx={{ my: 0.5 }}
                      />
                    ))}
                  </List>
                  {empleadoActual && (
                    <Typography variant="body2" sx={{ mt: 1 }}>
                      Se asignarán <b>{activosDelGrupo.length}</b>{' '}
                      {activosDelGrupo.length === 1 ? 'activo' : 'activos'} del grupo «{grupoElegido.nombre}» a{' '}
                      <b>
                        {empleadoActual.nombre} {empleadoActual.apellido}
                      </b>
                      .
                    </Typography>
                  )}
              </>
          )}
          </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          {modoDialogo === MODO_ACTIVOS ? (
            <Button
              onClick={() => handleAssign(form)}
              variant="contained"
              disabled={form.ids_activos.length === 0}
            >
              Asignar{form.ids_activos.length > 1 ? ` (${form.ids_activos.length})` : ''}
            </Button>
          ) : (
            <Button
              onClick={asignarGrupo}
              variant="contained"
              disabled={!esAsignable(grupoElegido) || asignandoGrupo}
            >
              Asignar grupo{grupoElegido ? ` (${activosDelGrupo.length})` : ''}
            </Button>
          )}
        </DialogActions>
      </Dialog>

      {accionActaAbierta && accionActaActual ? (
        <ComentarioActaDialog
          titulo={accionActaActual.titulo}
          descripcion={accionActaActual.descripcion}
          textoAccion={accionActaActual.textoAccion}
          colorAccion={accionActaActual.colorAccion}
          onCancel={cancelarAccionActa}
          onConfirm={confirmarAccionActa}
        />
      ) : null}
    </Box>
  );
};

export default Asignaciones;
