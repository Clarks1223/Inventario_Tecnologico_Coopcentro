# Sistema de Inventario - Frontend

Frontend desarrollado en React con Vite y Material UI.

## Requisitos

- Node.js 16+
- NPM

## Instalación

1.  Navegar al directorio:
    ```bash
    cd FrontEnd
    ```
2.  Instalar dependencias:
    ```bash
    npm install
    ```

## Ejecución

```bash
npm run dev
```

La aplicación abrirá en `http://localhost:5173` (o similar).

## Estructura

- `src/layout`: Componentes de estructura (Menú lateral, Barra superior).
- `src/pages`: Vistas principales (Dashboard, Oficinas, Activos, etc.).
- `src/services`: Configuración de Axios (`api.js`).

## Configuración

El archivo `src/services/api.js` apunta por defecto a `http://127.0.0.1:8000/api/`. Modificar si el backend corre en otor puerto/host.
