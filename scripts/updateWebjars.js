#!/usr/bin/env node
/**
 * Script for updating the webjar dependency(14.X LTS branches) used in the pom.xml
 * This can be triggered by using `node updateWebjars.js`
 */

const xml2js = require('xml2js');
const fs = require('fs');
const path = require('path');
const https = require("https");
const parser = new xml2js.Parser({ attrkey: "ATTR" });

let modules = [];

/**
 * Collect all modules from root pom
 */
async function computeModules() {
    const parentJs = await xml2js.parseStringPromise(fs.readFileSync(`../pom.xml`, 'utf8'));
    modules = parentJs.project.modules[0].module.filter(m => !/shared/.test(m));
}

/**
 * Update webjars for one module
 */
async function updateWebjars(moduleName){

    const vaadinWebjarGroupId = `org.webjars.bowergithub.vaadin`;
    updateFile = false;

    subModule = moduleName.replace('-parent','');
    pom = '../' + moduleName + '/' + subModule + '/pom.xml';
    pomJs = await xml2js.parseStringPromise(fs.readFileSync(pom, 'utf8'));
    //collect vaadin webjars
    webjars = pomJs.project.dependencies[0].dependency.filter(dep => dep.groupId == vaadinWebjarGroupId);

    for (j=0; j<webjars.length; j++){
        
        webjarName = webjars[j].artifactId +'';
        webjarVersion = webjars[j].version +'';
        updatedVersion = await findUpdatedVersion(webjarName, webjarVersion);
        
        if (updatedVersion != webjarVersion){
            updateFile = true;
            webjars[j].version = updatedVersion; 
            pomJs.project.dependencies[0].dependency.filter(dep => {
                if (dep.artifactId == webjarName){
                    dep.version = updatedVersion;
                    console.log('\x1b[33m', "In "+subModule+',' +dep.artifactId+ " has been updated from "+ webjarVersion +" to " + updatedVersion);
                }
            })
        }
    }

    if(updateFile){
        const updatedPom = new xml2js.Builder().buildObject(pomJs);
        console.log('\x1b[32m', `Writing ${pom}`);
        fs.writeFileSync(pom, updatedPom + '\n', 'utf8'); 
    } else {
        console.log("\x1b[0m",'No need to updated '+ subModule);
    }
       
}

/**
 * Return the latest patch version from maven central
 * 
 * @param {*} name webjar artifactId
 * @param {*} version current webjar version
 */
async function findUpdatedVersion(name, version){
    let verArray = version.split('.');
    xml = await getMetadata(name);
    xml = await xml2js.parseStringPromise(xml, 'utf8');
  
    aviVersions = xml.metadata.versioning[0].versions[0].version; 
    minorVersions = aviVersions.filter(version => version.startsWith([verArray[0],verArray[1]].join('.')));

    return minorVersions[minorVersions.length-1]
}

/**
 * Get the metadata of webjar from maven central
 * @param {*} name webjar artifactId
 */
async function getMetadata(name) {
    return new Promise(resolve => {
        https.get("https://repo1.maven.org/maven2/org/webjars/bowergithub/vaadin/"+name+"/maven-metadata.xml", function(res) {
            let data = '';
            res.on('data', function(stream) {
                data += stream;
            });
            res.on('end', function(){
               resolve(data);
            });
        });
    }).then(data => {
        return data;
    })
}

async function main() {
  await computeModules();
  for (i = 0; i < modules.length; i++){
    await updateWebjars(modules[i]);
  }
}

main();