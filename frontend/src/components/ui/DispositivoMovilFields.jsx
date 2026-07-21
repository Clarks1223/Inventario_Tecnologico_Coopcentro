import { memo } from 'react';
import PropTypes from 'prop-types';
import { Grid, TextField, Typography } from '@mui/material';

const DispositivoMovilFields = memo(function DispositivoMovilFields({ detailData, setDetailData }) {
  return (
    <>
      <Grid size={{ xs: 12 }}>
        <Typography variant="h6">Detalles Dispositivo Móvil</Typography>
      </Grid>
      <Grid size={{ xs: 6 }}>
        <TextField
          label="Tipo de Dispositivo"
          fullWidth
          value={detailData.tipo_dispositivo || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, tipo_dispositivo: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 6 }}>
        <TextField
          label="Sistema Operativo"
          fullWidth
          value={detailData.sistema_operativo || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, sistema_operativo: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 6 }}>
        <TextField
          label="IMEI"
          fullWidth
          value={detailData.imei || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, imei: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 6 }}>
        <TextField
          label="Número de Línea"
          fullWidth
          value={detailData.numero_linea || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, numero_linea: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 6 }}>
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
    </>
  );
});

DispositivoMovilFields.propTypes = {
  detailData: PropTypes.object.isRequired,
  setDetailData: PropTypes.func.isRequired,
};

export default DispositivoMovilFields;
