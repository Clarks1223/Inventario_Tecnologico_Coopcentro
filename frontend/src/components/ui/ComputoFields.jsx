import { memo } from 'react';
import PropTypes from 'prop-types';
import { Grid, TextField, Typography } from '@mui/material';

const ComputoFields = memo(function ComputoFields({ detailData, setDetailData }) {
  return (
    <>
      <Grid size={{ xs: 12 }}>
        <Typography variant="h6">Detalles de Cómputo</Typography>
      </Grid>
      <Grid size={{ xs: 6 }}>
        <TextField
          label="Procesador"
          fullWidth
          value={detailData.procesador || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, procesador: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 6 }}>
        <TextField
          label="RAM (GB)"
          type="number"
          fullWidth
          value={detailData.ram_gb || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, ram_gb: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 6 }}>
        <TextField
          label="Tipo de Almacenamiento"
          fullWidth
          value={detailData.tipo_almacenamiento || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, tipo_almacenamiento: e.target.value })
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
      <Grid size={{ xs: 6 }}>
        <TextField
          label="IP"
          fullWidth
          value={detailData.ip || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, ip: e.target.value })
          }
        />
      </Grid>
      <Grid size={{ xs: 6 }}>
        <TextField
          label="Dominio"
          fullWidth
          value={detailData.dominio || ''}
          onChange={(e) =>
            setDetailData({ ...detailData, dominio: e.target.value })
          }
        />
      </Grid>
    </>
  );
});

ComputoFields.propTypes = {
  detailData: PropTypes.object.isRequired,
  setDetailData: PropTypes.func.isRequired,
};

export default ComputoFields;
