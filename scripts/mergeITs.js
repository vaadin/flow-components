#!/usr/bin/env node
/**
 * Merge IT modules of all components to the `integration-tests` module
 * - creates the new module pom file
 * - compute dependencies needed for merged modules.
 */

const xml2js = require('xml2js');
const fs = require('fs');
const path = require('path');
const itFolder = 'integration-tests';
let version;

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
    modules = parentJs.project.modules[0].module.filter(m => !/shared-parent/.test(m)).filter(m => !/demo-helpers/.test(m));
  }
}

// Add a dependency to the array, if not already present
function addDependency(arr, groupId, artifactId, version, scope, exclusions) {
  if (!arr.find(e => e.groupId[0] === groupId && e.artifactId[0] === artifactId)) {
    const obj = {
      groupId: [groupId],
      artifactId: [artifactId]
    }
    version && (obj.version = [version]);
    scope && (obj.scope = [scope]);
    exclusions && (obj.exclusions = exclusions);
    arr.push(obj);
  }
}

async function computeVersion() {
  const parentJs = await xml2js.parseStringPromise(fs.readFileSync('pom.xml', 'utf8'));
  version = parentJs.project.version[0];
}

// Creates the pom.xml for the integration-tests module
async function createPom() {

   const dependency = await modules.reduce(async (prevP, name) => {
    const prev = await prevP;
    const id = name.replace('-flow-parent', '');
    // Add component-flow and component-testbench dependencies
    const componentVersion = /^(14\.[3-4]|17\.0)/.test(version) ? `\$\{${id.replace(/-/g, '.')}.version\}` : '${project.version}'

    if (fs.existsSync(`${name}/${id}-flow/pom.xml`)) {
      const js = await xml2js.parseStringPromise(fs.readFileSync(`${name}/${id}-flow/pom.xml`, 'utf8'));
      addDependency(prev, 'com.vaadin', js.project.artifactId[0], `${componentVersion}`);
    }
    if (fs.existsSync(`${name}/${id}-testbench/pom.xml`)) {
      addDependency(prev, 'com.vaadin', `${id}-testbench`, `${componentVersion}`, 'test');
    }
    const js = await xml2js.parseStringPromise(fs.readFileSync(`${name}/${id}-flow-integration-tests/pom.xml`, 'utf8'));
    // Read original IT dependencies of module
    js.project.dependencies[0].dependency.forEach(dep => {
      addDependency(prev, dep.groupId[0], dep.artifactId[0], dep.version && dep.version[0], dep.scope && dep.scope[0], dep.exclusions);
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
  delete tplJs.project.version;
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
  // if target is a directory a new file with the same name will be created
  if (fs.existsSync(target)) {
    if (fs.lstatSync(target).isDirectory()) {
      targetFile = path.join(target, path.basename(source));
    }
  }
  if (fs.existsSync(targetFile)) {
    // When file exists we can merge both or override depending on the type
    if (/.properties$/.test(source)) {
      let content = fs.readFileSync(source, 'utf8');
      content += '\n' + fs.readFileSync(targetFile, 'utf8');
      fs.writeFileSync(targetFile, content, 'utf8');
      console.log(`Merging ${targetFile}`);
      return;
    }
    console.log(`Overriding ${targetFile}`);
  }

  if (/\.(java|html|js|ts)$/.test(source)) {
    let content = fs.readFileSync(source, 'utf8');
    // remove CR in windows
    content = content.replace('\r', '');
    [targetFile, content] = replaceCall ? replaceCall(source, targetFile, content) : [targetFile, content];
    targetFile && content && fs.writeFileSync(targetFile, content, 'utf8');
  } else {
    fs.copyFileSync(source, targetFile);
  }
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
        fs.rmSync(`${dir}`, { recursive: true } );
      }
    });

  modules.forEach(parent => {
    const id = parent.replace('-parent', '');
    console.log(`Copying ${parent}/${id}-integration-tests`);
    // copy frontend sources
    copyFolderRecursiveSync(`${parent}/${id}-integration-tests/frontend`, `${itFolder}`);
    // copy java sources
    copyFolderRecursiveSync(`${parent}/${id}-integration-tests/src`, `${itFolder}`, (source, target, content) => {
      return /\n\s*@Theme.*Material/.test(content) ? []: [target, content];
    });
  });
}

async function main() {
  await computeVersion();
  await computeModules();
  await copySources();
  await createFrontendIndex();
  await createPom();
}

main();
