import React, { useState } from 'react';
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
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  TextField,
  DialogActions,
  MenuItem,
  TablePagination,
  Select,
  FormControl,
  InputLabel,
  Tooltip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import BlockIcon from '@mui/icons-material/Block';
import ReportProblemIcon from '@mui/icons-material/ReportProblem';
import DownloadIcon from '@mui/icons-material/Download';
import { useActivos } from '../hooks/useActivos';
import { useActivoEditor } from '../hooks/useActivoEditor';
import ActivoFormModal from '../components/ui/ActivoFormModal';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import PageHeader from '../components/ui/PageHeader';
import PageToolbar from '../components/ui/PageToolbar';
import {
  TIPO_ACTIVO_OPTIONS,
  ESTADO_OPTIONS,
} from '../constants/activosConstants';

const Activos = () => {
  const [decommissionDialogOpen, setDecommissionDialogOpen] = useState(false);
  const [activoToDecommission, setActivoToDecommission] = useState(null);
  const [stolenDialogOpen, setStolenDialogOpen] = useState(false);
  const [activoToReportStolen, setActivoToReportStolen] = useState(null);

  const {
    activos,
    isLoading,
    page,
    setPage,
    count,
    rowsPerPage,
    oficinas,
    filters,
    setFilters,
    fetchActivos,
    handleDecommission,
    handleReportStolen,
    handleExportExcel,
  } = useActivos();

  const activoEditor = useActivoEditor(() => fetchActivos());

  return (
    <Box>
      <PageHeader title="Activos">
        <PageToolbar>
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
            onClick={activoEditor.handleOpenNew}
          >
            Nuevo Activo
          </Button>
        </PageToolbar>
      </PageHeader>

      <TableContainer component={Paper}>
        <Table size="small" sx={{ minWidth: 1400, tableLayout: 'fixed' }}>
          <TableHead>
            <TableRow>
              <TableCell sx={{ width: '10%' }}>
                <Typography variant="subtitle2">Tipo</Typography>
                <FormControl variant="standard" size="small" fullWidth>
                  <Select
                    displayEmpty
                    value={filters.tipo_activo}
                    onChange={(e) =>
                      setFilters({ ...filters, tipo_activo: e.target.value })
                    }
                  >
                    <MenuItem value="">
                      <em>Tipos (Todos)</em>
                    </MenuItem>
                    {TIPO_ACTIVO_OPTIONS.map((opt) => (
                      <MenuItem key={opt.value} value={opt.value}>
                        {opt.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </TableCell>
              <TableCell sx={{ width: '9%' }}>
                <Typography variant="subtitle2">Marca</Typography>
                <TextField
                  size="small"
                  variant="standard"
                  placeholder="Filtrar..."
                  value={filters.marca}
                  onChange={(e) =>
                    setFilters({ ...filters, marca: e.target.value })
                  }
                />
              </TableCell>
              <TableCell sx={{ width: '9%' }}>
                <Typography variant="subtitle2">Modelo</Typography>
                <TextField
                  size="small"
                  variant="standard"
                  placeholder="Filtrar..."
                  value={filters.modelo}
                  onChange={(e) =>
                    setFilters({ ...filters, modelo: e.target.value })
                  }
                />
              </TableCell>
              <TableCell sx={{ width: '9%' }}>
                <Typography variant="subtitle2">Serial</Typography>
                <TextField
                  size="small"
                  variant="standard"
                  placeholder="Filtrar..."
                  value={filters.serial}
                  onChange={(e) =>
                    setFilters({ ...filters, serial: e.target.value })
                  }
                />
              </TableCell>
              <TableCell sx={{ width: '13%' }}>
                <Typography variant="subtitle2">Estado</Typography>
                <FormControl variant="standard" size="small" fullWidth>
                  <Select
                    displayEmpty
                    value={filters.estado}
                    onChange={(e) =>
                      setFilters({ ...filters, estado: e.target.value })
                    }
                  >
                    <MenuItem value="">
                      <em>Todos</em>
                    </MenuItem>
                    {ESTADO_OPTIONS.map((opt) => (
                      <MenuItem key={opt.value} value={opt.value}>
                        {opt.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </TableCell>
              <TableCell sx={{ width: '14%' }}>
                <Typography variant="subtitle2">Ubicación</Typography>
                <FormControl variant="standard" size="small" fullWidth>
                  <Select
                    displayEmpty
                    value={filters.id_oficina || ''}
                    onChange={(e) =>
                      setFilters({ ...filters, id_oficina: e.target.value })
                    }
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
              </TableCell>
              <TableCell sx={{ width: '13%' }}>
                <Typography variant="subtitle2">Empleado Asignado</Typography>
              </TableCell>
              <TableCell sx={{ width: '9%' }}>
                <Typography variant="subtitle2">IP</Typography>
                <TextField
                  size="small"
                  variant="standard"
                  placeholder="Filtrar..."
                  value={filters.ip}
                  onChange={(e) =>
                    setFilters({ ...filters, ip: e.target.value })
                  }
                />
              </TableCell>
              <TableCell sx={{ width: '9%' }}>
                <Typography variant="subtitle2">Dominio</Typography>
                <TextField
                  size="small"
                  variant="standard"
                  placeholder="Filtrar..."
                  value={filters.dominio}
                  onChange={(e) =>
                    setFilters({ ...filters, dominio: e.target.value })
                  }
                />
              </TableCell>
              <TableCell sx={{ width: 120 }}>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={10} align="center" sx={{ py: 3 }}>
                  <LoadingSpinner />
                </TableCell>
              </TableRow>
            ) : activos.length > 0 ? (
              activos.map((row) => (
                <TableRow key={row.id_activo}>
                  <TableCell
                    sx={{
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    <Tooltip title={row.tipo_activo || ''}>
                      <span>{row.tipo_activo}</span>
                    </Tooltip>
                  </TableCell>
                  <TableCell
                    sx={{
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    <Tooltip title={row.marca || ''}>
                      <span>{row.marca}</span>
                    </Tooltip>
                  </TableCell>
                  <TableCell
                    sx={{
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    <Tooltip title={row.modelo || ''}>
                      <span>{row.modelo}</span>
                    </Tooltip>
                  </TableCell>
                  <TableCell
                    sx={{
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    <Tooltip title={row.serial || ''}>
                      <span>{row.serial}</span>
                    </Tooltip>
                  </TableCell>
                  <TableCell>
                    <Chip label={row.estado} size="small" />
                  </TableCell>
                  <TableCell
                    sx={{
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    <Tooltip title={row.oficina_nombre || ''}>
                      <span>{row.oficina_nombre || '-'}</span>
                    </Tooltip>
                  </TableCell>
                  <TableCell
                    sx={{
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    <Tooltip title={row.empleado_asignado || ''}>
                      <span>{row.empleado_asignado || '-'}</span>
                    </Tooltip>
                  </TableCell>
                  <TableCell>
                    <Typography
                      variant="body2"
                      sx={{ fontFamily: 'monospace', fontSize: '0.75rem' }}
                    >
                      {row.detalle?.ip || '-'}
                    </Typography>
                  </TableCell>
                  <TableCell
                    sx={{
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    <Tooltip title={row.detalle?.dominio || ''}>
                      <span>{row.detalle?.dominio || '-'}</span>
                    </Tooltip>
                  </TableCell>
                  <TableCell>
                    <IconButton
                      aria-label="Editar activo"
                      size="small"
                      onClick={() => activoEditor.handleEdit(row)}
                      color="primary"
                    >
                      <EditIcon />
                    </IconButton>
                    {row.estado !== 'DADO_DE_BAJA' ? (
                      <IconButton
                        aria-label="Dar de baja activo"
                        size="small"
                        onClick={() => {
                          setActivoToDecommission(row);
                          setDecommissionDialogOpen(true);
                        }}
                        color="error"
                        title="Dar de baja"
                      >
                        <BlockIcon />
                      </IconButton>
                    ) : null}
                    {row.estado !== 'DADO_DE_BAJA' &&
                    row.estado !== 'ROBADO_PERDIDO' ? (
                      <IconButton
                        aria-label="Reportar activo robado o perdido"
                        size="small"
                        onClick={() => {
                          setActivoToReportStolen(row);
                          setStolenDialogOpen(true);
                        }}
                        color="warning"
                        title="Reportar robado/perdido"
                      >
                        <ReportProblemIcon />
                      </IconButton>
                    ) : null}
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={10} align="center" sx={{ py: 3 }}>
                  No hay activos.
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
          onRowsPerPageChange={() => {}}
        />
      </TableContainer>

      <ActivoFormModal editorProps={activoEditor} oficinas={oficinas} />

      <Dialog
        open={decommissionDialogOpen}
        onClose={() => setDecommissionDialogOpen(false)}
        aria-labelledby="activos-baja-dialog-title"
      >
        <DialogTitle id="activos-baja-dialog-title">
          Confirmar Baja de Activo
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            ¿Está seguro que desea dar de baja el activo{' '}
            <strong>{activoToDecommission?.codigo_inventario}</strong>? Esta
            acción cambiará su estado a "Dado de baja".
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDecommissionDialogOpen(false)}>
            Cancelar
          </Button>
          <Button
            onClick={() => {
              if (activoToDecommission) {
                handleDecommission(activoToDecommission.id_activo);
              }
              setDecommissionDialogOpen(false);
            }}
            color="error"
            variant="contained"
          >
            Dar de Baja
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={stolenDialogOpen}
        onClose={() => setStolenDialogOpen(false)}
        aria-labelledby="activos-robado-dialog-title"
      >
        <DialogTitle id="activos-robado-dialog-title">
          Confirmar Reporte de Activo Robado/Perdido
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            ¿Está seguro que desea reportar el activo{' '}
            <strong>{activoToReportStolen?.serial}</strong> como robado o
            perdido? Esta acción cambiará su estado a "Robado/Perdido".
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStolenDialogOpen(false)}>Cancelar</Button>
          <Button
            onClick={() => {
              if (activoToReportStolen) {
                handleReportStolen(activoToReportStolen.id_activo);
              }
              setStolenDialogOpen(false);
            }}
            color="warning"
            variant="contained"
          >
            Reportar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Activos;
