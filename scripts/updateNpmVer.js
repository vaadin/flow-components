#!/usr/bin/env node
/**
 * Update the NpmPackage annotation for all modules in the project
 * by checking versions published in npm repository.
 * By using `--exclude <component>,<component>`, the certain package update will be skipped
 * Example
 *   ./scripts/updateNpmVer.js
 *   ./scripts/updateNpmVer.js --exclude button,text-field
 */

const fs = require('fs');
const util = require('util');
const exec = util.promisify(require('child_process').exec);
const replace = require('replace-in-file');
const {getAnnotations, computeVersionToUpdate} = require('./lib/versions.js');

let exclude=[];

async function updateFiles(moduleData){
  if(moduleData.annotation.length>0){
    if (moduleData.version != moduleData.updatedVersion){
      updatedNpm = moduleData.annotation.replace(moduleData.version, moduleData.updatedVersion)
      let options = {
        files: moduleData.path,
        from: moduleData.annotation,
        to: updatedNpm,
      };
      try {
        const results = await replace(options)
        console.log('\x1b[33m', "Updated "+ moduleData.package + " from version " +
                    moduleData.version + " to " + moduleData.updatedVersion);
      }
      catch (error) {
        console.error('Error occurred:', error);
      }
    } else {
      console.log('\x1b[32m', "No need to update annotation for package " + moduleData.package +
                  ", as version " + moduleData.version + " is the latest");
    }
  }
}

async function run(cmd) {
  const { stdout, stderr } = await exec(cmd);
  return stdout;
}

/**
 * Allow exclude certain component package update, use ',' as separator
 */
function excludeComponents() {
  for (i = 2;process.argv[i]; i++) {
    switch(process.argv[i]) {
      case '--exclude':
        components = process.argv[++i]
        break;
      }
  }
  exclude=components.split(',');
  packageBase='@vaadin/';
  for(j = 0; j < exclude.length; j++){
	  exclude[j] = packageBase.concat(exclude[j]);
  }
  return exclude;
}

async function main() {
  console.log("Updating the NpmPackage annotation.")
  const annotations = await getAnnotations();

  if (process.argv.length > 2) {
    exclude = excludeComponents();
  }

  for (i = 0; i < annotations.length; i++) {
    if (exclude.includes(annotations[i].package)) {
      console.log('\x1b[33m', "skip updating " + annotations[i].package + " package");
    } else {
      await computeVersionToUpdate(annotations[i]);
      await updateFiles(annotations[i]);
    }
  }
}

main();
