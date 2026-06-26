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
          import { use } from 'chai';
          import sinonChai from 'sinon-chai';
          use(sinonChai);
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
