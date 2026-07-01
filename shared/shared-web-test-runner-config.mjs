import { esbuildPlugin } from '@web/dev-server-esbuild';
import { defaultReporter, summaryReporter } from '@web/test-runner';
import { junitReporter } from '@web/test-runner-junit-reporter';

/** @type {import('@web/test-runner').TestRunnerConfig} */
export const sharedConfig = {
  plugins: [esbuildPlugin({ ts: true })],
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
