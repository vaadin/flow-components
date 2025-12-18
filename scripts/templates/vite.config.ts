// @ts-ignore can not be resolved until NPM packages are installed
import { defineConfig, UserConfigFn } from 'vite';
// @ts-ignore can not be resolved until Flow generates base Vite config
import { vaadinConfig } from './vite.generated';
import { sharedConfig, mergeConfigs } from '../shared/shared-vite-config';

export default defineConfig((env) => mergeConfigs(
  vaadinConfig(env),
  sharedConfig(env)
));
