import { memo } from 'react';
import PropTypes from 'prop-types';
import { TablePagination } from '@mui/material';

const CustomTablePagination = memo(function CustomTablePagination({ count, page, setPage, rowsPerPage = 10, fetchData }) {
    return (
        <TablePagination
            component="div"
            count={count}
            page={page}
            onPageChange={(e, newPage) => {
                setPage(newPage);
                if (fetchData) {
                    fetchData(newPage);
                }
            }}
            rowsPerPage={rowsPerPage}
            rowsPerPageOptions={[rowsPerPage]}
            onRowsPerPageChange={() => { }}
        />
    );
});

CustomTablePagination.propTypes = {
  count: PropTypes.number.isRequired,
  page: PropTypes.number.isRequired,
  setPage: PropTypes.func.isRequired,
  rowsPerPage: PropTypes.number,
  fetchData: PropTypes.func,
};

export default CustomTablePagination;
