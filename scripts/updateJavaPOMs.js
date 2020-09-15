#!/usr/bin/env node
/**
 * Update POMs for all modules of a component.
 * Example
 *   git clone git@github.com:vaadin/vaadin-button-flow.git
 *   ./scripts/updateJavaPOMs.js vaadin-button-flow-parent
 */

const xml2js = require('xml2js');
const fs = require('fs');
const path = require('path');

const templateDir = path.dirname(process.argv[1]) + '/templates';
const mod = process.argv[2] || process.exit(1);
const name = mod.replace('-flow-parent', '');
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

function artifactId2versionName(artifactId) {
  return `${artifactId.replace(/-/g, '.')}.version`;
}

async function readRootPoms() {
  rootJs = await xml2js.parseStringPromise(fs.readFileSync('pom.xml', 'utf8'));
  rootVersion = rootJs.project.version[0];
  originalVersion = (await xml2js.parseStringPromise(fs.readFileSync(`${mod}/pom.xml`, 'utf8'))).project.version[0];
  oldVersionSchema = (/^14\.[3-4]/.test(rootVersion));
  if (oldVersionSchema) {
    componentVersion = `\$\{${artifactId2versionName(name)}\}`;
  } else {
    componentVersion = '${project.version}';
  }
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

function renamePlugin(js){
  // component name in Bundle-SymbolicName uses '.' as separator
  const symbolicName = componentName.replace(/-/g, '.');
  // Implementation Title uses uppercase for the first letter in each word
  nameArray = componentName.split('-');
  for(let i = 0; nameArray && i < nameArray.length; i++) {
    nameArray[i] = nameArray[i].charAt(0).toUpperCase() + nameArray[i].slice(1);
  }
  impTitle = nameArray.join(' ');

  js.project.build[0].plugins[0].plugin[0].configuration[0].instructions[0]['Bundle-SymbolicName'][0] = js.project.build[0].plugins[0].plugin[0].configuration[0].instructions[0]['Bundle-SymbolicName'][0].replace(/proComponent/, symbolicName);
  js.project.build[0].plugins[0].plugin[0].configuration[0].instructions[0]['Implementation-Title'][0] = js.project.build[0].plugins[0].plugin[0].configuration[0].instructions[0]['Implementation-Title'][0].replace(/proComponent/, impTitle);
}

function setDependenciesVersion(dependencies) {
  dependencies && dependencies[0] && dependencies[0].dependency.forEach(dep => {
    if (dep.groupId[0] === 'com.vaadin' && /^vaadin-.*(flow.*|testbench)$/.test(dep.artifactId[0])) {
      version = oldVersionSchema ? artifactId2versionName(dep.artifactId[0].replace(/-(flow.*|testbench)$/, '')) : 'project.version';
      dep.version = [`\$\{${version}\}`];
    }
  });
  return dependencies;
}

async function consolidate(template, pom, cb) {
  const tplJs = await xml2js.parseStringPromise(fs.readFileSync(`${templateDir}/${template}`, 'utf8'));
  const pomJs = await xml2js.parseStringPromise(fs.readFileSync(pom, 'utf8'));

  await renameBase(tplJs);

  if (template === "pom-flow-pro.xml"){
    renamePlugin(tplJs);
  }

  tplJs.project.dependencies = setDependenciesVersion(pomJs.project.dependencies);
  tplJs.project.parent[0].version = [rootVersion];
  if (oldVersionSchema) {
    tplJs.project.version = [componentVersion];
  } else {
    delete js.project.version;
  }
  cb && cb(tplJs);

  const xml = new xml2js.Builder().buildObject(tplJs);
  console.log(`writing ${pom}`);
  fs.writeFileSync(pom, xml + '\n', 'utf8');
}

async function consolidatePomParent() {
  await consolidate('pom-parent.xml', `${mod}/pom.xml`, js => {
    renameComponent(js.project.modules[0].module, name);
    renameComponent(js.project.profiles[0].profile[0].modules[0].module, name);
    js.project.parent[0].version = [rootVersion];
  });
}
async function consolidatePomFlow() {
  if (proComponents.includes(componentName)){
    await consolidate('pom-flow-pro.xml', `${mod}/${name}-flow/pom.xml`);
  } else {
    await consolidate('pom-flow.xml', `${mod}/${name}-flow/pom.xml`);
  }
}
async function consolidatePomTB() {
  await consolidate('pom-testbench.xml', `${mod}/${name}-testbench/pom.xml`)
}
async function consolidatePomDemo() {
  await consolidate('pom-demo.xml', `${mod}/${name}-flow-demo/pom.xml`)
}
async function consolidatePomIT() {
  await consolidate('pom-integration-tests.xml', `${mod}/${name}-flow-integration-tests/pom.xml`, js => {
    js.project.dependencies[0].dependency.push({
      groupId: ['com.vaadin'],
      artifactId: ['vaadin-flow-components-shared'],
      version: [oldVersionSchema ? '${vaadin.flow.components.shared.version}' : '${project.version}'],
      scope: ['test']
    });
  });
}

async function saveRootPom() {
  if (oldVersionSchema) {
    const propertyName = artifactId2versionName(name);
    console.log(`updating ${propertyName} = ${originalVersion} in root pom.xml`);
    rootJs.project.properties[0]['vaadin.flow.components.shared.version'] = [rootVersion];
    rootJs.project.properties[0][propertyName] = [originalVersion];
    const xml = new xml2js.Builder().buildObject(rootJs);
    fs.writeFileSync('pom.xml', xml + '\n', 'utf8');
  }
}

function saveVersionVariable(name, version) {
  return name;
}

async function main() {
  await readRootPoms();
  await consolidatePomParent();
  await consolidatePomFlow();
  await consolidatePomTB();
  await consolidatePomDemo();
  await consolidatePomIT();
  await saveRootPom();
}

main()

