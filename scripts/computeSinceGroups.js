#!/usr/bin/env node
/**
 * Compute the reconciliation groups used by the "Update @since tags" workflow.
 *
 * The reconcile-since-tags action derives `@since` from the published release
 * history: it merges the releases of every artifact it is given onto a single
 * version axis and dates an API element to the start of the unbroken run of
 * releases in which it is present. That is only correct when all pooled
 * artifacts share one release timeline.
 *
 * Component artifacts in this repository do not share one. Each of them kept
 * its own version scheme until the platform aligned them at 14.5:
 * `vaadin-button-flow` ran 1.0 -> 4.0, `vaadin-grid-flow` 1.0 -> 6.0,
 * `vaadin-charts-flow` 6.0 -> 9.0. Pooling them drops each component's
 * versions into the others' history as releases the component never made, the
 * presence run breaks there, and `@since 1.0` silently becomes `@since 14.5`.
 *
 * So every component gets its own group, and therefore its own index. Within a
 * group the `-flow`, `-testbench` and (for charts) `-svg-generator` artifacts
 * are pooled: they are released together, so they do share an axis, and
 * pooling them tracks API that moves between them.
 *
 * Usage:
 *   node scripts/computeSinceGroups.js
 *     Prints the groups as a GitHub Actions matrix JSON.
 *
 *   node scripts/computeSinceGroups.js grid button
 *     Limits the matrix to the named components.
 *
 *   node scripts/computeSinceGroups.js --list
 *     Prints the groups in a readable form, with the excluded modules and the
 *     reason each is excluded.
 */

const fs = require('fs');
const { parseArgs } = require('util');
const { readParentModules } = require('./lib/modules.js');

// Modules that have Java sources but no usable release history on Maven
// Central, and which the reconciliation must therefore leave alone. Anything
// listed here is reported by --list so the list stays reviewable.
const EXCLUDED_MODULES = {
  'vaadin-lumo-theme-flow': 'not published to Maven Central',
  'vaadin-aura-theme-flow': 'not published to Maven Central',
  'vaadin-ai-core-flow': 'only pre-releases published so far',
  'vaadin-ai-extensions-flow': 'only pre-releases published so far',
  'vaadin-spreadsheet-flow':
    'carries @since tags from the pre-Flow vaadin-spreadsheet add-on (Vaadin 7/8), which was never released to Maven Central; reconciling against the 23.1+ history of vaadin-spreadsheet-flow would rewrite all of them',
  'vaadin-spreadsheet-testbench': 'see vaadin-spreadsheet-flow',
  'vaadin-spreadsheet-flow-client': 'published history has a gap (no 24.0 - 24.5 releases)'
};

// Artifacts to index in addition to the modules found in the working tree.
// An artifact that was renamed keeps its old releases under the old id, and
// pooling both ids is what lets the tool see the full history of the sources.
const RENAMED_ARTIFACTS = {
  'vaadin-flow-components-base': ['vaadin-flow-components-shared']
};

// Groups that are not component parent modules
const EXTRA_GROUPS = {
  shared: ['vaadin-flow-components-shared-parent/vaadin-flow-components-base']
};

// Published modules of a parent module: the `-flow` and `-testbench`
// artifacts, plus the charts `-svg-generator`. Integration test modules are
// not published and are skipped.
function readPublishedModules(parentModule) {
  return fs
    .readdirSync(parentModule, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && /-(flow|testbench|svg-generator)$/.test(entry.name))
    .map((entry) => `${parentModule}/${entry.name}`)
    .filter((module) => fs.existsSync(`${module}/pom.xml`) && fs.existsSync(`${module}/src/main/java`))
    .sort();
}

// The `<artifact>=<source root>` entries the action expects. A former id of a
// renamed artifact is listed without a source root: it contributes its
// releases to the index, but the sources live in the module of the current id.
function artifactEntries(modules) {
  return modules.flatMap((module) => {
    const artifactId = module.split('/').at(-1);
    return [`${artifactId}=${module}/src/main/java`, ...(RENAMED_ARTIFACTS[artifactId] || [])];
  });
}

// Compute one group per component, each with its own index directory
function computeGroups() {
  const parentModules = {
    ...Object.fromEntries(
      readParentModules().map((parentModule) => [
        parentModule.replace(/^vaadin-(.+)-flow-parent$/, '$1'),
        readPublishedModules(parentModule)
      ])
    ),
    ...EXTRA_GROUPS
  };
  return Object.entries(parentModules)
    .map(([name, modules]) => ({
      name,
      'index-dir': `.since-index-${name}`,
      artifacts: artifactEntries(modules.filter((module) => !EXCLUDED_MODULES[module.split('/').at(-1)])).join('\n')
    }))
    .filter((group) => group.artifacts)
    .sort((a, b) => a.name.localeCompare(b.name));
}

function main() {
  const { values, positionals } = parseArgs({
    options: { list: { type: 'boolean', default: false } },
    allowPositionals: true
  });
  let groups = computeGroups();
  if (positionals.length > 0) {
    groups = groups.filter((group) => positionals.includes(group.name));
  }
  if (values.list) {
    for (const group of groups) {
      console.log(`${group.name}\n  ${group.artifacts.split('\n').join('\n  ')}`);
    }
    console.log('\nexcluded:');
    for (const [module, reason] of Object.entries(EXCLUDED_MODULES)) {
      console.log(`  ${module}: ${reason}`);
    }
  } else {
    console.log(JSON.stringify({ include: groups }));
  }
}

if (require.main === module) {
  main();
}

module.exports = { computeGroups, EXCLUDED_MODULES };
