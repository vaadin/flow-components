#!/usr/bin/env node
/**
 * Merge IT modules of all components to the `integration-tests` module
 * - creates the new module pom file
 * - compute dependencies needed for merged modules.
 */

const xml2js = require('xml2js');
const fs = require('fs');
const path = require('path');
const version = '18.0-SNAPSHOT';
const itFolder = 'integration-tests';

const templateDir = path.dirname(process.argv[1]) + '/templates';

// List of tests that need to be excluded
exclude = [
//// tests that always fail in TC
// 'PreSelectedValueIT',
//// We can disable tests of a specific component
// '%regex[com.vaadin.flow.component.charts.*]',
]

let modules = [];
async function computeModules() {
  if (process.argv.length > 2) {
    // Modules are passed as arguments
    for (let i = 2; i < process.argv.length; i++) {
      modules.push(`vaadin-${process.argv[i]}-flow-parent`);
    }
  } else {
    // Read modules from the parent pom.xml
    const parentJs = await xml2js.parseStringPromise(fs.readFileSync(`pom.xml`, 'utf8'));
    modules = parentJs.project.modules[0].module.filter(m => !/shared/.test(m));
  }
}

// Add a dependency to the array, if not already present
function addDependency(arr, groupId, artifactId, version, scope) {
  if (!arr.find(e => e.groupId[0] === groupId && e.artifactId[0] === artifactId)) {
    const obj = {
      groupId: [groupId],
      artifactId: [artifactId]
    }
    version && (obj.version = [version]);
    scope && (obj.scope = [scope]);
    arr.push(obj);
  }
}

// Creates the pom.xml for the integration-tests module
async function createPom() {
   const dependency = await modules.reduce(async (prevP, name) => {
    const prev = await prevP;
    const id = name.replace('-flow-parent', '');
    // Add component-flow and component-testbench dependencies
    addDependency(prev, 'com.vaadin', `${id}-flow`, '${project.version}');
    addDependency(prev, 'com.vaadin', `${id}-testbench`, '${project.version}', 'test');
    // Read original IT dependencies in master and add them
    const js = await xml2js.parseStringPromise(fs.readFileSync(`${name}/${id}-flow-integration-tests/pom.xml`, 'utf8'))
    js.project.dependencies[0].dependency.forEach(dep => {
      addDependency(prev, dep.groupId[0], dep.artifactId[0], dep.version && dep.version[0], dep.scope && dep.scope[0]);
    });
    return prev;
  }, Promise.resolve([
    // these dependencies should be always there
    {
      groupId: ['com.vaadin'],
      artifactId: ['vaadin-testbench-core'],
      scope: ['test']
    },{
      groupId: ['com.vaadin'],
      artifactId: ['flow-test-util'],
      scope: ['compile']
    }
  ]));

  const tplJs = await xml2js.parseStringPromise(fs.readFileSync(`${templateDir}/pom-integration-tests.xml`, 'utf8'));
  tplJs.project.dependencies = [{dependency}];

  tplJs.project.artifactId = ['vaadin-flow-components-integration-tests'];
  tplJs.project.parent[0].artifactId = ['vaadin-flow-components'];
  tplJs.project.parent[0].version = [version];
  const tests = tplJs.project.build[0].plugins[0].plugin.find(p => p.artifactId[0] === 'maven-failsafe-plugin');
  tests.configuration = [{excludes: [{exclude: exclude}]}]
  if (!fs.existsSync(itFolder)) {
    console.log(`Creating Folder ${itFolder}`);
    fs.mkdirSync(itFolder)
  }
  const xml = new xml2js.Builder().buildObject(tplJs);
  const pom = `${itFolder}/pom.xml`;
  console.log(`writing ${pom}`);
  fs.writeFileSync(pom, xml + '\n', 'utf8');
}

// copy a file
function copyFileSync(source, target, replaceCall) {
  var targetFile = target;
  //if target is a directory a new file with the same name will be created
  if (fs.existsSync(target)) {
    if (fs.lstatSync(target).isDirectory()) {
      targetFile = path.join(target, path.basename(source));
    }
  }
  if (fs.existsSync(targetFile)) {
    console.log(`Overriding ${targetFile}`);
  }
  // fs.copyFileSync(source, targetFile);
  let content = fs.readFileSync(source, 'utf8');
  // remove CR in windows
  if (/\.(java)$/.test(source)) {
    content = content.replace('\r', '');
  }
  [targetFile, content] = replaceCall ? replaceCall(source, targetFile, content) : [targetFile, content];
  fs.writeFileSync(targetFile, content, 'utf8');
}

// copy recursively a folder without failing, and reusing already created folders in target
function copyFolderRecursiveSync(source, target, replaceCall) {
  if (!fs.existsSync(source)) {
    return;
  }

  //check if folder needs to be created or integrated
  var targetFolder = path.join(target, path.basename(source));
  if (!fs.existsSync(targetFolder)) {
    fs.mkdirSync(targetFolder);
  }
  //copy
  if (fs.lstatSync(source).isDirectory()) {
    const files = fs.readdirSync(source);
    files.forEach(function (file) {
      var curSource = path.join(source, file);
      if (fs.lstatSync(curSource).isDirectory()) {
        copyFolderRecursiveSync(curSource, targetFolder, replaceCall);
      } else {
        copyFileSync(curSource, targetFolder, replaceCall);
      }
    });
  }
}

// Create an index.html. Useful for monkey patching
async function createFrontendIndex() {
  if (/^14/.test(version)) {
    const javaFolder = `${itFolder}/src/main/java/com/vaadin`;
    const servicesFolder = `${itFolder}/src/main/resources/META-INF/services`
    fs.mkdirSync(servicesFolder, { recursive: true });
    fs.mkdirSync(javaFolder, { recursive: true });
    copyFileSync(`${templateDir}/com.vaadin.flow.server.VaadinServiceInitListener`, `${servicesFolder}`);
    copyFileSync(`${templateDir}/AppVaadinServiceInitListener.java`, `${javaFolder}`);
  } else {
    const frontendFolder = `${itFolder}/frontend`;
    fs.mkdirSync(frontendFolder, { recursive: true });
    copyFileSync(`${templateDir}/index.html`, `${frontendFolder}`);
  }
}

// Create an index.html. Useful for monkey patching
async function createInitListener() {
  const targetFolder = `${itFolder}/frontend`;
  if (!fs.existsSync(targetFolder)) {
    fs.mkdirSync(targetFolder);
  }
  copyFileSync(`${templateDir}/index.html`, `${targetFolder}/index.html`);
}

// Copy components sources from master to the merged integration-tests module
// At the same time does some source-code changes to adapt them to the new module
async function copySources() {
  if (!fs.existsSync(itFolder)) {
    fs.mkdirSync(itFolder);
  }
  // clean old stuff
  ['target', 'node_modules', 'src', 'frontend']
    .forEach(f => {
      const dir = `${itFolder}/${f}`;
      if (fs.existsSync(dir)) {
        console.log(`removing ${dir}`);
        fs.rmdirSync(`${dir}`, { recursive: true } );
      }
    });

  modules.forEach(parent => {
    const id = parent.replace('-parent', '');
    console.log(`Copying ${parent}/${id}-integration-tests`);
    // copy frontend sources
    copyFolderRecursiveSync(`${parent}/${id}-integration-tests/frontend`, `${itFolder}`);
    // copy java sources
    copyFolderRecursiveSync(`${parent}/${id}-integration-tests/src`, `${itFolder}`);
  });
}

async function main() {
  await computeModules();
  await copySources();
  await createFrontendIndex();
  await createPom();
}

main();
