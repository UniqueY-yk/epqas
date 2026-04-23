import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import mkcert from 'vite-plugin-mkcert'

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        mkcert()
    ],
    resolve: {
        alias: {
            '@': '/src'
        }
    },
    server: {
        proxy: {
            '/api': {
                target: 'https://localhost:8443',
                changeOrigin: true,
                secure: false, // Accept self-signed SSL certificate in dev
                rewrite: (path) => path.replace(/^\/api/, '')
            }
        }
    }
})
