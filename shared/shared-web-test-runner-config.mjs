import { esbuildPlugin } from '@web/dev-server-esbuild';

/** @type {import('@web/test-runner').TestRunnerConfig} */
export const sharedConfig = {
  plugins: [esbuildPlugin({ ts: true })],
  testFramework: {
    config: {
      ui: 'bdd',
      timeout: '10000'
    }
  }
};
