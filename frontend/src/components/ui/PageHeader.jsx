import { memo } from 'react';
import PropTypes from 'prop-types';
import { Box, Typography } from '@mui/material';

const PageHeader = memo(function PageHeader({ title, children }) {
    return (
        <Box
            sx={{
                display: 'flex',
                flexDirection: { xs: 'column', sm: 'row' },
                justifyContent: 'space-between',
                alignItems: { xs: 'flex-start', sm: 'center' },
                mb: 3,
                gap: 2,
            }}
        >
            <Typography variant="h4" component="h1">{title}</Typography>
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
        </Box>
    );
});

PageHeader.propTypes = {
  title: PropTypes.string.isRequired,
  children: PropTypes.node,
};

export default PageHeader;
