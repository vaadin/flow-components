#!/usr/bin/env node
/**
 * Script for updating the webjar dependency(14.X LTS branches) used in the pom.xml
 * This can be triggered by using `node updateWebjars.js`
 */

const xml2js = require('xml2js');
const fs = require('fs');
const https = require("https");

let modules = [];

/**
 * Collect all modules from root pom
 */
async function computeModules() {
    const parentJs = await xml2js.parseStringPromise(fs.readFileSync(`./pom.xml`, 'utf8'));
    modules = parentJs.project.modules[0].module.filter(m => !/shared/.test(m));
}

/**
 * Update webjars for one pom file
 */
async function updateWebjars(pom){

    const vaadinWebjarGroupId = `org.webjars.bowergithub.vaadin`;
    let updateFile = false;
    const pomJs = await xml2js.parseStringPromise(fs.readFileSync(pom, 'utf8'));
    name = pomJs.project.artifactId[0];

    //collect vaadin webjars
    //if there is dependencyManagement section, collect both places
    const deps = pomJs.project.dependencyManagement ? 
          [].concat(pomJs.project.dependencyManagement[0].dependencies[0].dependency, pomJs.project.dependencies[0].dependency): 
          pomJs.project.dependencies[0].dependency;

    const webjars = deps.filter(dep => dep.groupId == vaadinWebjarGroupId);

    for (j=0; j<webjars.length; j++){
        const webjarName = webjars[j].artifactId +'';
        const webjarVersion = webjars[j].version +'';
        const updatedVersion = await findUpdatedVersion(webjarName, webjarVersion);

        if (updatedVersion != webjarVersion){
            updateFile = true;
            webjars[j].version = updatedVersion; 

			if (pomJs.project.dependencyManagement){
				pomJs.project.dependencyManagement[0].dependencies[0].dependency.filter(dep => {
					if (dep.artifactId == webjarName){
						dep.version = updatedVersion;
						console.log('\x1b[33m', "In "+name+' dependencyManagement,' +dep.artifactId+ " has been updated from "+ webjarVersion +" to " + updatedVersion);
					}
				})
			}

            pomJs.project.dependencies[0].dependency.filter(dep => {
                if (dep.artifactId == webjarName){
                    dep.version = updatedVersion;
                    console.log('\x1b[33m', "In "+name+',' +dep.artifactId+ " has been updated from "+ webjarVersion +" to " + updatedVersion);
                }
            })
        }
    }

    if(updateFile){
        const updatedPom = new xml2js.Builder().buildObject(pomJs);
        console.log('\x1b[32m', `Writing ${pom}`);
        fs.writeFileSync(pom, updatedPom + '\n', 'utf8'); 
    } else {
        console.log("\x1b[0m",'No need to updated '+ name);
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
    name = modules[i].replace('-parent','');
    pom = './' + modules[i] + '/' + name + '/pom.xml';
    await updateWebjars(pom);
  }
  await updateWebjars('./pom.xml');
}

main();
