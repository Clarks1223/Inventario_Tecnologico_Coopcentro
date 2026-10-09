import { memo, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import {
  Box, MenuItem, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, Typography,
} from '@mui/material';
import CustomTablePagination from '../ui/CustomTablePagination';
import { etiquetaDeCustodia, etiquetaDeEstadoActivo } from '../../constants/activosConstants';
import { TODAS, etiquetaDeTipo, filtrarDetalle, opcionesDeTipo } from '../../utils/dashboardStats';

const FILAS_POR_PAGINA = 10;
const VACIO = '—';
const COLUMNAS = [
  'N°', 'Código', 'Tipo', 'Marca', 'Modelo', 'Serial', 'Custodio', 'Custodia',
  'Procesador', 'RAM (GB)', 'Almac. (GB)', 'IP', 'Dominio',
];

/**
 * En pantallas angostas se oculta el N° y se deja fija la columna Código, para
 * que al deslizar la tabla de lado siempre se sepa de qué equipo es cada fila.
 */
const estiloColumnaInicial = (indice) => {
  if (indice === 0) return { display: { xs: 'none', sm: 'table-cell' } };
  if (indice === 1) {
    return {
      position: { xs: 'sticky', sm: 'static' },
      left: 0,
      zIndex: 1,
      bgcolor: 'background.paper',
    };
  }
  return {};
};

const DetalleOficinaTable = memo(function DetalleOficinaTable({
  activos, filasOficina, subtipos, incluirFueraDeServicio = false,
}) {
  const [oficinaElegida, setOficinaElegida] = useState(null);
  const [tipo, setTipo] = useState('');
  const [page, setPage] = useState(0);

  // Por defecto se muestra la oficina con más equipos, que es la primera de la tabla de resumen.
  const idOficina = oficinaElegida ?? filasOficina[0]?.id_oficina ?? TODAS;

  // Con el interruptor encendido aparece la columna Estado, para distinguir los dados de baja o robados.
  const columnas = incluirFueraDeServicio ? [...COLUMNAS.slice(0, 8), 'Estado', ...COLUMNAS.slice(8)] : COLUMNAS;

  const equipos = useMemo(
    () => filtrarDetalle(activos, idOficina, tipo, incluirFueraDeServicio),
    [activos, idOficina, tipo, incluirFueraDeServicio]
  );
  // Si al apagar el interruptor la página elegida ya no existe, se muestra la última.
  const ultimaPagina = Math.max(0, Math.ceil(equipos.length / FILAS_POR_PAGINA) - 1);
  const pagina = Math.min(page, ultimaPagina);
  const visibles = equipos.slice(pagina * FILAS_POR_PAGINA, (pagina + 1) * FILAS_POR_PAGINA);

  return (
    <Paper variant="outlined" sx={{ p: { xs: 1.5, sm: 2 } }}>
      <Typography variant="h6" color="primary.dark" gutterBottom>
        Detalle de equipos por oficina
      </Typography>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: { xs: 1.5, sm: 2 }, mb: 2 }}>
        <TextField
          select
          size="small"
          label="Oficina"
          value={idOficina}
          onChange={(e) => { setOficinaElegida(e.target.value); setPage(0); }}
          sx={{ flex: '1 1 200px', maxWidth: { sm: 280 } }}
        >
          <MenuItem value={TODAS}>Todas las oficinas</MenuItem>
          {filasOficina.map((f) => (
            <MenuItem key={f.id_oficina} value={f.id_oficina}>{f.nombre}</MenuItem>
          ))}
        </TextField>
        <TextField
          select
          size="small"
          label="Tipo"
          value={tipo}
          onChange={(e) => { setTipo(e.target.value); setPage(0); }}
          sx={{ flex: '1 1 200px', maxWidth: { sm: 280 } }}
        >
          <MenuItem value="">Todos</MenuItem>
          {opcionesDeTipo(subtipos).map((o) => (
            <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>
          ))}
        </TextField>
        <Typography variant="body2" color="text.secondary" sx={{ width: { xs: '100%', md: 'auto' } }}>
          Equipos mostrados: <strong>{equipos.length}</strong>
        </Typography>
      </Box>

      <TableContainer tabIndex={0} role="region" aria-label="Tabla de detalle de equipos">
        <Table size="small" aria-label="Detalle de equipos">
          <TableHead>
            <TableRow>
              {columnas.map((titulo, i) => (
                <TableCell key={titulo} sx={{ whiteSpace: 'nowrap', ...estiloColumnaInicial(i) }}>
                  {titulo}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {visibles.length > 0 ? (
              visibles.map((a, i) => (
                <TableRow key={a.id_activo} hover>
                  <TableCell sx={estiloColumnaInicial(0)}>{pagina * FILAS_POR_PAGINA + i + 1}</TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap', ...estiloColumnaInicial(1) }}>
                    {a.codigo_inventario}
                  </TableCell>
                  <TableCell>{etiquetaDeTipo(a)}</TableCell>
                  <TableCell>{a.marca || VACIO}</TableCell>
                  <TableCell>{a.modelo || VACIO}</TableCell>
                  <TableCell>{a.serial || VACIO}</TableCell>
                  <TableCell>{a.empleado_asignado || VACIO}</TableCell>
                  <TableCell>{etiquetaDeCustodia(a)}</TableCell>
                  {incluirFueraDeServicio && <TableCell>{etiquetaDeEstadoActivo(a.estado)}</TableCell>}
                  <TableCell>{a.detalle?.procesador || ''}</TableCell>
                  <TableCell>{a.detalle?.ram_gb ?? ''}</TableCell>
                  <TableCell>{a.detalle?.almacenamiento_gb ?? ''}</TableCell>
                  <TableCell>{a.detalle?.ip || ''}</TableCell>
                  <TableCell>{a.detalle?.dominio || ''}</TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={columnas.length} align="center">
                  No hay equipos para esta selección.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
      <CustomTablePagination
        count={equipos.length}
        page={pagina}
        setPage={setPage}
        rowsPerPage={FILAS_POR_PAGINA}
      />
    </Paper>
  );
});

DetalleOficinaTable.propTypes = {
  activos: PropTypes.arrayOf(PropTypes.object).isRequired,
  filasOficina: PropTypes.arrayOf(PropTypes.object).isRequired,
  subtipos: PropTypes.arrayOf(PropTypes.object).isRequired,
  incluirFueraDeServicio: PropTypes.bool,
};

export default DetalleOficinaTable;
