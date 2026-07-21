import { memo } from 'react';
import PropTypes from 'prop-types';
import { Grid, Autocomplete, TextField, Typography } from '@mui/material';

const SUGERENCIAS_TIPO_PERIFERICO = ['Teclado', 'Mouse', 'Monitor'];

const PerifericoFields = memo(function PerifericoFields({ detailData, setDetailData }) {
  return (
    <>
      <Grid size={{ xs: 12 }}>
        <Typography variant="h6">Detalles del Periférico</Typography>
      </Grid>
      <Grid size={{ xs: 12 }}>
        <Autocomplete
          freeSolo
          options={SUGERENCIAS_TIPO_PERIFERICO}
          value={detailData.tipo_dispositivo || ''}
          onInputChange={(event, newValue) =>
            setDetailData({ ...detailData, tipo_dispositivo: newValue })
          }
          renderInput={(params) => (
            <TextField {...params} label="Tipo de Periférico" fullWidth />
          )}
        />
      </Grid>
    </>
  );
});

PerifericoFields.propTypes = {
  detailData: PropTypes.object.isRequired,
  setDetailData: PropTypes.func.isRequired,
};

export default PerifericoFields;
