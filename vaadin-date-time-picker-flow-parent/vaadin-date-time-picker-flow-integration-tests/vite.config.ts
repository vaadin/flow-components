// @ts-ignore can not be resolved until NPM packages are installed
import { UserConfigFn } from 'vite';
// @ts-ignore can not be resolved until Flow generates base Vite config
import { overrideVaadinConfig } from './vite.generated';
import { useLocalWebComponents } from '../../shared/web-components-vite-plugin';

const customConfig: UserConfigFn = (env) => ({
  // Here you can add custom Vite parameters
  // https://vitejs.dev/config/

  // Use local version of web-components
  plugins: [
    useLocalWebComponents('/Users/vursen/dev/vaadin/web-components/node_modules')
  ]
});

export default overrideVaadinConfig(customConfig);
