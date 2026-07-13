#!/usr/bin/env node
/**
 * Post the code-review findings to the PR as one review.
 *
 * Runs in code-review.yml as a plain workflow step, after the claude-code-action
 * step. Reads the action's execution file and takes the input of the last
 * ReportFindings tool call. No ReportFindings call at all means the review was lost
 * (background-subagent failure mode): the script exits non-zero instead of
 * posting, so a lost review is never mistaken for a clean one. An empty
 * findings array is a real result and posts a body-only "no findings" review.
 *
 * Every finding is posted as its own inline review thread, demoted
 * line-level → file-level → summary list as anchoring fails. The summary body
 * holds the overview line, a table over all findings, and the full prose of
 * any findings that could not be attached to the diff.
 *
 * Posting uses the GraphQL pending-review flow so all threads and the summary
 * are posted as one review (single notification).
 *
 * Reads from the environment (set by GitHub Actions):
 *   PR_NUMBER / argv[2]  pull request number reviewed
 *   EXECUTION_FILE       JSON transcript of the review run
 *   GITHUB_REPOSITORY    owner/repo
 *   GH_TOKEN             token for the gh CLI; its identity is the review
 *                        author
 *   DRY_RUN              if set, print the routing and rendered bodies and
 *                        exit without calling the GitHub API
 *
 * Usage:
 *   node .github/claude/post-review.js <pr-number>
 */

import fs from 'node:fs';
import { execFileSync } from 'node:child_process';

const MAX_BUFFER = 100 * 1024 * 1024;

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

// Run a GraphQL query/mutation via gh. Numbers go through -F so they arrive
// typed; everything else as strings (valid over the wire for enums and IDs).
function graphql(query, variables = {}) {
  const args = ['api', 'graphql', '-f', `query=${query}`];
  for (const [key, value] of Object.entries(variables)) {
    args.push(typeof value === 'number' ? '-F' : '-f', `${key}=${value}`);
  }
  return JSON.parse(capture('gh', args)).data;
}

// The action writes the execution file as a single JSON array of events.
function readEvents(executionFile) {
  const events = JSON.parse(fs.readFileSync(executionFile, 'utf8'));
  if (!Array.isArray(events)) {
    throw new Error('Execution file is not a JSON array of events');
  }
  return events;
}

// The input of the last ReportFindings tool_use in the transcript.
function extractReport(executionFile) {
  let report = null;
  for (const event of readEvents(executionFile)) {
    if (event.type !== 'assistant') continue;
    for (const block of event.message?.content ?? []) {
      if (block.type === 'tool_use' && block.name === 'ReportFindings') {
        report = block.input;
      }
    }
  }
  return report;
}

function normalize(finding) {
  return {
    ...finding,
    category: (finding.category || 'uncategorized').toLowerCase(),
    verdict: (finding.verdict || 'plausible').toLowerCase(),
  };
}

// --- Rendering ------------------------------------------------------------
// Every finding starts with a category emoji on the title and ends in the
// same meta line: file link, category, verdict.

const CATEGORY_EMOJI = {
  correctness: '⚠️',
  simplification: '🧹',
  reuse: '🧹',
  conventions: '🧹',
  efficiency: '⚡',
};

function categoryEmoji(finding) {
  return CATEGORY_EMOJI[finding.category] || '👀';
}

function findingTitle(finding) {
  return `**${categoryEmoji(finding)} ${finding.summary}**`;
}

// Pipes would break the table; newlines can't appear inside a cell.
function tableCell(text) {
  return text.replace(/\|/g, '\\|').replace(/\s*\n\s*/g, ' ');
}

// One table over ALL findings, commented and unanchored alike, in report
// order (most severe first). The first column holds only the category emoji.
function findingsTable(findings) {
  return [
    '| | Finding |',
    '| --- | --- |',
    ...findings.map((f) => `| ${categoryEmoji(f)} | ${tableCell(f.summary)} |`),
  ].join('\n');
}

function fileLabel(finding) {
  return finding.line ? `${finding.file}:${finding.line}` : finding.file;
}

