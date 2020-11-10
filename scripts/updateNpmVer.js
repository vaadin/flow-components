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

let exclude=[];

async function computeModules(){
  const cmd = 'grep -r @NpmPackage ./vaadin*parent/*/src/*/java';
  const output = await run(cmd);
  const lines = output.split('\n').filter(Boolean);
  return lines.map(line => {
    const r = /(.*(vaadin-.*)-parent.*):(.*value *= *"([^"]+).*version *= *"((\d+)\.(\d+)[^"]*).*)/.exec(line);
    return {
      path: r[1],
      name: r[2],
      annotation: r[3],
      package: r[4],
      version: r[5],
      major: r[6],
      minor: r[7],
      updatedVersion: ''
    };
  });
}

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

async function calculateVersions(data) {
  cmd = "npm view " + data.package + " versions --json";
  const versions = JSON.parse(await run(cmd)).filter(version => version.startsWith(`${data.major}.${data.minor}`));
  data['updatedVersion'] = versions.pop();
  return data;
}

/**
 * Allow exclude certain component package update, use ',' as separator
 */
async function excludeComponents() {
  for (i = 2;process.argv[i]; i++) {
    switch(process.argv[i]) {
      case '--exclude':
        components = process.argv[++i]
        break;
      }
  }
  
  exclude=components.split(',');
  
  packageBase='@vaadin/vaadin-';
  for(j = 0; j < exclude.length; j++){
	  exclude[j] = packageBase.concat(exclude[j]);
  }
  
  return exclude;
}

async function main() {
  console.log("Updating the NpmPackage annotation.")
  const modules = await computeModules();
  if (process.argv.length > 2){
    exclude = await excludeComponents();
  } 
  
  for (i = 0; i < modules.length; i++){
	if (exclude.includes(modules[i].package)){
		console.log('\x1b[33m',"skip updating "+ modules[i].package +" package");
	} else {
		modules[i] = await calculateVersions(modules[i]);
        await updateFiles(modules[i]);
	}
    
  }
}

main();
