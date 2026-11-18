import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    // Bind to 0.0.0.0 so a phone on the same wifi can reach the dev server.
    host: true,
  },
});
