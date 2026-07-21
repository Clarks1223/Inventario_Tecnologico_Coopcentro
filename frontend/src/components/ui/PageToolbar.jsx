import { memo } from 'react';
import PropTypes from 'prop-types';
import { Box } from '@mui/material';

const PageToolbar = memo(function PageToolbar({ children }) {
  return (
    <Box
      sx={{
        display: 'flex',
        gap: 1,
        width: { xs: '100%', sm: 'auto' },
        flexDirection: { xs: 'column', sm: 'row' },
      }}
    >
      {children}
    </Box>
  );
});

PageToolbar.propTypes = {
  children: PropTypes.node,
};

export default PageToolbar;
