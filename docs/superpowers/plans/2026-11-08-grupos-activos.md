# Grupos de activos — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Grupos fijos con nombre de activos de cualquier tipo que se asignan completos a un empleado en una sola acción, incluida la opción "Por grupo" en Asignaciones.

**Architecture:** Nueva tabla `grupos_activos` y FK nullable `activos.id_grupo` (un activo, máximo un grupo). El estado del grupo se deriva de las actas abiertas. `asignarGrupo` vive en `ActaEntregaRecepcionUseCaseImpl` y reutiliza la lógica de `asignar` + `archivarLote` dentro de una transacción todo-o-nada.

**Tech Stack:** Spring Boot 4 / Java 21, JPA + PostgreSQL (`ddl-auto=update`), MapStruct, Lombok, JUnit 5 + Mockito · React + MUI, axios, Vitest.

**Spec:** `docs/superpowers/specs/2026-11-08-grupos-activos-design.md`

## Global Constraints

- Arquitectura hexagonal existente: `dominio` / `aplicacion/casosuso/{entrada,impl}` / `infraestructura/{persistencia,repositorios,configuracion}` / `presentacion/{controladores,dto,mapeadores}`. Los casos de uso son clases planas registradas como `@Bean` en `InventarioConfig`.
- Un activo pertenece como máximo a un grupo; la pertenencia solo cambia desde `/api/grupos`.
- Asignar grupo es todo-o-nada; el error lista cada serial bloqueante con su motivo.
- El fallo de PDF nunca revierte la asignación (`generarPdfsDelPaqueteSinFallar`).
- `NegocioException` → HTTP 400 `ErrorResponseDto(mensaje)` (ya lo hace `GlobalExceptionHandler`).
- Frontend: el JSON viaja en snake_case (`id_grupo`, `nombre_grupo`, `ids_activos`), igual que el resto de services.
- `nombre` de grupo: obligatorio, máx. 100, único (ignorando mayúsculas y espacios en los extremos). `observacion` de asignación: máx. 100.
- Textos de estado en UI: "En bodega", "Asignado a <Nombre Apellido>", "Mixto", "Vacío".
- No se añaden dependencias (JUnit/Mockito llegan por los starters `-test` del `pom.xml`).

## Review Focus

1. **Editar un activo que está en un grupo** (`PUT /api/activos/{id}`) debe conservar su `id_grupo` → test en Task 4.
2. **Un activo del grupo dado de baja o robado después de agruparlo** debe bloquear la asignación del grupo con su serial y "dado de baja"/"robado/perdido", no lanzar NPE por no tener acta abierta → test en Task 3.
3. **Nombre duplicado con distinta capitalización o espacios** ("Caja 1" vs " caja 1 ") debe rechazarse; editar un grupo sin cambiar su nombre no debe chocar consigo mismo → test en Task 2.
4. **Asignar el grupo a un empleado inactivo** debe fallar sin cerrar ninguna custodia → test en Task 3.
5. **Agregar al grupo un activo que ya está en ese mismo grupo** se ignora (idempotente), no es error → test en Task 2.

---

### Task 0: Rama de trabajo

- [ ] **Step 1:** Commitear o apartar los cambios pendientes del usuario en `frontend/src/hooks/useActivos.js` y `frontend/src/pages/Activos.jsx` (preguntar antes; Task 5 toca `Activos.jsx`).
- [ ] **Step 2:** `git checkout -b feature/grupos-activos` y commitear la spec y este plan: `git add docs/superpowers && git commit -m "docs: spec y plan de grupos de activos"`.

### Task 1: Dominio y persistencia del grupo

**Files:**
- Create: `backend/src/main/java/com/uisrael/inventario/dominio/entidades/GrupoActivos.java`, `dominio/entidades/ResumenGrupo.java`, `dominio/valores/EstadoGrupo.java`, `dominio/repositorios/IGrupoActivosRepositorio.java`, `infraestructura/persistencia/jpa/GrupoActivosEntity.java`, `infraestructura/repositorios/IGrupoActivosJpaRepositorio.java`, `infraestructura/persistencia/adaptadores/GrupoActivosRepositorioImpl.java`, `infraestructura/persistencia/mapeadores/IGrupoActivosJpaMapper.java`
- Modify: `dominio/entidades/Activo.java`, `infraestructura/persistencia/jpa/ActivoEntity.java`, `infraestructura/persistencia/mapeadores/IActivoJpaMapper.java`, `infraestructura/persistencia/mapeadores/IReferenciaJpaMapper.java`, `dominio/repositorios/IActivoRepositorio.java`, `infraestructura/repositorios/IActivoJpaRepositorio.java`, `infraestructura/persistencia/adaptadores/ActivoRepositorioImpl.java`
- Test: `backend/src/test/java/com/uisrael/inventario/infraestructura/persistencia/mapeadores/ReferenciaJpaMapperTest.java`

