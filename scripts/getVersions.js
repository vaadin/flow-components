#!/usr/bin/env node
/**
 * Read versions.json file from the appropriate branch in platform
 * Example
 *   ./scripts/getVersions.js
 */
const {getVersionsCsv, getVersionsJson} = require('./lib/versions.js');

async function main() {
  if (process.argv[2] == '--json') {
    console.log(JSON.stringify(await getVersionsJson()));
  } else {
    console.log((await getVersionsCsv()).join('\n'));
  }
}

main();
