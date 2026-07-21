import { Navigate, Outlet } from 'react-router-dom';
import { useSession } from '../../hooks/useSession';

const RequireAuth = () => {
  const { sesion } = useSession();

  if (!sesion) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

export default RequireAuth;
