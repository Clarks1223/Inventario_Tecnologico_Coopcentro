import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'
import { fileURLToPath } from 'url'
import viteCompression from 'vite-plugin-compression'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  // El navegador pide siempre /api al mismo host que sirve la pagina (ver VITE_API_URL)
  // y es Vite quien reenvia al backend. Asi la app funciona igual desde localhost que
  // desde otro PC de la red local, sin hardcodear la IP ni depender de CORS.
  const backendProxy = {
    '/api': {
      target: env.VITE_BACKEND_URL || 'http://localhost:8080',
      changeOrigin: true,
      secure: false,
    },
  }

  return {
    plugins: [
      react(),
      viteCompression({
        algorithm: 'gzip',
        ext: '.gz',
        threshold: 10240,
        deleteOriginFile: false,
      }),
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      // 0.0.0.0: escucha en todas las interfaces para que la app sea accesible
      // desde otros equipos de la red local, no solo desde este PC.
      host: '0.0.0.0',
      port: 5173,
      strictPort: false,
      proxy: backendProxy,
      hmr: {
        overlay: true,
      },
    },
    preview: {
      host: '0.0.0.0',
      port: 4173,
      strictPort: false,
      proxy: backendProxy,
    },
    build: {
      target: 'es2020',
      sourcemap: false,
      rollupOptions: {
        output: {
          manualChunks: {
            'react-vendor': ['react', 'react-dom', 'react-router-dom'],
            'mui-core': ['@mui/material', '@emotion/react', '@emotion/styled'],
            'mui-icons': ['@mui/icons-material'],
            xlsx: ['xlsx'],
          },
        },
      },
    },
    css: {
      modules: {
        localsConvention: 'camelCase',
      },
    },
    test: {
      globals: true,
      environment: 'jsdom',
      setupFiles: './src/test/setup.js',
    },
  }
})
