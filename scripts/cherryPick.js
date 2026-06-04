#!/usr/bin/env node
/**
 * This script is used for cherry-pick commits for vaadin-flow-components repos.
 * To run this script:  
 * 1.collect committed-PRs marked with target/<branch-name>  labels
 * 2.cherry-pick the commit to the target branchs
 * 3.label the original commit PR with cherry-picked-<branch-name>
 * Exception handling:
 * - works for closed and merged PRs
 * - Commit PR labelled with cherry-picked will be ignored.
 * - if cherry-pick cannot be done, the original PR will be labelled with need to pick manually
 *
 */

const https = require("https");
const { spawn } = require('child_process');
const readline = require('readline');
const exec = require('util').promisify(require('child_process').exec);
const { exe } = require('child_process');

let arrPR = [];
let arrTitle = [];
let arrURL = [];
let arrSHA = [];
let arrBranch = [];
let arrUser = [];
let arrMergedBy = [];
const arrBody = [];

const repo = "vaadin/flow-components";
const token = process.env['GITHUB_TOKEN'];
if (!token) {
  console.log(`GITHUB_TOKEN is not set, skipping PR creation`);
  process.exit(1);
}

async function getAllCommits(){
  let url = `https://api.github.com/repos/${repo}/pulls?state=closed&sort=updated&direction=desc&per_page=100`;
  try {
    const options = {
      headers:
      {
        'User-Agent': 'Vaadin Cherry Pick',
        'Authorization': `token ${token}`,
        'Content-Type': 'application/json',
      }
    };
    
    res = await fetch(url, options);
    if (!res.ok) {
      throw new Error(`HTTP ${res.status} ${res.statusText}`);
    }
    data = await res.json();
    data = data.filter(da => da.labels.length > 0 && da.merged_at !== null);
    
    if (data.length === 0) {
      console.log("No commits needs to be picked.");
      process.exit(0);
    }
    return data;
  } catch (error) {
    console.error(`Cannot get the commits. ${error}`);
    process.exit(1);
  }
}

async function getCommit(commitURL){
  try {
    const options = {
      headers:
      {
        'User-Agent': 'Vaadin Cherry Pick',
        'Authorization': `token ${token}`,
        'Content-Type': 'application/json',
      }
    };
    
    res = await fetch(commitURL, options);
    if (!res.ok) {
      throw new Error(`HTTP ${res.status} ${res.statusText}`);
    }
    data = await res.json();

    return data;
  } catch (error) {
    console.error(`Cannot get the commit. ${error}`);
    process.exit(1);
  }
}

async function filterCommits(commits){
  for (let commit of commits) {
    let target = false;
    let picked = false;
    for (let label of commit.labels){
      if(label.name.includes("target/")){
        target = true;
      }
      if(label.name.includes("cherry-picked") || label.name.includes("need to pick manually")){
        picked = true;
      }
    }
    if(target === true && picked === false){
      let singleCommit = await getCommit(commit.url);
      commit.labels.forEach(label => {
        let branch = /target\/(.*)/.exec(label.name);
        if (branch){
          console.log(commit.number, commit.user.login, commit.url, commit.merge_commit_sha, branch[1], singleCommit.merged_by.login);
          arrPR.push(commit.number);
          arrSHA.push(commit.merge_commit_sha);
          arrURL.push(commit.url);
          arrBranch.push(branch[1]);
          arrTitle.push(`${commit.title} (#${commit.number}) (CP: ${branch[1]})`);
          arrUser.push(`@${commit.user.login}`);
          arrMergedBy.push(`@${singleCommit.merged_by.login}`);
          arrBody.push(singleCommit.body || '');
        }
      })
    }
  }
}

function buildCherryPickBody(originalPRNumber, originalBody, targetBranch) {
  const quotedBody = (originalBody || '_No description provided in the original PR._')
    .split('\n')
    .map((line) => `> ${line}`)
    .join('\n');

  return `
This PR cherry-picks changes from the original PR #${originalPRNumber} to branch ${targetBranch}.

---

${quotedBody}
`.trim();
}

async function cherryPickCommits(){
  for(let i=arrPR.length-1; i>=0; i--){
    let branchName = `cherry-pick-${arrPR[i]}-to-${arrBranch[i]}-${Date.now()}`;
    await exec('git checkout main');
    await exec('git pull');
    await exec(`git checkout ${arrBranch[i]}`);
    await exec(`git reset --hard origin/${arrBranch[i]}`);

    try {
      await exec(`git checkout -b ${branchName}`);
    } catch (err) {
      console.error(`Cannot Create Branch, error : ${err}`);
      process.exit(1);
    }

    let conflicted = false;
    try {
      await exec(`git cherry-pick ${arrSHA[i]}`);
    } catch (err) {
      console.error(`Automatic cherry-pick of ${arrSHA[i]} to ${arrBranch[i]} failed: ${err}`);
      conflicted = true; // leave the conflicted tree in place for Claude
    }

    if (conflicted) {
      const resolved = await resolveWithClaude(arrPR[i], arrSHA[i], arrBranch[i], arrTitle[i]);
      if (!resolved) {
        // Fallback: keep the original "manual" label, no PR comment.
        await labelCommit(arrURL[i], `need to pick manually ${arrBranch[i]}`);
        await exec(`git cherry-pick --abort`).catch(() => {});
        await exec(`git checkout main`);
        await exec(`git branch -D ${branchName}`);
        continue;
      }
    }

    await exec(`git push origin HEAD:${branchName}`);
    await createPR(arrTitle[i], branchName, arrBranch[i], buildCherryPickBody(arrPR[i], arrBody[i], arrBranch[i]));
    await exec(`git checkout main`);
    await exec(`git branch -D ${branchName}`);
    await labelCommit(arrURL[i], `cherry-picked-${arrBranch[i]}`);
  }
}