(Rutas relativas a `backend/src/main/java/com/uisrael/inventario/` salvo indicación.)

**Interfaces:**
- Produces:
  - `GrupoActivos { int idGrupo; String nombre; String descripcion; LocalDateTime createdAt; LocalDateTime updatedAt; }` (Lombok `@Data @AllArgsConstructor @NoArgsConstructor`)
  - `Activo.idGrupo: Integer`
  - `EstadoGrupo.VACIO = "VACIO"`, `EN_BODEGA = "EN_BODEGA"`, `ASIGNADO = "ASIGNADO"`, `MIXTO = "MIXTO"`
  - `record ResumenGrupo(GrupoActivos grupo, List<Activo> activos, String estado, Integer idEmpleadoAsignado, String nombreEmpleadoAsignado)`
  - `IGrupoActivosRepositorio`: `GrupoActivos guardar(GrupoActivos)`, `Optional<GrupoActivos> buscarPorId(int)`, `List<GrupoActivos> listarTodos()`, `void eliminar(int)`
  - `IActivoRepositorio.listarPorGrupo(int idGrupo): List<Activo>` (JPA: `findByGrupoIdGrupo(int)`)
  - `IReferenciaJpaMapper.mapGrupo(Integer idGrupo): GrupoActivosEntity` — `null` si `idGrupo == null`

- [ ] **Step 1: Test fallido** `ReferenciaJpaMapperTest`:
  - `mapGrupoConNullDevuelveNull`: `assertNull(new IReferenciaJpaMapper(){}.mapGrupo(null))`
  - `mapGrupoConIdDevuelveStub`: `assertEquals(7, new IReferenciaJpaMapper(){}.mapGrupo(7).getIdGrupo())`
- [ ] **Step 2:** `cd backend && mvn -q test -Dtest=ReferenciaJpaMapperTest` → FAIL (no compila: `mapGrupo` no existe).
- [ ] **Step 3: Implementar** las clases listadas. `GrupoActivosEntity`: `@Table(name = "grupos_activos")`, `nombre` `length = 100, nullable = false, unique = true`, `descripcion` `TEXT`. `ActivoEntity.grupo`: `@ManyToOne @JoinColumn(name = "id_grupo", foreignKey = @ForeignKey(name = "fk_activos_id_grupo_grupos_activos"))`. `IActivoJpaMapper`: `@Mapping(source = "grupo.idGrupo", target = "idGrupo")` en `toDomain` y `@Mapping(source = "idGrupo", target = "grupo")` en `toEntity`.
- [ ] **Step 4:** `mvn -q test -Dtest=ReferenciaJpaMapperTest` → PASS; `mvn -q compile` sin errores de MapStruct (no "Unmapped target property: grupo").
- [ ] **Step 5:** Commit `feat: modelo y persistencia de grupos de activos`.

### Task 2: Caso de uso de gestión de grupos

**Files:**
- Create: `aplicacion/casosuso/entrada/IGrupoActivosUseCase.java`, `aplicacion/casosuso/impl/GrupoActivosUseCaseImpl.java`
- Modify: `infraestructura/configuracion/InventarioConfig.java` (nuevo `@Bean`)
- Test: `backend/src/test/java/com/uisrael/inventario/aplicacion/casosuso/impl/GrupoActivosUseCaseImplTest.java`

