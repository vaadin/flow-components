import fs from 'node:fs';
import path from 'node:path';
import { esbuildPlugin } from '@web/dev-server-esbuild';
import { defaultReporter, summaryReporter } from '@web/test-runner';
import { junitReporter } from '@web/test-runner-junit-reporter';

/**
 * A plugin that serves frontend files imported from `frontend/generated/jar-resources`
 * from their original source in a component module's `META-INF/frontend` when it exists,
 * instead of the stale build-time copy. The request URL stays in the flat `jar-resources`
 * namespace, so cross-module sibling imports (e.g. `./contextMenuConnector.js`) continue
 * to resolve the same way Flow's flattening makes them.
 *
 * @param {string[]} sourceDirs Absolute paths to `META-INF/frontend` directories to look up
 * sources in, checked in order. Falls back to the build-time copy when no source matches.
 */
export function frontendSourcePlugin(sourceDirs) {
  function resolveSourcePath(requestPath) {
    const match = requestPath.match(/\/frontend\/generated\/jar-resources\/(.+)$/);
    if (!match) {
      return;
    }
    for (const dir of sourceDirs) {
      const sourcePath = path.join(dir, match[1]);
      if (fs.existsSync(sourcePath)) {
        return sourcePath;
      }
    }
  }

  return {
    name: 'frontend-source',
    serverStart({ fileWatcher }) {
      sourceDirs.forEach((dir) => fileWatcher.add(dir));
    },
    serve(context) {
      const sourcePath = resolveSourcePath(context.path);
      if (sourcePath) {
        return { body: fs.readFileSync(sourcePath, 'utf8') };
      }
    },
    transform(context) {
      const sourcePath = resolveSourcePath(context.path);
      if (sourcePath) {
        // Don't cache frontend sources, so a rerun picks up the latest source
        // instead of a transform cached from a previous run.
        return { transformCache: false };
      }
    }
  };
}

/** @type {import('@web/test-runner').TestRunnerConfig} */
export const sharedConfig = {
  plugins: [esbuildPlugin({ ts: true })],
  nodeResolve: true,
  testFramework: {
    config: {
      ui: 'bdd',
      timeout: '10000'
    }
  },
  testRunnerHtml: (testFramework) => `
    <!DOCTYPE html>
    <html>
      <body>
        <script type="module">
          import { use, Assertion } from 'chai';
          import sinonChai from 'sinon-chai';
          use(sinonChai);

          // When a test fails, web-test-runner ships the error from the browser
          // to the Node reporter, serializing it with structuredClone. sinon-chai
          // sets the assertion error's \`actual\` to the spy function itself, and
          // structuredClone throws on functions. That rejection kills the
          // session-finished message, so the run reports a 120s "did not finish"
          // timeout instead of the actual assertion failure. Drop any
          // actual/expected that can't be cloned so the failure reports cleanly.
          const assert = Assertion.prototype.assert;
          Assertion.prototype.assert = function (...args) {
            try {
              return assert.apply(this, args);
            } catch (error) {
              for (const key of ['actual', 'expected']) {
                try {
                  structuredClone(error[key]);
                } catch {
                  delete error[key];
                  error.showDiff = false;
                }
              }
              throw error;
            }
          };
        </script>
        <script type="module" src="${testFramework}"></script>
      </body>
    </html>
  `,
  ...(process.env.GITHUB_ACTIONS
    ? {
        reporters: [
          defaultReporter(),
          summaryReporter(),
          junitReporter({ outputPath: 'wtr-results.xml', reportLogs: true })
        ]
      }
    : {})
};
