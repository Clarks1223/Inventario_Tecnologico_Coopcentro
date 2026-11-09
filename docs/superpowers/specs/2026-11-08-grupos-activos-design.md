# Grupos de activos — Diseño

Fecha: 2026-11-08
Estado: aprobado en conversación, pendiente de revisión escrita

## Problema

Cuando un empleado sale y entra otro a su puesto, hoy hay que:

1. "Devolver todo" al empleado saliente (los activos vuelven a custodia de TI).
2. Desactivar al empleado (el sistema lo impide mientras tenga activos).
3. Asignar al nuevo empleado cada activo, uno por uno.

El paso 3 es repetitivo y propenso a olvidos: el puesto siempre lleva el mismo
conjunto de equipos (PC, monitor, teclado, mouse, etc.).

## Objetivo

Poder definir **grupos fijos con nombre** (p. ej. "Puesto Caja 1") que contienen
activos concretos de cualquier tipo, y **asignar el grupo completo a un empleado
en una sola acción**, generando las actas como un paquete agrupado.

Criterio de éxito: cambiar al ocupante de un puesto se reduce a
"Devolver todo" al saliente → "Asignar grupo" al entrante.

## Decisiones tomadas

| Tema | Decisión |
|---|---|
| Concepto | Grupo fijo con nombre que persiste aunque cambie el empleado |
| Contenido | Cualquier tipo de activo (PC, laptop, periférico, móvil, impresora térmica) |
| Activo no disponible al asignar | Se bloquea la asignación completa y se informa qué activos la impiden |
| Pertenencia | Un activo pertenece como máximo a **un** grupo |
| Asignación suelta | Un activo del grupo se puede seguir asignando individualmente; el grupo queda bloqueado para asignación hasta que ese activo vuelva a bodega |
| Eliminar grupo | Solo desagrupa (los activos quedan sin grupo); no toca actas ni custodia |
| Devolución | No hay "devolver grupo": se usa el "Devolver todo" del empleado, que ya existe |
| Estado del grupo | Derivado, nunca almacenado |

## Fuera de alcance

- Que un activo pertenezca a varios grupos.
- Traspaso directo empleado → empleado sin pasar por bodega.
- Actas a nivel de grupo (las actas siguen siendo por activo).
- Asignación parcial del grupo.

## Modelo de datos

Nueva tabla `grupos_activos`:

| Columna | Tipo | Notas |
|---|---|---|
| `id_grupo` | serial PK | |
| `nombre` | varchar(100) | obligatorio, único |
| `descripcion` | text | opcional |
| `created_at` | timestamp | |
| `updated_at` | timestamp | |

Tabla `activos`: nueva columna `id_grupo` (FK nullable a `grupos_activos`,
`fk_activos_id_grupo_grupos_activos`). Hibernate (`ddl-auto=update`) la crea;
no hay scripts de migración.

El estado del grupo **se deriva** de las actas abiertas de sus miembros:

- `VACIO`: el grupo no tiene activos.
- `EN_BODEGA`: todos los miembros están operativos y en custodia de TI → asignable.
- `ASIGNADO`: todos los miembros están entregados al mismo empleado (se devuelve
  `idEmpleado` y nombre).
- `MIXTO`: cualquier otra combinación (alguno entregado, a distintos empleados,
  dado de baja o robado/perdido) → no asignable.

## Backend

Sigue la arquitectura hexagonal actual (dominio / aplicación / infraestructura / presentación).

### Dominio

- `dominio/entidades/GrupoActivos.java`: `idGrupo`, `nombre`, `descripcion`,
  `createdAt`, `updatedAt`.
- `Activo`: nuevo campo `Integer idGrupo` (null = sin grupo).
- `dominio/valores/EstadoGrupo.java`: constantes `VACIO`, `EN_BODEGA`, `ASIGNADO`, `MIXTO`.
- `dominio/entidades/ResumenGrupo.java` (record): `grupo`, `activos`, `estado`,
  `idEmpleadoAsignado` (Integer), `nombreEmpleadoAsignado` — lo que devuelve el
  caso de uso para listar/consultar.
- `dominio/repositorios/IGrupoActivosRepositorio.java`: `guardar`, `buscarPorId`,
  `listarTodos`, `eliminar`, `existePorNombre(nombre, idExcluir)`.
- `IActivoRepositorio`: añadir `listarPorGrupo(int idGrupo)`.

### Infraestructura

