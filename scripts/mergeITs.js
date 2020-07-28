#!/usr/bin/env node
/**
 * Merge IT modules of all components to the `integration-tests` module
 * - creates the new module pom file
 * - compute dependencies needed for merged modules.
 * - adjust the sources so as there are no duplicate routes.
 */

const xml2js = require('xml2js');
const fs = require('fs');
const path = require('path');
const version = '17.0-SNAPSHOT';
const itFolder = 'integration-tests';

const templateDir = path.dirname(process.argv[1]) + '/templates';

// List of tests that need to be excluded
exclude = [
//// A bunch of tests that always fail in TC
// 'PreSelectedValueIT',
// 'StringItemsWithTextRendererIT',
// 'CloseListenerReopenDialogIT',
// 'DialogWithComboBoxIT',
// 'DialogWithTemplateIT',
// 'NotificationWithTemplateIT'

//// We can disable tests of a specific component
// '%regex[com.vaadin.flow.component.charts.*]',
// '%regex[com.vaadin.flow.component.accordion.*]',
// '%regex[com.vaadin.flow.component.applayout.*]',
// '%regex[com.vaadin.flow.component.board.*]',
// '%regex[com.vaadin.flow.component.confirmdialog.*]',
// '%regex[com.vaadin.flow.component.cookieconsent.*]',
// '%regex[com.vaadin.flow.component.crud.*]',
// '%regex[com.vaadin.flow.component.customfield.*]',
// '%regex[com.vaadin.flow.component.details.*]',
// '%regex[com.vaadin.flow.component.gridpro.*]',
// '%regex[com.vaadin.flow.component.login.*]',
// '%regex[com.vaadin.flow.component.richtexteditor.*]'
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
    modules = parentJs.project.modules[0].module;
  }
  console.log(modules)
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
  const tplJs = await xml2js.parseStringPromise(fs.readFileSync(`${templateDir}/pom-integration-tests.xml`, 'utf8'));

  tplJs.project.dependencies[0].dependency = await modules.reduce(async (prevP, name) => {
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
  [targetFile, content] = replaceCall ? replaceCall(source, targetFile, content) : [targetFile, content];
  fs.writeFileSync(targetFile, content, 'utf8');
}

// copy recursively a folder without failing, and reusing already created folders in target
function copyFolderRecursiveSync(source, target, replaceCall) {
  if (!fs.existsSync(source)) {
    return;
  }
  var files = [];
  //check if folder needs to be created or integrated
  var targetFolder = path.join(target, path.basename(source));
  if (!fs.existsSync(targetFolder)) {
    fs.mkdirSync(targetFolder);
  }
  //copy
  if (fs.lstatSync(source).isDirectory()) {
    files = fs.readdirSync(source);
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

// delete recursively a folder without failing
function deleteFolderRecursive (name) {
  if (fs.existsSync(name)) {
    fs.readdirSync(name).forEach((file, index) => {
      const curPath = path.join(name, file);
      if (fs.lstatSync(curPath).isDirectory()) {
        // recurse
        deleteFolderRecursive(curPath);
      } else {
        // delete file
        fs.unlinkSync(curPath);
      }
    });
    fs.rmdirSync(name);
  }
};

// a cache to use the already computed root @Route in IT's
const rootRoutes = {};
// compute the new value for a @Route
function computeRoute(wcname, clname, prefix, route, suffix) {
  if (!route) {
    rootRoutes[wcname] = rootRoutes[wcname] || '';
  }
  route = new RegExp(wcname).test(route) || /^\/?iron-list$/.test(route)? route
    : `${wcname}/${route ? route : rootRoutes[wcname]}`;
  return `${prefix}${route}${suffix}`;
}
// Replace @Route values from master to unique routes in the merged module
function replaceRoutes(wcname, clname, content) {
  content = content.replace(/\@Route *\n/, (...args) => {
    return `@Route(value = "${/^Main(View)?$/.test(clname) ? '': clname.replace(/View$/, '').toLowerCase()}")\n`
  });
  const routeRegex = /(\@Route *?)(?:(\( *)(?:(")(.*?)(")|(.*?value *= *")(.*?)(".*?))( *\))) *\n/;
  return content.replace(routeRegex, (...args) => {
    let [prefix, route, suffix] = !args[2] && !args[6] ? [`${args[1]}("`, '', '")\n'] :
      args[6] ? [`${args[1]}${args[2]}${args[6]}`, args[7], `${args[8]}${args[9]}\n`] :
      [`${args[1]}${args[2]}${args[3]}`, args[4], `${args[5]}${args[9]}\n`];
    return computeRoute(wcname, clname, prefix, route, suffix);
  });
}

// Create an index.html. Useful for monkey patching
async function createFrontendIndex() {
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
    .forEach(f => deleteFolderRecursive(`${itFolder}/${f}`));

  modules.forEach(parent => {
    const id = parent.replace('-parent', '');
    const wc = parent.replace('-flow-parent', '');
    // copy frontend sources
    copyFolderRecursiveSync(`${parent}/${id}-integration-tests/frontend`, `${itFolder}`, (source, target, content) => {
      // test-template is used in several components, it must not be repeated in
      // merged module, changing component name to unique localNames
      if (/test-template.js$/.test(target)) {
        target = target.replace('test-template', `${id}-test-template`);
        content = content.replace('test-template', `${id}-test-template`);
      }
      return [target, content];
    });
    // copy java sources
    copyFolderRecursiveSync(`${parent}/${id}-integration-tests/src`, `${itFolder}`, (source, target, content) => {
      if (/\.java$/.test(source)) {
        // replace test-template localName for the new computed above
        content = content.replace(/\@(Tag|JsModule.*)test-template/, `@$1${id}-test-template`);
        const clname = path.basename(source, '.java');
        // change @Route in views
        content = replaceRoutes(wc, clname, content);
        // adjust @TestPath and getTestPath to match routes changed in views
        content = content.replace(/(\@TestPath.*?")(.*?)(")/, (...args) => {
          return computeRoute(wc, clname, args[1], args[2], args[3]);
        });
        content = content.replace(/(getTestPath\s*\(\)[\S\s]*return *\(? *"\/+?)(.*?)(")/, (...args) => {
          return computeRoute(wc, clname, args[1], args[2], args[3]);
        });

        // pro components do not use TestPath but the following pattern
        content = content.replace(/(getDriver\(\).get\(getBaseURL\(\) *)(\+ *".+"|)/g, (...args) => {
          return `${args[1]} + "/${wc}" ${args[2] ? args[2] : '+ "/' + rootRoutes[wc] + '"'}`;
        });
        // pro components use 8080 and do not use TestPath, this is a hack
        // to adjust the route used in tests
        content = content.replace(/(return\ +"?)8080("?)/, (...args) => {
          return `${args[1]}9998${args[2]}`;
        });
        // App layout: IT tests search for links based on href
        content = content.replace(/\.attribute\("href", *"([^"]*)"\)/g, (...args) => {
          return `.attribute("href", "${wc}/${args[1]}")`;
        });
        // Combo box - PreSelectedValueIT.selectedValueIsNotResetAfterClientResponse
        // The test fails because when running in the project there is an iron-icon-set-svg element
        // which contains an element with id "info", which conflicts with the element
        // the test is trying to find.
        if (/PreSelectedValueIT\.java$/.test(source)) {
          content = content.replace(/findElement\(By.id\("info"\)\)/, '$("div").id("info")')
        }
        // pro components: temporary disable tests in FF and Edge in pro components
        content = content.replace(/\( *BrowserUtil.(safari|firefox|edge)\(\) *,/g, "(");
        content = content.replace(/,[ \r\n]*BrowserUtil.(safari|firefox|edge)\(\)/g, "");
        content = content.replace(/(safari|firefox|edge) *,/g, "");
        content = content.replace(/,[ \r\n]*(safari|firefox|edge)/g, "");
        // pro components: have screen comparisons, disable temporary since paths changed
        content = content.replace(/testBench\(\).compareScreen\(.*?\)/g, 'true');

        // vaadin-board
        content = content.replace(/(getDeploymentPath\(Class.*?\) *{ *\n)/, 
        `$1com.vaadin.flow.router.Route[] ann = viewClass.getAnnotationsByType(com.vaadin.flow.router.Route.class);
        if (ann.length > 0) {
            return "/" + ann[0].value();
        }\n`);

        // vaadin-chart
        content = content.replace('.replace("com.vaadin.flow.component.charts.examples.", "")',
         '.replace("com.vaadin.flow.component.charts.examples.", "vaadin-charts/")');
      }
      return [target, content];
    });
  });
}

async function main() {
  await computeModules();
  await copySources();
  await createFrontendIndex();
  await createPom();
}

main();

