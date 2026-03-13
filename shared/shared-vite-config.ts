import path from 'node:path';
// @ts-expect-error this dependency is added by flow-server
import { UserConfigFn, mergeConfig, loadEnv } from 'vite';
// @ts-expect-error this dependency is added by flow-build-tools
import useLocalWebComponents from '@vaadin/vite-plugin-local-web-components';

export const mergeConfigs = (...configs: UserConfigFn[]) => {
  return configs.reduce((acc, config) => mergeConfig(acc, config));
};

// @ts-expect-error
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
