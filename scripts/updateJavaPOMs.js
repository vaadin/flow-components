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
                       'rich-text-editor',
                       'map',
                       'spreadsheet'];

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
  const arr1 = build1 && build1[0] && build1[0].plugins && build1[0].plugins[0] && build1[0].plugins[0].plugin || [];
  const arr2 = build2 && build2[0] && build2[0].plugins && build2[0].plugins[0] && build2[0].plugins[0].plugin || [];
  return [...arr1, ...arr2.filter(a => !arr1.find(b => b.artifactId[0] === a.artifactId[0]))];
}

function mergeDependencies(prj1, prj2) {
  const arr1 = prj1 && prj1.dependencies && prj1.dependencies[0] && prj1.dependencies[0].dependency || [];
  const arr2 = prj2 && prj2.dependencies && prj2.dependencies[0] && prj2.dependencies[0].dependency || [];
  return [...arr1, ...arr2.filter(a => !arr1.find(b => b.artifactId[0] === a.artifactId[0]))];
}

function mergeProfiles(prj1, prj2) {
  const arr1 = prj1 && prj1.profiles && prj1.profiles[0] && prj1.profiles[0].profile || [];
  const arr2 = prj2 && prj2.profiles && prj2.profiles[0] && prj2.profiles[0].profile || [];
  return [...arr1, ...arr2.filter(a => !arr1.find(b => b.id[0] === a.id[0]))];
}

function mergeProperties(props1, props2) {
  const arr1 = (props1 || []).filter(o => typeof o === 'object');
  const arr2 = (props2 || []).filter(o => typeof o === 'object');
  return [...arr1, ...arr2.filter(a => !arr1.find(b => Object.keys(b)[0] === Object.keys(a)[0]))];
}

async function consolidate(template, pom, cb) {
  const tplJs = await xml2js.parseStringPromise(fs.readFileSync(`${templateDir}/${template}`, 'utf8'));
  const pomJs = await xml2js.parseStringPromise(fs.readFileSync(pom, 'utf8'));

  await renameBase(tplJs);

  tplJs.project.artifactId[0] = pomJs.project.artifactId[0] || tplJs.project.artifactId[0];

  pomJs.project.dependencies = pomJs.project.dependencies || [];
  pomJs.project.dependencies[0] = {dependency: mergeDependencies(tplJs.project, pomJs.project)};

  tplJs.project.dependencies = setDependenciesVersion(pomJs.project.dependencies);

  cb && cb(tplJs, pomJs);

  const xml = new xml2js.Builder({renderOpts: {pretty: true, indent: '    '}}).buildObject(tplJs);
  console.log(`writing ${pom}`);
  fs.writeFileSync(pom, xml + '\n', 'utf8');
}

async function consolidatePomParent() {
  const template = proComponents.includes(componentName) ? 'pom-parent-pro.xml' : 'pom-parent.xml';
  consolidate(template, `${mod}/pom.xml`, (js, org)  => {
    const modules = js.project.modules[0].module;

    renameComponent(modules, name);
    // add testbench if module exists
    if (fs.existsSync(`${mod}/${name}-testbench/pom.xml`)) {
      modules.push(`${name}-testbench`);
    }
    // add it's if module exists
    if (fs.existsSync(`${mod}/${name}-flow-demo/pom.xml`)) {
      modules.push(`${name}-flow-demo`);
    }
    // add other modules present in original pom
    org.project.modules[0].module.forEach(
      mod => !/(flow|flow-demo|testbench|flow-integration-test)$/.test(mod) && modules.push(mod));

    renameComponent(js.project.profiles[0].profile[0].modules[0].module, name);
    const a = mergeProfiles(js.project, org.project);
    js.project.profiles[0].profile = mergeProfiles(js.project, org.project)
  });
}

async function consolidatePomFlow() {
  const template = proComponents.includes(componentName) ? 'pom-flow-pro.xml' : 'pom-flow.xml';
  consolidate(template, `${mod}/${name}-flow/pom.xml`, (tplJs, pomJs) => {
    tplJs.project.build && (tplJs.project.build[0].plugins[0] = {plugin: mergePlugins(tplJs.project.build, pomJs.project.build)});
    tplJs.project.properties = mergeProperties(tplJs.project.properties, pomJs.project.properties);
    if (pomJs.project.build[0].resources) {
      tplJs.project.build[0].resources =  pomJs.project.build[0].resources;
    }
  });
}
async function consolidatePomTB() {
  const tbPom = `${mod}/${name}-testbench/pom.xml`;
  fs.existsSync(tbPom) && await consolidate('pom-testbench.xml', tbPom)
}
async function consolidatePomDemo() {
  const demoPom = `${mod}/${name}-flow-demo/pom.xml`;
  fs.existsSync(demoPom) && await consolidate('pom-demo.xml', demoPom);
}
async function consolidatePomIT() {
  const itPom = `${mod}/${name}-flow-integration-tests/pom.xml`;
  consolidate('pom-integration-tests.xml', itPom);
}

consolidatePomParent();
consolidatePomFlow();
consolidatePomTB();
consolidatePomDemo();
consolidatePomIT();
