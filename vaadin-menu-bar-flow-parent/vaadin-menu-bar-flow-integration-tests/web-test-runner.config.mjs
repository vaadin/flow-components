import path from 'node:path';
import { frontendSourcePlugin, sharedConfig } from '../../shared/shared-web-test-runner-config.mjs';

/** @type {import('@web/test-runner').TestRunnerConfig} */
export default {
  ...sharedConfig,

  plugins: [
    frontendSourcePlugin([
      path.resolve(import.meta.dirname, '../vaadin-menu-bar-flow/src/main/resources/META-INF/frontend'),
      // The menu bar connector imports the context menu connector, which Flow
      // merges into the same jar-resources folder
      path.resolve(
        import.meta.dirname,
        '../../vaadin-context-menu-flow-parent/vaadin-context-menu-flow/src/main/resources/META-INF/frontend'
      )
    ]),

    ...sharedConfig.plugins
  ]
};
