#!/usr/bin/env node
/**
 * Update the `vaadin-core` version in the versions.json file of the
 * appropriate platform branch, and open a PR for it.
 *
 * `@vaadin/vaadin-core` is released together with the web components, so it
 * follows their version. The script sets its `jsVersion` to the newest
 * `@vaadin/vaadin-core` published in npm for the major.minor of the platform
 * branch. The other web components are not pinned in platform, so they are
 * not touched.
 *
 * Branch is computed from the version property in the pom.xml, unless it is
 * given with the `baseBranch` environment variable.
 *
 * It expects GITHUB_TOKEN with the credentials for creating the PR.
 * Provide the PATH_TO_PLATFORM with the folder with platform, otherwise it will
 * checkout the project in the `./tmp/platform` directory.
 *
 * Example
 *   ./scripts/updatePlatform.js
 */

const fs = require('fs');
const https = require('https');
const xml2js = require('xml2js');
const {checkoutPlatorm, currentBranch, run, getLatestNpmVersion} = require('./lib/versions.js');

const NPM_NAME = '@vaadin/vaadin-core';

/**
 * Returns the major.minor that `vaadin-core` must follow: the platform branch
 * name for a maintenance branch, or the pom.xml version for `main`.
 */
async function computeMajorMinor(branch, pomFile) {
  let version = branch;
  if (!/^\d+\.\d+$/.test(branch)) {
    const pom = await xml2js.parseStringPromise(fs.readFileSync(pomFile, 'utf8'));
    version = pom.project.version[0];
  }
  const [, major, minor] = version.match(/^(\d+)\.(\d+)/);
  return {major, minor};
}

async function createPR(repo, title, head, base) {
  const token = process.env['GITHUB_TOKEN'];
  if (!token) {
    console.log(`GITHUB_TOKEN is not set, skipping PR creation`);
    process.exit(1);
  }
  const content = JSON.stringify({title, head, base}, null, 1);
  const body = await new Promise((resolve, reject) => {
    const req = https.request({
      method: 'POST',
      hostname: 'api.github.com',
      path: `/repos/${repo}/pulls`,
      headers: {
        'Authorization': `token ${token}`,
        'User-Agent': 'Node https client',
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(content),
      }
    }, res => {
      let body = '';
      res.on('data', data => body += data);
      res.on('end', () => resolve(body));
    });
    req.on('error', reject);
    req.write(content);
    req.end();
  });
  const resp = JSON.parse(body);
  if (!resp.html_url) {
    console.log(`Failed to create PR '${title}': ${body}`);
    process.exit(1);
  }
  console.log(`Created PR '${title}' ${resp.html_url}`);
}

async function commitChanges(branch, msg) {
  const newBranch = `update-vaadin-core-${branch}-${new Date().getTime()}`;
  await run(`git checkout -b ${newBranch}`);
  await run(`git commit -a -m ${JSON.stringify(msg)}`);
  await run(`git push origin ${newBranch}`);
  return newBranch;
}

async function main() {
  const pomFile = `${process.cwd()}/pom.xml`;
  const platformDir = process.env['PATH_TO_PLATFORM'] || './tmp/platform';
  if (!fs.existsSync(platformDir)) {
    fs.mkdirSync(platformDir, {recursive: true});
    await checkoutPlatorm(platformDir);
  }
  if (!fs.existsSync(`${platformDir}/versions.json`)) {
    console.log(`${platformDir} is not a valid platform local repo`);
    process.exit(1);
  }
  const branch = process.env['baseBranch'] || await currentBranch();
  const {major, minor} = await computeMajorMinor(branch, pomFile);

  process.chdir(platformDir);
  console.log(`Using ${branch} platform branch`);
  await run(`git checkout ${branch}`);
  await run(`git pull origin ${branch}`);

  const json = JSON.parse(fs.readFileSync('./versions.json', 'utf-8'));
  const vaadinCore = json.vaadin && json.vaadin['vaadin-core'];
  if (!vaadinCore) {
    console.log(`vaadin-core is not found in versions.json of ${branch}`);
    process.exit(1);
  }

  const currentVersion = vaadinCore.jsVersion;
  const nextVersion = await getLatestNpmVersion(NPM_NAME, currentVersion, major, minor);
  if (!nextVersion) {
    console.log(`No ${NPM_NAME} ${major}.${minor} version is published in npm`);
    process.exit(1);
  }
  if (nextVersion === currentVersion) {
    console.log(`vaadin-core is already ${currentVersion} in ${branch}, nothing to do`);
    return;
  }

  console.log(`Bumping vaadin-core from ${currentVersion} to ${nextVersion}`);
  vaadinCore.jsVersion = nextVersion;
  fs.writeFileSync('versions.json', JSON.stringify(json, null, 4).concat('\n'), 'utf-8');
  const msg = `chore: update vaadin-core to ${nextVersion} in ${branch}`;
  const prBranch = await commitChanges(branch, msg);
  await createPR('vaadin/platform', msg, prBranch, branch);
}

main().catch(e => {
  console.error(e);
  process.exit(1);
});
