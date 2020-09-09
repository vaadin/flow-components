#!/usr/bin/env node
/**
 * Update the NpmPackage annotation for all moudles
 * This script should be triggered from `updateNpmVer.sh`
 * by using `sh updateNpmVer.sh`
 */

const fs = require('fs');
const util = require('util');
const exec = util.promisify(require('child_process').exec);
const replace = require('replace-in-file');

let infos = fs.readFileSync("info.json").toString('utf-8');
let results = infos.split("\n").filter(info => info.startsWith('../vaadin-'));
let modules = [];

function computeModules(){
  for (i = 0; i < results.length; i++){
    //(results[i]);
    path = results[i].slice(0, results[i].indexOf(':'));
    pathArray = path.split("/");
    annotation = results[i].slice(results[i].lastIndexOf(':')+1);
    array = annotation.split('"');
    verArray = array[3].split('.')

    modules.push({
      'name': pathArray[1],
      'path': path,
      'annotation':annotation,
      'package':array[1],
      'major': verArray[0],
      'minor': [verArray[0],verArray[1]].join('.'),
      'version':array[3],
      'updatedVersion':""
    });
  }
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
        //console.log('Replacement results:', results);
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

async function calculateVersions(moduleData) {
  cmd = "npm view " + moduleData.package + " versions --json";
  let versions = await run(cmd);
  if ( versions.includes(moduleData.version) ){
    avaiVersion = versions.split('"').filter(version => version.startsWith(moduleData.minor));
    moduleData['updatedVersion'] = avaiVersion[avaiVersion.length-1];
  } 

  //console.log(moduleData);
  return moduleData;
}

async function main() {
  console.log("Updating the NpmPackage annotation.")
  
  await computeModules();
  
  for (i = 0; i < modules.length; i++){
    modules[i] = await calculateVersions(modules[i]);
    await updateFiles(modules[i]);
  }
}

main();
