#!/usr/bin/env node
/**
 * Create change-log.
 * Example
 *   ./scripts/generateChangeLog.js --from from_tag --to to_tag --version version
 *
 * When --to is not given HEAD is selected.
 * When --from is not given latest tag is selected.
 * When --version is not given version is computed increasing from.
 */

const { create } = require('domain');
const fs = require('fs');
const exec = require('util').promisify(require('child_process').exec);
let from, to, version, flowVersion;

const keyName = {
  feat: 'New Features',
  refactor: 'Code Refactoring',
  fix: 'Fixes',
  chore: 'Chore',
  ci: 'Continuous Integration',
  test: 'Tests'
}

async function run(cmd) {
  const { stdout, stderr } = await exec(cmd, {maxBuffer: 5000 * 1024});
  return stdout.trim();
}

function computeVersion() {
  if (to == 'HEAD') {
    const r = /^(.*)(\d+)$/.exec(from);
    if (r) {
      version = r[1] + (parseInt(r[2]) + 1);
    }
  }
}

// Compute tags used for commit delimiters
async function getReleases() {
  for (i = 2;process.argv[i]; i++) {
    switch(process.argv[i]) {
      case '--version':
        version = process.argv[++i]
        break;
      case '--from':
        from = process.argv[++i]
        break;
      case '--to':
        to = process.argv[++i]
        break;
    }
  }
  if (!from) {
    const branch = await run(`git rev-parse --abbrev-ref HEAD`);
    await run(`git pull origin ${branch} --tags`);
    const tags = (await run(`git tag --merged ${branch} --sort=-committerdate`));
    from = tags.split('\n')[0];
  }
  flowVersion = (await run('grep /flow.version pom.xml')).replace(/.*>(.*)<.*/, '$1');
  pomVersion = (await run('grep /version pom.xml')).split('\n')[0].replace(/.*>(.*)<.*/, '$1')
  !to && (to = 'HEAD');
  computeVersion();
  !version && (version = pomVersion)
}

// Parse git log string and return an array of parsed commits as a JS object.
function parseLog(log) {
  let commits = [];
  let commit, pos, result;
  log.split('\n').forEach(line => {
    pos != 'head' && (line = line.replace(/^    /, ''));
    switch(pos) {
      case 'head':
        if (!line) {
          pos = 'title';
          break;
        }
        result = /^(\w+): +(.+)$/.exec(line);
        if (result) {
          commit.head[result[1]] = result[2];
          break;
        }
        break;
      case 'title':
        if (!line) {
          pos = 'body';
          break;
        }
        result = /^(\w+)(!?): +(.*)$/.exec(line);
        if (result) {
          commit.type = result[1].toLowerCase();
          commit.breaking = !!result[2];
          commit.isBreaking = commit.breaking;
          commit.skip = !commit.breaking && !/(feat|fix|perf)/.test(commit.type);
          commit.isIncluded = !commit.skip;
          commit.title += result[3];
        } else {
          commit.title += line;
        }
        break;
      case 'body':
        result = /^([A-Z][\w-]+): +(.*)$/.exec(line);
        if (result) {
          let k = result[1].toLowerCase();
          if (/(fixes|fix|related-to|connected-to)/i.test(k)) {
            commit.footers.fixes = commit.footers.fixes || []
            commit.footers.fixes.push(...result[2].split(/[, ]+/));
          } else {
            commit.footers[k] = commit.footers[k] || []
            commit.footers[k].push(result[2]);
          }
          break;
        }
      default:
        result = /^commit (.+)$/.exec(line);
        if (result) {
          if (commit) {
            commit.body = commit.body.trim();
          }
          commit = {head: {}, title: '', body: '', isIncluded: false, isBreaking: false, components: [], footers: {fixes: []}, commits: []};
          commits.push(commit);
          commit.commit = result[1];
          pos = 'head';
        } else {
          if (line.startsWith('    ')) {
            commit.body += `${line}\n`;
          } else if (/^vaadin-.*-flow-parent/.test(line)){
            const wc = line.replace(/^vaadin-(.*)-flow-parent.*/, '$1');
            if (!commit.components.includes(wc)) {
              commit.components.push(wc);
            }
          }
        }
    }
  });
  return commits;
}

// Parse body part of a commit and extract squashed commits, nesting them
// to the commit JS object passed as argument
function parseBody(commit) {
  commit.originalBody = commit.body.trim();
  commit.body = '';
  const commitTitle = commit.title.replace(/ +\(.+\)$/, '').toLowerCase();
  let nestedCommit, result;
  commit.originalBody.split('\n').forEach(line => {
    line = line.trim();
    if (!line) {
      return;
    }
    result = /^\* (\w+)(!?): +(.+$)/.exec(line);
    if (result) {
      const nestedTitle = result[3].toLowerCase();
      if (commitTitle.includes(nestedTitle)) {
        return;
      }
      nestedCommit = {
        type: result[1].toLowerCase(),
        title: result[3],
        breaking: !!result[2],
        skip: !result[2] && !/(feat|fix|perf)/.test(result[1].toLowerCase()),
        footers: {
          "web-component": commit.footers['web-component']
        }
      }
      commit.isIncluded = commit.isIncluded && !nestedCommit.skip;
      commit.isBreaking = commit.isBreaking || nestedCommit.breaking;
      commit.commits.push(nestedCommit);
      return;
    }
    result = /^(fixes|fix|related-to|connected-to):? +(.+)$/i.exec(line);
    if (result) {
      commit.footers.fixes.push(...result[2].split(/[, ]+/));
      nestedCommit = undefined;
      return;
    }
    if (nestedCommit) {
      nestedCommit.body = `${nestedCommit.body} ${line}`.trim();
    } else {
      commit.body = `${commit.body} ${line}`.trim();
    }
  });
  if (commit.footers['web-component']) {
    let wc = [];
    commit.footers['web-component'].forEach(line => {
      wc = wc.concat(line.replace(/vaadin-/g, '').split(/[ ,]+/));
    });
    commit.footers['web-component'] = wc;
  }
}

