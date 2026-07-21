import {
  Box,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  TextField,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TablePagination,
  Autocomplete,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import RestoreFromTrashIcon from '@mui/icons-material/RestoreFromTrash';
import DownloadIcon from '@mui/icons-material/Download';
import { useEmpleados } from '../hooks/useEmpleados';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import PageHeader from '../components/ui/PageHeader';
import PageToolbar from '../components/ui/PageToolbar';

const Empleados = () => {
  const {
    empleados,
    isLoading,
    page,
    setPage,
    count,
    rowsPerPage,
    oficinas,
    cargos,
    open,
    setOpen,
    currentEmpleado,
    setCurrentEmpleado,
    isEdit,
    filters,
    setFilters,
    showInactivos,
    setShowInactivos,
    confirmDialog,
    handleSave,
    initiateDeactivate,
    initiateReactivate,
    handleConfirmAction,
    handleCloseConfirm,
    openDialog,
    handleExportExcel,
  } = useEmpleados();

  return (
    <Box>
      <PageHeader title="Empleados">
        <PageToolbar>
          <Button
            onClick={() => setShowInactivos(!showInactivos)}
            color={showInactivos ? 'warning' : 'primary'}
            variant="outlined"
          >
            {showInactivos ? 'Ver Activos' : 'Ver Inactivos'}
          </Button>
          <Button
            variant="outlined"
            color="success"
            startIcon={<DownloadIcon />}
            onClick={handleExportExcel}
          >
            Descargar Excel
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => openDialog()}
          >
            Nuevo Empleado
          </Button>
        </PageToolbar>
      </PageHeader>

      <TableContainer component={Paper}>
        <Table size="small" sx={{ minWidth: 650 }}>
          <TableHead>
            <TableRow>
              <TableCell sx={{ width: 50 }}>ID</TableCell>
              <TableCell sx={{ width: '20%' }}>
                <Typography variant="subtitle2">Nombre</Typography>
                <TextField
                  size="small"
                  variant="standard"
                  placeholder="Filtrar..."
                  value={filters.nombre}
                  onChange={(e) =>
                    setFilters({ ...filters, nombre: e.target.value })
                  }
                />
              </TableCell>
              <TableCell sx={{ width: '14%' }}>
                <Typography variant="subtitle2">Cédula</Typography>
                <TextField
                  size="small"
                  variant="standard"
                  placeholder="Filtrar..."
                  value={filters.cedula}
                  onChange={(e) =>
                    setFilters({ ...filters, cedula: e.target.value })
                  }
                />
              </TableCell>
              <TableCell sx={{ width: '16%' }}>
                <Typography variant="subtitle2">Correo</Typography>
                <TextField
                  size="small"
                  variant="standard"
                  placeholder="Filtrar..."
                  value={filters.correo}
                  onChange={(e) =>
                    setFilters({ ...filters, correo: e.target.value })
                  }
                />
              </TableCell>
              <TableCell sx={{ width: '10%' }}>
                <Typography variant="subtitle2">Extensión</Typography>
                <TextField
                  size="small"
                  variant="standard"
                  placeholder="Filtrar..."
                  value={filters.extension_telefonica}
                  onChange={(e) =>
                    setFilters({
                      ...filters,
                      extension_telefonica: e.target.value,
                    })
                  }
                />
              </TableCell>
              <TableCell sx={{ width: '14%' }}>
                <Typography variant="subtitle2">Cargo</Typography>
                <TextField
                  size="small"
                  variant="standard"
                  placeholder="Filtrar..."
                  value={filters.id_cargo}
                  onChange={(e) =>
                    setFilters({ ...filters, id_cargo: e.target.value })
                  }
                />
              </TableCell>
              <TableCell sx={{ width: '14%' }}>
                <Typography variant="subtitle2">Oficina</Typography>
                <FormControl size="small" variant="standard" fullWidth>
                  <Select
                    value={filters.id_oficina}
                    displayEmpty
                    onChange={(e) =>
                      setFilters({ ...filters, id_oficina: e.target.value })
                    }
                  >
                    <MenuItem value="">
                      <em>Todas</em>
                    </MenuItem>
                    {oficinas.map((oficina) => (
                      <MenuItem
                        key={oficina.id_oficina}
                        value={oficina.id_oficina}
                      >
                        {oficina.nombre}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </TableCell>
              <TableCell align="right" sx={{ width: 80 }}>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 3 }}>
                  <LoadingSpinner />
                </TableCell>
              </TableRow>
            ) : empleados.length > 0 ? (
              empleados.map((row) => (
                <TableRow key={row.id_empleado}>
                  <TableCell>{row.id_empleado}</TableCell>
                  <TableCell>
                    {row.nombre} {row.apellido}
                  </TableCell>
                  <TableCell>{row.cedula}</TableCell>
                  <TableCell sx={{ maxWidth: 140, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {row.correo}
                  </TableCell>
                  <TableCell>{row.extension_telefonica}</TableCell>
                  <TableCell>{row.nombre_cargo}</TableCell>
                  <TableCell>{row.nombre_oficina}</TableCell>
                  <TableCell align="right">
                    <IconButton aria-label="Editar empleado" onClick={() => openDialog(row)}>
                      <EditIcon />
                    </IconButton>
                    {row.activo ? (
                      <IconButton
                        aria-label="Desactivar empleado"
                        onClick={() => initiateDeactivate(row.id_empleado)}
                        color="error"
                        title="Desactivar"
                      >
                        <BlockIcon />
                      </IconButton>
                    ) : (
                      <IconButton
                        aria-label="Reactivar empleado"
                        onClick={() => initiateReactivate(row.id_empleado)}
                        color="success"
                        title="Reactivar"
                      >
                        <RestoreFromTrashIcon />
                      </IconButton>
                    )}
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 3 }}>
                  <Typography variant="body1" color="text.secondary">
                    No existen empleados registrados.
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={count}
          page={page}
          onPageChange={(e, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          rowsPerPageOptions={[10]}
          onRowsPerPageChange={() => { }}
        />
      </TableContainer>

      <Dialog open={open} onClose={() => setOpen(false)} aria-labelledby="empleados-form-dialog-title">
        <DialogTitle id="empleados-form-dialog-title">
          {isEdit ? 'Editar Empleado' : 'Nuevo Empleado'}
        </DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Nombre"
            fullWidth
            value={currentEmpleado.nombre}
            onChange={(e) =>
              setCurrentEmpleado({ ...currentEmpleado, nombre: e.target.value })
            }
          />
          <TextField
            margin="dense"
            label="Apellido"
            fullWidth
            value={currentEmpleado.apellido}
            onChange={(e) =>
              setCurrentEmpleado({
                ...currentEmpleado,
                apellido: e.target.value,
              })
            }
          />
          <TextField
            margin="dense"
            label="Cédula"
            fullWidth
            value={currentEmpleado.cedula}
            onChange={(e) =>
              setCurrentEmpleado({ ...currentEmpleado, cedula: e.target.value })
            }
          />
          <TextField
            margin="dense"
            label="Correo"
            fullWidth
            type="email"
            value={currentEmpleado.correo}
            onChange={(e) =>
              setCurrentEmpleado({ ...currentEmpleado, correo: e.target.value })
            }
          />
          <TextField
            margin="dense"
            label="Extensión"
            fullWidth
            value={currentEmpleado.extension_telefonica}
            onChange={(e) =>
              setCurrentEmpleado({
                ...currentEmpleado,
                extension_telefonica: e.target.value,
              })
            }
          />
          <Autocomplete
            options={cargos}
            getOptionLabel={(option) => option.nombre || ''}
            isOptionEqualToValue={(option, value) =>
              option.id_cargo === value.id_cargo
            }
            value={
              cargos.find((c) => c.id_cargo === currentEmpleado.id_cargo) || null
            }
            onChange={(event, newValue) => {
              setCurrentEmpleado({
                ...currentEmpleado,
                id_cargo: newValue ? newValue.id_cargo : '',
              });
            }}
            renderInput={(params) => (
              <TextField {...params} label="Cargo" margin="dense" fullWidth />
            )}
          />
          <FormControl fullWidth margin="dense">
            <InputLabel>Oficina</InputLabel>
            <Select
              value={currentEmpleado.id_oficina}
              label="Oficina"
              onChange={(e) =>
                setCurrentEmpleado({
                  ...currentEmpleado,
                  id_oficina: e.target.value,
                })
              }
            >
              {oficinas.map((of) => (
                <MenuItem key={of.id_oficina} value={of.id_oficina}>
                  {of.nombre}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button onClick={handleSave} variant="contained">
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={confirmDialog.open}
        onClose={handleCloseConfirm}
        aria-labelledby="empleados-confirm-dialog-title"
      >
        <DialogTitle id="empleados-confirm-dialog-title">Confirmación</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {confirmDialog.message}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseConfirm}>Cancelar</Button>
          <Button
            onClick={handleConfirmAction}
            color={confirmDialog.type === 'deactivate' ? 'error' : 'primary'}
            variant="contained"
          >
            Aceptar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Empleados;
