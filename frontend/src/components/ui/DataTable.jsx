import { memo } from 'react';
import PropTypes from 'prop-types';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TablePagination,
  Typography,
} from '@mui/material';
import LoadingSpinner from './LoadingSpinner';

const DataTable = memo(function DataTable({
  columns,
  data,
  isLoading,
  page,
  count,
  rowsPerPage = 10,
  onPageChange,
  keyExtractor,
  renderActions,
  emptyMessage = 'No hay registros.',
  ariaLabel = 'Tabla de datos',
}) {
  return (
    <TableContainer component={Paper}>
      <Table sx={{ minWidth: 650 }} aria-label={ariaLabel}>
        <caption style={{ captionSide: 'bottom', textAlign: 'center', padding: '8px', fontSize: '0.75rem', color: '#666' }}>
          {data.length} de {count} registros
        </caption>
        <TableHead>
          <TableRow>
            {columns.map((col) => (
              <TableCell key={col.key} align={col.align || 'left'} sx={col.sx}>
                {col.label}
              </TableCell>
            ))}
            {renderActions && (
              <TableCell align="right">Acciones</TableCell>
            )}
          </TableRow>
        </TableHead>
        <TableBody>
          {isLoading ? (
            <TableRow>
              <TableCell colSpan={columns.length + (renderActions ? 1 : 0)} align="center" sx={{ py: 3 }}>
                <LoadingSpinner />
              </TableCell>
            </TableRow>
          ) : data.length > 0 ? (
            data.map((row) => (
              <TableRow key={keyExtractor(row)}>
                {columns.map((col) => (
                  <TableCell key={col.key} align={col.align || 'left'}>
                    {col.render ? col.render(row) : row[col.key]}
                  </TableCell>
                ))}
                {renderActions && (
                  <TableCell align="right">
                    {renderActions(row)}
                  </TableCell>
                )}
              </TableRow>
            ))
          ) : (
            <TableRow>
              <TableCell colSpan={columns.length + (renderActions ? 1 : 0)} align="center" sx={{ py: 3 }}>
                <Typography variant="body1" color="text.secondary">
                  {emptyMessage}
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
        onPageChange={(e, newPage) => onPageChange(newPage)}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[rowsPerPage]}
        onRowsPerPageChange={() => {}}
      />
    </TableContainer>
  );
});

DataTable.propTypes = {
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      key: PropTypes.string.isRequired,
      label: PropTypes.node.isRequired,
      align: PropTypes.string,
      render: PropTypes.func,
      sx: PropTypes.object,
    })
  ).isRequired,
  data: PropTypes.array.isRequired,
  isLoading: PropTypes.bool,
  page: PropTypes.number,
  count: PropTypes.number,
  rowsPerPage: PropTypes.number,
  onPageChange: PropTypes.func.isRequired,
  keyExtractor: PropTypes.func.isRequired,
  renderActions: PropTypes.func,
  emptyMessage: PropTypes.string,
  ariaLabel: PropTypes.string,
};

export default DataTable;