**Interfaces:**
- Consumes: Task 1 (`IGrupoActivosRepositorio`, `IActivoRepositorio.listarPorGrupo`, `ResumenGrupo`, `EstadoGrupo`), `IActaEntregaRecepcionRepositorio.listarTodos()`, `IEmpleadoRepositorio.buscarPorId`.
- Produces (`IGrupoActivosUseCase`):
  - `GrupoActivos guardar(GrupoActivos grupo)` — crea si `idGrupo == 0`, si no actualiza (conserva `createdAt`, actualiza `updatedAt`)
  - `GrupoActivos buscarPorId(int idGrupo)` — `NegocioException("Grupo no encontrado")`
  - `ResumenGrupo resumen(int idGrupo)`, `List<ResumenGrupo> listarResumenes()`
  - `void eliminar(int idGrupo)`
  - `ResumenGrupo agregarActivos(int idGrupo, List<Integer> idsActivos)`
  - `ResumenGrupo quitarActivo(int idGrupo, int idActivo)`
- Constructor: `GrupoActivosUseCaseImpl(IGrupoActivosRepositorio, IActivoRepositorio, IActaEntregaRecepcionRepositorio, IEmpleadoRepositorio)`

Regla de estado (en `resumen`): sin activos → `VACIO`; todos `OPERATIVO` con acta abierta `CUSTODIA` → `EN_BODEGA`; todos con acta `ACTIVA` del mismo `idEmpleado` → `ASIGNADO` (+ id y "Nombre Apellido"); otro caso → `MIXTO`.

- [ ] **Step 1: Tests fallidos** (Mockito, repos mockeados):
  - `guardarRechazaNombreVacio` → `NegocioException` "El nombre del grupo es obligatorio"
  - `guardarRechazaNombreDuplicadoIgnorandoMayusculasYEspacios` (existe "Caja 1", guarda " caja 1 ") → "Ya existe un grupo con ese nombre"
  - `actualizarConElMismoNombreNoChocaConsigoMismo` → no lanza
  - `agregarActivoDeOtroGrupoFalla` (activo con `idGrupo = 2`, grupo 1) → mensaje contiene el serial y el nombre del grupo 2
  - `agregarActivoDadoDeBajaFalla` → mensaje contiene el serial
  - `agregarActivoYaEnElMismoGrupoSeIgnora` → no lanza, no llama `activoRepositorio.guardar` para ese activo
  - `agregarEsTodoONada`: [válido, inválido] → `NegocioException` y `verify(activoRepositorio, never()).guardar(any())`
  - `quitarActivoQueNoPerteneceFalla` → "El activo no pertenece a este grupo"
  - `eliminarDesagrupaMiembros`: 2 miembros → ambos guardados con `idGrupo == null`, luego `grupoRepositorio.eliminar(id)`
  - `resumenEstados`: un caso por estado `VACIO`, `EN_BODEGA`, `ASIGNADO` (con `nombreEmpleadoAsignado == "Ana Paz"`), `MIXTO` (uno en custodia + uno entregado)
- [ ] **Step 2:** `mvn -q test -Dtest=GrupoActivosUseCaseImplTest` → FAIL (clase inexistente).
- [ ] **Step 3: Implementar** `GrupoActivosUseCaseImpl`. `guardar`, `agregarActivos`, `quitarActivo`, `eliminar` con `@Transactional`. Normalizar nombre con `trim()` antes de guardar; comparar con `equalsIgnoreCase` contra `listarTodos()` excluyendo el propio `idGrupo`. Registrar el bean en `InventarioConfig`.
- [ ] **Step 4:** `mvn -q test -Dtest=GrupoActivosUseCaseImplTest` → PASS.
- [ ] **Step 5:** Commit `feat: caso de uso de gestion de grupos de activos`.

### Task 3: Asignar grupo (todo o nada)

**Files:**
- Modify: `aplicacion/casosuso/entrada/IActaEntregaRecepcionUseCase.java`, `aplicacion/casosuso/impl/ActaEntregaRecepcionUseCaseImpl.java`, `infraestructura/configuracion/InventarioConfig.java` (nuevas dependencias del bean)
- Test: `backend/src/test/java/com/uisrael/inventario/aplicacion/casosuso/impl/AsignarGrupoTest.java`

**Interfaces:**
- Consumes: `IGrupoActivosRepositorio.buscarPorId`, `IActivoRepositorio.listarPorGrupo` (Task 1).
- Produces: `List<ActaEntregaRecepcion> asignarGrupo(int idGrupo, int idEmpleado, int idUsuarioTi, String motivo, String observacion)` — devuelve las actas de entrega creadas.
- Constructor de `ActaEntregaRecepcionUseCaseImpl` gana `IGrupoActivosRepositorio` como último parámetro.

