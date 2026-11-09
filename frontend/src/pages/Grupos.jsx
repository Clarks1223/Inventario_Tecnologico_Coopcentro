import {
  Autocomplete,
  Box,
  Button,
  Checkbox,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { useGrupos } from '../hooks/useGrupos';
import { useDialogFullScreen } from '../hooks/useDialogFullScreen';
import PageHeader from '../components/ui/PageHeader';
import PageToolbar from '../components/ui/PageToolbar';
import DataTable from '../components/ui/DataTable';
import { asignadoA, colorEstadoGrupo, etiquetaEstadoGrupo } from '../utils/grupos';
import { chipDeEstadoActivo } from '../constants/activosConstants';
import { filtrarActivosPorTexto } from '../utils/filtrarActivos';

const columns = [
  { key: 'nombre', label: 'Nombre' },
  { key: 'descripcion', label: 'Descripción' },
  { key: 'activos', label: 'Activos', align: 'center', render: (row) => row.activos?.length || 0 },
  {
    key: 'estado',
    label: 'Estado',
    render: (row) => (
      <Chip size="small" label={etiquetaEstadoGrupo(row)} color={colorEstadoGrupo(row)} />
    ),
  },
];

const Grupos = () => {
  const {
    grupos,
    count,
    isLoading,
    page,
    setPage,
    rowsPerPage,
    open,
    setOpen,
    currentGrupo,
    setCurrentGrupo,
    opcionesDeActivos,
    isSaving,
    openDialog,
    handleSave,
    grupoPorEliminar,
    iniciarEliminar,
    cancelarEliminar,
    confirmarEliminar,
  } = useGrupos();
  const dialogoPantallaCompleta = useDialogFullScreen();

  const activosElegidos = opcionesDeActivos.filter((a) =>
    currentGrupo.ids_activos.includes(a.id_activo)
  );

  return (
    <Box>
      <PageHeader title="Grupos de activos">
        <PageToolbar>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => openDialog()} fullWidth>
            Nuevo Grupo
          </Button>
        </PageToolbar>
      </PageHeader>

      <DataTable
        columns={columns}
        data={grupos}
        isLoading={isLoading}
        page={page}
        count={count}
        rowsPerPage={rowsPerPage}
        onPageChange={(newPage) => setPage(newPage)}
        keyExtractor={(row) => row.id_grupo}
        renderActions={(row) => (
          <>
            <IconButton aria-label="Editar grupo" onClick={() => openDialog(row)}>
              <EditIcon />
            </IconButton>
            <IconButton
              aria-label="Eliminar grupo"
              onClick={() => iniciarEliminar(row)}
              color="error"
              title="Eliminar"
            >
              <DeleteIcon />
            </IconButton>
          </>
        )}
        emptyMessage="No existen grupos registrados."
      />

      <Dialog
        open={open}
        onClose={() => setOpen(false)}
        fullScreen={dialogoPantallaCompleta}
        maxWidth="sm"
        fullWidth
        aria-labelledby="grupos-form-dialog-title"
      >
        <DialogTitle id="grupos-form-dialog-title">
          {currentGrupo.id_grupo ? 'Editar Grupo' : 'Nuevo Grupo'}
        </DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Nombre del grupo"
            placeholder="Ej. Puesto Caja 1"
            fullWidth
            required
            inputProps={{ maxLength: 100 }}
            value={currentGrupo.nombre}
            onChange={(e) => setCurrentGrupo({ ...currentGrupo, nombre: e.target.value })}
          />
          <TextField
            margin="dense"
            label="Descripción"
            fullWidth
            multiline
            minRows={2}
            value={currentGrupo.descripcion}
            onChange={(e) => setCurrentGrupo({ ...currentGrupo, descripcion: e.target.value })}
          />
          <Autocomplete
            multiple
            disableCloseOnSelect
            options={opcionesDeActivos}
            value={activosElegidos}
            getOptionLabel={(option) => option.serial || ''}
            isOptionEqualToValue={(option, value) => option.id_activo === value.id_activo}
            filterOptions={filtrarActivosPorTexto}
            onChange={(event, newValue) =>
              setCurrentGrupo({ ...currentGrupo, ids_activos: newValue.map((a) => a.id_activo) })
            }
            noOptionsText="Ningún activo operativo sin grupo coincide"
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
                label="Activos del grupo"
                placeholder="Escribe el serial, código, marca o modelo"
                margin="dense"
                fullWidth
              />
            )}
          />
          <Typography variant="caption" color="text.secondary">
            Solo se ofrecen activos operativos que no pertenecen a otro grupo.
          </Typography>
          {activosElegidos.length > 0 && (
            <TableContainer sx={{ mt: 2, border: 1, borderColor: 'divider', borderRadius: 1 }}>
              <Table size="small" aria-label="Activos seleccionados y a quién están asignados">
                <TableHead>
                  <TableRow>
                    <TableCell>Serial</TableCell>
                    <TableCell>Tipo</TableCell>
                    <TableCell>Marca / Modelo</TableCell>
                    <TableCell>Asignado a</TableCell>
                    <TableCell>Estado</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {activosElegidos.map((activo) => {
                    const { label, color } = chipDeEstadoActivo(activo.estado);
                    return (
                      <TableRow key={activo.id_activo}>
                        <TableCell>{activo.serial}</TableCell>
                        <TableCell>{activo.tipo_activo}</TableCell>
                        <TableCell>{[activo.marca, activo.modelo].filter(Boolean).join(' ') || '—'}</TableCell>
                        <TableCell>{asignadoA(activo)}</TableCell>
                        <TableCell>
                          <Chip size="small" label={label} color={color} />
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button
            onClick={handleSave}
            variant="contained"
            disabled={isSaving || !currentGrupo.nombre.trim()}
          >
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!grupoPorEliminar} onClose={cancelarEliminar} aria-labelledby="grupos-confirm-dialog-title">
        <DialogTitle id="grupos-confirm-dialog-title">Eliminar grupo</DialogTitle>
        <DialogContent>
          <DialogContentText>
            ¿Deseas eliminar el grupo «{grupoPorEliminar?.nombre}»? Sus activos quedarán sin grupo; las
            asignaciones y actas no cambian.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={cancelarEliminar}>Cancelar</Button>
          <Button onClick={confirmarEliminar} color="error" variant="contained">
            Eliminar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Grupos;
