import { useState, useEffect, useCallback, useMemo } from 'react';
import * as XLSX from 'xlsx';
import { activosService } from '../services/activosService';
import { oficinasService } from '../services/oficinasService';
import { empleadosService } from '../services/empleadosService';
import { asignacionesService } from '../services/asignacionesService';
import { useSnackbar } from './useSnackbar';
import { useSession } from './useSession';
import {
  ESTADO_ACTIVO,
  ESTADO_ACTA,
  CUSTODIA_FILTRO,
  esActaAbierta,
  etiquetaDeEstadoActivo,
  etiquetaDeCustodia,
} from '../constants/activosConstants';
import axios from 'axios';

export const useActivos = () => {
  const [allActivos, setAllActivos] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage] = useState(10);
  const [oficinas, setOficinas] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [asignaciones, setAsignaciones] = useState([]);
  const [filters, setFilters] = useState({
    tipo_activo: '',
    marca: '',
    modelo: '',
    serial: '',
    estado: '',
    custodia: '',
    id_oficina: '',
    ip: '',
    dominio: '',
  });
  const showSnackbar = useSnackbar();
  const { sesion } = useSession();

  const fetchActivos = useCallback(async (signal) => {
    setIsLoading(true);
    try {
      const response = await activosService.getActivos({ signal });
      setAllActivos(response.data || []);
    } catch (error) {
      if (!axios.isCancel(error)) console.error(error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const fetchOficinas = useCallback(async () => {
    try {
      const response = await oficinasService.getOficinas();
      setOficinas(response.data || []);
    } catch (error) {
      console.error(error);
    }
  }, []);

  const fetchEmpleados = useCallback(async () => {
    try {
      const response = await empleadosService.getEmpleados();
      setEmpleados(response.data || []);
    } catch (error) {
      console.error(error);
    }
  }, []);

  const fetchAsignaciones = useCallback(async () => {
    try {
      const response = await asignacionesService.getAsignaciones();
      setAsignaciones(response.data || []);
    } catch (error) {
      console.error(error);
    }
  }, []);

  useEffect(() => {
    fetchOficinas();
    fetchEmpleados();
    fetchAsignaciones();
  }, [fetchOficinas, fetchEmpleados, fetchAsignaciones]);

  useEffect(() => {
    const controller = new AbortController();
    fetchActivos(controller.signal);
    return () => controller.abort();
  }, [fetchActivos]);

  const oficinaNombre = useCallback((id) => oficinas.find((o) => o.id_oficina === id)?.nombre || '', [oficinas]);

  /**
   * Quién responde por el activo ahora mismo. Sale siempre del acta abierta:
   * el activo no guarda a quién está entregado. Si el acta es de custodia, el
   * custodio es el técnico de TI que lo tiene en bodega.
   */
  const custodioDe = useCallback((idActivo) => {
    const acta = asignaciones.find(
      (a) => a.id_activo === idActivo && esActaAbierta(a.estado_asignacion)
    );
    if (!acta) return { nombre: '', en_bodega: false };
    const empleado = empleados.find((e) => e.id_empleado === acta.id_empleado);
    return {
      nombre: empleado ? `${empleado.nombre} ${empleado.apellido}` : '',
      en_bodega: acta.estado_asignacion === ESTADO_ACTA.CUSTODIA,
    };
  }, [asignaciones, empleados]);

  // El custodio se resuelve antes de filtrar: así se puede filtrar por él y el
  // contador de la paginación cuadra con lo que realmente se muestra.
  const activosConCustodio = useMemo(
    () => allActivos.map((a) => {
      const custodio = custodioDe(a.id_activo);
      return {
        ...a,
        oficina_nombre: oficinaNombre(a.id_oficina),
        empleado_asignado: custodio.nombre,
        en_bodega: custodio.en_bodega,
      };
    }),
    [allActivos, custodioDe, oficinaNombre]
  );

  const coincideCustodia = useCallback((activo) => {
    if (!filters.custodia) return true;
    if (filters.custodia === CUSTODIA_FILTRO.BODEGA) return activo.en_bodega;
    // Entregado = tiene un acta de entrega abierta. Un activo dado de baja o
    // robado no tiene acta abierta, así que no cuenta como entregado.
    return !activo.en_bodega && Boolean(activo.empleado_asignado);
  }, [filters.custodia]);

  const filteredActivos = useMemo(() => {
    return activosConCustodio
      .filter(coincideCustodia)
      .filter((a) => !filters.tipo_activo || a.tipo_activo === filters.tipo_activo)
      .filter((a) => !filters.marca || a.marca?.toLowerCase().includes(filters.marca.toLowerCase()))
      .filter((a) => !filters.modelo || a.modelo?.toLowerCase().includes(filters.modelo.toLowerCase()))
      .filter((a) => !filters.serial || a.serial?.toLowerCase().includes(filters.serial.toLowerCase()))
      .filter((a) => !filters.estado || a.estado === filters.estado)
      .filter((a) => !filters.id_oficina || a.id_oficina === filters.id_oficina)
      .filter((a) => !filters.ip || a.detalle?.ip?.includes(filters.ip))
      .filter((a) => !filters.dominio || a.detalle?.dominio?.toLowerCase().includes(filters.dominio.toLowerCase()));
  }, [activosConCustodio, coincideCustodia, filters]);

  const activos = useMemo(
    () => filteredActivos.slice(page * rowsPerPage, (page + 1) * rowsPerPage),
    [filteredActivos, page, rowsPerPage]
  );

  const handleExportExcel = useCallback(() => {
    if (filteredActivos.length === 0) {
      showSnackbar('No hay datos para exportar', 'warning');
      return;
    }

    const rows = filteredActivos.map((row) => {
      const detalle = row.detalle || {};
      return {
        Código: row.codigo_inventario || '',
        Tipo: row.tipo_activo || '',
        Marca: row.marca || '',
        Modelo: row.modelo || '',
        Serial: row.serial || '',
        Estado: etiquetaDeEstadoActivo(row.estado),
        Oficina: row.oficina_nombre || '',
        Custodio: row.empleado_asignado || '',
        Custodia: etiquetaDeCustodia(row),
        Observaciones: row.observaciones || '',
        'Tipo Conexión': detalle.tipo_conexion || '',
        'Estado Batería': detalle.estado_bateria || '',
        'Modelo Cabezal': detalle.modelo_cabezal || '',
        'Tipo Dispositivo': detalle.tipo_dispositivo || '',
        'Sistema Operativo': detalle.sistema_operativo || '',
        IMEI: detalle.imei || '',
        'Número de Línea': detalle.numero_linea || '',
        Procesador: detalle.procesador || '',
        'RAM (GB)': detalle.ram_gb ?? '',
        'Tipo Almacenamiento': detalle.tipo_almacenamiento || '',
        'Almacenamiento (GB)': detalle.almacenamiento_gb ?? '',
        IP: detalle.ip || '',
        Dominio: detalle.dominio || '',
        Cargador: detalle.incluye_cargador === undefined || detalle.incluye_cargador === null
          ? '' : (detalle.incluye_cargador ? 'Sí' : 'No'),
        'Cable USB': detalle.incluye_cable_usb === undefined || detalle.incluye_cable_usb === null
          ? '' : (detalle.incluye_cable_usb ? 'Sí' : 'No'),
        'Fecha Creación': row.created_at || '',
        'Última Actualización': row.updated_at || '',
      };
    });

    const worksheet = XLSX.utils.json_to_sheet(rows);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Activos');
    XLSX.writeFile(workbook, `Activos_${new Date().toISOString().split('T')[0]}.xlsx`);
  }, [filteredActivos, showSnackbar]);

  /**
   * Cambiar la situación del activo cierra su acta abierta en el backend, sea
   * de custodia o de entrega: por eso se puede reportar como robado un equipo
   * que estaba en manos de un empleado, sin simular antes una devolución.
   */
  const cambiarSituacion = useCallback(async (id, estado, mensajeExito, mensajeError) => {
    try {
      const activo = allActivos.find((a) => a.id_activo === id);
      if (!activo) return;
      await activosService.updateActivo(id, {
        ...activo,
        estado,
        id_usuario_ti: sesion?.id_usuario_ti,
      });
      fetchActivos();
      showSnackbar(mensajeExito, 'success');
    } catch (error) {
      showSnackbar(error.response?.data?.message || mensajeError, 'error');
    }
  }, [allActivos, fetchActivos, showSnackbar, sesion]);

  const handleDecommission = useCallback(
    (id) => cambiarSituacion(id, ESTADO_ACTIVO.DADO_DE_BAJA,
      'Activo dado de baja correctamente.', 'Error al dar de baja'),
    [cambiarSituacion]
  );

  const handleReportStolen = useCallback(
    (id) => cambiarSituacion(id, ESTADO_ACTIVO.ROBADO_PERDIDO,
      'Activo reportado como robado/perdido.', 'Error al reportar el activo'),
    [cambiarSituacion]
  );

  return {
    activos,
    isLoading,
    page,
    setPage,
    count: filteredActivos.length,
    rowsPerPage,
    oficinas,
    filters,
    setFilters,
    fetchActivos,
    handleDecommission,
    handleReportStolen,
    handleExportExcel,
  };
};
