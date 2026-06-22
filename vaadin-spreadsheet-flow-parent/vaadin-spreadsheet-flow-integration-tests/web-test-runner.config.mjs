import { sharedConfig } from '../../shared/shared-web-test-runner-config.mjs';

/** @type {import('@web/test-runner').TestRunnerConfig} */
export default {
  ...sharedConfig,
  // The Lit tests import production code from the sibling
  // `vaadin-spreadsheet-flow` module via `../../vaadin-spreadsheet-flow/...`
  // relative paths. The default rootDir (this module) doesn't serve
  // files from the sibling, so resolve up to the parent that contains
  // both modules.
  rootDir: '..',
  files: ['test/lit-*.test.ts'],
  nodeResolve: true,
};
