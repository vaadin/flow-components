import { esbuildPlugin } from "@web/dev-server-esbuild";
import { defaultReporter, summaryReporter } from "@web/test-runner";
import { junitReporter } from "@web/test-runner-junit-reporter";

export default {
  plugins: [esbuildPlugin({ ts: true })],
  testFramework: {
    config: {
      ui: 'bdd',
      timeout: '10000',
    },
  },
  ...(process.env.GITHUB_ACTIONS
    ? {
        reporters: [
          defaultReporter(),
          summaryReporter(),
          junitReporter({ outputPath: 'wtr-results.xml', reportLogs: true }),
        ],
      }
    : {}),
};
