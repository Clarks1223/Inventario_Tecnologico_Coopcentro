import { memo, useId } from 'react';
import PropTypes from 'prop-types';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';

const CustomDialog = memo(function CustomDialog({ open, setOpen, title, children, handleSave, saveLabel = "Guardar", cancelLabel = "Cancelar", maxWidth = "md", fullWidth = true }) {
    const titleId = useId();
    return (
        <Dialog open={open} onClose={() => setOpen(false)} maxWidth={maxWidth} fullWidth={fullWidth} aria-labelledby={titleId}>
            <DialogTitle id={titleId}>{title}</DialogTitle>
            <DialogContent>{children}</DialogContent>
            <DialogActions>
                <Button onClick={() => setOpen(false)}>{cancelLabel}</Button>
                {handleSave && (
                    <Button onClick={handleSave} variant="contained">
                        {saveLabel}
                    </Button>
                )}
            </DialogActions>
        </Dialog>
    );
});

CustomDialog.propTypes = {
  open: PropTypes.bool.isRequired,
  setOpen: PropTypes.func.isRequired,
  title: PropTypes.string.isRequired,
  children: PropTypes.node,
  handleSave: PropTypes.func,
  saveLabel: PropTypes.string,
  cancelLabel: PropTypes.string,
  maxWidth: PropTypes.string,
  fullWidth: PropTypes.bool,
};

export default CustomDialog;
