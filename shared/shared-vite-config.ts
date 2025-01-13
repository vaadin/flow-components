import { UserConfigFn, mergeConfig } from 'vite';
import { useLocalWebComponents } from './web-components-vite-plugin';

export const mergeConfigs = (...configs: UserConfigFn[]) => {
  return configs.reduce((acc, config) => mergeConfig(acc, config));
};

export const sharedConfig: UserConfigFn = (env) => ({
  plugins: [
    // Use local version of web-components, disabled by default.
    // To use this, uncomment the lines below and change the path
    // to your local web-components folder if needed (absolute or
    // relative to this shared config).
    // DO NOT COMMIT THESE CHANGES!
    // useLocalWebComponents('../../web-components')
  ]
});
