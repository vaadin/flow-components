// @ts-ignore can not be resolved until NPM packages are installed
import { UserConfigFn } from 'vite';
// @ts-ignore can not be resolved until Flow generates base Vite config
import { overrideVaadinConfig } from './vite.generated';
// import { useLocalWebComponents } from '../../shared/web-components-vite-plugin';

const customConfig: UserConfigFn = (env) => ({
  // Here you can add custom Vite parameters
  // https://vitejs.dev/config/

  // Use local version of web-components, disabled by default
  // To use this un-comment the lines below and change the path to
  // the absolute path of your web-components repo's node_modules
  // folder
  // DO NOT COMMIT THESE CHANGES!
  /*
  plugins: [
    useLocalWebComponents('/path/to/web-components/node_modules')
  ]
   */
});

export default overrideVaadinConfig(customConfig);