- [ ] **Step 1: Tests fallidos:**
  - `asignaTodosYEmiteUnSoloLote`: grupo con 3 activos en custodia → 3 custodias cerradas (`DEVUELTA`), 3 actas `ACTIVA` para el empleado, cada una con `observacion` dada; `verify(actaDocumentoUseCase, times(1)).archivarLote(argThat(ids -> ids.size() == 3))` y `never()).archivarActa(...)`
  - `motivoIncluyeNombreDelGrupo`: el motivo de cada acta empieza por `"Asignacion del grupo Caja 1"`
  - `bloqueaSiUnMiembroYaEstaEntregado` → `NegocioException` con el serial y "entregado a Juan Perez"; `verify(repositorio, never()).guardar(any())`
  - `bloqueaSiUnMiembroEstaDadoDeBaja` (sin acta abierta) → mensaje con serial y "dado de baja"; sin NPE; nada guardado
  - `bloqueaSiUnMiembroEstaRobadoPerdido` → mensaje con "robado/perdido"
  - `listaTodosLosBloqueantes`: 2 miembros inválidos → ambos seriales en el mismo mensaje
  - `grupoVacioFalla` → "El grupo no tiene activos"
  - `empleadoInactivoFalla` → "El empleado no esta activo"; nada guardado
  - `fallaDelPdfNoRevierte`: `archivarLote` lanza `RuntimeException` → el método devuelve las 3 actas
  - `asignarIndividualSigueEmitiendoSuActa`: `asignar(...)` existente → `archivarActa` llamado una vez (regresión del refactor)
- [ ] **Step 2:** `mvn -q test -Dtest=AsignarGrupoTest` → FAIL.
- [ ] **Step 3: Implementar.** Extraer el cuerpo de `asignar` a `private ActaEntregaRecepcion asignar(int idActivo, Empleado empleado, int idUsuarioTi, String motivo, String observacion, boolean emitirActa)`; el público valida y delega con `emitirActa = true`. `asignarGrupo`: valida grupo, miembros no vacíos, empleado activo y usuario TI una sola vez; recorre miembros acumulando bloqueantes (`"<serial> (<motivo>)"`) sin escribir; si hay alguno lanza `NegocioException("No se puede asignar el grupo: " + String.join(", ", bloqueantes))`; si no, asigna cada uno con `emitirActa = false` y llama `generarPdfsDelPaqueteSinFallar(ids)`. `@Transactional`.
- [ ] **Step 4:** `mvn -q test` → todos PASS.
- [ ] **Step 5:** Commit `feat: asignar grupo completo de activos a un empleado`.

### Task 4: API REST de grupos

**Files:**
- Create: `presentacion/controladores/GrupoActivosController.java`, `presentacion/dto/request/GrupoActivosRequestDto.java`, `presentacion/dto/request/AgregarActivosGrupoRequestDto.java`, `presentacion/dto/request/AsignarGrupoRequestDto.java`, `presentacion/dto/response/GrupoActivosResponseDto.java`, `presentacion/dto/response/ActivoGrupoResponseDto.java`, `presentacion/mapeadores/IGrupoActivosDtoMapper.java`
- Modify: `presentacion/dto/response/ActivoResponseDto.java` (`Integer idGrupo`, `String nombreGrupo`), `presentacion/controladores/ActivoController.java`
- Modify: `tests/postman/Inventario_Tecnologico_Coopcentro.postman_collection.json` (carpeta "Grupos")
- Test: `backend/src/test/java/com/uisrael/inventario/presentacion/controladores/ActivoControllerTest.java`

**Interfaces:**
- Consumes: `IGrupoActivosUseCase` (Task 2), `IActaEntregaRecepcionUseCase.asignarGrupo` (Task 3).
- Produces (JSON snake_case, lo consume el frontend):
  - `GET/POST /api/grupos`, `GET/PUT/DELETE /api/grupos/{id}`, `POST /api/grupos/{id}/activos` `{ ids_activos }`, `DELETE /api/grupos/{id}/activos/{idActivo}`, `POST /api/grupos/{id}/asignar` `{ id_empleado, id_usuario_ti, motivo, observacion }` → 201 `List<ActaEntregaRecepcionResponseDto>`
  - `GrupoActivosResponseDto { id_grupo, nombre, descripcion, created_at, updated_at, estado, id_empleado_asignado, nombre_empleado_asignado, activos: [{ id_activo, codigo_inventario, tipo_activo, marca, modelo, serial, estado }] }`
  - `ActivoResponseDto` + `id_grupo`, `nombre_grupo`

