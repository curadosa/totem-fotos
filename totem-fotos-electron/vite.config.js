import { networkInterfaces } from 'node:os'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

function encontrarIpv4Local() {
  return Object.values(networkInterfaces())
    .flat()
    .find(endereco => endereco?.family === 'IPv4' && !endereco.internal)?.address
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const hostPublico = encontrarIpv4Local() || 'localhost'
  const urlMobile = env.VITE_MOBILE_URL || `http://${hostPublico}:5173`

  return {
    plugins: [vue()],
    define: {
      'import.meta.env.VITE_MOBILE_URL': JSON.stringify(urlMobile)
    },
    resolve: {
      dedupe: ['vue']
    },
    server: {
      host: '127.0.0.1',
      port: 9000,
      strictPort: true
    },
    build: {
      outDir: 'dist',
      emptyOutDir: true
    }
  }
})
