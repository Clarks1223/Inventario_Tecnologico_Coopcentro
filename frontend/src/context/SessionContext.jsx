/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useCallback } from 'react';
import PropTypes from 'prop-types';
import { authService } from '../services/authService';

export const SessionContext = createContext(null);

const STORAGE_KEY = 'sesion_usuario';

const leerSesionGuardada = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

export function SessionProvider({ children }) {
  const [sesion, setSesion] = useState(leerSesionGuardada);

  const login = useCallback(async (correo, contrasena) => {
    const response = await authService.login(correo, contrasena);
    setSesion(response.data);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(response.data));
    return response.data;
  }, []);

  const logout = useCallback(() => {
    setSesion(null);
    localStorage.removeItem(STORAGE_KEY);
  }, []);

  return (
    <SessionContext.Provider value={{ sesion, login, logout }}>
      {children}
    </SessionContext.Provider>
  );
}

SessionProvider.propTypes = {
  children: PropTypes.node,
};
