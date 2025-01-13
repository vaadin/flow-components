import { UserConfigFn, mergeConfig } from 'vite';
import { useLocalWebComponents } from './web-components-vite-plugin';

export const mergeConfigs = (...configs: UserConfigFn[]) => {
  return configs.reduce((acc, config) => mergeConfig(acc, config));
};

export const sharedConfig: UserConfigFn = (env) => ({
  plugins: [
    // Use local version of web-components, disabled by default
    // To use this un-comment the lines below and change the path to
    // the absolute path of your web-components repo's node_modules
    // folder
    // DO NOT COMMIT THESE CHANGES!
    // useLocalWebComponents('../../web-components')
  ]
});
