#!/usr/bin/env node
/**
 * Prepare the review scope for the CI code review.
 *
 * It pre-computes everything the review needs so the review agent never has to
 * derive it, then writes a markdown overview file that points at each input by
 * absolute path.
 *
 * Reads from the environment (set by GitHub Actions):
 *   PR_NUMBER / argv[2]  pull request number to review
 *   RUNNER_TEMP          base dir for generated files
 *   GITHUB_REPOSITORY    owner/repo, for the compare API
 *   GITHUB_WORKSPACE     PR head checkout (the review's working tree)
 *   GH_TOKEN             token for the gh CLI
 *
 * Produces under $RUNNER_TEMP/pr-review:
 *   pr.diff, pr-files.txt, pr-meta.json, base/ (merge-base worktree), overview.md
 * Prints the overview path to stdout.
 *
 * Usage:
 *   node .github/claude/prepare-review.js <pr-number>
 */

import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';

// Diffs can be large; give captured output room.
const MAX_BUFFER = 100 * 1024 * 1024;

// Run a command, capture stdout as a string, let stderr pass through.
function capture(file, args) {
  return execFileSync(file, args, {
    encoding: 'utf8',
    maxBuffer: MAX_BUFFER,
    stdio: ['ignore', 'pipe', 'inherit'],
  });
}

function requireEnv(name) {
  const value = process.env[name];
  if (!value) {
    console.error(`${name} must be set`);
    process.exit(2);
  }
  return value;
}

function buildOverview({
  metaPath,
  diffPath,
  filesPath,
  headPath,
  basePath,
  flowPath,
  webComponentsPath,
}) {
  return `# Code review inputs

This file names the absolute path of every prepared input for the review.
Read the paths below and use them; do not compute your own diff or fetch
your own PR data.

- **PR metadata**: \`${metaPath}\` — title, description, author, labels. The
  title and description state the intent the change is reviewed against.
- **Diff**: \`${diffPath}\` — the complete review scope, exactly as GitHub
  renders it.
- **Changed files**: \`${filesPath}\`.
- **Head**: \`${headPath}\` — the PR head checkout and the review's working tree;
  read related and surrounding code here.
- **Base (pre-change state)**: \`${basePath}\` — a checkout of the merge-base;
  consult it when a finding depends on prior behavior.
- **Reference — Vaadin Flow framework** (read-only): \`${flowPath}\` — base
  component classes, the \`Element\` API, framework internals.
- **Reference — Vaadin web components** (read-only): \`${webComponentsPath}\` —
  client-side properties, events, and DOM behavior of the wrapped components.

The reference checkouts are side-trip context: consult them only when this
repository's code does not answer the question.
`;
}

function main() {
  const prNumber = process.argv[2] || process.env.PR_NUMBER;
  if (!prNumber) {
    console.error('PR number required (argv[2] or PR_NUMBER)');
    process.exit(2);
  }
  const runnerTemp = requireEnv('RUNNER_TEMP');
  const repo = requireEnv('GITHUB_REPOSITORY');
  const headPath = process.env.GITHUB_WORKSPACE || process.cwd();

  const scopeDir = path.join(runnerTemp, 'pr-review');
  fs.mkdirSync(scopeDir, { recursive: true });

  const diffPath = path.join(scopeDir, 'pr.diff');
  const filesPath = path.join(scopeDir, 'pr-files.txt');
  const metaPath = path.join(scopeDir, 'pr-meta.json');
  const basePath = path.join(scopeDir, 'base');
  const overviewPath = path.join(scopeDir, 'overview.md');

  // Cloned by a separate workflow step into this dir; recorded here so the
  // overview is the single source of every path.
  const referenceDir = path.join(runnerTemp, 'reference');
  const flowPath = path.join(referenceDir, 'flow');
  const webComponentsPath = path.join(referenceDir, 'web-components');

  // The diff exactly as GitHub renders it, the changed-file list, and metadata.
  fs.writeFileSync(diffPath, capture('gh', ['pr', 'diff', prNumber]));
  fs.writeFileSync(filesPath, capture('gh', ['pr', 'diff', prNumber, '--name-only']));
  fs.writeFileSync(
    metaPath,
    capture('gh', [
      'pr', 'view', prNumber,
      '--json', 'title,body,author,labels,baseRefName,headRefName',
    ])
  );

  // Merge-base = the pre-change state, resolved by the compare API regardless
  // of local history or where the base branch has moved since.
  const headSha = capture('gh', ['pr', 'view', prNumber, '--json', 'headRefOid', '--jq', '.headRefOid']).trim();
  const baseRefSha = capture('gh', ['pr', 'view', prNumber, '--json', 'baseRefOid', '--jq', '.baseRefOid']).trim();
  const baseSha = capture('gh', ['api', `repos/${repo}/compare/${baseRefSha}...${headSha}`, '--jq', '.merge_base_commit.sha']).trim();

  execFileSync('git', ['fetch', '--depth=1', 'origin', baseSha], { stdio: 'inherit' });
  execFileSync('git', ['worktree', 'add', basePath, baseSha], { stdio: 'inherit' });

  fs.writeFileSync(
    overviewPath,
    buildOverview({ metaPath, diffPath, filesPath, headPath, basePath, flowPath, webComponentsPath })
  );

  console.log(overviewPath);
}

main();
