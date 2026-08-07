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

const DispositivoMovilFields = memo(function DispositivoMovilFields({ detailData, setDetailData }) {
  return (
    <>
      <Grid size={{ xs: 12 }}>
        <Typography variant="h6">Detalles Dispositivo Móvil</Typography>
      </Grid>
      <Grid size={{ xs: 12, sm: 6 }}>
        <TextField
          label="Tipo de Dispositivo"
          fullWidth
          value={detailData.tipo_dispositivo || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, tipo_dispositivo: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 12, sm: 6 }}>
        <TextField
          label="Sistema Operativo"
          fullWidth
          value={detailData.sistema_operativo || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, sistema_operativo: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 12, sm: 6 }}>
        <TextField
          label="IMEI"
          fullWidth
          value={detailData.imei || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, imei: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 12, sm: 6 }}>
        <TextField
          label="Número de Línea"
          fullWidth
          value={detailData.numero_linea || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, numero_linea: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 12, sm: 6 }}>
        <TextField
          label="Almacenamiento (GB)"
          type="number"
          fullWidth
          value={detailData.almacenamiento_gb || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, almacenamiento_gb: e.target.value })
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

DispositivoMovilFields.propTypes = {
  detailData: PropTypes.object.isRequired,
  setDetailData: PropTypes.func.isRequired,
};

export default DispositivoMovilFields;
