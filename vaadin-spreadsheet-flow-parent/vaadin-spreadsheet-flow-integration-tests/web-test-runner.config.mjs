import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { sharedConfig } from '../../shared/shared-web-test-runner-config.mjs';

// The Lit tests import production code from the sibling
// `vaadin-spreadsheet-flow` module via `../../vaadin-spreadsheet-flow/...`
// relative paths. The default rootDir (this module) doesn't serve
// files from the sibling, so resolve up to the parent that contains
// both modules.
const rootDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');

/**
 * The element module imports its TypeScript connector as `./lit/Spreadsheet.js`,
 * which the application's build resolves to the `.ts` file next to it. The
 * test server does that only for imports made from TypeScript, so a `.js`
 * import from a JavaScript module is pointed at the `.ts` source when only
 * that exists.
 */
function typeScriptFromJavaScript() {
  return {
    name: 'typescript-from-javascript',
    resolveImport({ source, context }) {
      if (!source.startsWith('.') || !source.endsWith('.js') || !context.path.endsWith('.js')) {
        return undefined;
      }
      const importer = path.join(rootDir, context.path);
      const target = path.resolve(path.dirname(importer), source);
      const typeScript = target.replace(/\.js$/, '.ts');
      if (!fs.existsSync(target) && fs.existsSync(typeScript)) {
        return source.replace(/\.js$/, '.ts');
      }
      return undefined;
    }
  };
}

/** @type {import('@web/test-runner').TestRunnerConfig} */
export default {
  ...sharedConfig,
  plugins: [...sharedConfig.plugins, typeScriptFromJavaScript()],
  rootDir: '..',
  files: ['test/lit-*.test.ts'],
  nodeResolve: true,
};
