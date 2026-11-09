import { useState, useEffect, useCallback, useMemo } from 'react';
import axios from 'axios';
import { gruposService } from '../services/gruposService';
import { activosService } from '../services/activosService';
import { useSnackbar } from './useSnackbar';
import { activosAgrupables } from '../utils/grupos';

const GRUPO_VACIO = { nombre: '', descripcion: '', ids_activos: [] };

export const useGrupos = () => {
  const [grupos, setGrupos] = useState([]);
  const [activos, setActivos] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [rowsPerPage] = useState(10);
  const [open, setOpen] = useState(false);
  const [currentGrupo, setCurrentGrupo] = useState(GRUPO_VACIO);
  const [isSaving, setIsSaving] = useState(false);
  const [grupoPorEliminar, setGrupoPorEliminar] = useState(null);
  const showSnackbar = useSnackbar();

  const fetchAll = useCallback(async (signal) => {
    setIsLoading(true);
    try {
      const [gruposRes, activosRes] = await Promise.all([
        gruposService.getGrupos({ signal }),
        activosService.getActivos({ signal }),
      ]);
      setGrupos(gruposRes.data || []);
      setActivos(activosRes.data || []);
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

  const gruposOrdenados = useMemo(
    () => [...grupos].sort((a, b) => a.nombre.localeCompare(b.nombre)),
    [grupos]
  );

  const gruposPagina = useMemo(
    () => gruposOrdenados.slice(page * rowsPerPage, (page + 1) * rowsPerPage),
    [gruposOrdenados, page, rowsPerPage]
  );

  // Se ofrecen los activos libres más los que ya son del grupo que se edita,
  // para que estos últimos sigan visibles como elegidos.
  const opcionesDeActivos = useMemo(() => {
    const libres = activosAgrupables(activos);
    const propios = activos.filter(
      (a) => currentGrupo.id_grupo && a.id_grupo === currentGrupo.id_grupo
    );
    return [...propios, ...libres];
  }, [activos, currentGrupo.id_grupo]);

  const openDialog = useCallback((grupo = null) => {
    setCurrentGrupo(
      grupo
        ? {
            id_grupo: grupo.id_grupo,
            nombre: grupo.nombre,
            descripcion: grupo.descripcion || '',
            ids_activos: (grupo.activos || []).map((a) => a.id_activo),
            ids_originales: (grupo.activos || []).map((a) => a.id_activo),
          }
        : GRUPO_VACIO
    );
    setOpen(true);
  }, []);

  /**
   * Guarda nombre/descripción y luego sincroniza los miembros: agrega los
   * nuevos en una sola llamada (todo o nada en el backend) y quita los que
   * se sacaron de la lista.
   *
   * Son varias peticiones, así que puede fallar a medias. Por eso se va
   * anotando lo que el servidor ya confirmó (id del grupo y miembros actuales):
   * si el técnico corrige y vuelve a guardar, se continúa desde ahí en vez de
   * crear el grupo de nuevo o repetir bajas ya hechas.
   */
  const handleSave = useCallback(async () => {
    setIsSaving(true);
    const esNuevo = !currentGrupo.id_grupo;
    let idGrupo = currentGrupo.id_grupo;
    let idsEnServidor = currentGrupo.ids_originales || [];
    try {
      const datos = { nombre: currentGrupo.nombre, descripcion: currentGrupo.descripcion };
      const { data: guardado } = idGrupo
        ? await gruposService.updateGrupo(idGrupo, datos)
        : await gruposService.createGrupo(datos);
      idGrupo = guardado.id_grupo;

      const nuevos = currentGrupo.ids_activos.filter((id) => !idsEnServidor.includes(id));
      const quitados = idsEnServidor.filter((id) => !currentGrupo.ids_activos.includes(id));
      const idsDe = (grupo) => (grupo?.activos || []).map((a) => a.id_activo);

      if (nuevos.length > 0) {
        idsEnServidor = idsDe((await gruposService.agregarActivos(idGrupo, nuevos)).data);
      }
      for (const idActivo of quitados) {
        idsEnServidor = idsDe((await gruposService.quitarActivo(idGrupo, idActivo)).data);
      }

      showSnackbar(esNuevo ? 'Grupo creado correctamente' : 'Grupo actualizado correctamente', 'success');
      setOpen(false);
    } catch (error) {
      setCurrentGrupo((previo) => ({ ...previo, id_grupo: idGrupo, ids_originales: idsEnServidor }));
      showSnackbar(error.response?.data?.message || 'Error al guardar el grupo', 'error');
    } finally {
      setIsSaving(false);
      fetchAll();
    }
  }, [currentGrupo, fetchAll, showSnackbar]);

  const confirmarEliminar = useCallback(async () => {
    const grupo = grupoPorEliminar;
    setGrupoPorEliminar(null);
    if (!grupo) return;
    try {
      await gruposService.deleteGrupo(grupo.id_grupo);
      showSnackbar('Grupo eliminado. Sus activos quedaron sin grupo', 'success');
      fetchAll();
    } catch (error) {
      showSnackbar(error.response?.data?.message || 'Error al eliminar el grupo', 'error');
    }
  }, [grupoPorEliminar, fetchAll, showSnackbar]);

  return {
    grupos: gruposPagina,
    count: grupos.length,
    isLoading,
    page,
    setPage,
    rowsPerPage,
    open,
    setOpen,
    currentGrupo,
    setCurrentGrupo,
    opcionesDeActivos,
    isSaving,
    openDialog,
    handleSave,
    grupoPorEliminar,
    iniciarEliminar: setGrupoPorEliminar,
    cancelarEliminar: () => setGrupoPorEliminar(null),
    confirmarEliminar,
  };
};