async function resolveWithClaude(prNumber, sha, branch, title) {
  const prompt = [
    `A "git cherry-pick" for PR #${prNumber} (commit ${sha}) onto branch ${branch} ` +
      `is already in progress in the current working tree and has merge conflicts.`,
    `Resolve it by following these steps, and do nothing else — do NOT push, do NOT ` +
      `open a pull request, and do NOT change any PR labels (the surrounding script handles those):`,
    ``,
    `1. Resolve the merge conflicts. If needed, read the conflicting files on both the ` +
      `source branch and the ${branch} branch to understand the differences before resolving.`,
    `2. Verify the changes:`,
    `   - Run the unit tests for the affected component module(s).`,
    `   - If tests fail, investigate and fix the issue before continuing.`,
    `   - Run the formatter: mvn spotless:apply`,
    `3. Commit the resolved cherry-pick:`,
    `   - Stage only the files that were part of the original cherry-pick or that you had ` +
      `to fix after running tests. Do not stage untracked files.`,
    `   - Complete the cherry-pick commit using this exact subject line: "${title}"`
  ].join('\n');

  return new Promise((resolve) => {
    const child = spawn(
      'claude',
      [
        '-p',
        prompt,
        '--allowedTools',
        'Read,Edit,Bash(git:*),Bash(mvn:*)',
        '--permission-mode',
        'acceptEdits',
        '--output-format',
        'stream-json',
        '--verbose' // required by stream-json in -p mode
      ],
      { stdio: ['inherit', 'pipe', 'inherit'] }
    ); // pipe stdout so we can parse it

    const rl = readline.createInterface({ input: child.stdout });
    rl.on('line', (line) => {
      if (!line.trim()) return;
      try {
        logClaudeEvent(JSON.parse(line));
      } catch {
        console.log(line); // not JSON (shouldn't happen) — pass through
      }
    });

    child.on('error', (err) => {
      console.error(`Failed to launch Claude: ${err}`);
      resolve(false);
    });

    child.on('exit', async (code) => {
      rl.close();
      if (code !== 0) {
        console.error(`Claude exited with code ${code}`);
        return resolve(false);
      }
      try {
        const inProgress = require('fs').existsSync('.git/CHERRY_PICK_HEAD');
        const { stdout } = await exec('git status --porcelain');
        resolve(!inProgress && stdout.trim() === '');
      } catch {
        resolve(false);
      }
    });
  });
}

function logClaudeEvent(event) {
  switch (event.type) {
    case 'system':
      if (event.subtype === 'init') {
        console.log(`[claude] session started (model: ${event.model})`);
      }
      break;
    case 'assistant':
      for (const block of event.message?.content ?? []) {
        if (block.type === 'text' && block.text.trim()) {
          console.log(`[claude] ${block.text.trim()}`);
        } else if (block.type === 'tool_use') {
          const detail = block.input?.command ?? block.input?.file_path ?? '';
          console.log(`[claude] → ${block.name}${detail ? ' ' + detail : ''}`);
        }
      }
      break;
    case 'user': // tool results coming back — keep it terse
      console.log(`[claude] ← tool result`);
      break;
    case 'result':
      console.log(
        `[claude] done: ${event.subtype} ` +
          `(${event.num_turns} turns, ${event.duration_ms}ms, $${event.total_cost_usd ?? '?'})`
      );
      break;
  }
}

async function labelCommit(url, label){
  let issueURL = url.replace("pulls", "issues") + "/labels";
  const options = {
    method: 'POST',
    headers: {
      'User-Agent': 'Vaadin Cherry Pick',
      Authorization: `token ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ labels: [label] })
  };

  const res = await fetch(issueURL, options);
  if (!res.ok) {
    throw new Error(`HTTP ${res.status} ${res.statusText}`);
  }
}

async function postComment(url, userName, mergedBy, branch, message){
  let issueURL = url.replace("pulls", "issues") + "/comments";
  const options = {
    method: 'POST',
    headers: {
      'User-Agent': 'Vaadin Cherry Pick',
      Authorization: `token ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      body: `Hi ${userName} and ${mergedBy}, when i performed cherry-pick to this commit to ${branch}, i have encountered the following issue. Can you take a look and pick it manually?\n Error Message:\n ${message}`
    })
  };

  const res = await fetch(issueURL, options);
  if (!res.ok) {
    throw new Error(`HTTP ${res.status} ${res.statusText}`);
  }
}

async function createPR(title, head, base, body){
  const payload = {title, head, base, body};
  
  return new Promise(resolve => {
    const content = JSON.stringify({ title, head, base, body }, null, 1)
    const req = https.request({
      method: 'POST',
      hostname: 'api.github.com',
      path: `/repos/${repo}/pulls`,
      headers: {
        'Authorization': `token ${token}`,
        'User-Agent': 'Vaadin Cherry Pick',
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(content),
      },
      body: content
    }, res => {
      let responseBody = "";
      res.on("data", data => {
        responseBody += data;
      });
      res.on("end", () => {
        resolve({ status: res.statusCode, body: responseBody });
      });
    });
    req.write(content)
    req.end();
  }).then(({ status, body }) => {
    if (status >= 300) {
      console.error(`Failed to create PR '${title}' (HTTP ${status}): ${body}`);
      return;
    }
    const resp = JSON.parse(body);
    console.log(`Created PR '${title}' ${resp.html_url || resp.url}`);
  });
}

async function main(){
  let allCommits = await getAllCommits();
  await filterCommits(allCommits);
  await cherryPickCommits();

}

main();
