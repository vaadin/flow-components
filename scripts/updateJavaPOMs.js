#!/usr/bin/env node
/**
 * Update POMs for all modules of a component.
 * Example
 *   git clone git@github.com:vaadin/vaadin-button-flow.git
 *   ./scripts/updateJavaPOMs.js vaadin-button-flow
 */

const xml2js = require('xml2js');
const fs = require('fs');
const path = require('path');
const version = '17.0-SNAPSHOT';

const templateDir = path.dirname(process.argv[1]) + '/templates';
const mod = process.argv[2] || process.exit(1);
const name = mod.replace('-flow-parent', '');
const desc = name.split('-').map(w => w.replace(/./, m => m.toUpperCase())).join(' ');

function renameComponent(array, name) {
  for(let i = 0; array && i < array.length; i++) {
    array[i] = array[i].replace(/^component/, name);
  }
}

function renameBase(js) {
  renameComponent(js.project.parent[0].artifactId, name);
  renameComponent(js.project.artifactId, name);
  renameComponent(js.project.name, desc);
  renameComponent(js.project.description, desc);
  js.project.parent[0].version = [version];
}

function setDependenciesVersion(dependencies) {
  dependencies && dependencies[0] && dependencies[0].dependency.forEach(dep => {
    if (dep.groupId[0] === 'com.vaadin' && /^vaadin-.*(flow|testbench)$/.test(dep.artifactId[0])) {
      dep.version = [ '${project.version}' ];
    }
  });
  return dependencies;
}

async function consolidate(template, pom, cb) {
  const tplJs = await xml2js.parseStringPromise(fs.readFileSync(`${templateDir}/${template}`, 'utf8'));
  const pomJs = await xml2js.parseStringPromise(fs.readFileSync(pom, 'utf8'));

  renameBase(tplJs);
  tplJs.project.dependencies = setDependenciesVersion(pomJs.project.dependencies);
  cb && cb(tplJs);

  const xml = new xml2js.Builder().buildObject(tplJs);
  console.log(`writing ${pom}`);
  fs.writeFileSync(pom, xml + '\n', 'utf8');
}

async function consolidatePomParent() {
  consolidate('pom-parent.xml', `${mod}/pom.xml`, js => {
    renameComponent(js.project.modules[0].module, name);
    renameComponent(js.project.profiles[0].profile[0].modules[0].module, name);
  });
}
async function consolidatePomFlow() {
  consolidate('pom-flow.xml', `${mod}/${name}-flow/pom.xml`)
}
async function consolidatePomTB() {
  consolidate('pom-testbench.xml', `${mod}/${name}-testbench/pom.xml`)
}
async function consolidatePomDemo() {
  consolidate('pom-demo.xml', `${mod}/${name}-flow-demo/pom.xml`)
}
async function consolidatePomIT() {
  consolidate('pom-integration-tests.xml', `${mod}/${name}-flow-integration-tests/pom.xml`)
}

consolidatePomParent();
consolidatePomFlow();
consolidatePomTB();
consolidatePomDemo();
consolidatePomIT();
