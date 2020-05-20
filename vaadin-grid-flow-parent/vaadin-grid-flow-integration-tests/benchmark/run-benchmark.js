const { spawn, execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const options = (args => {
    const result = {};
    for(let i = 0; i < args.length; i++) {
        const arg = args[i];
        let property;
        if (arg === "--browser") property = "browser";
        else if(arg === "--huburl") property = "huburl";

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
const refGridTestPath = path.resolve(refGridPath, TEST_DIR);

const resultsPath = path.resolve('./results');

const REF_JETTY_PORT = 8088;
// The branch whose latest revision is used as the reference Grid
const REF_GIT_BRANCH = 'benchmark';

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

const startJetty = (cwd) => {
  return new Promise((resolve) => {
    const jetty = spawn('mvn', ['-B', '-q', 'jetty:run'], { cwd });
    processes.push(jetty);
    jetty.stderr.on('data', (data) => console.error(data.toString()));
    jetty.stdout.on('data', (data) => {
      if (data.toString().includes('Frontend compiled successfully')) {
        resolve();
      }
    });
  });
};

const prepareReferenceGrid = () => {
  execSync(
    `git clone --depth=1 --single-branch --branch ${REF_GIT_BRANCH} https://github.com/vaadin/vaadin-grid-flow.git ${refGridPath}`
  );

  // Add Jetty config to start the server on a different port
  const pomFile = path.resolve(refGridTestPath, 'pom.xml');
  const pomFileContent = fs.readFileSync(pomFile, 'utf8');

  const result = pomFileContent.replace(
    /<artifactId>jetty-maven-plugin<\/artifactId>/g,
    `
    <artifactId>jetty-maven-plugin</artifactId>
      <configuration>
        <httpConnector>
          <port>${REF_JETTY_PORT}</port>
        </httpConnector>
        <stopPort>${REF_JETTY_PORT + 1}</stopPort>
      </configuration>
    `
  );

  fs.writeFileSync(pomFile, result, 'utf8');

  execSync(`mvn versions:set -B -q -DnewVersion=${REF_GIT_BRANCH}-BENCHMARK`, {
    cwd: refGridPath,
  });

  console.log('Installing the reference grid');
  execSync(`mvn -B -q install -DskipTests`, { cwd: refGridPath });
};

const reportTestResults = (testVariantName, testResultsFilePath) => {
  const testResultsFileContent = fs.readFileSync(testResultsFilePath, 'utf-8');
  const { benchmarks } = JSON.parse(testResultsFileContent);
  const { low, high } = benchmarks[0].differences.find((d) => d).percentChange;
  const relativeDifferenceAverage = (low + high) / 2;

  // Print the test result as TeamCity build statistics
  console.log(
    `##teamcity[buildStatisticValue key='${testVariantName}' value='${relativeDifferenceAverage.toFixed(
      6
    )}']\n`
  );
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
  const ports = [9998, REF_JETTY_PORT];
  ports.forEach((port) => {
    args.push(
      `http://${clientHostname}:${port}/benchmark?variant=${gridVariantName}&metric=${metricName}`
    );
  });

  return new Promise((resolve) => {
    const tach = spawn('node_modules/.bin/tach', args, {
      cwd: gridTestPath,
      stdio: [process.stdin, process.stdout, process.stderr],
    });
    tach.on('close', () => {
      reportTestResults(testVariantName, testResultsFilePath);
      resolve();
    });
  });
};

const run = async () => {
  // Remove a possibly existing reference grid
  execSync(`rm -rf ${refGridPath}`);

  console.log('Prepare the reference Grid project');
  prepareReferenceGrid();

  console.log('Starting the Jetty server: Grid');
  await startJetty(gridTestPath);

  console.log('Starting the Jetty server: reference Grid');
  await startJetty(refGridTestPath);

  if (
    !fs.existsSync(path.resolve(gridTestPath, 'node_modules', '.bin', 'tach'))
  ) {
    console.log('Installing tachometer');
    execSync('npm i --quiet tachometer@0.4.18', { cwd: gridTestPath });
  }

  for (const testVariant of testVariants) {
    console.log(
      'Running test:',
      `${testVariant.gridVariantName}-${testVariant.metricName}`
    );
    await runTachometerTest(testVariant);
  }

  // Exit
  process.exit(0);
};

run();
