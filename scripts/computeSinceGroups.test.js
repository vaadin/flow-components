/**
 * Tests for the reconciliation groups of the "Update @since tags" workflow.
 *
 * Run with `node --test scripts/*.test.js`.
 *
 * The invariant these guard is the one that makes the reconciliation correct:
 * one group == one release timeline. Pooling artifacts that do not share a
 * timeline re-dates existing `@since` tags (see computeSinceGroups.js), and a
 * component that falls out of the matrix is silently never reconciled.
 */

const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const { computeGroups, EXCLUDED_MODULES } = require('./computeSinceGroups.js');
const { readParentModules } = require('./lib/modules.js');

const groups = computeGroups();
const artifactIds = (group) => group.artifacts.split('\n').map((entry) => entry.split('=')[0]);
const sourceRoots = (group) => group.artifacts.split('\n').map((entry) => entry.split('=')[1]);

test('every component parent module is reconciled', () => {
  const covered = groups.map((group) => group.name);
  const missing = readParentModules()
    .map((parentModule) => parentModule.replace(/^vaadin-(.+)-flow-parent$/, '$1'))
    .filter((component) => !covered.includes(component))
    .sort();
  // The only components left out are the ones whose every published module is
  // in EXCLUDED_MODULES. A new component must not end up here by accident.
  assert.deepStrictEqual(missing, ['ai-components', 'aura-theme', 'lumo-theme', 'spreadsheet']);
});

test('each artifact is indexed in exactly one group', () => {
  const seen = new Set();
  for (const group of groups) {
    for (const artifactId of artifactIds(group)) {
      assert.ok(!seen.has(artifactId), `${artifactId} is in more than one group`);
      seen.add(artifactId);
    }
  }
});

test('each group has its own index directory', () => {
  const indexDirs = groups.map((group) => group['index-dir']);
  assert.strictEqual(new Set(indexDirs).size, indexDirs.length);
});

test('every source root exists, and each is applied once', () => {
  for (const group of groups) {
    const roots = sourceRoots(group).filter(Boolean);
    assert.ok(roots.length > 0, `${group.name} has no source root`);
    assert.strictEqual(new Set(roots).size, roots.length, `${group.name} applies to a source root twice`);
    for (const root of roots) {
      assert.ok(fs.existsSync(root), `${root} does not exist`);
    }
  }
});

test('the testbench artifact is pooled with the component it tests', () => {
  const button = groups.find((group) => group.name === 'button');
  assert.deepStrictEqual(artifactIds(button), ['vaadin-button-flow', 'vaadin-button-testbench']);
});

test('the charts SVG generator is pooled with charts', () => {
  const charts = groups.find((group) => group.name === 'charts');
  assert.ok(artifactIds(charts).includes('vaadin-charts-flow-svg-generator'));
});

test('a renamed artifact contributes its old releases without a source root', () => {
  const shared = groups.find((group) => group.name === 'shared');
  assert.deepStrictEqual(shared.artifacts.split('\n'), [
    'vaadin-flow-components-base=vaadin-flow-components-shared-parent/vaadin-flow-components-base/src/main/java',
    'vaadin-flow-components-shared'
  ]);
});

test('excluded modules are not reconciled', () => {
  const indexed = groups.flatMap(artifactIds);
  for (const module of Object.keys(EXCLUDED_MODULES)) {
    assert.ok(!indexed.includes(module), `${module} is excluded but still indexed`);
  }
});
