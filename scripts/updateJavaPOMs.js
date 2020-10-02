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

function renamePlugin(js){
  // component name in Bundle-SymbolicName uses '.' as separator
  const symbolicName = componentName.replace('-', '.');
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
    if (dep.groupId[0] === 'com.vaadin' && /^vaadin-.*(flow|testbench)$/.test(dep.artifactId[0])) {
      dep.version = [ '${project.version}' ];
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
  const template = proComponents.includes(componentName) ? 'pom-flow-pro.xml' : 'pom-flow.xml';
  consolidate(template, `${mod}/${name}-flow/pom.xml`, js => {
    js.project.dependencies[0].dependency.push({
      groupId: ['org.slf4j'],
      artifactId: ['slf4j-simple'],
      scope: ['test']
    })
  });
}
async function consolidatePomTB() {
  consolidate('pom-testbench.xml', `${mod}/${name}-testbench/pom.xml`)
}
async function consolidatePomDemo() {
  consolidate('pom-demo.xml', `${mod}/${name}-flow-demo/pom.xml`)
}
async function consolidatePomIT() {
  consolidate('pom-integration-tests.xml', `${mod}/${name}-flow-integration-tests/pom.xml`, js => {
    js.project.dependencies[0].dependency.push({
      groupId: ['com.vaadin'],
      artifactId: ['vaadin-flow-components-shared'],
      version: ['${project.version}'],
      scope: ['test']
    }, {
      groupId: ['com.vaadin'],
      artifactId: ['flow-lit-template'],
    }, {
      groupId: ['com.vaadin'],
      artifactId: ['flow-polymer-template'],      
    });
  });
}

consolidatePomParent();
consolidatePomFlow();
consolidatePomTB();
consolidatePomDemo();
consolidatePomIT();