- `GrupoActivosEntity` (`@Table(name = "grupos_activos")`), `IGrupoActivosJpaRepositorio`,
  `GrupoActivosRepositorioImpl`, `IGrupoActivosJpaMapper`.
- `ActivoEntity`: `@ManyToOne` nullable `grupo` con `@JoinColumn(name = "id_grupo")`.
- `IActivoJpaMapper`: mapear `grupo.idGrupo` ↔ `idGrupo`.
- `IReferenciaJpaMapper`: añadir `mapGrupo(Integer idGrupo)` que devuelve `null`
  cuando el id es `null` (a diferencia de las demás FKs, esta es opcional).
- `InventarioConfig`: bean de `GrupoActivosUseCaseImpl`.

### Aplicación

`IGrupoActivosUseCase` / `GrupoActivosUseCaseImpl`:

- `guardar(GrupoActivos)`: valida nombre obligatorio y único.
- `buscarPorId`, `listarTodos`.
- `eliminar(idGrupo)`: pone `idGrupo = null` en sus miembros y borra el grupo (transaccional).
- `agregarActivos(idGrupo, List<Integer> idsActivos)`: cada activo debe existir,
  no estar dado de baja/robado y no pertenecer ya a **otro** grupo (si ya está en
  este, se ignora). Falla en bloque si alguno no es válido.
- `quitarActivo(idGrupo, idActivo)`: el activo debe pertenecer al grupo.
- `resumen(idGrupo)` y `listarResumenes()`: devuelven `ResumenGrupo` con los
  miembros y el estado derivado.

En `IActaEntregaRecepcionUseCase` / `ActaEntregaRecepcionUseCaseImpl`:

