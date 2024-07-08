/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
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
