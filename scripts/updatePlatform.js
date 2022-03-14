#!/usr/bin/env node
/**
 * Update the versions.json file in the appropriate branch of platform.
 *
 * Branch is computed from the version property in the pom.xml.
 *
 * It expects GITHUB_TOKEN with the credentials for creating the PR.
 * Provide the PATH_TO_PLATFORM with the folder with platform, otherwise it will
 * checkout the project in the `./tmp/platform` directory.
 *
 * Example
 *   ./scripts/updatePlatorm.js
 */

const fs = require('fs');
const https = require('https');
const {checkoutPlatorm, currentBranch, run, getLatestBowerVersion, getLatestNpmVersion, getAnnotations} = require('./lib/versions.js');

async function computeNextVersions(byName) {
  const names = Object.keys(byName);
  for (let i = 0; i < names.length; i++) {
    const pkg = byName[names[i]];
    if (pkg.org.npmName) {
      let o = pkg.currentVersion = {version: pkg.org.npmVersion || pkg.org.jsVersion};
      [o.major, o.minor, o.patch, o.extra] = o.version.split(/[\.\-]+/);

      let nextVersion = await getLatestNpmVersion(pkg.org.npmName, o.version, o.major, o.minor);
      o = pkg.nextVersion = {version: nextVersion };
      [o.major, o.minor, o.patch, o.extra] = nextVersion.split(/[\.\-]+/);

      if (pkg.org.jsVersion && pkg.org.npmVersion) {
        o = pkg.bowerVersion = {version: pkg.org.jsVersion};
        [o.major, o.minor, o.patch, o.extra] = o.version.split(/[\.\-]+/);
        o.nextVersion = await getLatestBowerVersion(names[i], o.version, o.major, o.minor);
      }
    }
  }
}

async function updateNextVersions(byName) {
  let modified = false;
  const names = Object.keys(byName);
  for (let i = 0; i < names.length; i++) {
    const pkg = byName[names[i]];
    if (pkg.nextVersion && pkg.nextVersion.version !== pkg.currentVersion.version) {
      modified = true;
      console.log(`Bumping ${names[i]} from ${pkg.currentVersion.version} to ${pkg.nextVersion.version}`);
      pkg.org.npmVersion && (pkg.org.npmVersion = pkg.nextVersion.version) || (pkg.org.jsVersion = pkg.nextVersion.version);
    }
    if (pkg.bowerVersion && pkg.bowerVersion.version != pkg.bowerVersion.nextVersion) {
      modified = true;
      console.log(`Bumping ${names[i]} (bower) from ${pkg.org.jsVersion} to ${pkg.bowerVersion.nextVersion}`);
      pkg.org.jsVersion = pkg.bowerVersion.nextVersion;
    }
  }
  return modified;
}

async function createPR(repo, title, head, base) {
  const payload = {
    title, head, base
  }
  const token = process.env['GITHUB_TOKEN'];
  if (!token) {
    console.log(`GITHUB_TOKEN is not set, skipping PR creation`);
    process.exit(1);
  }
  return new Promise(resolve => {
    const content = JSON.stringify({ title, head, base }, null, 1)
    const req = https.request({
      method: 'POST',
      hostname: 'api.github.com',
      path: `/repos/${repo}/pulls`,
      headers: {
        'Authorization': `token ${token}`,
        'User-Agent': 'Node https client',
        'Content-Type': 'application/json',
        'Content-Length': content.length,
      },
      body: content
    }, res => {
      let body = "";
      res.on("data", data => {
        body += data;
      });
      res.on("end", () => {
        resolve(body);
      });
    });
    req.write(content)
  }).then(body => {
    const resp = JSON.parse(body);
    console.log(`Created PR '${title}' ${resp.url}`);
  });
}

async function commitChanges(branch, msg) {
  const newBranch = `update-${branch}-${new Date().getTime()}`;
  await run(`git checkout -b ${newBranch}`);
  await run(`git commit -m '${msg}' -a`);
  await run(`git push origin ${newBranch}`);
  return newBranch;
}

async function main() {
  let platformDir = process.env['PATH_TO_PLATFORM'] || './tmp/platform';
  if (!fs.existsSync(platformDir)) {
    fs.mkdirSync(platformDir, {recursive: true});
    await checkoutPlatorm(platformDir);
  }
  if (!fs.existsSync(`${platformDir}/versions.json`)) {
    console.log(`${platformDir} is not a valid platform local repo`);
    process.exit(1);
  }
  const annotations = await getAnnotations();
  const branch = await currentBranch();
  process.chdir(platformDir);
  console.log(`Using ${branch} platform branch`);
  await run(`git checkout ${branch}`);
  await run(`git pull origin ${branch}`);

  const json = JSON.parse(fs.readFileSync('./versions.json', 'utf-8'));
  const byName = ['core', 'vaadin', 'bundles'].reduce((prev, k) => {
      Object.keys(json[k]).filter(pkg => (json[k][pkg].npmName || json[k][pkg].javaVersion) && pkg !== 'vaadin-core' ).map(pkg => {
      const version = json[k][pkg].javaVersion;
      const branch = version && version.replace('{{version}}', 'master').replace(/^(\d+\.\d+).*$/, '$1');
      prev[pkg] = prev[pkg] || {};
      prev[pkg]['org'] = json[k][pkg];
      prev[pkg].package = pkg;
      prev[pkg].branch = branch;
      prev[pkg].module = pkg === 'iron-list' ? 'vaadin-' + pkg : pkg;
    });
    return prev;
  }, {});

  await computeNextVersions(byName);
  if (await updateNextVersions(byName)) {
    fs.writeFileSync('versions.json', JSON.stringify(json, null, 4).concat('\n'), 'utf-8');
    const msg = `chore: update versions.json in ${branch} with latest WC`;
    const prBranch = await commitChanges(branch, msg);
    const pr = createPR('vaadin/platform', msg, prBranch, branch);
  }
}

main();