// Link to the blob at the head SHA so non-anchored findings stay clickable.
function fileLink(repo, headSha, finding) {
  if (!finding.file) return null;
  const base = finding.file.split('/').pop();
  const label = finding.line ? `${base}:${finding.line}` : base;
  const anchor = finding.line ? `#L${finding.line}` : '';
  return `[${label}](https://github.com/${repo}/blob/${headSha}/${encodeURI(finding.file)}${anchor})`;
}

function findingMeta(repo, headSha, finding) {
  const meta = [
    fileLink(repo, headSha, finding),
    `\`${finding.category}\``,
    `\`${finding.verdict}\``,
  ]
    .filter(Boolean)
    .join(' · ');
  return `<sub>${meta}</sub>`;
}

function findingBlock(repo, headSha, finding) {
  const scenario = (finding.failure_scenario || '').trim();
  return [findingTitle(finding), scenario, findingMeta(repo, headSha, finding)]
    .filter(Boolean)
    .join('\n');
}

function threadBody(repo, headSha, finding, { aroundLine } = {}) {
  const parts = [];
  if (aroundLine) {
    parts.push(`_Around line ${aroundLine} — could not attach to the exact diff line._`);
  }
  parts.push(
    findingTitle(finding),
    finding.failure_scenario || '',
    findingMeta(repo, headSha, finding)
  );
  return parts.filter(Boolean).join('\n\n');
}

function pluralize(count, noun) {
  return `${count} ${noun}${count === 1 ? '' : 's'}`;
}

function joinNaturally(parts) {
  if (parts.length <= 1) return parts.join('');
  return `${parts.slice(0, -1).join(', ')} and ${parts[parts.length - 1]}`;
}

// One-line overview that opens every summary body; doubles as the whole body
// when there is nothing else to show, so "reviewed, nothing found" stays
// distinguishable from "review never happened".
function overviewLine(unanchored, inlineCount) {
  const clauses = [];
  if (inlineCount) {
    clauses.push(
      inlineCount === 1 ? 'left **1 comment**' : `left **${inlineCount} comments**`
    );
  }
  if (unanchored.length) {
    clauses.push(
      `collected ${pluralize(unanchored.length, 'finding')} below that could not be attached to the diff`
    );
  }
  if (!clauses.length) {
    return '✅ Nothing to flag — the changes look good.';
  }
  return `Reviewed the changes — ${joinNaturally(clauses)}.`;
}

// Summary body: overview line, then the table over all findings, then the
// full prose of the findings whose inline anchoring failed (visible, not
// collapsed) — commented findings keep their prose in the inline comment.
// Zero-findings runs still post.
function summaryBody(repo, headSha, findings, unanchored, inlineCount) {
  const parts = [overviewLine(unanchored, inlineCount)];
  if (findings.length) {
    parts.push(findingsTable(findings));
  }
  if (unanchored.length) {
    parts.push(unanchored.map((f) => findingBlock(repo, headSha, f)).join('\n\n'));
  }
  return parts.join('\n\n');
}

// --- Posting ----------------------------------------------------------------

const PR_QUERY = `query($owner: String!, $name: String!, $number: Int!) {
  repository(owner: $owner, name: $name) {
    pullRequest(number: $number) {
      id
      headRefOid
      reviews(states: PENDING, first: 10) { nodes { id } }
    }
  }
}`;

// Only works on PENDING reviews, and pending reviews are only visible to
// their author — so everything the query above returns is a leftover of ours
// (a crashed previous run would otherwise block creating a new one forever).
const DELETE_PENDING = `mutation($reviewId: ID!) {
  deletePullRequestReview(input: {pullRequestReviewId: $reviewId}) {
    pullRequestReview { id }
  }
}`;

const ADD_PENDING = `mutation($prId: ID!) {
  addPullRequestReview(input: {pullRequestId: $prId}) {
    pullRequestReview { id }
  }
}`;

const ADD_BODY_ONLY = `mutation($prId: ID!, $body: String!) {
  addPullRequestReview(input: {pullRequestId: $prId, event: COMMENT, body: $body}) {
    pullRequestReview { url author { login } }
  }
}`;

const ADD_LINE_THREAD = `mutation($reviewId: ID!, $path: String!, $line: Int!, $body: String!) {
  addPullRequestReviewThread(input: {
    pullRequestReviewId: $reviewId, path: $path, line: $line, side: RIGHT,
    subjectType: LINE, body: $body
  }) { thread { id } }
}`;

