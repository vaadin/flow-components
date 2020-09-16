#!/usr/bin/env node

// Move files from the "build" directory to the target dir

const rimraf = require('rimraf');
const fs = require('fs');

const frontendVaadin = "../../../#frontend-target-folder#";


for (dir of ["frontend-es5","frontend-es6"]) {
    const sourceDir = "build/" + dir;
    const targetDir = "../"+dir;

    rimraf.sync(targetDir);
    fs.renameSync(sourceDir, targetDir);
}
