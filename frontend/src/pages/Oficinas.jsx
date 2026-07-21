import {
  Box,
  Button,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  TextField,
  DialogActions,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import BlockIcon from '@mui/icons-material/Block';
import RestoreFromTrashIcon from '@mui/icons-material/RestoreFromTrash';
import { useOficinas } from '../hooks/useOficinas';
import PageHeader from '../components/ui/PageHeader';
import PageToolbar from '../components/ui/PageToolbar';
import DataTable from '../components/ui/DataTable';

const columns = [
  { key: 'id_oficina', label: 'ID' },
  { key: 'nombre', label: 'Nombre' },
  { key: 'direccion', label: 'Dirección' },
];

const Oficinas = () => {
  const {
    oficinas,
    isLoading,
    page,
    setPage,
    count,
    rowsPerPage,
    open,
    setOpen,
    currentOficina,
    setCurrentOficina,
    isEdit,
    showInactivos,
    setShowInactivos,
    confirmDialog,
    handleSave,
    initiateDeactivate,
    initiateReactivate,
    handleConfirmAction,
    handleCloseConfirm,
    openDialog,
  } = useOficinas();

  return (
    <Box>
      <PageHeader title="Oficinas">
        <PageToolbar>
          <Button
            onClick={() => setShowInactivos(!showInactivos)}
            color={showInactivos ? 'warning' : 'primary'}
            variant="outlined"
            fullWidth
          >
            {showInactivos ? 'Ver Activas' : 'Ver Inactivas'}
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => openDialog()}
            fullWidth
          >
            Nueva Oficina
          </Button>
        </PageToolbar>
      </PageHeader>

      <DataTable
        columns={columns}
        data={oficinas}
        isLoading={isLoading}
        page={page}
        count={count}
        rowsPerPage={rowsPerPage}
        onPageChange={(newPage) => setPage(newPage)}
        keyExtractor={(row) => row.id_oficina}
        renderActions={(row) => (
          <>
            <IconButton aria-label="Editar oficina" onClick={() => openDialog(row)}>
              <EditIcon />
            </IconButton>
            {row.activo ? (
              <IconButton
                aria-label="Desactivar oficina"
                onClick={() => initiateDeactivate(row.id_oficina)}
                color="error"
                title="Desactivar"
              >
                <BlockIcon />
              </IconButton>
            ) : (
              <IconButton
                aria-label="Reactivar oficina"
                onClick={() => initiateReactivate(row.id_oficina)}
                color="success"
                title="Reactivar"
              >
                <RestoreFromTrashIcon />
              </IconButton>
            )}
          </>
        )}
        emptyMessage="No existen oficinas registradas."
      />

      <Dialog open={open} onClose={() => setOpen(false)} aria-labelledby="oficinas-form-dialog-title">
        <DialogTitle id="oficinas-form-dialog-title">{isEdit ? 'Editar Oficina' : 'Nueva Oficina'}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Nombre de Oficina"
            fullWidth
            value={currentOficina.nombre}
            onChange={(e) =>
              setCurrentOficina({ ...currentOficina, nombre: e.target.value })
            }
          />
          <TextField
            margin="dense"
            label="Dirección"
            fullWidth
            required
            inputProps={{ maxLength: 150 }}
            value={currentOficina.direccion || ''}
            onChange={(e) =>
              setCurrentOficina({ ...currentOficina, direccion: e.target.value })
            }
          />
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
        aria-labelledby="oficinas-confirm-dialog-title"
      >
        <DialogTitle id="oficinas-confirm-dialog-title">Confirmación</DialogTitle>
        <DialogContent>
          <DialogContentText style={{ whiteSpace: 'pre-line' }}>
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
    </Box >
  );
};

export default Oficinas;
