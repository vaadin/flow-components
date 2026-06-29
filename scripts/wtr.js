#!/usr/bin/env node
/**
 * Run web-test-runner tests for components if they exist
 */

const fs = require('fs');
const xml2js = require('xml2js');
const { execSync } = require('child_process');

let modules = [];
async function computeModules() {
  if (process.argv.length > 2) {
    // Modules are passed as arguments
    for (let i = 2; i < process.argv.length; i++) {
      modules.push(`vaadin-${process.argv[i]}-flow-parent`);
    }
  } else {
    // Read modules from the parent pom.xml
    const parentJs = await xml2js.parseStringPromise(fs.readFileSync(`pom.xml`, 'utf8'));
    modules = parentJs.project.modules[0].module
      .filter((m) => !/shared-parent/.test(m))
      .filter((m) => !/demo-helpers/.test(m));
  }
}

const wtrTestsFolderName = 'test';

function runTests() {
  for (const module of modules) {
    const id = module.replace('-parent', '');
    const itFolder = `${module}/${id}-integration-tests`;

    // Check if the IT module has wtr tests
    if (fs.existsSync(`${itFolder}/${wtrTestsFolderName}`)) {
      console.log(`Installing dependencies in ${itFolder}`);

      // Set up an empty node_modules and package.json before running Flow build
      // Those will get cleaned up by `flow:build-frontend` unless they existed
      // before, but we need them for running web-test-runner
      const nodeModules = `${itFolder}/node_modules`;
      const packageJson = `${itFolder}/package.json`;
      if (!fs.existsSync(nodeModules)) {
        fs.mkdirSync(nodeModules);
      }
      if (!fs.existsSync(packageJson)) {
        fs.writeFileSync(packageJson, '{}');
      }

      // Install the IT module dependencies
      execSync(`mvn -DskipTests flow:prepare-frontend flow:build-frontend`, {
        cwd: itFolder,
        stdio: 'inherit'
      });

      // Install dependencies required to run the web-test-runner tests
      execSync(`npm install --ignore-scripts @open-wc/testing @web/dev-server-esbuild @web/test-runner @web/test-runner-playwright @web/test-runner-junit-reporter @types/mocha sinon @vaadin/testing-helpers --save-dev --legacy-peer-deps`, {
        cwd: itFolder,
        stdio: 'inherit'
      });

      // Install Playwright Chromium
      execSync(`npx playwright install chromium`, {
        cwd: itFolder,
        stdio: 'inherit'
      });

      // Generate a CI config that adds the JUnit reporter on top of the existing config
      const hasBaseConfig = fs.existsSync(`${itFolder}/web-test-runner.config.mjs`);
      const ciConfigPath = `${itFolder}/wtr-ci.config.mjs`;
      fs.writeFileSync(ciConfigPath, [
        `import { defaultReporter, summaryReporter } from '@web/test-runner';`,
        `import { junitReporter } from '@web/test-runner-junit-reporter';`,
        hasBaseConfig ? `import baseConfig from './web-test-runner.config.mjs';` : '',
        `export default {`,
        hasBaseConfig ? `  ...baseConfig,` : '',
        `  reporters: [defaultReporter(), summaryReporter(), junitReporter({ outputPath: 'wtr-results.xml', reportLogs: true })],`,
        `};`,
      ].filter(Boolean).join('\n'));

      // Run the tests
      console.log(`Running tests in ${itFolder}`);
      try {
        execSync(`npx web-test-runner --playwright ${wtrTestsFolderName}/**/*.test.ts --node-resolve --config wtr-ci.config.mjs`, {
          cwd: itFolder,
          stdio: 'inherit'
        });
      } finally {
        fs.unlinkSync(ciConfigPath);
      }
    }
  }
}

async function main() {
  await computeModules();
  runTests();
}

main();