// return absolute link to GH given a path
function createGHLink(path) {
  return `https://github.com/vaadin/${path}`;
}
// create link to low-components repo given a type or id
function createLink(type, id, wrap) {
  return id ? `${wrap ? '(' : ''}[${id}](${createGHLink(`vaadin-flow-component/${type}/${id})`)}${wrap ? ')' : ''}` : '';
}
// convert GH internal links to absolute links
function parseLinks(message) {
  const r = /^(?:([\da-f]+) )?(.*)(?:\((#\d+)\))?(.*)/.exec(message);
  return !r ? message :
    (r[1] ? createLink('commit', r[1]) + ' ' : '') + r[2][0].toUpperCase() + r[2].slice(1) + createLink('pull', r[3], true) + r[4] + '.';
}

// log web-component footers
function logWCFoot(c) {
  if (c.footers['web-component'][0]) {
    const component = `component${c.footers['web-component'][1] ? 's' : ''}`;
    console.log(`    **${component}**: ${c.footers['web-component'].map(k => '[' + k + '](' + createGHLink('vaadin-' + k) +')').join(', ')}`);
  }
}
// log fix footers
function logLinkFoot(c) {
  if (c.footers['fixes'][0]) {
    const fix = `fix${c.footers['fixes'][1] ? 'es' : ''}`;
    const links = c.footers['fixes'].reduce((prev, f) => {
      let link = f;
      if (/^#?\d/.test(f)) {
        f = f.replace(/^#/, '');
        link = `createLink('issues', f)`;
      } else if (/^(vaadin\/|https:\/\/github.com\/vaadin\/).*\d+$/.test(f)) {
        const n = f.replace(/^.*?(\d+)$/, '$1');
        f = f.replace(/^https:\/\/github.com\/vaadin\//, '');
        link = `[${n}](${createGHLink(f)}`;
      }
      return (prev ? `${prev}, ` : '') + link;
    }, '');
    console.log(`    **${fix}**: ${links}`);
  }
}

// log a commit for release notes
function logCommit(c) {
  console.log(`    - ${parseLinks(c.commit.substring(0, 7) + ' ' + c.title)}${c.body && '\n    ' + c.body} `);
  logLinkFoot(c);
}
// log a set of commits, and group by types
function logCommitsByType(commits) {
  if (!commits[0]) return;
  const byType = {};
  commits.forEach(commit => byType[commit.type] = [...(byType[commit.type] || []), commit]);
  Object.keys(byType).forEach(k => {
    console.log(`\n - **${keyName[k]}**:`);
    byType[k].forEach(c => logCommit(c));
  });
}
// log breaking changes in a commit
function logCommitByBreakingChange(commits){
  if (!commits[0]) return;
  console.log(`\n - **Breaking Changes**:`);
  commits.forEach(c => logCommit(c));
}
// log a set of commits, and group by web component
function logByComponent(commits) {
  const byComponent = {};
  commits.forEach(commit => {
    commit.components.forEach(name => {
      byComponent[name] = [...(byComponent[name] || []), commit];
    });
  });

  Object.keys(byComponent).forEach(k => {
    console.log(`\n#### Changes in &lt;vaadin-${k}&gt;`)
    logCommitsByType(byComponent[k]);
  });
}

// Output the release notes for the set of commits
function generateReleaseNotes(commits) {
  let featuredCommits = [];
  let breakingCommits = [];

  let includedCommits = commits.filter(c => c.isIncluded);

  includedCommits.forEach(commit => {
    breakingCommits = breakingCommits.concat([ ...(commit.breaking ? [commit] : []), ...(commit.commits.filter(c => c.breaking))]);
    featuredCommits = featuredCommits.concat([ ...(!commit.skip && !commit.breaking ? [commit] : []), ...(commit.commits.filter(c => !c.breaking && !c.skip))]);
  });

  console.log(`
## Vaadin Flow Components V${version}
This is a release of the Java integration for [Vaadin Components](https://github.com/vaadin/vaadin) to be used from the server side Java with [Vaadin Flow](https://vaadin.com/flow).
### Changes from [${from}](https://github.com/vaadin-flow-components/releases/tag/${from})
  `)

  logByComponent(includedCommits);

  console.log(`
### Compatibility
  - This release use Web Components listed in Vaadin Platform [${version}](https://github.com/vaadin/platform/releases/tag/${version})
  - Tested with Vaadin Flow version [${flowVersion}](https://github.com/vaadin/flow/releases/tag/${flowVersion})`);
}

async function main() {
  await getReleases();
  const gitLog = await run(`git log ${from}..${to} --name-only`);
  commits = parseLog(gitLog);
  commits.forEach(commit => parseBody(commit));
  generateReleaseNotes(commits);
  // console.log(JSON.stringify(commits, null, 1));
}

main();