const ADD_FILE_THREAD = `mutation($reviewId: ID!, $path: String!, $body: String!) {
  addPullRequestReviewThread(input: {
    pullRequestReviewId: $reviewId, path: $path, subjectType: FILE, body: $body
  }) { thread { id } }
}`;

const SUBMIT_REVIEW = `mutation($reviewId: ID!, $body: String!) {
  submitPullRequestReview(input: {pullRequestReviewId: $reviewId, event: COMMENT, body: $body}) {
    pullRequestReview { url author { login } }
  }
}`;

// Try-then-demote: line-level → file-level → summary list. Anchoring rules
// (line must be in the PR diff; renamed/deleted/binary files) are GitHub's to
// judge — any failure demotes, nothing breaks the review. A failure is either
// a GraphQL error OR a 200 with thread: null — GitHub reports a line outside
// the diff the latter way, without an error.
function tryAddThread(mutation, variables) {
  try {
    return Boolean(graphql(mutation, variables).addPullRequestReviewThread?.thread?.id);
  } catch {
    return false;
  }
}

function postThread(repo, headSha, reviewId, finding) {
  if (finding.file && finding.line) {
    const created = tryAddThread(ADD_LINE_THREAD, {
      reviewId,
      path: finding.file,
      line: finding.line,
      body: threadBody(repo, headSha, finding),
    });
    if (created) return true;
    console.warn(`Line thread failed for ${fileLabel(finding)}, retrying file-level`);
  }
  if (finding.file) {
    const created = tryAddThread(ADD_FILE_THREAD, {
      reviewId,
      path: finding.file,
      body: threadBody(repo, headSha, finding, { aroundLine: finding.line }),
    });
    if (created) return true;
    console.warn(`File thread failed for ${finding.file}, demoting to summary`);
  }
  return false;
}

function logPosted(review) {
  console.log(`Posted review ${review.url} as ${review.author.login}`);
}

function main() {
  const prNumber = process.argv[2] || process.env.PR_NUMBER;
  if (!prNumber) {
    console.error('PR number required (argv[2] or PR_NUMBER)');
    process.exit(2);
  }
  const repo = requireEnv('GITHUB_REPOSITORY');
  const executionFile = requireEnv('EXECUTION_FILE');

  const report = extractReport(executionFile);
  if (!report) {
    console.error(
      'No ReportFindings call in the execution file — the review was lost; refusing to post.'
    );
    process.exit(1);
  }
  const findings = (Array.isArray(report.findings) ? report.findings : []).map(normalize);
  console.log(`Findings: ${findings.length} total`);

  if (process.env.DRY_RUN) {
    console.log('\n--- inline thread bodies ---');
    for (const finding of findings) {
      console.log(`\n[${fileLabel(finding)}]\n${threadBody(repo, 'HEAD', finding)}`);
    }
    console.log('\n--- summary body ---\n');
    console.log(summaryBody(repo, 'HEAD', findings, [], findings.length));
    return;
  }

  const [owner, name] = repo.split('/');
  const pr = graphql(PR_QUERY, { owner, name, number: Number(prNumber) }).repository.pullRequest;

  for (const stale of pr.reviews.nodes) {
    console.warn(`Deleting stale pending review ${stale.id}`);
    graphql(DELETE_PENDING, { reviewId: stale.id });
  }

  if (!findings.length) {
    const body = summaryBody(repo, pr.headRefOid, [], [], 0);
    const result = graphql(ADD_BODY_ONLY, { prId: pr.id, body });
    logPosted(result.addPullRequestReview.pullRequestReview);
    return;
  }

  const pending = graphql(ADD_PENDING, { prId: pr.id });
  const reviewId = pending.addPullRequestReview.pullRequestReview.id;

  const unanchored = [];
  let posted = 0;
  for (const finding of findings) {
    if (postThread(repo, pr.headRefOid, reviewId, finding)) {
      posted += 1;
    } else {
      unanchored.push(finding);
    }
  }

  const body = summaryBody(repo, pr.headRefOid, findings, unanchored, posted);
  const result = graphql(SUBMIT_REVIEW, { reviewId, body });
  logPosted(result.submitPullRequestReview.pullRequestReview);
}

main();
