import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';
// import { useLocalWebComponents } from '../../shared/web-components-vite-plugin';

const customConfig: UserConfigFn = (env) => ({
  // Here you can add custom Vite parameters
  // https://vitejs.dev/config/

  // Disable the Vite deps pre-bundling in the app to prevent Vite
  // from caching connectors’ source code in the file system
  optimizeDeps: {
    disabled: true
  },

  // Use local version of web-components, disabled by default
  // To use this un-comment the lines below and adapt the path to your local checkout
  // DO NOT COMMIT THESE CHANGES!
  /*
  plugins: [
    useLocalWebComponents('/path/to/web-components/node_modules')
  ]
   */
});

export default overrideVaadinConfig(customConfig);
