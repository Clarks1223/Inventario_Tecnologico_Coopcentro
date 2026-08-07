import { memo } from 'react';
import PropTypes from 'prop-types';
import {
  Grid,
  TextField,
  Typography,
  FormGroup,
  FormControlLabel,
  Checkbox,
} from '@mui/material';

const ImpresoraFields = memo(function ImpresoraFields({ detailData, setDetailData }) {
  return (
    <>
      <Grid size={{ xs: 12 }}>
        <Typography variant="h6">Detalles Impresora Térmica</Typography>
      </Grid>
      <Grid size={{ xs: 12, sm: 6 }}>
        <TextField
          label="Tipo de Conexión"
          fullWidth
          value={detailData.tipo_conexion || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, tipo_conexion: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 12, sm: 6 }}>
        <TextField
          label="Modelo de Cabezal"
          fullWidth
          value={detailData.modelo_cabezal || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, modelo_cabezal: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 12, sm: 6 }}>
        <TextField
          label="Estado de Batería"
          fullWidth
          value={detailData.estado_bateria || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, estado_bateria: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 12 }}>
        <Typography variant="subtitle2" color="text.secondary">
          Accesorios que acompañan al equipo (se imprimen en el acta)
        </Typography>
        <FormGroup row>
          <FormControlLabel
            control={
              <Checkbox
                checked={Boolean(detailData.incluye_cargador)}
                onChange={(e) =>
                  setDetailData({ ...detailData, incluye_cargador: e.target.checked })
                }
              />
            }
            label="Cargador"
          />
          <FormControlLabel
            control={
              <Checkbox
                checked={Boolean(detailData.incluye_cable_usb)}
                onChange={(e) =>
                  setDetailData({ ...detailData, incluye_cable_usb: e.target.checked })
                }
              />
            }
            label="Cable USB"
          />
        </FormGroup>
      </Grid>
    </>
  );
});

ImpresoraFields.propTypes = {
  detailData: PropTypes.object.isRequired,
  setDetailData: PropTypes.func.isRequired,
};

export default ImpresoraFields;
