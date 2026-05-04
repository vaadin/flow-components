#!/usr/bin/env node
/**
 * Decide whether a partial build is appropriate based on a list of changed
 * file paths read from stdin (one per line), and print the affected Vaadin
 * component slugs (e.g. "grid combo-box") to stdout. Empty output means
 * "build everything".
 *
 * A partial build is only emitted when both:
 *   - fewer than 5 components are touched, AND
 *   - every changed file lives under a vaadin-*-flow-parent directory.
 *
 * Pure filter: caller is responsible for collecting the changed-file list
 * (e.g. via `gh api` for PRs or `git diff --name-only` for merge_group).
 *
 * Usage: <produce file list> | node scripts/detect-modified-components.js
 */

const fs = require('fs');

const maxComponentsForPartial = 5;
const componentPathRegex = /vaadin.*flow-parent/;

let changedFiles = [];

function readChangedFiles() {
  const input = fs.readFileSync(0, 'utf8');
  changedFiles = input.split('\n').filter(Boolean);
}

function computeComponents() {
  if (changedFiles.length === 0) return '';

  // If any file lives outside a component module, fall back to a full build.
  const componentFiles = changedFiles.filter((f) => componentPathRegex.test(f));
  if (componentFiles.length !== changedFiles.length) return '';

  const components = [
    ...new Set(componentFiles.map((f) => f.replace(/^vaadin-(.*?)-flow-parent.*/, '$1'))),
  ].sort();

  // Many components changed -> safer to build everything.
  if (components.length >= maxComponentsForPartial) return '';
  return components.join(' ');
}

function main() {
  readChangedFiles();
  const result = computeComponents();
  if (result) process.stdout.write(result + '\n');
}

main();
