import { memo, useId } from 'react';
import PropTypes from 'prop-types';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  TextField,
  MenuItem,
  Button,
} from '@mui/material';
import ComputoFields from './ComputoFields';
import DispositivoMovilFields from './DispositivoMovilFields';
import ImpresoraFields from './ImpresoraFields';
import PerifericoFields from './PerifericoFields';

const ActivoFormModal = memo(function ActivoFormModal({ editorProps, oficinas = [] }) {
  const {
    open,
    setOpen,
    tipo,
    setTipo,
    baseData,
    setBaseData,
    detailData,
    setDetailData,
    isEditing,
    handleSave,
  } = editorProps;

  const titleId = useId();
  const contentId = useId();

  return (
    <>
      <Dialog
        open={open}
        onClose={() => setOpen(false)}
        maxWidth="md"
        fullWidth
        aria-labelledby={titleId}
        aria-describedby={contentId}
      >
        <DialogTitle id={titleId}>
          {isEditing ? 'Editar Activo' : 'Registrar Activo'}
        </DialogTitle>
        <DialogContent id={contentId}>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid size={{ xs: 12 }}>
              <TextField
                select
                label="Tipo de Activo"
                fullWidth
                value={tipo}
                onChange={(e) => setTipo(e.target.value)}
                disabled={isEditing}
              >
                <MenuItem value="periferico">Periférico</MenuItem>
                <MenuItem value="desktop">Desktop</MenuItem>
                <MenuItem value="laptop">Laptop</MenuItem>
                <MenuItem value="dispositivo_movil">Dispositivo Móvil</MenuItem>
                <MenuItem value="impresora_termica">Impresora Térmica</MenuItem>
              </TextField>
            </Grid>

            <Grid size={{ xs: 12 }}>
              <TextField
                select
                label="Oficina Base"
                fullWidth
                value={baseData.id_oficina || ''}
                onChange={(e) =>
                  setBaseData({ ...baseData, id_oficina: e.target.value })
                }
              >
                <MenuItem value=""><em>Ninguna</em></MenuItem>
                {oficinas.map((of) => (
                  <MenuItem key={of.id_oficina} value={of.id_oficina}>
                    {of.nombre}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid size={{ xs: 6 }}>
              <TextField
                label="Marca"
                fullWidth
                value={baseData.marca || ''}
                onChange={(e) =>
                  setBaseData({ ...baseData, marca: e.target.value })
                }
              />
            </Grid>
            <Grid size={{ xs: 6 }}>
              <TextField
                label="Modelo"
                fullWidth
                value={baseData.modelo || ''}
                onChange={(e) =>
                  setBaseData({ ...baseData, modelo: e.target.value })
                }
              />
            </Grid>
            <Grid size={{ xs: 6 }}>
              <TextField
                label="Serial"
                fullWidth
                value={baseData.serial || ''}
                onChange={(e) =>
                  setBaseData({ ...baseData, serial: e.target.value })
                }
                disabled={isEditing}
              />
            </Grid>

            {['desktop', 'laptop'].includes(tipo) ? (
              <ComputoFields detailData={detailData} setDetailData={setDetailData} />
            ) : null}

            {tipo === 'dispositivo_movil' ? (
              <DispositivoMovilFields detailData={detailData} setDetailData={setDetailData} />
            ) : null}

            {tipo === 'impresora_termica' ? (
              <ImpresoraFields detailData={detailData} setDetailData={setDetailData} />
            ) : null}

            {tipo === 'periferico' ? (
              <PerifericoFields detailData={detailData} setDetailData={setDetailData} />
            ) : null}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button onClick={handleSave} variant="contained">
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
});

ActivoFormModal.propTypes = {
  editorProps: PropTypes.shape({
    open: PropTypes.bool,
    setOpen: PropTypes.func,
    tipo: PropTypes.string,
    setTipo: PropTypes.func,
    baseData: PropTypes.object,
    setBaseData: PropTypes.func,
    detailData: PropTypes.object,
    setDetailData: PropTypes.func,
    isEditing: PropTypes.bool,
    handleSave: PropTypes.func,
  }).isRequired,
  oficinas: PropTypes.array,
};

export default ActivoFormModal;
