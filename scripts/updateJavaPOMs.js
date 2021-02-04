#!/usr/bin/env node
/**
 * Update POMs for all modules of a component.
 * Example
 *   ./scripts/updateJavaPOMs.js vaadin-button-flow-parent
 */

const xml2js = require('xml2js');
const fs = require('fs');
const path = require('path');

const templateDir = path.dirname(process.argv[1]) + '/templates';
const mod = process.argv[2] || process.exit(1);
const name = mod.replace('-flow-parent', '');
const componentName = name.replace('vaadin-', '');
const desc = name.split('-').map(w => w.replace(/./, m => m.toUpperCase())).join(' ');
const proComponents = ['board',
                       'charts',
                       'confirm-dialog',
                       'cookie-consent',
                       'crud',
                       'grid-pro',
                       'rich-text-editor'];

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

  const parentJs = await xml2js.parseStringPromise(fs.readFileSync('pom.xml', 'utf8'));
  js.project.parent[0].version = [parentJs.project.version[0]];
}

function setDependenciesVersion(dependencies) {
  dependencies && dependencies[0] && dependencies[0].dependency.forEach(dep => {
    if (dep.groupId[0] === 'com.vaadin' && /^vaadin-.*(flow|testbench)$/.test(dep.artifactId[0])) {
      dep.version = [ '${project.version}' ];
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
  tplJs.project.dependencies = setDependenciesVersion(pomJs.project.dependencies);

  cb && cb(tplJs, pomJs);

  const xml = new xml2js.Builder().buildObject(tplJs);
  console.log(`writing ${pom}`);
  fs.writeFileSync(pom, xml + '\n', 'utf8');
}

async function consolidatePomParent() {
  const template = proComponents.includes(componentName) ? 'pom-parent-pro.xml' : 'pom-parent.xml';
  consolidate(template, `${mod}/pom.xml`, js => {
    renameComponent(js.project.modules[0].module, name);
    renameComponent(js.project.profiles[0].profile[0].modules[0].module, name);
  });
}

async function consolidatePomFlow() {
  const template = proComponents.includes(componentName) ? 'pom-flow-pro.xml' : 'pom-flow.xml';
  consolidate(template, `${mod}/${name}-flow/pom.xml`, (tplJs, pomJs) => {
    tplJs.project.build && (tplJs.project.build[0].plugins[0] = {plugin: mergePlugins(tplJs.project.build, pomJs.project.build)});
  });
}
async function consolidatePomTB() {
  consolidate('pom-testbench.xml', `${mod}/${name}-testbench/pom.xml`)
}
async function consolidatePomDemo() {
  await consolidate('pom-demo.xml', `${mod}/${name}-flow-demo/pom.xml`)
}
async function consolidatePomIT() {
  consolidate('pom-integration-tests.xml', `${mod}/${name}-flow-integration-tests/pom.xml`);
}

consolidatePomParent();
consolidatePomFlow();
consolidatePomTB();
consolidatePomDemo();
consolidatePomIT();
