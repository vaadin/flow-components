/**
 * Helpers for discovering the published Maven modules of a component parent
 * module.
 */

const fs = require('fs');

// Collect the published component module poms of a parent module: the main
// `-flow` module and any additional ones, such as `vaadin-ai-core-flow` and
// `vaadin-ai-extensions-flow` in `vaadin-ai-components-flow-parent`
function readComponentPoms(parentModule) {
  return fs
    .readdirSync(parentModule, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && /-flow$/.test(entry.name))
    .map((entry) => `${parentModule}/${entry.name}/pom.xml`)
    .filter((pomPath) => fs.existsSync(pomPath))
    .sort();
}

module.exports = { readComponentPoms };
