import { memo } from 'react';
import PropTypes from 'prop-types';
import { Box, Paper, Typography } from '@mui/material';
import { alpha } from '@mui/material/styles';

/**
 * Barra superior de indicadores: cada celda lleva su título sobre fondo azul
 * oscuro y el valor debajo sobre fondo azul claro. Si hay muchas celdas, la
 * barra baja a una segunda fila en lugar de comprimirlas.
 */
const BarraKpis = memo(function BarraKpis({ items }) {
  return (
    <Paper
      variant="outlined"
      sx={{
        display: 'grid',
        // En celular quedan dos celdas por fila; en pantallas mayores se reparten según el ancho.
        gridTemplateColumns: { xs: 'repeat(2, 1fr)', sm: 'repeat(auto-fit, minmax(130px, 1fr))' },
        gap: '1px',
        bgcolor: 'background.paper',
        overflow: 'hidden',
      }}
    >
      {items.map((item) => (
        <Box key={item.id} sx={{ display: 'flex', flexDirection: 'column' }}>
          <Typography
            variant="caption"
            component="div"
            title={item.label}
            sx={{
              bgcolor: item.secundario ? 'primary.main' : 'primary.dark',
              color: 'common.white',
              fontWeight: 600,
              textAlign: 'center',
              px: 1,
              py: 0.5,
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
            }}
          >
            {item.label}
          </Typography>
          <Typography
            variant="h4"
            component="p"
            sx={{
              flex: 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              py: { xs: 0.75, sm: 1 },
              fontSize: { xs: '1.6rem', sm: '2.125rem' },
              fontWeight: 700,
              color: 'primary.dark',
              bgcolor: (theme) => alpha(theme.palette.primary.main, 0.12),
            }}
          >
            {item.value}
          </Typography>
        </Box>
      ))}
    </Paper>
  );
});

BarraKpis.propTypes = {
  items: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      value: PropTypes.number.isRequired,
      secundario: PropTypes.bool,
    })
  ).isRequired,
};

export default BarraKpis;
