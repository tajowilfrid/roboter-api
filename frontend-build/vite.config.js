import { defineConfig } from 'vite';
import path from 'path';

export default defineConfig({
  build: {
    lib: {
      // The entry point that imports everything
      entry: path.resolve(__dirname, 'src/main.js'),
      name: 'RobotComponents',
      // The output file names
      fileName: (format) => `robot-components.${format}.js`
    },
    outDir: '../dist', // Output directory for the built files
    emptyOutDir: true,
  },
  base: './' 
});