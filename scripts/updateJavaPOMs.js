#!/usr/bin/env node
/**
 * Update POMs for all modules of a component.
 * Example
 *   ./scripts/updateJavaPOMs.js vaadin-button-flow-parent
 */

const xml2js = require('xml2js');
const fs = require('fs');
const path = require('path');
const { version } = require('os');
const exec = require('util').promisify(require('child_process').exec);

const templateDir = path.dirname(process.argv[1]) + '/templates';
const mod = process.argv[2] || process.exit(1);
const name = mod.replace('-flow-parent', '');
const propertyName = artifactId2versionName(name);
const componentName = name.replace('vaadin-', '');
const versionName = `version.${name.replace(/-/g, '.')}`;
const desc = name.split('-').map(w => w.replace(/./, m => m.toUpperCase())).join(' ');
const proComponents = ['board',
                       'charts',
                       'confirm-dialog',
                       'cookie-consent',
                       'crud',
                       'grid-pro',
                       'rich-text-editor'];
let rootJs;
let rootVersion;
let componentVersion;
let originalVersion;
let oldVersionSchema;
let versions;

function artifactId2versionName(artifactId) {
  return `${artifactId.replace(/-/g, '.')}.version`;
}

async function readVersions() {
  const { stdout, stderr } = await exec('node ./scripts/getVersions.js --json');
  versions = JSON.parse(stdout);
}

async function readRootPoms() {
  rootJs = await xml2js.parseStringPromise(fs.readFileSync('pom.xml', 'utf8'));
  rootVersion = rootJs.project.version[0];
  const projectVersion = (await xml2js.parseStringPromise(fs.readFileSync(`${mod}/pom.xml`, 'utf8'))).project.version;
  if (projectVersion) {
    originalVersion = projectVersion[0];
  } else if (rootJs.project.properties[0][propertyName]) {
    originalVersion = rootJs.project.properties[0][propertyName][0];
  }
  oldVersionSchema = (/^(14\.[3-4]|17\.0)/.test(rootVersion));
  if (oldVersionSchema) {
    componentVersion = `\$\{${artifactId2versionName(name)}\}`;
  } else {
    componentVersion = '${project.version}';
  }
}

function mergeDependencies(prj1, prj2) {
  const arr1 = prj1 && prj1.dependencies && prj1.dependencies[0] && prj1.dependencies[0].dependency || [];
  const arr2 = prj2 && prj2.dependencies && prj2.dependencies[0] && prj2.dependencies[0].dependency || [];
  return [...arr1, ...arr2.filter(a => !arr1.find(b => b.artifactId[0] === a.artifactId[0]))];
}

function renameComponent(array, name) {
  for(let i = 0; array && i < array.length; i++) {
    array[i] = array[i].replace(/^component/, name);
  }
}

async function renameBase(js) {
  renameComponent(js.project.parent[0].artifactId, name);
  renameComponent(js.project.artifactId, name);
  renameComponent(js.project.name, desc);
  renameComponent(js.project.description, desc);
  js.project.parent[0].version = [originalVersion];
}

function setDependenciesVersion(dependencies) {
  dependencies && dependencies[0] && dependencies[0].dependency.forEach(dep => {
    if (dep.groupId[0] === 'com.vaadin' 
      && /^vaadin-.*(flow|testbench|demo)$/.test(dep.artifactId[0])
      && !/shared/.test(dep.artifactId[0])
      ) {
      const version = oldVersionSchema ? artifactId2versionName(dep.artifactId[0].replace(/-(flow.*|testbench)$/, '')) : 'project.version';
      dep.version = [`\$\{${version}\}`];
    }
    if (dep.artifactId[0] == 'vaadin-flow-components-shared') {
      const version = oldVersionSchema ? 'vaadin.flow.components.shared.version' : 'project.version';
      dep.version = [`\$\{${version}\}`];
    }
  });
  return dependencies;
}

function mergePlugins(build1, build2) {
  return [
    ... (build1 && build1[0] && build1[0].plugins && build1[0].plugins[0] && build1[0].plugins[0].plugin || []),
    ... (build2 && build2[0] && build2[0].plugins && build2[0].plugins[0] && build2[0].plugins[0].plugin || [])
  ]
}

async function consolidate(template, pom, cb) {
  const tplJs = await xml2js.parseStringPromise(fs.readFileSync(`${templateDir}/${template}`, 'utf8'));
  const pomJs = await xml2js.parseStringPromise(fs.readFileSync(pom, 'utf8'));

  await renameBase(tplJs);

  pomJs.project.dependencies[0] = {dependency: mergeDependencies(tplJs.project, pomJs.project)};

  tplJs.project.dependencies = setDependenciesVersion(pomJs.project.dependencies);
  tplJs.project.parent[0].version = [rootVersion];
  if (oldVersionSchema) {
    tplJs.project.version = [componentVersion];
  } else {
    delete tplJs.project.version;
  }
  cb && cb(tplJs, pomJs);

  const xml = new xml2js.Builder().buildObject(tplJs);
  console.log(`writing ${pom}`);
  fs.writeFileSync(pom, xml
      // ident using 4 spaces to make sonar happy
      .replace(/\n( +)</g, '\n$1$1<') + '\n', 'utf8');
}

async function consolidatePomParent() {
  const template = proComponents.includes(componentName) ? 'pom-parent-pro.xml' : 'pom-parent.xml';
  await consolidate(template, `${mod}/pom.xml`, js => {
    renameComponent(js.project.modules[0].module, name);

    renameComponent(js.project.profiles[0].profile[0].modules[0].module, name);
    js.project.parent[0].version = [rootVersion];
    delete js.project.version;
  });
}
async function consolidatePomFlow() {
  const template = proComponents.includes(componentName) ? 'pom-flow-pro.xml' : 'pom-flow.xml';
  consolidate(template, `${mod}/${name}-flow/pom.xml`, (tplJs, pomJs) => {
        tplJs.project.build && (tplJs.project.build[0].plugins[0] = {plugin: mergePlugins(tplJs.project.build, pomJs.project.build)});
      });
}
async function consolidatePomTB() {
  await consolidate('pom-testbench.xml', `${mod}/${name}-testbench/pom.xml`)
}
async function consolidatePomDemo() {
  const demoPom = `${mod}/${name}-flow-demo/pom.xml`;
  fs.existsSync(demoPom) && consolidate('pom-demo.xml', demoPom);
}
async function consolidatePomIT() {
  await consolidate('pom-integration-tests.xml', `${mod}/${name}-flow-integration-tests/pom.xml`);
}

async function consolidatePomBowerIT() {
 const bowerITPom = `${mod}/${name}-flow-integration-tests/pom-bower-mode.xml`;
 fs.existsSync(bowerITPom) && consolidate('pom-bower-mode.xml', bowerITPom);
}

async function saveRootPom() {
  if (oldVersionSchema) {
    console.log(`updating ${propertyName} = ${originalVersion} in root pom.xml`);
    rootJs.project.properties[0]['vaadin.flow.components.shared.version'] = [rootVersion];
    rootJs.project.properties[0][propertyName] = [versions[mod] + '-SNAPSHOT'];

    const xml = new xml2js.Builder().buildObject(rootJs);
    fs.writeFileSync('pom.xml', xml + '\n', 'utf8');
  }
}

function saveVersionVariable(name, version) {
  return name;
}

async function main() {
  await readVersions()
  await readRootPoms();
  await consolidatePomParent();
  await consolidatePomFlow();
  await consolidatePomTB();
  await consolidatePomDemo();
  await consolidatePomIT();
  await consolidatePomBowerIT();
  await saveRootPom();
}

main()