- [ ] **Step 1: Test fallido** `ActivoControllerTest` (unitario, controlador instanciado con mocks): `actualizarConservaElGrupo` — `buscarPorId` devuelve activo con `idGrupo = 5`; tras `actualizar(id, request)` el `Activo` pasado a `activoUseCase.guardar` tiene `idGrupo == 5` (`ArgumentCaptor`).
- [ ] **Step 2:** `mvn -q test -Dtest=ActivoControllerTest` → FAIL.
- [ ] **Step 3: Implementar.** En `ActivoController.actualizar`: `activo.setIdGrupo(existente.getIdGrupo())`. En `listarTodos`/`buscarPorId`, rellenar `nombreGrupo` desde un mapa `idGrupo → nombre` de `IGrupoActivosUseCase.listarResumenes()` (inyectar el use case). Validaciones de DTO según Global Constraints (`@NotBlank @Size(max=100)` nombre, `@NotEmpty` ids, `@NotNull @Positive` ids de empleado/usuario, `@Size(max=100)` observación). Postman: un request por endpoint con el environment existente.
- [ ] **Step 4:** `mvn -q test` → PASS. `mvn spring-boot:run` (con variables de entorno cargadas) y en Postman: crear grupo → agregar 2 activos en bodega → `GET` muestra `estado: "EN_BODEGA"` → asignar → 201 con 2 actas → `GET` muestra `ASIGNADO`. Crear otro grupo con nombre en otra capitalización → 400.
- [ ] **Step 5:** Commit `feat: API REST de grupos de activos`.

### Task 5: Página Grupos y columna en Activos

**Files:**
- Create: `frontend/src/services/gruposService.js`, `frontend/src/utils/grupos.js`, `frontend/src/hooks/useGrupos.js`, `frontend/src/pages/Grupos.jsx`
- Modify: `frontend/src/App.jsx` (ruta `grupos`), `frontend/src/layout/Layout.jsx` (menú "Grupos", icono `WorkspacesIcon`, tras "Activos"), `frontend/src/pages/Activos.jsx` (columna "Grupo" = `nombre_grupo || '—'`)
- Test: `frontend/src/utils/__tests__/grupos.test.js`

**Interfaces:**
- Consumes: endpoints de Task 4.
- Produces:
  - `gruposService`: `getGrupos(config)`, `getGrupo(id)`, `createGrupo({ nombre, descripcion })`, `updateGrupo(id, { nombre, descripcion })`, `deleteGrupo(id)`, `agregarActivos(id, idsActivos)`, `quitarActivo(id, idActivo)`, `asignarGrupo(id, { id_empleado, id_usuario_ti, motivo, observacion })`
  - `utils/grupos.js`: `ESTADO_GRUPO = { VACIO, EN_BODEGA, ASIGNADO, MIXTO }`, `etiquetaEstadoGrupo(grupo): string`, `colorEstadoGrupo(grupo): 'success'|'info'|'warning'|'default'`, `esAsignable(grupo): boolean`, `activosAgrupables(activos): Activo[]` (operativos y sin `id_grupo`)

- [ ] **Step 1: Tests fallidos** `grupos.test.js`:
  - `etiquetaEstadoGrupo({ estado: 'ASIGNADO', nombre_empleado_asignado: 'Ana Paz' }) === 'Asignado a Ana Paz'`; `EN_BODEGA → 'En bodega'`; `MIXTO → 'Mixto'`; `VACIO → 'Vacío'`
  - `esAsignable` es `true` solo para `EN_BODEGA`
  - `activosAgrupables` excluye `estado: 'DADO_DE_BAJA'` y los que tienen `id_grupo`
