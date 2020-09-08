#!/usr/bin/env node
/**
 * Read versions.json file from the appropriate branch in platform
 * Example
 *   ./scripts/getVersions.js
 */

const https = require("https");
const xml2js = require('xml2js');
const fs = require('fs');

async function checkBranch(branch) {
  return new Promise(resolve => {
    https.request({
      method: 'HEAD',
      hostname: 'raw.githubusercontent.com',
      path: `vaadin/platform/${branch}/versions.json`
    }, r => {
      resolve(r.statusCode === 200);
    }).end();
  });
}

async function getVersions(branch) {
  return new Promise(resolve => {
    https.get(`https://raw.githubusercontent.com/vaadin/platform/${branch}/versions.json`, res => {
      res.setEncoding("utf8");
      let body = "";
      res.on("data", data => {
        body += data;
      });
      res.on("end", () => {
        resolve(body);
      });
    });
  }).then(body => {
    return JSON.parse(body);
  });
}

async function main() {
  const parentJs = await xml2js.parseStringPromise(fs.readFileSync('pom.xml', 'utf8'));
  const version = parentJs.project.version[0];
  const branch = version.replace(/^(\d+\.\d+).*$/, '$1');
  const isMaster = !await checkBranch(branch);

  return getVersions(isMaster ? 'master' : branch).then(json => {
    return ['core', 'vaadin'].reduce((prev, k) => {
      return prev.concat(Object.keys(json[k]).filter(pkg => /\-/.test(pkg) && json[k][pkg].javaVersion).map(pkg => {
        const branch = json[k][pkg].javaVersion.replace('{{version}}', 'master').replace(/^(\d+\.\d+).*$/, '$1');
        return `${pkg === 'iron-list' ? 'vaadin-' + pkg : pkg}-flow-parent:${branch}`
      }));
    }, []);
  });

}

main().then(arr => console.log(arr.join('\n')));
