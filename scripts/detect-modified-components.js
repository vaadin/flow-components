#!/usr/bin/env node
/**
 * Detect which Vaadin component modules were modified in a PR or merge_group
 * event, for use as input to scripts/mergeITs.js and scripts/wtr.js.
 *
 * Prints a space-separated list of component slugs (e.g. "grid combo-box") to
 * stdout. Empty output means "build everything".
 *
 * A partial build is only emitted when both:
 *   - fewer than 5 components are touched, AND
 *   - every changed file lives under a vaadin-*-flow-parent directory.
 *
 * Reads the GitHub Actions context from the standard env vars
 * GITHUB_EVENT_NAME and GITHUB_EVENT_PATH (the JSON payload of the
 * triggering event). For pull_request events GH_TOKEN must also be set so
 * the gh CLI can query the files API.
 */

const fs = require('fs');
const { execFileSync } = require('child_process');

const maxComponentsForPartial = 5;
const componentPathRegex = /vaadin.*flow-parent/;

let event = {};
let eventName = '';
let changedFiles = [];

function loadEvent() {
  eventName = process.env.GITHUB_EVENT_NAME || '';
  const eventPath = process.env.GITHUB_EVENT_PATH;
  if (eventPath && fs.existsSync(eventPath)) {
    event = JSON.parse(fs.readFileSync(eventPath, 'utf8'));
  }
}

function readWorkflowDispatchInput() {
  if (eventName !== 'workflow_dispatch') return '';
  return (event.inputs && event.inputs.components) || '';
}

function readChangedFiles() {
  if (eventName === 'pull_request') {
    const out = execFileSync(
      'gh',
      ['api', `repos/${process.env.GITHUB_REPOSITORY}/pulls/${event.pull_request.number}/files`, '--jq', '.[].filename'],
      { encoding: 'utf8' }
    );
    changedFiles = out.split('\n').filter(Boolean);
  } else if (eventName === 'merge_group') {
    const out = execFileSync(
      'git',
      ['diff', '--name-only', event.merge_group.base_sha, event.merge_group.head_sha],
      { encoding: 'utf8' }
    );
    changedFiles = out.split('\n').filter(Boolean);
  }
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
  loadEvent();

  // workflow_dispatch input takes precedence over auto-detection.
  const override = readWorkflowDispatchInput();
  if (override) {
    process.stdout.write(override + '\n');
    return;
  }

  readChangedFiles();
  const result = computeComponents();
  if (result) process.stdout.write(result + '\n');
}

main();
