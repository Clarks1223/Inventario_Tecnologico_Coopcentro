import { useId, useState } from 'react';
import PropTypes from 'prop-types';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  TextField,
} from '@mui/material';

// Lo que cabe en el campo Observacion_General del acta impresa.
export const MAX_COMENTARIO_ACTA = 100;

/**
 * Pide el comentario que saldrá impreso en el acta antes de ejecutar la acción.
 *
 * Se muestra en las cuatro acciones que generan un documento (imprimir una
 * acta, imprimirlas todas, registrar una devolución y devolver todo), porque
 * cada una produce un acta independiente que se firma por separado.
 *
 * El padre lo monta solo mientras está abierto: así cada apertura empieza con
 * el campo vacío sin necesidad de resetearlo con un efecto.
 */
const ComentarioActaDialog = ({
  titulo,
  descripcion,
  textoAccion = 'Aceptar',
  colorAccion = 'primary',
  onCancel,
  onConfirm,
}) => {
  const [comentario, setComentario] = useState('');
  const titleId = useId();

  return (
    <Dialog open onClose={onCancel} maxWidth="sm" fullWidth aria-labelledby={titleId}>
      <DialogTitle id={titleId}>{titulo}</DialogTitle>
      <DialogContent>
        {descripcion ? <DialogContentText>{descripcion}</DialogContentText> : null}
        <TextField
          margin="dense"
          label="Comentario para el acta (opcional)"
          fullWidth
          multiline
          rows={2}
          value={comentario}
          onChange={(e) => setComentario(e.target.value)}
          slotProps={{ htmlInput: { maxLength: MAX_COMENTARIO_ACTA } }}
          helperText={`Puedes dejarlo vacío. Se imprime en el apartado de observaciones del acta. ${comentario.length}/${MAX_COMENTARIO_ACTA} caracteres.`}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel}>Cancelar</Button>
        <Button onClick={() => onConfirm(comentario)} color={colorAccion} variant="contained">
          {textoAccion}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

ComentarioActaDialog.propTypes = {
  titulo: PropTypes.string,
  descripcion: PropTypes.string,
  textoAccion: PropTypes.string,
  colorAccion: PropTypes.string,
  onCancel: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
};

export default ComentarioActaDialog;
