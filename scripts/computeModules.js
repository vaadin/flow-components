#!/usr/bin/env node
/**
 * Compute the set of components affected by a change. This is the single
 * place that decides which component modules CI should validate; unit tests,
 * WTR tests and merged ITs all consume its output.
 *
 * Usage:
 *   node scripts/computeModules.js grid combo-box
 *     Expands an explicit list of components with components that depend
 *     on them.
 *
 *   node scripts/computeModules.js --changed-files -- [file...]
 *     Maps changed file paths to components and expands the result with
 *     dependent components.
 *
 * Prints the resulting component names (e.g. "grid crud grid-pro") to
 * stdout. Prints nothing when everything should be validated: when the
 * change touches files outside component modules, or when there is no
 * input.
 */

const fs = require('fs');
const { parseArgs } = require('util');
const { readComponentPoms } = require('./lib/modules.js');

// Read component parent modules from the root pom.xml
function readParentModules() {
  const rootPom = fs.readFileSync('pom.xml', 'utf8');
  return [...rootPom.matchAll(/<module>([^<]+)<\/module>/g)]
    .map((match) => match[1])
    .filter((module) => /^vaadin-.*-flow-parent$/.test(module));
}

// Check whether a pom declares a dependency on com.vaadin:<artifactId>
function dependsOn(pomXml, artifactId) {
  return [...pomXml.matchAll(/<dependency>([\s\S]*?)<\/dependency>/g)].some(
    ([, dependency]) =>
      dependency.includes('<groupId>com.vaadin</groupId>') &&
      dependency.includes(`<artifactId>${artifactId}</artifactId>`)
  );
}

// Collect the artifact ids of the published modules of a component. The
// artifact id of each module matches its directory name.
function readComponentArtifactIds(component) {
  const parentModule = `vaadin-${component}-flow-parent`;
  if (!fs.existsSync(parentModule)) {
    return [`vaadin-${component}-flow`];
  }
  return readComponentPoms(parentModule).map(
    (pomPath) => pomPath.split('/').at(-2)
  );
}

// Map changed file paths to component names. Returns null (= full
// validation) if any file is outside a component parent module.
function componentsFromChangedFiles(changedFiles) {
  const components = new Set();
  for (const file of changedFiles) {
    const match = file.match(/^vaadin-(.+)-flow-parent\//);
    if (!match) {
      return null;
    }
    components.add(match[1]);
  }
  return [...components];
}

// Recursively add components whose modules depend on one of the given
// components
function addDependentComponents(components) {
  const parentModules = readParentModules();
  const result = [...components];
  const queue = [...components];
  while (queue.length > 0) {
    const artifactIds = readComponentArtifactIds(queue.shift());
    for (const parentModule of parentModules) {
      const componentName = parentModule.replace(/^vaadin-(.+)-flow-parent$/, '$1');
      if (result.includes(componentName)) {
        continue;
      }
      const dependent = readComponentPoms(parentModule).some((pomPath) => {
        const pomXml = fs.readFileSync(pomPath, 'utf8');
        return artifactIds.some((artifactId) => dependsOn(pomXml, artifactId));
      });
      if (dependent) {
        result.push(componentName);
        queue.push(componentName);
      }
    }
  }
  return result;
}

function main() {
  const { values, positionals } = parseArgs({
    options: {
      'changed-files': { type: 'boolean', default: false }
    },
    allowPositionals: true
  });
  let components;
  if (values['changed-files']) {
    const changedFiles = positionals.filter(Boolean);
    if (changedFiles.length === 0) {
      return;
    }
    components = componentsFromChangedFiles(changedFiles);
    if (!components) {
      return;
    }
  } else if (positionals.length > 0) {
    components = positionals;
  } else {
    return;
  }
  console.log(addDependentComponents(components).join(' '));
}

main();
