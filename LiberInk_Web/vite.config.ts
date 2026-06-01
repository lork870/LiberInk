import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  build: {
    // Збільшуємо ліміт до 1000 кБ, щоб не падати від попереджень про розмір файлів
    chunkSizeWarningLimit: 1000, 
    rollupOptions: {
      output: {
        // Автоматичне розділення великих бібліотек на менші частини
        manualChunks(id) {
          if (id.includes('node_modules')) {
            return 'vendor';
          }
        }
      }
    }
  }
})