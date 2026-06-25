import path from 'node:path';
import { connectorSourcePlugin, sharedConfig } from '../../shared/shared-web-test-runner-config.mjs';

/** @type {import('@web/test-runner').TestRunnerConfig} */
export default {
  ...sharedConfig,

  plugins: [
    connectorSourcePlugin([
      path.resolve(import.meta.dirname, '../vaadin-grid-flow/src/main/resources/META-INF/frontend')
    ]),

    ...sharedConfig.plugins
  ]
};
