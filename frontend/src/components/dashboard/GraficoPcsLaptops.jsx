import { memo } from 'react';
import PropTypes from 'prop-types';
import { Box, Paper, Tooltip, Typography } from '@mui/material';

const COLOR_PCS = 'primary.main';
const COLOR_LAPTOPS = 'secondary.main';

const Segmento = ({ cantidad, color, titulo }) =>
  cantidad > 0 ? (
    <Tooltip title={`${titulo}: ${cantidad}`} arrow>
      <Box
        sx={{
          flex: `${cantidad} 0 0`,
          bgcolor: color,
          color: 'common.white',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: 12,
          fontWeight: 600,
          minWidth: 0,
        }}
      >
        {cantidad}
      </Box>
    </Tooltip>
  ) : null;

Segmento.propTypes = {
  cantidad: PropTypes.number.isRequired,
  color: PropTypes.string.isRequired,
  titulo: PropTypes.string.isRequired,
};

const Leyenda = ({ color, texto }) => (
  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
    <Box sx={{ width: 12, height: 12, borderRadius: 0.5, bgcolor: color }} />
    <Typography variant="caption">{texto}</Typography>
  </Box>
);

Leyenda.propTypes = { color: PropTypes.string.isRequired, texto: PropTypes.string.isRequired };

/** Barras apiladas horizontales hechas solo con Material UI, sin librería de gráficos. */
const GraficoPcsLaptops = memo(function GraficoPcsLaptops({ filas }) {
  const maximo = Math.max(1, ...filas.map((f) => f.pcs_laptops));

  return (
    <Paper variant="outlined" sx={{ p: { xs: 1.5, sm: 2 } }}>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', gap: 1, mb: 2 }}>
        <Typography variant="h6" color="primary.dark">
          PCs y Laptops por oficina
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Leyenda color={COLOR_PCS} texto="PCs (desktop)" />
          <Leyenda color={COLOR_LAPTOPS} texto="Laptops" />
        </Box>
      </Box>
      <Box component="ul" sx={{ listStyle: 'none', m: 0, p: 0, display: 'grid', gap: 1 }}>
        {filas.map((fila) => (
          <Box
            component="li"
            key={fila.id_oficina}
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: '90px 1fr 32px', sm: '130px 1fr 40px' },
              alignItems: 'center',
              gap: 1,
            }}
          >
            <Typography variant="body2" noWrap title={fila.nombre}>{fila.nombre}</Typography>
            <Box sx={{ bgcolor: 'action.hover', borderRadius: 0.5, height: 24 }}>
              <Box
                sx={{
                  display: 'flex',
                  height: '100%',
                  width: `${(fila.pcs_laptops / maximo) * 100}%`,
                  borderRadius: 0.5,
                  overflow: 'hidden',
                }}
              >
                <Segmento cantidad={fila.pcs} color={COLOR_PCS} titulo="PCs" />
                <Segmento cantidad={fila.laptops} color={COLOR_LAPTOPS} titulo="Laptops" />
              </Box>
            </Box>
            <Typography variant="body2" fontWeight={700} align="right">{fila.pcs_laptops}</Typography>
          </Box>
        ))}
      </Box>
    </Paper>
  );
});

GraficoPcsLaptops.propTypes = {
  filas: PropTypes.arrayOf(PropTypes.object).isRequired,
};

export default GraficoPcsLaptops;
