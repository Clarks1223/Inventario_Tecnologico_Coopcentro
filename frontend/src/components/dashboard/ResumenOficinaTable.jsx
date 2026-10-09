import { memo } from 'react';
import PropTypes from 'prop-types';
import {
  Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography,
} from '@mui/material';

const formatoPorcentaje = (v) => `${(v * 100).toFixed(1)}%`;

const celdaEncabezado = {
  bgcolor: 'primary.dark',
  color: 'common.white',
  fontWeight: 600,
  textAlign: 'center',
  whiteSpace: 'nowrap',
};

const celdaFija = { position: 'sticky', left: 0, zIndex: 1 };

const ResumenOficinaTable = memo(function ResumenOficinaTable({
  filas, totales, subtipos, sinOficinaListada, incluirFueraDeServicio = false,
}) {
  const hayMoviles = totales.moviles > 0;
  // Cada columna sabe leer su valor tanto de una fila de oficina como de la fila de totales.
  const columnas = [
    { clave: 'pcs', titulo: 'PCs (desktop)', valor: (f) => f.pcs },
    { clave: 'laptops', titulo: 'Laptops', valor: (f) => f.laptops },
    { clave: 'pcs_laptops', titulo: 'PCs + Laptops', valor: (f) => f.pcs_laptops, negrita: true },
    { clave: 'perifericos', titulo: 'Periféricos', valor: (f) => f.perifericos },
    ...subtipos.map((s) => ({
      clave: `subtipo-${s.clave}`,
      titulo: s.etiqueta,
      valor: (f) => f.subtipos[s.clave],
      secundaria: true,
    })),
    { clave: 'impresoras', titulo: 'Impresora térmica', valor: (f) => f.impresoras },
    ...(hayMoviles ? [{ clave: 'moviles', titulo: 'Móviles', valor: (f) => f.moviles }] : []),
    { clave: 'total', titulo: 'Total equipos', valor: (f) => f.total, negrita: true },
    { clave: 'entregados', titulo: 'Entregados', valor: (f) => f.entregados },
    { clave: 'en_bodega', titulo: 'En bodega (TI)', valor: (f) => f.en_bodega },
    // Solo con el interruptor encendido: así cada fila sigue cuadrando con su total.
    ...(incluirFueraDeServicio
      ? [{ clave: 'fuera_de_servicio', titulo: 'Baja / Robado', valor: (f) => f.fuera_de_servicio }]
      : []),
  ];

  return (
    <Paper variant="outlined" sx={{ p: { xs: 1.5, sm: 2 } }}>
      <Typography variant="h6" color="primary.dark" gutterBottom>
        Resumen por oficina
      </Typography>
      {/* En pantallas angostas la tabla se desliza de lado y la columna Oficina queda fija. */}
      <TableContainer tabIndex={0} role="region" aria-label="Tabla de resumen por oficina">
        <Table size="small" aria-label="Resumen de equipos por oficina">
          <TableHead>
            <TableRow>
              <TableCell sx={{ ...celdaEncabezado, ...celdaFija, textAlign: 'left', zIndex: 2 }}>
                Oficina
              </TableCell>
              {columnas.map((c) => (
                <TableCell
                  key={c.clave}
                  sx={{ ...celdaEncabezado, ...(c.secundaria && { bgcolor: 'primary.main' }) }}
                >
                  {c.titulo}
                </TableCell>
              ))}
              <TableCell sx={celdaEncabezado}>% del total</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filas.map((fila) => (
              <TableRow
                key={fila.id_oficina}
                hover
                sx={{
                  '&:nth-of-type(even)': {
                    bgcolor: 'action.hover',
                    // La celda fija es opaca: el rayado se le superpone en vez de dejarla transparente.
                    '& > td:first-of-type': {
                      backgroundImage: (theme) =>
                        `linear-gradient(${theme.palette.action.hover}, ${theme.palette.action.hover})`,
                    },
                  },
                }}
              >
                <TableCell sx={{ ...celdaFija, bgcolor: 'background.paper', fontWeight: 600, whiteSpace: 'nowrap' }}>
                  {fila.nombre}
                </TableCell>
                {columnas.map((c) => (
                  <TableCell key={c.clave} align="center" sx={{ fontWeight: c.negrita ? 700 : 400 }}>
                    {c.valor(fila)}
                  </TableCell>
                ))}
                <TableCell align="center">{formatoPorcentaje(fila.porcentaje)}</TableCell>
              </TableRow>
            ))}
            <TableRow>
              <TableCell sx={{ ...celdaEncabezado, ...celdaFija, textAlign: 'left' }}>TOTAL</TableCell>
              {columnas.map((c) => (
                <TableCell key={c.clave} sx={celdaEncabezado}>{c.valor(totales)}</TableCell>
              ))}
              <TableCell sx={celdaEncabezado}>{formatoPorcentaje(totales.total === 0 ? 0 : 1)}</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </TableContainer>
      <Typography
        variant="caption"
        color={sinOficinaListada > 0 ? 'warning.main' : 'text.secondary'}
        sx={{ display: 'block', mt: 1, fontStyle: 'italic' }}
      >
        Control: equipos con oficina no listada arriba → {sinOficinaListada}
      </Typography>
    </Paper>
  );
});

ResumenOficinaTable.propTypes = {
  filas: PropTypes.arrayOf(PropTypes.object).isRequired,
  totales: PropTypes.object.isRequired,
  subtipos: PropTypes.arrayOf(PropTypes.object).isRequired,
  sinOficinaListada: PropTypes.number.isRequired,
  incluirFueraDeServicio: PropTypes.bool,
};

export default ResumenOficinaTable;