- [ ] **Step 2:** `cd frontend && npx vitest run src/utils/__tests__/grupos.test.js` → FAIL.
- [ ] **Step 3: Implementar** utils, service (patrón de `asignacionesService.js`), hook (patrón de `useAsignaciones`: `fetchAll` con `AbortController`, `useSnackbar`, `useConfirmDialog` para eliminar, mensajes de error del backend con `error.response?.data?.message`) y página: tabla (nombre, nº activos, chip de estado), diálogo crear/editar con nombre, descripción y `Autocomplete multiple` de miembros (mismo `renderOption`/`filtrarActivos` que `Asignaciones.jsx`, opciones = `activosAgrupables` + miembros actuales; al guardar se llama `agregarActivos` con los nuevos y `quitarActivo` con los quitados). Columna "Grupo" en `Activos.jsx`.
- [ ] **Step 4:** `npx vitest run` → PASS; `npm run lint` sin errores nuevos.
- [ ] **Step 5:** Commit `feat: pagina de grupos de activos`.

### Task 6: Opción "Por grupo" en Asignaciones

**Files:**
- Modify: `frontend/src/hooks/useAsignaciones.js`, `frontend/src/pages/Asignaciones.jsx`

**Interfaces:**
- Consumes: `gruposService.getGrupos`, `gruposService.asignarGrupo` (Task 5), `etiquetaEstadoGrupo`, `esAsignable` (Task 5), `asignacionesService.archivarLote` y `archivarYAvisar` (existentes).
- Produces (del hook): `grupos: Grupo[]`, `handleAssignGrupo({ id_grupo, motivo }): Promise<boolean>` — `true` si asignó.

- [ ] **Step 1:** En `useAsignaciones`: cargar `grupos` dentro del `Promise.all` de `fetchAll`. `handleAssignGrupo` llama `gruposService.asignarGrupo(id_grupo, { id_empleado: empleadoSeleccionado, id_usuario_ti: sesion.id_usuario_ti, motivo, observacion: null })`; éxito → `setOpen(false)`, `fetchAll()`, `archivarYAvisar(() => asignacionesService.archivarLote(ids), 'No se pudieron generar las actas de entrega')`, devuelve `true`; error → `showSnackbar(error.response?.data?.message || 'Error al asignar el grupo', 'error')`, diálogo abierto, devuelve `false`.
- [ ] **Step 2:** En `Asignaciones.jsx`, diálogo "Nueva Asignación": `ToggleButtonGroup` exclusivo arriba (`'activos'` | `'grupo'`, por defecto `'activos'`, se reinicia en `openDialog`). Modo `'activos'` = UI actual sin cambios. Modo `'grupo'`: `Autocomplete` simple con `grupos`, `getOptionDisabled={(g) => !esAsignable(g)}`, cada opción muestra nombre + `etiquetaEstadoGrupo(g)` + nº de activos; al elegir, lista de miembros (`serial · tipo_activo` / `marca modelo`) y "Se asignarán **N** activos del grupo «nombre» a **Empleado**". Botón primario: modo grupo → "Asignar grupo (N)", deshabilitado sin grupo elegido; llama `handleAssignGrupo`.
- [ ] **Step 3: Verificar en la app** (`npm run dev` + backend): empleado A → Nueva Asignación → Por grupo → grupo en bodega → Asignar → snackbar con la ruta de las actas y los activos aparecen en la tabla del empleado. Abrir de nuevo: el grupo aparece deshabilitado como "Asignado a A". Asignar un grupo con un miembro entregado aparte (provocar con Postman entre carga y clic) → mensaje del backend, diálogo sigue abierto. Modo "Por activos" funciona como antes.
- [ ] **Step 4:** `npx vitest run` y `npm run lint` → PASS.
- [ ] **Step 5:** Commit `feat: asignar por grupo desde Asignaciones`.

### Task 7: Verificación de punta a punta

- [ ] **Step 1:** `cd backend && mvn -q test` y `cd frontend && npx vitest run && npm run lint` → todo PASS.
- [ ] **Step 2:** Flujo real: crear grupo "Puesto Prueba" (PC + monitor + teclado) → asignar a empleado A por grupo → revisar que el PDF de entrega de la PC incluye los periféricos → "Devolver todo" a A → desactivar A → asignar el grupo a B → PDF agrupado para B. Editar el monitor en Activos y confirmar que sigue en "Puesto Prueba". Dar de baja el teclado → el grupo pasa a "Mixto" y no se puede asignar; quitarlo del grupo → vuelve a "En bodega".
- [ ] **Step 3:** Reportar resultados; si todo pasa, proponer PR con superpowers:finishing-a-development-branch.
