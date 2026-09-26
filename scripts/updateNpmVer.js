#!/usr/bin/env node
/**
 * Update the NpmPackage annotation for all modules in the project
 * by checking versions published in npm repository.
 *
 * By default every annotation is bumped to the latest published npm version
 * for its major.minor. By using `--exclude <component>,<component>`, the
 * certain package update will be skipped.
 *
 * With `--version <npm-version>` every annotation is instead pinned to that
 * exact version - typically a web-components *feature snapshot* published by
 * the `publishFeatureSnapshot.sh` script under an immutable `<base>-dev.<hash>`
 * version. Only packages that actually publish that version are updated;
 * packages outside the web-components monorepo (which never got the snapshot)
 * are left untouched. Revert with the regular latest bump once released.
 *
 * Example
 *   ./scripts/updateNpmVer.js
 *   ./scripts/updateNpmVer.js --exclude button,text-field
 *   ./scripts/updateNpmVer.js --version 25.1.0-dev.a1b2c3d
 */

const fs = require('fs');
const util = require('util');
const exec = util.promisify(require('child_process').exec);
const {getAnnotations, computeVersionToUpdate} = require('./lib/versions.js');

let exclude=[];
let fixedVersion='';

async function updateFiles(moduleData){
  if(moduleData.annotation.length>0){
    if (!moduleData.updatedVersion){
      // Fixed-version mode: the package does not publish the requested version.
      console.log('\x1b[90m%s\x1b[0m', "skip " + moduleData.package +
                  ": no '" + fixedVersion + "' version published");
    } else if (moduleData.version != moduleData.updatedVersion){
      updatedNpm = moduleData.annotation.replace(moduleData.version, moduleData.updatedVersion)
      let options = {
        files: moduleData.path,
        from: moduleData.annotation,
        to: updatedNpm,
      };
      try {
        const { replaceInFile } = await import('replace-in-file');
        const results = await replaceInFile(options)
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
 * Parse arguments:
 *   --exclude <component>,<component>  skip these packages (',' as separator)
 *   --version <npm-version>            pin every package to this exact version
 */
function parseArgs() {
  for (let i = 2; process.argv[i]; i++) {
    switch(process.argv[i]) {
      case '--exclude':
        exclude = process.argv[++i].split(',').map(c => '@vaadin/'.concat(c));
        break;
      case '--version':
        fixedVersion = process.argv[++i];
        break;
      }
  }
}

async function main() {
  parseArgs();

  if (fixedVersion) {
    console.log("Pinning the NpmPackage annotations to '" + fixedVersion + "'.");
  } else {
    console.log("Updating the NpmPackage annotation to the latest npm version.");
  }

  const annotations = await getAnnotations();

  for (i = 0; i < annotations.length; i++) {
    if (exclude.includes(annotations[i].package)) {
      console.log('\x1b[33m', "skip updating " + annotations[i].package + " package");
    } else {
      await computeVersionToUpdate(annotations[i], fixedVersion);
      await updateFiles(annotations[i]);
    }
  }
}

main();
