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
