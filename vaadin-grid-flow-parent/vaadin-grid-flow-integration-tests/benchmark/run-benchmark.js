/*
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
const { spawn, execSync } = require('child_process');
const fs = require('fs');
const path = require('path');
const https = require('https');
const { URLSearchParams } = require('url');

const options = (args => {
    const result = {};
    for(let i = 0; i < args.length; i++) {
        const arg = args[i];
        let property;
        if (arg === "--browser") property = "browser";
        else if(arg === "--huburl") property = "huburl";
        else if(arg === "--submit-results-path") property = "submitResultsPath";

        if(property) result[property] = args[++i];
    }
    return result;
})(process.argv.slice(2));

const GRID_DIR = '../../';
const REF_GRID_DIR = './reference-clone';
const TEST_DIR = 'vaadin-grid-flow-integration-tests';

const gridPath = path.resolve(GRID_DIR);
const refGridPath = path.resolve(REF_GRID_DIR);

const gridTestPath = path.resolve(gridPath, TEST_DIR);
const refGridTestPath = path.resolve(refGridPath, 'vaadin-grid-flow-parent',TEST_DIR);

const resultsPath = path.resolve('./results');

const JETTY_PORT = 8080;
const REF_JETTY_PORT = 8088;
// The branch whose latest revision is used as the reference Grid
const REF_GIT_BRANCH = 'grid-benchmark';

const processes = [];
const cleanup = () => processes.forEach((ps) => ps.kill());
process.on('exit', cleanup);
process.on('SIGINT', cleanup);

const testVariants = [];
const browsers = options.browser ? [options.browser] : ['firefox-headless', 'chrome-headless'];
browsers.forEach((browserName) => {
  [
    'simple',
    'multicolumn',
    'componentrenderers',
    'detailsopened',
    'tree',
    'mixed',
  ].forEach((gridVariantName) => {
    testVariants.push({
      gridVariantName,
      browserName,
      metricName: 'rendertime',
    });
    testVariants.push({
      gridVariantName,
      browserName,
      metricName: 'verticalscrollframetime',
    });
    if (['tree', 'mixed'].includes(gridVariantName)) {
      testVariants.push({
        gridVariantName,
        browserName,
        metricName: 'expandtime',
      });
    }
    if (['multicolumn', 'mixed'].includes(gridVariantName)) {
      testVariants.push({
        gridVariantName,
        browserName,
        metricName: 'horizontalscrollframetime',
      });
    }
  });
});

const startJetty = (cwd,port,stopPort) => {
  return new Promise((resolve) => {
    const jetty = spawn('mvn', ['-B', '-q', 'package', 'jetty:run', '-DskipTests', `-Djetty.http.port=${port}`, `-Djetty.stop.port=${stopPort}`], { cwd });
    processes.push(jetty);
    jetty.stderr.on('data', (data) => console.error(data.toString()));
    jetty.stdout.on('data', (data) => {
      const message = data.toString();
      if (message.includes('Frontend compiled successfully') || message.includes('Vaadin application has been deployed and started')) {
        resolve();
      }
    });
  });
};

const prepareReferenceGrid = () => {
  execSync(
    `git clone --depth=1 --single-branch --branch ${REF_GIT_BRANCH} https://github.com/vaadin/vaadin-flow-components.git ${refGridPath}`
  );

  const pomFile = path.resolve(refGridTestPath, 'pom.xml');
  const pomFileContent = fs.readFileSync(pomFile, 'utf8');

  execSync(`mvn versions:set -B -q -DnewVersion=${REF_GIT_BRANCH}-BENCHMARK`, {
    cwd: refGridPath,
  });

  console.log('Installing the reference grid');
  execSync(`mvn install -B -DskipTests -Drelease -T 1C`, { cwd: refGridPath });
};

const getTestResultValue = (testResultsFilePath) => {
  const testResultsFileContent = fs.readFileSync(testResultsFilePath, 'utf-8');
  const { benchmarks } = JSON.parse(testResultsFileContent);
  const { low, high } = benchmarks[0].differences.find((d) => d).percentChange;
  const relativeDifferenceAverage = (low + high) / 2;
  return relativeDifferenceAverage;
};

const runTachometerTest = ({ gridVariantName, metricName, browserName }) => {
  const sampleSize = {
    rendertime: 40,
    expandtime: 40,
    horizontalscrollframetime: 20,
    verticalscrollframetime: 20,
  }[metricName];

  const testVariantName = `${gridVariantName}-${metricName}-${browserName}`;
  if (!fs.existsSync(resultsPath)) {
    fs.mkdirSync(resultsPath);
  }

  const testResultsFilePath = path.resolve(
    resultsPath,
    `${testVariantName}.json`
  );
  const hubAddress = options.huburl;
  const browserParamValue = hubAddress ? `${browserName}@${hubAddress}` : browserName;
  const args = [];
  args.push('--measure', 'global');
  args.push('--sample-size', sampleSize);
  args.push('--json-file', testResultsFilePath);
  args.push('--browser', browserParamValue);
  let clientHostname = process.env['CLIENT_HOSTNAME'] || 'localhost';
  const ports = [JETTY_PORT, REF_JETTY_PORT];
  ports.forEach((port) => {
    args.push(
      `http://${clientHostname}:${port}/vaadin-grid/benchmark?variant=${gridVariantName}&metric=${metricName}`
    );
  });

  return new Promise((resolve) => {
    const tach = spawn('node_modules/.bin/tach', args, {
      cwd: gridTestPath,
      stdio: [process.stdin, process.stdout, process.stderr],
    });
    tach.on('close', () => {
      const value = getTestResultValue(testResultsFilePath);
      resolve({
        testVariantName,
        value
      });
    });
  });
};

const submitBenchmarkResults = (results, submitResultsPath) => {
  return new Promise(resolve => {
    const params = new URLSearchParams();
    results.forEach(result => {
      params.append(result.testVariantName, result.value);
    });
    const data = params.toString();

    const requestOptions = {
      hostname: 'script.google.com',
      port: 443,
      path: submitResultsPath,
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
        'Content-Length': Buffer.byteLength(data)
      },
      body: data
    };

    const req = https.request(requestOptions, (res) => {
      console.log(`statusCode: ${res.statusCode}`);

      res.on('data', (d) => {
        process.stdout.write(d);
      });

      res.on('end', resolve);
    });

    req.on('error', (error) => {
      console.error(error);
    });

    req.write(data);
    req.end();
  });
};

const run = async () => {
  // Remove a possibly existing reference grid
  execSync(`rm -rf ${refGridPath}`);

  console.log('Prepare the reference Grid project');
  prepareReferenceGrid();

  console.log('Starting the Jetty server: Grid');
  await startJetty(gridTestPath,JETTY_PORT,JETTY_PORT+1);

  console.log('Starting the Jetty server: reference Grid');
  await startJetty(refGridTestPath,REF_JETTY_PORT,REF_JETTY_PORT+1);

  if (
    !fs.existsSync(path.resolve(gridTestPath, 'node_modules', '.bin', 'tach'))
  ) {
    console.log('Installing tachometer');
    execSync('npm i --quiet tachometer@0.4.18', { cwd: gridTestPath });
  }

  const results = [];
  for (const testVariant of testVariants) {
    console.log(
      'Running test:',
      `${testVariant.gridVariantName}-${testVariant.metricName}`
    );
    results.push(await runTachometerTest(testVariant));
  }

  // Print the test result as TeamCity build statistics
  results.forEach(({ testVariantName, value }) => {
    console.log(
      `##teamcity[buildStatisticValue key='${testVariantName}' value='${value.toFixed(
        6
      )}']\n`
    );
  });

  if (options.submitResultsPath) {
    // Submit results to Google Spreadsheet
    await submitBenchmarkResults(results, options.submitResultsPath);
  }

  // Exit
  process.exit(0);
};

run();
