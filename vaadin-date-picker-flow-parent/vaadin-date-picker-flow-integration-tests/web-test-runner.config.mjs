import path from 'node:path';
import { frontendSourcePlugin, sharedConfig } from '../../shared/shared-web-test-runner-config.mjs';

/** @type {import('@web/test-runner').TestRunnerConfig} */
export default {
  ...sharedConfig,

  plugins: [
    frontendSourcePlugin([
      path.resolve(import.meta.dirname, '../vaadin-date-picker-flow/src/main/resources/META-INF/frontend')
    ]),

    ...sharedConfig.plugins
  ]
};
