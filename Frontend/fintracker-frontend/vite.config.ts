/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from "@tailwindcss/vite";
import mkcert from 'vite-plugin-mkcert';
import { visualizer } from 'rollup-plugin-visualizer';
// https://vite.dev/config/


// More info at: https://storybook.js.org/docs/next/writing-tests/integrations/vitest-addon
export default defineConfig({
  plugins: [react(),visualizer({
      open: true,        // opens browser automatically after build
      gzipSize: true,
      brotliSize: true,
      filename: "dist/stats.html"
    }) , tailwindcss(), mkcert()],
  server: {
    https: true
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor-react':   ['react', 'react-dom', 'react-router'],
          'vendor-redux':   ['@reduxjs/toolkit', 'react-redux'],
          'vendor-mui':     ['@mui/material', '@mui/icons-material', '@emotion/react', '@emotion/styled'],
          'vendor-motion':  ['framer-motion'],
          'vendor-recharts':['recharts'],
          'vendor-d3':      ['d3-scale', 'd3-color', 'd3-format', 'd3-shape', 'd3-time', 'd3-time-format'],
          'vendor-axios':   ['axios'],
        }
      }
    }
  },
  test: {
    projects: [{
      extends: true,
      plugins: [
      // The plugin will run tests for the stories defined in your Storybook config
      // See options at: https://storybook.js.org/docs/next/writing-tests/integrations/vitest-addon#storybooktest
      ]
    }]
  }
});