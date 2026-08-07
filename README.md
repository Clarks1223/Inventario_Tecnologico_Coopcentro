# Inventario Tecnológico — Coopcentro

Sistema para llevar el inventario de equipos de TI y documentar su entrega y
devolución a los empleados.

Cada equipo tiene siempre un responsable: al registrarlo queda a cargo de quien
lo dio de alta, al entregarlo pasa al empleado y al devolverlo vuelve a bodega.
Cada movimiento genera un **acta de entrega-recepción en PDF**, lista para
imprimir y firmar.

- Inventario por tipo de activo (laptop, desktop, móvil, impresora térmica,
  periférico), cada uno con sus campos técnicos propios.
- Actas en PDF generadas sobre plantillas oficiales y archivadas por oficina y
  empleado.
- Gestión de empleados, oficinas y cargos.
- Acceso del personal de TI con recuperación de contraseña por correo.
- Exportación del inventario a Excel.

## Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 21+ |
| Maven | 3.9+ (el proyecto no incluye `mvnw`) |
| Node.js | 20+ |
| PostgreSQL | 14+ |

## Configuración

**1. Base de datos.** Crea una base vacía; Hibernate genera las tablas al
arrancar. No hay scripts que ejecutar.

**2. Variables de entorno.** Copia las plantillas y complétalas con los datos de
tu entorno:

```powershell
Copy-Item backend\.env.example backend\.env
Copy-Item frontend\.env.example frontend\.env
```

> El backend **no lee archivos `.env`**: los valores deben existir como
> variables de entorno del proceso. `backend\.env.example` solo documenta
> cuáles hacen falta.

Para cargarlas en la sesión de PowerShell antes de arrancar:

```powershell
Get-Content backend\.env | Where-Object { $_ -match '^\s*[^#].*=' } | ForEach-Object { $p = $_ -split '=', 2; [Environment]::SetEnvironmentVariable($p[0].Trim(), $p[1].Trim(), 'Process') }
```

En IntelliJ puedes definirlas en *Run → Edit Configurations → Environment
variables*.

## Ejecutar

Dos terminales.

```powershell
cd backend
mvn spring-boot:run
```

```powershell
cd frontend
npm install
npm run dev
```

El frontend queda en <http://localhost:5173> y el backend en el puerto 8080.

> Arranca el backend siempre desde la carpeta `backend`: las rutas de las
> plantillas y de las actas generadas son relativas al directorio de trabajo.

El primer arranque crea los datos iniciales y un usuario administrador a partir
de las variables que configuraste. El sistema obliga a cambiar la contraseña en
el primer ingreso.

## Actas

Las plantillas viven en `backend/plantillas/` y están versionadas: son
formularios PDF cuyos campos rellena el sistema. Si faltan, la generación falla.

Las actas generadas se guardan en `backend/actas-generadas/`, organizadas por
oficina y empleado. Esa carpeta está excluida del repositorio porque contiene
datos personales.

## Pruebas

```powershell
cd frontend
npm run lint
npm test
```

```powershell
cd backend
mvn test
```

> Los tests del backend corren contra la base de datos configurada (no usan H2
> ni Testcontainers). Cada test se revierte al terminar.

En `tests/postman/` hay una colección que recorre el flujo completo de la API.

## Estructura

```
backend/     Spring Boot · arquitectura hexagonal
  dominio/         entidades, puertos y reglas de negocio
  aplicacion/      casos de uso
  infraestructura/ persistencia, PDF y correo
  presentacion/    controladores REST y DTOs
frontend/    React · Vite · Material UI
tests/       colección de Postman
```

## Problemas comunes

| Síntoma | Causa |
|---|---|
| `Could not resolve placeholder` al arrancar | Faltan las variables de entorno en esa terminal |
| `No se encontró la plantilla de acta` | Arrancaste el backend fuera de la carpeta `backend` |
| El frontend carga pero no trae datos | `VITE_API_URL` mal configurado o backend apagado |
| Error de CORS | El origen del navegador no está entre los permitidos en `InventarioConfig` |
