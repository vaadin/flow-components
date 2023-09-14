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
      execSync(`npm install @open-wc/testing @web/dev-server-esbuild @web/test-runner @web/test-runner-playwright sinon --save-dev`, {
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
      execSync(`npx web-test-runner --playwright ${wtrTestsFolderName}/**/*.test.ts --node-resolve`, {
        cwd: itFolder,
        stdio: 'inherit'
      });
    }
  }
}

async function main() {
  await computeModules();
  runTests();
}

main();
