const https = require("https");
const xml2js = require('xml2js');
const fs = require('fs');
const exec = require('util').promisify(require('child_process').exec);

async function run(cmd) {
  const { stdout, stderr } = await exec(cmd);
  return stdout;
}

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

async function checkoutPlatorm(dir) {
  console.log(`Checking out platform into ${dir} ...`);
  await run(`git clone https://github.com/vaadin/platform.git ${dir}`);
}

async function currentBranch() {
  const parentJs = await xml2js.parseStringPromise(fs.readFileSync('pom.xml', 'utf8'));
  const version = parentJs.project.version[0];
  const branch = version.replace(/^(\d+\.\d+).*$/, '$1');
  const isMaster = !await checkBranch(branch);
  return isMaster ? 'master' : branch;
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
  const branch = await currentBranch();
  return getPlatformVersions(branch).then(json => {
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

async function getAnnotations(){
  const cmd = 'grep -r @NpmPackage ./vaadin*parent/*/src/*/java';
  const output = await run(cmd);
  const lines = output.split('\n').filter(Boolean);
  return lines.map(line => {
    const r = /(.*(vaadin-.*)-parent.*):(.*value *= *"([^"]+).*version *= *"((\d+)\.(\d+)[^"]*).*)/.exec(line);
    if (!r){
      const errorPackage = /(.*(vaadin-.*)-parent.*)*/.exec(line); 
      console.log(`versions.js::getAnnotations : cannot get the annotation properly for ${errorPackage[2]} in ${errorPackage[1]}`);
      process.exit(1);
    }
    return {
      path: r[1],
      name: r[2],
      annotation: r[3],
      package: r[4],
      version: r[5],
      major: r[6],
      minor: r[7],
      updatedVersion: ''
    };
  });
}

async function getLatestNpmVersion(package, version, major, minor) {
  cmd = `npm view ${package} versions --json`;
  const json = await JSON.parse(await run(cmd))
  const versions = json.filter(version => version.startsWith(`${major}.${minor}`));
  const next =  versions.pop();
  console.log(`Checking next version for ${package} ${version} ${next}`);
  return next;
}

async function computeVertionToUpdate(data) {
  return (data['updatedVersion'] = await getLatestNpmVersion(data.package, data.version, data.major, data.minor));
}

module.exports = {
  getVersions,
  getVersionsCsv,
  getVersionsJson,
  computeVertionToUpdate,
  getLatestNpmVersion,
  getAnnotations,
  checkoutPlatorm,
  currentBranch,
  run
};
