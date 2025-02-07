import path from 'path';
import { UserConfigFn, mergeConfig, loadEnv } from 'vite';
import { useLocalWebComponents } from './web-components-vite-plugin';

export const mergeConfigs = (...configs: UserConfigFn[]) => {
  return configs.reduce((acc, config) => mergeConfig(acc, config));
};

export const sharedConfig: UserConfigFn = ({ mode }) => {
  const env = loadEnv(mode, path.resolve(__dirname, '../'), '');

  return {
    plugins: [
      // Use local web components:
      // 1. Copy .env.example to .env
      // 2. Set LOCAL_WEB_COMPONENTS_PATH to your repo
      env.LOCAL_WEB_COMPONENTS_PATH && useLocalWebComponents(env.LOCAL_WEB_COMPONENTS_PATH)
    ]
  };
};
