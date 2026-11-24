import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Em desenvolvimento, o Vite repassa /api para o backend. Assim o navegador
    // enxerga tudo como mesma origem e as URLs de imagem (/api/images/..) funcionam.
    // API_PROXY_TARGET permite apontar para outra porta (ex.: http://localhost:8081).
    proxy: {
      '/api': process.env.API_PROXY_TARGET ?? 'http://localhost:8080',
    },
  },
})
