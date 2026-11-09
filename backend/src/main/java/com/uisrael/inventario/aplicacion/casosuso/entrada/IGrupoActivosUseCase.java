package com.uisrael.inventario.aplicacion.casosuso.entrada;

import java.util.List;

import com.uisrael.inventario.dominio.entidades.GrupoActivos;
import com.uisrael.inventario.dominio.entidades.ResumenGrupo;

public interface IGrupoActivosUseCase {
	/** Crea el grupo si idGrupo es 0; si no, lo actualiza conservando su createdAt. */
	GrupoActivos guardar(GrupoActivos grupo);
	GrupoActivos buscarPorId(int idGrupo);
	ResumenGrupo resumen(int idGrupo);
	List<ResumenGrupo> listarResumenes();
	/** Desagrupa sus activos y borra el grupo; no toca actas ni custodia. */
	void eliminar(int idGrupo);
	/** Todo o nada: si algun activo no puede entrar, no se agrega ninguno. */
	ResumenGrupo agregarActivos(int idGrupo, List<Integer> idsActivos);
	ResumenGrupo quitarActivo(int idGrupo, int idActivo);
}
