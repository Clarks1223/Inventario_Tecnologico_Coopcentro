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
import EditIcon from '@mui/icons-material/Edit';
import BlockIcon from '@mui/icons-material/Block';
import RestoreFromTrashIcon from '@mui/icons-material/RestoreFromTrash';
import AddIcon from '@mui/icons-material/Add';
import { useCargos } from '../hooks/useCargos';
import PageHeader from '../components/ui/PageHeader';
import PageToolbar from '../components/ui/PageToolbar';
import DataTable from '../components/ui/DataTable';

const columns = [
  { key: 'id_cargo', label: 'ID' },
  { key: 'nombre', label: 'Nombre' },
];

const Cargos = () => {
  const {
    cargos,
    isLoading,
    page,
    setPage,
    count,
    rowsPerPage,
    open,
    setOpen,
    currentCargo,
    setCurrentCargo,
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
  } = useCargos();

  return (
    <Box>
      <PageHeader title="Cargos">
        <PageToolbar>
          <Button
            onClick={() => setShowInactivos(!showInactivos)}
            color={showInactivos ? 'warning' : 'primary'}
            variant="outlined"
            fullWidth
          >
            {showInactivos ? 'Ver Activos' : 'Ver Inactivos'}
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => openDialog()}
            fullWidth
          >
            Nuevo Cargo
          </Button>
        </PageToolbar>
      </PageHeader>

      <DataTable
        columns={columns}
        data={cargos}
        isLoading={isLoading}
        page={page}
        count={count}
        rowsPerPage={rowsPerPage}
        onPageChange={(newPage) => setPage(newPage)}
        keyExtractor={(row) => row.id_cargo}
        renderActions={(row) => (
          <>
            <IconButton aria-label="Editar cargo" onClick={() => openDialog(row)}>
              <EditIcon />
            </IconButton>
            {row.activo ? (
              <IconButton
                aria-label="Desactivar cargo"
                onClick={() => initiateDeactivate(row.id_cargo)}
                color="error"
                title="Desactivar"
              >
                <BlockIcon />
              </IconButton>
            ) : (
              <IconButton
                aria-label="Reactivar cargo"
                onClick={() => initiateReactivate(row.id_cargo)}
                color="success"
                title="Reactivar"
              >
                <RestoreFromTrashIcon />
              </IconButton>
            )}
          </>
        )}
        emptyMessage="No existen cargos registrados."
      />

      <Dialog open={open} onClose={() => setOpen(false)} aria-labelledby="cargos-form-dialog-title">
        <DialogTitle id="cargos-form-dialog-title">{isEdit ? 'Editar Cargo' : 'Nuevo Cargo'}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Nombre de Cargo"
            fullWidth
            value={currentCargo.nombre}
            onChange={(e) =>
              setCurrentCargo({ ...currentCargo, nombre: e.target.value })
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
        aria-labelledby="cargos-confirm-dialog-title"
      >
        <DialogTitle id="cargos-confirm-dialog-title">Confirmación</DialogTitle>
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

export default Cargos;
