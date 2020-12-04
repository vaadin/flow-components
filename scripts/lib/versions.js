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

async function getPlatformVersions(branch) {
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

async function getVersions() {
  const parentJs = await xml2js.parseStringPromise(fs.readFileSync('pom.xml', 'utf8'));
  const version = parentJs.project.version[0];
  const branch = version.replace(/^(\d+\.\d+).*$/, '$1');
  const isMaster = !await checkBranch(branch);

  return getPlatformVersions(isMaster ? 'master' : branch).then(json => {
    return ['core', 'vaadin'].reduce((prev, k) => {
      return prev.concat(Object.keys(json[k]).filter(pkg => json[k][pkg].npmName || json[k][pkg].javaVersion).map(pkg => {
        const version = json[k][pkg].javaVersion;
        const branch = version && version.replace('{{version}}', 'master').replace(/^(\d+\.\d+).*$/, '$1');
        json[k][pkg].name = pkg;
        json[k][pkg].branch = branch;
        json[k][pkg].module = pkg === 'iron-list' ? 'vaadin-' + pkg : pkg;
        return json[k][pkg];
      }));
    }, []);
  });  
}

async function getVersionsCsv() {
  return (await getVersions())
    .filter(pkg => pkg.npmName)
    .map(pkg => `${pkg.module}-flow-parent:${pkg.branch}:${pkg.javaVersion}`);
}

async function getVersionsJson() {
  return (await getVersions())
    .filter(pkg => pkg.npmName)
    .reduce((o, pkg) => { 
      o[pkg.name] = pkg.branch;
      return o;
    }, {});
}

module.exports = {
  getVersions,
  getVersionsCsv,
  getVersionsJson
};
