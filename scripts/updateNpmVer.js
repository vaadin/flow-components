#!/usr/bin/env node
/**
 * Update the NpmPackage annotation for all modules in the project
 * by checking versions published in npm repository.
 *
 * The web components (`@vaadin/*` packages) are released together, so they
 * are all updated to one version: the newest one published for any of them.
 * Right after a release, npm may not list the new version for every package
 * yet. The script then fails without changing any file, instead of updating
 * only some of the packages, and should be run again a bit later.
 *
 * By using `--exclude <component>,<component>`, the certain package update will be skipped
 * Example
 *   ./scripts/updateNpmVer.js
 *   ./scripts/updateNpmVer.js --exclude button,text-field
 */

const fs = require('fs');
const util = require('util');
const exec = util.promisify(require('child_process').exec);
const replace = require('replace-in-file');
const {getAnnotations, computeVersionToUpdate, getNpmVersions, sortVersions} = require('./lib/versions.js');

let exclude=[];

async function updateFiles(moduleData){
  if(moduleData.annotation.length>0 && moduleData.updatedVersion){
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

/**
 * Sets the version of every web component annotation to the newest version
 * published for any of them, exiting with an error when that version is not
 * yet available in npm for all of them.
 */
async function computeWebComponentsVersion(annotations) {
  const versionsByPackage = {};
  for (const annotation of annotations) {
    versionsByPackage[annotation.package] = await getNpmVersions(annotation.package, annotation.major, annotation.minor);
  }
  const latestVersions = Object.values(versionsByPackage).map(versions => versions[versions.length - 1]).filter(Boolean);
  const version = sortVersions(latestVersions).pop();
  if (!version) {
    return;
  }
  const missing = Object.keys(versionsByPackage).filter(pkg => !versionsByPackage[pkg].includes(version));
  if (missing.length > 0) {
    console.error('\x1b[31m', "Version " + version + " is not yet available in npm for: " + missing.join(', ') +
                  ". No annotation was updated, run the script again later.");
    process.exit(1);
  }
  console.log("Updating web components to version " + version);
  annotations.forEach(annotation => annotation.updatedVersion = version);
}

async function main() {
  console.log("Updating the NpmPackage annotation.")
  const annotations = await getAnnotations();

  if (process.argv.length > 2) {
    exclude = excludeComponents();
  }

  const included = annotations.filter(annotation => {
    if (exclude.includes(annotation.package)) {
      console.log('\x1b[33m', "skip updating " + annotation.package + " package");
      return false;
    }
    return true;
  });

  const webComponents = included.filter(annotation => annotation.package.startsWith('@vaadin/'));
  await computeWebComponentsVersion(webComponents);
  for (const annotation of included) {
    if (!webComponents.includes(annotation)) {
      await computeVersionToUpdate(annotation);
    }
  }

  for (const annotation of included) {
    await updateFiles(annotation);
  }
}

main();
