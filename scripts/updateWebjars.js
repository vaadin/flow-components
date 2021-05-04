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
    const proComponentsWebjars = deps.filter(dep => dep.groupId == `com.vaadin.webjar`);

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
    
    for (k=0; k<proComponentsWebjars.length; k++){
      const proWebjarName = proComponentsWebjars[k].artifactId +'';
      const proWebjarVersion = proComponentsWebjars[k].version +'';
      const proWebjarUpdatedVersion = await findProComponentUpdatedVersion(proWebjarName, proWebjarVersion);
      
      if (proWebjarUpdatedVersion != proWebjarVersion){
        updateFile = true;
        proComponentsWebjars[k].version = proWebjarUpdatedVersion; 

        if (pomJs.project.dependencyManagement){
				  pomJs.project.dependencyManagement[0].dependencies[0].dependency.filter(dep => {
					  if (dep.artifactId == proWebjarName){
						  dep.version = proWebjarUpdatedVersion;
						  console.log('\x1b[33m', "[PRO Component]In "+name+' dependencyManagement,' +dep.artifactId+ " has been updated from "+ proWebjarVersion +" to " + proWebjarUpdatedVersion);
					  }
				  })
			  }

        pomJs.project.dependencies[0].dependency.filter(dep => {
          if (dep.artifactId == proWebjarName){
            dep.version = proWebjarUpdatedVersion;
            console.log('\x1b[33m', "[PRO Component]In "+name+',' +dep.artifactId+ " has been updated from "+ proWebjarVersion +" to " + proWebjarUpdatedVersion);
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

async function findProComponentUpdatedVersion(name, version) {
    let verArray = version.split('.');
    let prereleaseUrl = "https://maven.vaadin.com/vaadin-prereleases/com/vaadin/webjar/"+name+"/maven-metadata.xml";
    let releaseUrl = "https://repo1.maven.org/maven2/com/vaadin/webjar/"+name+"/maven-metadata.xml";
    prereleaseXml = await getMetadata(prereleaseUrl, name);    
    prereleaseXml = await xml2js.parseStringPromise(prereleaseXml, 'utf8');
    
    releaseXml = await getMetadata(releaseUrl, name);
    releaseXml = await xml2js.parseStringPromise(releaseXml, 'utf8');
    
    let aviVersions = releaseXml.metadata.versioning[0].versions[0].version; 
    let minorVersions = aviVersions.filter(version => version.startsWith([verArray[0],verArray[1]].join('.')));
    
    if (minorVersions.length > 0){
      return minorVersions[minorVersions.length-1];
    } else {
      let aviPreVersions = prereleaseXml.metadata.versioning[0].versions[0].version; 
      let minorPreVersions = aviPreVersions.filter(version => version.startsWith([verArray[0],verArray[1]].join('.')));
      
      return minorPreVersions[minorPreVersions.length-1];
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
    let url = "https://repo1.maven.org/maven2/org/webjars/bowergithub/vaadin/"+name+"/maven-metadata.xml"
    xml = await getMetadata(url, name);
    xml = await xml2js.parseStringPromise(xml, 'utf8');
  
    let aviVersions = xml.metadata.versioning[0].versions[0].version; 
    let minorVersions = aviVersions.filter(version => version.startsWith([verArray[0],verArray[1]].join('.')));

    return minorVersions[minorVersions.length-1]
}

/**
 * Get the metadata of webjar from maven central
 * @param {*} name webjar artifactId
 */
async function getMetadata(url, name) {
    return new Promise(resolve => {
        https.get(url, function(res) {
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