- Nuevo `asignarGrupo(idGrupo, idEmpleado, idUsuarioTi, motivo, observacion)`:
  1. El grupo debe existir y tener al menos un activo.
  2. Valida empleado activo y usuario TI activo una sola vez.
  3. Valida **todos** los miembros antes de tocar nada: operativo + acta abierta
     en `CUSTODIA`. Si alguno falla, lanza `NegocioException` con la lista
     ("No se puede asignar el grupo: SERIAL1 (entregado a Juan Pérez),
     SERIAL2 (dado de baja)").
  4. Para cada miembro: cierra custodia y abre entrega (con `observacion` si viene),
     sin emitir PDF individual.
  5. Emite el paquete con `generarPdfsDelPaqueteSinFallar(idsActas)`
     (reutiliza `archivarLote`, que agrupa la PC con sus periféricos).
  6. Todo en una transacción (`@Transactional`): o se asigna todo o nada.
- Refactor: extraer la lógica actual de `asignar` a un método privado
  `asignar(..., boolean emitirActa)`, igual que ya hace `devolver`. El `asignar`
  público conserva su comportamiento.
- Motivo por defecto de cada acta: `"Asignacion del grupo <nombre>"` + motivo del usuario.

### Presentación

`GrupoActivosController` en `/api/grupos`:

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| GET | `/api/grupos` | — | `List<GrupoActivosResponseDto>` |
| GET | `/api/grupos/{id}` | — | `GrupoActivosResponseDto` |
| POST | `/api/grupos` | `GrupoActivosRequestDto` | 201 + grupo |
| PUT | `/api/grupos/{id}` | `GrupoActivosRequestDto` | grupo |
| DELETE | `/api/grupos/{id}` | — | 204 |
| POST | `/api/grupos/{id}/activos` | `{ idsActivos: [..] }` | grupo |
| DELETE | `/api/grupos/{id}/activos/{idActivo}` | — | grupo |
| POST | `/api/grupos/{id}/asignar` | `AsignarGrupoRequestDto` | `List<ActaEntregaRecepcionResponseDto>` (201) |

DTOs:

- `GrupoActivosRequestDto`: `nombre` (`@NotBlank`, `@Size(max=100)`), `descripcion`.
- `AgregarActivosGrupoRequestDto`: `idsActivos` (`@NotEmpty`).
- `AsignarGrupoRequestDto`: `idEmpleado`, `idUsuarioTi` (`@NotNull @Positive`),
  `motivo`, `observacion` (`@Size(max=100)`, igual que en devolver todo).
- `GrupoActivosResponseDto`: `idGrupo`, `nombre`, `descripcion`, `createdAt`,
  `updatedAt`, `estado`, `idEmpleadoAsignado`, `nombreEmpleadoAsignado`,
  `activos` (lista resumida: `idActivo`, `codigoInventario`, `tipoActivo`,
  `marca`, `modelo`, `serial`, `estado`).
- `ActivoResponseDto`: añadir `idGrupo` y `nombreGrupo`.

**Cuidado con `PUT /api/activos/{id}`:** hoy reconstruye el `Activo` completo
desde el request. Como `idGrupo` no viene en `ActivoRequestDto`, editar un activo
lo sacaría de su grupo. El controlador debe conservar `existente.getIdGrupo()`.
La pertenencia solo se modifica desde `/api/grupos`.

Los errores de negocio siguen saliendo por `GlobalExceptionHandler`: `NegocioException` → 400 con `ErrorResponseDto(mensaje)`.

## Frontend

- `services/gruposService.js`: llamadas a `/api/grupos` siguiendo el patrón de los demás services.
- `hooks/useGrupos.js`: carga de grupos, activos y empleados; acciones CRUD,
  agregar/quitar miembros y asignar; snackbar de éxito/error como el resto.
- `pages/Grupos.jsx` + ruta `grupos` en `App.jsx` + entrada en el menú de `Layout.jsx`:
  - Tabla de grupos: nombre, nº de activos, estado (chip: En bodega / Asignado a X / Mixto / Vacío).
  - Diálogo crear/editar: nombre, descripción y miembros. Los miembros se agregan
    buscando por serial (mismo patrón que la asignación múltiple de `Asignaciones.jsx`);
    solo se ofrecen activos operativos sin grupo.
  - Acción "Asignar" visible cuando el estado es `EN_BODEGA`: elige empleado activo,
    motivo y observación opcionales.
- `pages/Asignaciones.jsx` — **nueva opción "Por grupo"** en el diálogo
  "Nueva Asignación" del empleado seleccionado:
  - Selector de modo arriba del diálogo: **Por activos** (comportamiento actual)
    | **Por grupo**.
  - En modo "Por grupo": un `Autocomplete` de selección única con todos los
    grupos, buscable por nombre. Los grupos que no están `EN_BODEGA` aparecen
    deshabilitados con su motivo ("Asignado a X", "Mixto", "Vacío").
  - Al elegir un grupo se muestra la lista de sus activos (serial · tipo · marca
    modelo) y el texto "Se asignarán N activos del grupo «nombre» a Empleado".
  - Botón "Asignar grupo (N)" → `POST /api/grupos/{id}/asignar` con el empleado
    seleccionado y el usuario en sesión. Si el backend rechaza (400), se muestra
    su mensaje y el diálogo queda abierto; si acepta, se cierra, se recargan los
    datos y se archivan las actas con `archivarLote(ids)` para mostrar la ruta
    (mismo patrón que "Devolver todo").
- `pages/Activos.jsx`: columna "Grupo" (`nombreGrupo`).

## Manejo de errores

- Nombre de grupo duplicado → error de negocio "Ya existe un grupo con ese nombre".
- Agregar un activo que ya está en otro grupo → error indicando el grupo actual.
- Asignar grupo vacío o con miembros no disponibles → error con la lista de seriales y el motivo de cada uno; no se modifica nada.
- Fallo en la generación del PDF → no revierte la asignación (mismo criterio que hoy: `generarPdfsDelPaqueteSinFallar`).

## Pruebas

- Unitarias (JUnit 5 + Mockito, que ya llegan por `spring-boot-starter-webmvc-test`
  y `spring-boot-starter-data-jpa-test` en `pom.xml`; no se añaden dependencias).
  Los casos de uso se construyen a mano con repositorios mockeados (ya usan
  inyección por constructor, sin Spring).
- Frontend (Vitest): utilidades puras de grupos (`utils/grupos.js`).
  - `asignarGrupo`: caso feliz (todas las actas cerradas/abiertas y un solo lote PDF),
    un miembro ya entregado, un miembro dado de baja, grupo vacío, empleado inactivo.
  - `GrupoActivosUseCaseImpl`: nombre duplicado, activo ya en otro grupo, eliminar desagrupa.
- Postman: añadir la carpeta "Grupos" a `tests/postman/Inventario_Tecnologico_Coopcentro.postman_collection.json`.
- Manual: crear grupo → asignar → devolver todo → desactivar empleado → asignar grupo a otro empleado; verificar el PDF agrupado y que editar un activo no lo saca del grupo.
