import { useState, useEffect, useCallback, useMemo } from 'react';
import { asignacionesService } from '../services/asignacionesService';
import { empleadosService } from '../services/empleadosService';
import { activosService } from '../services/activosService';
import { oficinasService } from '../services/oficinasService';
import { useSession } from './useSession';
import { useSnackbar } from './useSnackbar';
import { useConfirmDialog } from './useConfirmDialog';
import { ESTADO_ACTA, ESTADO_ACTIVO } from '../constants/activosConstants';
import { mensajeDeError } from '../utils/apiError';
import axios from 'axios';

export const useAsignaciones = () => {
  const [allAsignaciones, setAllAsignaciones] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [activos, setActivos] = useState([]);
  const [usuariosTi, setUsuariosTi] = useState([]);
  const [oficinas, setOficinas] = useState([]);
  const [cargos, setCargos] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [busquedaEmpleado, setBusquedaEmpleado] = useState('');
  const [oficinaFiltro, setOficinaFiltro] = useState(null);
  const [empleadoSeleccionado, setEmpleadoSeleccionado] = useState(null);

  const { sesion } = useSession();
  const showSnackbar = useSnackbar();
  const { confirmDialog, openConfirm, closeConfirm } = useConfirmDialog();

  const fetchAll = useCallback(async (signal) => {
    setIsLoading(true);
    try {
      const [actasRes, empleadosRes, activosRes, usuariosTiRes, oficinasRes, cargosRes] = await Promise.all([
        asignacionesService.getAsignaciones({ signal }),
        empleadosService.getEmpleados({ signal }),
        activosService.getActivos({ signal }),
        asignacionesService.getUsuariosTi(),
        oficinasService.getOficinas(),
        empleadosService.getCargos(),
      ]);
      setAllAsignaciones(actasRes.data || []);
      setEmpleados(empleadosRes.data || []);
      setActivos(activosRes.data || []);
      setUsuariosTi(usuariosTiRes.data || []);
      setOficinas(oficinasRes.data || []);
      setCargos(cargosRes.data || []);
    } catch (error) {
      if (!axios.isCancel(error)) console.error(error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    fetchAll(controller.signal);
    return () => controller.abort();
  }, [fetchAll]);

  const empleadoNombre = useCallback(
    (id) => {
      const emp = empleados.find((e) => e.id_empleado === id);
      return emp ? `${emp.nombre} ${emp.apellido}` : '';
    },
    [empleados]
  );

  const tecnicoNombre = useCallback(
    (idUsuarioTi) => {
      const usuarioTi = usuariosTi.find((u) => u.id_usuario_ti === idUsuarioTi);
      return usuarioTi ? empleadoNombre(usuarioTi.id_empleado) : '';
    },
    [usuariosTi, empleadoNombre]
  );

  const activoDe = useCallback(
    (id) => activos.find((a) => a.id_activo === id),
    [activos]
  );

  const oficinaNombre = useCallback(
    (id) => oficinas.find((o) => o.id_oficina === id)?.nombre || '',
    [oficinas]
  );

  const cargoNombre = useCallback(
    (id) => cargos.find((c) => c.id_cargo === id)?.nombre || '',
    [cargos]
  );

  const conteoActivasPorEmpleado = useMemo(() => {
    const conteo = {};
    allAsignaciones.forEach((a) => {
      if (a.estado_asignacion === ESTADO_ACTA.ACTIVA) {
        conteo[a.id_empleado] = (conteo[a.id_empleado] || 0) + 1;
      }
    });
    return conteo;
  }, [allAsignaciones]);

  const empleadosFiltrados = useMemo(() => {
    const texto = busquedaEmpleado.trim().toLowerCase();
    return empleados
      .filter((e) => e.activo)
      .filter((e) => !oficinaFiltro || e.id_oficina === oficinaFiltro)
      .filter(
        (e) =>
          !texto ||
          `${e.nombre} ${e.apellido}`.toLowerCase().includes(texto) ||
          e.cedula?.includes(texto) ||
          e.correo?.toLowerCase().includes(texto)
      )
      .sort((a, b) => `${a.nombre} ${a.apellido}`.localeCompare(`${b.nombre} ${b.apellido}`));
  }, [empleados, oficinaFiltro, busquedaEmpleado]);

  const empleadoActual = useMemo(
    () => empleados.find((e) => e.id_empleado === empleadoSeleccionado) || null,
    [empleados, empleadoSeleccionado]
  );

  const asignacionesDelEmpleado = useMemo(() => {
    if (!empleadoSeleccionado) return [];
    return allAsignaciones
      .filter((a) => a.id_empleado === empleadoSeleccionado && a.estado_asignacion === ESTADO_ACTA.ACTIVA)
      .sort((a, b) => new Date(b.fecha_asignacion) - new Date(a.fecha_asignacion))
      .map((a) => ({
        ...a,
        serial: activoDe(a.id_activo)?.serial || '',
        tipo_activo: activoDe(a.id_activo)?.tipo_activo || '',
        nombre_tecnico: tecnicoNombre(a.id_usuario_ti),
      }));
  }, [allAsignaciones, empleadoSeleccionado, activoDe, tecnicoNombre]);

  /**
   * Un activo se puede entregar si está operativo y su acta abierta es de
   * custodia (está en bodega). El activo ya no guarda si está asignado: eso
   * siempre lo dice su acta.
   */
  const activosDisponibles = useMemo(() => {
    const idsEnBodega = new Set(
      allAsignaciones
        .filter((a) => a.estado_asignacion === ESTADO_ACTA.CUSTODIA)
        .map((a) => a.id_activo)
    );
    return activos.filter(
      (a) => a.estado === ESTADO_ACTIVO.OPERATIVO && idsEnBodega.has(a.id_activo)
    );
  }, [activos, allAsignaciones]);

  /**
   * Archiva las actas y avisa dónde quedaron guardadas.
   *
   * Las actas ya no se abren en el navegador: el backend las escribe en su
   * carpeta y devuelve la ruta, que es lo que se muestra al usuario.
   */
  const archivarYAvisar = useCallback(async (pedirArchivado, mensajeSiEsReglaDeNegocio) => {
    try {
      const { rutas = [], carpeta = '', cantidad = 0 } = (await pedirArchivado()).data || {};
      showSnackbar(
        cantidad === 1
          ? `Acta guardada en: ${rutas[0]}`
          : `${cantidad} actas guardadas en: ${carpeta}`,
        'success'
      );
      return true;
    } catch (error) {
      // 400 = regla de negocio (tipo de activo sin plantilla): el backend manda
      // un mensaje legible. 500 = la plantilla no está instalada en el servidor.
      const esReglaDeNegocio = error.response?.status === 400;
      const mensaje = await mensajeDeError(
        error,
        esReglaDeNegocio
          ? mensajeSiEsReglaDeNegocio
          : 'No se pudo generar el acta. Verifique que las plantillas estén instaladas en el servidor'
      );
      showSnackbar(mensaje, esReglaDeNegocio ? 'warning' : 'error');
      return false;
    }
  }, [showSnackbar]);

  const handlePrint = useCallback((idActa, observacion = null) => {
    return archivarYAvisar(
      () => asignacionesService.archivarActa(idActa, observacion),
      'No se puede generar el acta para este activo'
    );
  }, [archivarYAvisar]);

  // Quien recibe la devolución es el usuario en sesión: firma el acta de
  // recepción y queda como custodio del activo en bodega.
  const handleReturn = useCallback(async (idActa, observacion) => {
    try {
      await asignacionesService.returnActivo(idActa, null, sesion?.id_usuario_ti, observacion);
      fetchAll();
      // El comentario ya quedó guardado en la devolución; no se reenvía.
      await archivarYAvisar(
        () => asignacionesService.archivarActa(idActa),
        'No se puede generar el acta para este activo'
      );
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al devolver el activo', 'error');
    }
  }, [fetchAll, showSnackbar, archivarYAvisar, sesion]);

  const handleReturnAll = useCallback(async (idEmpleado, observacion) => {
    try {
      const { data } = await asignacionesService.returnAllForEmpleado(
        idEmpleado, null, sesion?.id_usuario_ti, observacion
      );
      fetchAll();
      // Las actas del lote ya están cerradas, así que se archivan por id: el
      // endpoint del empleado parte de las asignaciones activas y ya no hay.
      const idsActas = (data || []).map((acta) => acta.id_acta);
      if (idsActas.length === 0) {
        showSnackbar('Este empleado no tenía activos por devolver', 'warning');
        return;
      }
      await archivarYAvisar(
        () => asignacionesService.archivarLote(idsActas),
        'No se pudieron generar las actas de devolución'
      );
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al devolver los activos', 'error');
    }
  }, [fetchAll, showSnackbar, archivarYAvisar, sesion]);

  const handleAssign = useCallback(async ({ id_activo, motivo }) => {
    try {
      await asignacionesService.assignActivo({
        id_activo,
        id_empleado: empleadoSeleccionado,
        id_usuario_ti: sesion.id_usuario_ti,
        motivo,
      });
      showSnackbar('Activo asignado correctamente', 'success');
      setOpen(false);
      fetchAll();
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al asignar el activo', 'error');
    }
  }, [empleadoSeleccionado, sesion, fetchAll, showSnackbar]);

  /**
   * Archiva todas las actas vigentes del empleado, agrupadas entre sí (la PC
   * con sus periféricos, el móvil con su impresora), y avisa la carpeta.
   */
  const handlePrintAllForEmpleado = useCallback((idEmpleado, observacion = null) => {
    return archivarYAvisar(
      () => asignacionesService.archivarActasEmpleado(idEmpleado, observacion),
      'Este empleado no tiene actas para generar'
    );
  }, [archivarYAvisar]);

  /**
   * Las cuatro acciones que producen un acta pasan por el mismo diálogo, que
   * pide el comentario opcional del documento. Cada una genera un acta
   * independiente, por eso cada una tiene su propio comentario.
   */
  const ACCIONES_ACTA = useMemo(() => ({
    imprimirActa: {
      titulo: 'Generar acta de entrega',
      descripcion: 'El acta se guardará en su carpeta. Al terminar se indica la ruta del archivo.',
      textoAccion: 'Generar',
      colorAccion: 'primary',
      ejecutar: (id, comentario) => handlePrint(id, comentario),
    },
    imprimirTodo: {
      titulo: 'Generar todas las actas del empleado',
      descripcion: 'Se guardarán en su carpeta las actas vigentes del empleado. Al terminar se indica la ruta.',
      textoAccion: 'Generar',
      colorAccion: 'primary',
      ejecutar: (id, comentario) => handlePrintAllForEmpleado(id, comentario),
    },
    devolver: {
      titulo: 'Registrar devolución',
      descripcion: '¿Deseas registrar la devolución de este activo? Se guardará el acta de recepción.',
      textoAccion: 'Devolver',
      colorAccion: 'error',
      ejecutar: (id, comentario) => handleReturn(id, comentario),
    },
    devolverTodo: {
      titulo: 'Devolver todos los activos',
      descripcion: '¿Deseas registrar la devolución de todos los activos de este empleado? Se guardarán sus actas de recepción.',
      textoAccion: 'Devolver todo',
      colorAccion: 'error',
      ejecutar: (id, comentario) => handleReturnAll(id, comentario),
    },
  }), [handlePrint, handlePrintAllForEmpleado, handleReturn, handleReturnAll]);

  const iniciarAccionActa = useCallback((tipo, id) => {
    openConfirm(tipo, id, '');
  }, [openConfirm]);

  const accionActaActual = ACCIONES_ACTA[confirmDialog.type] || null;

  const confirmarAccionActa = useCallback((comentario) => {
    const { type, id } = confirmDialog;
    closeConfirm();
    const accion = ACCIONES_ACTA[type];
    if (accion) accion.ejecutar(id, comentario);
  }, [confirmDialog, closeConfirm, ACCIONES_ACTA]);

  return {
    isLoading,
    oficinas,
    busquedaEmpleado,
    setBusquedaEmpleado,
    oficinaFiltro,
    setOficinaFiltro,
    empleadosFiltrados,
    empleadoSeleccionado,
    setEmpleadoSeleccionado,
    empleadoActual,
    asignacionesDelEmpleado,
    conteoActivasPorEmpleado,
    activosDisponibles,
    oficinaNombre,
    cargoNombre,
    open,
    setOpen,
    handleAssign,
    accionActaAbierta: confirmDialog.open,
    accionActaActual,
    iniciarAccionActa,
    confirmarAccionActa,
    cancelarAccionActa: closeConfirm,
  };
};
