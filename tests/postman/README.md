# Pruebas de API con Postman

Colección de pruebas end-to-end de la API REST del Inventario Tecnológico Coopcentro.

## Archivos

| Archivo | Qué es |
| --- | --- |
| `Inventario_Tecnologico_Coopcentro.postman_collection.json` | Colección (importar en Postman con **Import → File**) |
| `Inventario - Local.postman_environment.json` | Environment con `baseUrl = http://localhost:8080/api` |

## Cómo ejecutarla

1. Levantar el backend:

```bash
cd backend && mvn spring-boot:run
```

2. Importar los dos archivos en Postman y seleccionar el environment **Inventario - Local**.
3. Abrir el **Runner**, elegir la colección completa y ejecutar **en orden**.

También se puede correr desde consola con [newman](https://github.com/postmanlabs/newman):

```bash
newman run "tests/postman/Inventario_Tecnologico_Coopcentro.postman_collection.json" -e "tests/postman/Inventario - Local.postman_environment.json"
```

## Cómo está organizada

| Carpeta | Contenido |
| --- | --- |
| `00 - Setup` | Health check y generación de datos únicos aleatorios de la corrida |
| `01 - Oficinas` | CRUD completo + duplicados + validaciones |
| `02 - Cargos` | CRUD completo + normalización a mayúsculas + duplicados |
| `03 - Empleados` | CRUD completo + rol por defecto + cédula/correo duplicados + FKs inexistentes |
| `04 - Usuarios TI` | CRUD + login inicial con cédula + cambio de contraseña (casos válidos e inválidos) |
| `05 - Activos` | CRUD con detalle embebido para laptop / móvil / periférico + unicidad de serial, código, IMEI e IP |
| `06 - Activo detalles` | CRUD del endpoint `/api/activo-detalles` |
| `07 - Asignaciones y actas` | Asignar → verificar estado → imprimir PDF → devolver → devolver todo del empleado, con sus reglas de negocio |
| `07b - Grupos de activos` | Crear grupo → agregar activos → asignarlo completo → verificar estado derivado → devolver → desagrupar y eliminar |
| `08 - Auth` | Login inválido, solicitud de recuperación y restablecimiento de contraseña |
| `09 - Limpieza` | Borra todo lo creado respetando el orden de las claves foráneas |

## Detalles a tener en cuenta

- **No hay autenticación por token.** El backend no usa Spring Security, así que ninguna petición lleva cabecera `Authorization`.
- **Todos los errores son HTTP 400.** `GlobalExceptionHandler` traduce cualquier `RuntimeException` y cualquier fallo de `@Valid` a `400` con el cuerpo `{"message": "..."}`. Por eso los casos "no encontrado" se verifican como 400 y no como 404.
- **Datos únicos por ejecución.** El script de `00 - Setup` genera un sufijo alfabético aleatorio y con él arma nombres, cédulas, correos, seriales, IMEI e IP. La colección se puede correr tantas veces como haga falta sin chocar con las validaciones de unicidad.
- **Contraseña inicial de un usuario TI = cédula del empleado**, con `debeCambiarContrasena = true`.
- **Impresión de actas:** `GET /api/actas/{id}/imprimir` necesita las plantillas AcroForm en `backend/plantillas/actas`. Si no están disponibles el backend responde 400 con el motivo; el test acepta 200 o 400 y registra el mensaje en la consola de Postman.
- **`POST /api/auth/solicitar-recuperacion` intenta enviar un correo real** vía Microsoft Graph. La colección usa un buzón ficticio (`@coopcentro.test`), así que el envío falla y solo queda en el log del backend. Omite esa petición si no quieres generar tráfico de correo en tu tenant.
- La carpeta `09 - Limpieza` deja la base como estaba. Si interrumpes la corrida a la mitad, quedarán registros de prueba identificables por el sufijo de esa ejecución (aparece en la consola de Postman).
