import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
  build: {
    lib: {
      entry: resolve(__dirname, 'src/index.ts'),
      name: 'Nexuspay',
      formats: ['umd', 'es'],
      fileName: (format) => `nexuspay.${format === 'umd' ? 'umd' : 'esm'}.js`
    },
    outDir: 'dist',
    sourcemap: true
  }
});
