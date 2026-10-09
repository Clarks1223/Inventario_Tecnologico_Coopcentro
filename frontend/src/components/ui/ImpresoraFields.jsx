import { memo, useEffect } from 'react';
import PropTypes from 'prop-types';
import {
  Grid,
  TextField,
  MenuItem,
  Typography,
  FormGroup,
  FormControlLabel,
  Checkbox,
} from '@mui/material';
import { TIPO_CONEXION_IMPRESORA_OPTIONS } from '../../constants/activosConstants';

const ImpresoraFields = memo(function ImpresoraFields({ detailData, setDetailData }) {
  useEffect(() => {
    setDetailData((prev) =>
      prev.tipo_conexion ? prev : { ...prev, tipo_conexion: 'bluetooth' }
    );
  }, [setDetailData]);

  return (
    <>
      <Grid size={{ xs: 12 }}>
        <Typography variant="h6">Detalles Impresora Térmica</Typography>
      </Grid>
      <Grid size={{ xs: 12, sm: 6 }}>
        <TextField
          select
          label="Tipo de Conexión"
          fullWidth
          value={detailData.tipo_conexion || 'bluetooth'}
          onChange={(e) =>
            setDetailData({ ...detailData, tipo_conexion: e.target.value })
          }
        >
          {TIPO_CONEXION_IMPRESORA_OPTIONS.map((opcion) => (
            <MenuItem key={opcion.value} value={opcion.value}>
              {opcion.label}
            </MenuItem>
          ))}
        </TextField>
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
