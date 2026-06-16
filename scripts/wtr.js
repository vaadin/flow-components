#!/usr/bin/env node
/**
 * Run web-test-runner tests for components if they exist
 */

const fs = require('fs');
const xml2js = require('xml2js');
const { execSync } = require('child_process');
const { parseArgs } = require('util');

const { values: options, positionals } = parseArgs({
  options: {
    watch: { type: 'boolean', default: false }
  },
  allowPositionals: true
});

let modules = [];
async function computeModules() {
  if (positionals.length > 0) {
    // Modules are passed as arguments
    for (const positional of positionals) {
      modules.push(`vaadin-${positional}-flow-parent`);
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

async function appendSessionError(xmlPath, error) {
  let xml;
  if (fs.existsSync(xmlPath)) {
    xml = await xml2js.parseStringPromise(fs.readFileSync(xmlPath, 'utf8'));
    if (!xml.testsuites.testsuite) {
      xml.testsuites.testsuite = [];
    }
  } else {
    xml = { testsuites: { $: {}, testsuite: [] } };
  }
  const hasFailures = xml.testsuites.testsuite?.some(
    (s) => parseInt(s.$.failures || '0') > 0 || parseInt(s.$.errors || '0') > 0
  );
  if (hasFailures) return;

  xml.testsuites.testsuite.push({
    $: { name: 'WTR Session', tests: '1', failures: '1', errors: '0', skipped: '0', time: '0' },
    testcase: [{
      $: { name: 'Browser session completed cleanly', classname: 'WTR Session', time: '0' },
      failure: [{ _: `WTR exited with code ${error.status}: ${error.message}`, $: { message: 'WTR session error' } }]
    }]
  });
  fs.writeFileSync(xmlPath, new xml2js.Builder().buildObject(xml));
}

async function runTests() {
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

      // Install Playwright Chromium
      execSync(`npx playwright install chromium`, {
        cwd: itFolder,
        stdio: 'inherit'
      });

      // Run the tests
      console.log(`Running tests in ${itFolder}`);
      try {
        const watchFlag = options.watch ? ' --watch' : '';
        execSync(`npx web-test-runner --playwright ${wtrTestsFolderName}/**/*.test.ts --node-resolve${watchFlag}`, {
          cwd: itFolder,
          stdio: 'inherit'
        });
      } catch (e) {
        if (process.env.GITHUB_ACTIONS) {
          await appendSessionError(`${itFolder}/wtr-results.xml`, e);
        }

        throw e;
      }
    }
  }
}

async function main() {
  await computeModules();
  await runTests();
}

main();
