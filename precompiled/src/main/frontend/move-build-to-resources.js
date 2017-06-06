#!/usr/bin/env node

// Move files from the "build" directory to the ../webapp/VAADIN/frontend/ dir 
// without the "bower_components" directory so that the server can serve the files

const rimraf = require('rimraf');
const mkdirp = require('mkdirp');
const fs = require('fs');

const frontendVaadin = "../../../src/main/generated-resources/META-INF/resources/VAADIN/frontend";

if (!fs.existsSync(frontendVaadin)) {
	mkdirp.sync(frontendVaadin);
}

for (es of ["es5","es6"]) {
    const sourceDir = "build/" + es;
    const targetDir = frontendVaadin + "/" + es;

    rimraf.sync(targetDir);
    fs.renameSync(sourceDir + "/bower_components", targetDir);
}
