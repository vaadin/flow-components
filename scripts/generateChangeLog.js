#!/usr/bin/env node
/**
 * Create change-log.
 * Example
 *   ./scripts/generateChangeLog.js --from from_tag --to to_tag --version version --compact
 *
 * When --to is not given HEAD is selected.
 * When --from is not given latest tag is selected.
 * When --version is not given version is computed increasing from.
 * When --compact is set, output is not structured in component sections, default is false
 * Use --product to change product name, default is Flow Components
 */

const fs = require('fs');
const https = require("https");
const exec = require('util').promisify(require('child_process').exec);
let product = 'Flow Components'
let from, to, version, flowVersion, pomVersion, compact;

const keyName = {
  bfp: '[Warranty Fixes](https://vaadin.com/support/for-business#warranty)',
  break: 'Breaking Changes',
  feat: 'New Features',
  fix: 'Fixes',
  refactor: 'Code Refactoring',
  test: 'Tests',
  chore: 'Chore',
  ci: 'Continuous Integration',
}

async function run(cmd) {
  const { stdout, stderr } = await exec(cmd, {maxBuffer: 5000 * 1024});
  return stdout.trim();
}

async function getPlatformVersions(branch) {
  return new Promise(resolve => {
    https.get(`https://raw.githubusercontent.com/vaadin/platform/${branch}/versions.json`, res => {
      res.setEncoding("utf8");
      let body = "";
      res.on("data", data => {
        body += data;
      });
      res.on("end", () => {
        resolve(body);
      });
    });
  }).then(body => {
    return JSON.parse(body);
  });
}

function computeVersion() {
  if (to == 'HEAD') {
    const r = /^(.*?)(\d+)$/.exec(from);
    if (r) {
      version = r[1] + (parseInt(r[2]) + 1);
    }
  } else if (/^(.*)(\d+)$/.exec(to)) {
    version = to;
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
      case '--product':
        product = process.argv[++i]
        break;
      case '--compact':
        compact = true;
        break;
      }
  }
  if (!from) {
    const branch = await run(`git rev-parse --abbrev-ref HEAD`);
    await run(`git pull origin ${branch} --tags`);
    const tags = await run(`git tag --merged ${branch} --sort=-committerdate`);
    from = tags.split('\n')[0];
  }
  fs.readFileSync('pom.xml', 'utf8').split("\n").forEach(line => {
    let r;
    !pomVersion && (r = /<version>(.*)<\/version>/.exec(line)) && (pomVersion = r[1]);
  });

  const platform = pomVersion.replace(/(\.\d+|-SNAPSHOT)$/, '');
  let json;
  try {
    json = await getPlatformVersions(platform);
  } catch (error) {
    json = await getPlatformVersions('main');
  }
  flowVersion = json.core.flow.javaVersion;

  !to && (to = 'HEAD');
  computeVersion();
  !version && (version = pomVersion)
}

// Parse git log string and return an array of parsed commits as a JS object.
function parseLog(log) {
  let commits = [];
  let commit, pos, result;
  log.split('\n').forEach(line => {
    switch(pos) {
      case 'head':
        if (!line.trim()) {
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
        if (!line.trim()) {
          pos = 'body';
          break;
        }
        result = /^ +(\w+)(!?): +(.*)$/.exec(line);
        if (result) {
          commit.type = result[1].toLowerCase();
          commit.breaking = !!result[2];
          commit.isBreaking = commit.breaking;
          commit.skip = !commit.breaking && !/(feat|fix|perf)/.test(commit.type);
          commit.isIncluded = !commit.skip;
          commit.title += result[3];
          if (commit.type == 'chore' && /Update.*(NpmPackages|Webjar)/i.test(commit.title)) {
            commit.title = 'Increase Web-Component version';
            commit.skip = false;
            commit.isIncluded = true;
          }
        } else {
          commit.title += line;
        }
        break;
      case 'body':
        result = /^ +([A-Z][\w-]+): +(.*)$/.exec(line);
        if (result) {
          let k = result[1].toLowerCase();
          if (k == 'warranty') {
            commit.bfp = true;
          }
          if (/(fixes|fix|related-to|connected-to|warranty)/i.test(k)) {
            commit.footers.fixes = commit.footers.fixes || []
            commit.footers.fixes.push(...result[2].split(/[, ]+/).filter(s => /\d+/.test(s)));
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
          if (line.startsWith(' ')) {
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

// Lines that must never reach the release notes (template noise, AI refs)
function isNoise(line) {
  return /^- \[[ xX]\]/.test(line)
    || /^🤖/.test(line)
    || /generated with .*claude/i.test(line)
    || /i have read the contribution guide/i.test(line);
}
// A line starting a non-prose markdown block (list, table, code fence, quote)
function isBlockStart(line) {
  return /^[-*+]\s/.test(line) || /^\d+\.\s/.test(line)
    || /^\|/.test(line) || /^```/.test(line) || /^>/.test(line);
}
function isHeading(line) {
  return /^#{1,6}\s/.test(line);
}
// A standalone footer reference (e.g. "Fixes #123", "Part of #9489").
// Prose that merely starts with such a keyword (e.g. "Follow-up to #9333. When a
// Spreadsheet...") is not treated as a footer.
function isFooter(line) {
  const m = /^(fixes|closes|resolves|related to|connected to|part of|follow-up to|warranty)\b[:\s]*(.*)$/i.exec(line);
  if (!m) return false;
  const rest = m[2]
    .replace(/https?:\/\/\S+/g, '')
    .replace(/#?\d[\w/-]*/g, '')
    .replace(/[#.,;:]/g, '')
    .trim();
  return rest.length < 12;
}
function isSeparator(line) {
  return /^([-*_])\1{2,}$/.test(line);
}
// Whether any real content (prose, list, table, code, non-template heading)
// remains after the captured first paragraph
function hasMoreContent(lines, from) {
  for (let i = from; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!line || isNoise(line) || isFooter(line) || isSeparator(line)) continue;
    if (isHeading(line)) {
      if (/^#{1,6}\s*(type of change|checklist|how.*tested|note)\b/i.test(line)) continue;
      return true;
    }
    return true;
  }
  return false;
}
function isListItem(line) {
  return /^[-*+]\s+/.test(line) || /^\d+\.\s+/.test(line);
}
// Push a list item (lines[start]) plus its wrapped continuation lines into para.
// Returns the index of the line that stopped the capture.
function collectListItem(lines, start, para) {
  const m = /^[-*+]\s+(.*)$/.exec(lines[start].trim()) || /^\d+\.\s+(.*)$/.exec(lines[start].trim());
  para.push(m[1]);
  let j = start + 1;
  for (; j < lines.length; j++) {
    const next = lines[j].trim();
    if (!next || isBlockStart(next) || isHeading(next) || isFooter(next) || isNoise(next) || isSeparator(next)) {
      break;
    }
    para.push(next);
  }
  return j;
}
// Extract the first cleaned paragraph of the Description/Summary section.
// Returns { text, truncated } where truncated marks that more content was cut.
function extractDescription(rawBody) {
  const lines = rawBody.replace(/<!--[\s\S]*?-->/g, ' ').split('\n');
  let i = 0;
  const headingIdx = lines.findIndex(l => /^\s*#{1,6}\s*(description|summary)\b/i.test(l));
  if (headingIdx >= 0) {
    i = headingIdx + 1;
  }
  const para = [];
  let started = false, truncated = false;
  for (; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!started) {
      if (!line || isNoise(line) || isSeparator(line) || isHeading(line)) continue;
      if (isListItem(line)) {
        // First content is a list item: capture it plus its continuation
        truncated = hasMoreContent(lines, collectListItem(lines, i, para));
        break;
      }
      if (isBlockStart(line) || isFooter(line)) {
        truncated = hasMoreContent(lines, i);
        break;
      }
      started = true;
      para.push(line);
      continue;
    }
    if (!line || isHeading(line) || isBlockStart(line) || isFooter(line) || isSeparator(line) || isNoise(line)) {
      // Lead-in like "This PR:" followed by a list: pull in the first item
      if (isListItem(line) && /:$/.test(para[para.length - 1] || '')) {
        truncated = hasMoreContent(lines, collectListItem(lines, i, para));
        break;
      }
      truncated = hasMoreContent(lines, line && !isNoise(line) && !isSeparator(line) ? i : i + 1);
      break;
    }
    para.push(line);
  }
  return { text: para.join(' ').trim(), truncated };
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
    result = /^\* (?:(\w+)(!?): )?(.+$)/.exec(line);
    if (result) {
      if (!result[1]) {
        return;
      }
      const nestedTitle = result[3].toLowerCase();
      if (commitTitle.includes(nestedTitle)) {
        return;
      }
      nestedCommit = {
        type: result[1].toLowerCase(),
        title: result[3],
        breaking: !!result[2],
        skip: !result[2] && !/(feat|fix|perf)/.test(result[1].toLowerCase()),
        commit: commit.commit,
        body: '',
        footers: {
          "web-component": commit.footers['web-component']
        }
      }
      commit.isIncluded = commit.isIncluded || !nestedCommit.skip || nestedCommit.breaking;
      commit.isBreaking = commit.isBreaking || nestedCommit.breaking;
      commit.commits.push(nestedCommit);
      return;
    }
    result = /^(fixes|fix|related-to|connected-to|warranty):? +(.+)$/i.exec(line);
    if (result) {
      commit.footers.fixes.push(...result[2].split(/[, ]+/).filter(s => /\d/.test(s)));
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
  const desc = extractDescription(commit.originalBody);
  commit.descText = desc.text;
  commit.descTruncated = desc.truncated;
  commit.commits.forEach(c => {
    c.descText = extractDescription(c.body).text;
    c.descTruncated = false;
  });
}

// return absolute link to GH given a path
function createGHLink(path) {
  return `https://github.com/vaadin/${path}`;
}
// create link to low-components repo given a type or id
function createLink(type, id, char) {
  return id ? `[${char ? char : id}](${createGHLink(`flow-components/${type}/${id})`)}` : '';
}
// convert GH internal links to absolute links
function parseLinks(message) {
  message = message.trim();
  message = message.replace(/^([\da-f]+) /, `${createLink('commit', '$1', '⧉')} `);
  message = message.replace(/ *\(#(\d+)\)$/g, `. **PR:**${createLink('pull', '$1')}`);
  return message;
}

// return web-components affected by this commit
function getComponents(c) {
  if (c.components[0]) {
    const component = `Component${c.components[1] ? 's' : ''}`;
    return `**${component}:** ${c.components.map(k => '[' + k + '](' + createGHLink('vaadin-' + k) +')').join(', ')}`;
  }
}
// return ticket links for this commit
function getTickets(c) {
  if (c.footers['fixes'] && c.footers['fixes'][0]) {
    const fix = `fix${c.footers['fixes'][1] ? 'es' : ''}`;
    const ticket = `Ticket${c.footers['fixes'].length > 1 ? 's' : ''}`;
    const links = c.footers['fixes'].reduce((prev, f) => {
      let link = f;
      if (/^#?\d/.test(f)) {
        f = f.replace(/^#/, '');
        link = `${createLink('issues', f)}`;
      } else if (/^(vaadin\/|https:\/\/github.com\/vaadin\/).*\d+$/.test(f)) {
        const of = f;
        const n = f.replace(/^.*?(\d+)$/, '$1');
        f = f.replace(/^(https:\/\/github.com\/vaadin|vaadin)\//, '').replace('#', '/issues/');
        link = `[${n}](${createGHLink(f)})`;
      }
      return (prev ? `${prev}, ` : '') + link;
    }, '');
    return `**${ticket}:**${links}`;
  }
}

// log a commit for release notes
function logCommit(c, withComponents) {
  let log = '    - ' + parseLinks(c.commit.substring(0, 7) + ' ' + c.title[0].toUpperCase() + c.title.slice(1));;
  const tickets = getTickets(c);
  tickets && (log += `. ${tickets}`);
  if (compact) {
    const components = getComponents(c);
    components && (log += `. ${components}`);
  }
  if (c.descText) {
    let desc = c.descText;
    if (c.descTruncated) {
      const pr = (/\(#(\d+)\)/.exec(c.title) || [])[1];
      const url = createGHLink(`flow-components/${pr ? `pull/${pr}` : `commit/${c.commit}`}`);
      desc += ` [(more)](${url})`;
    }
    log += `\n\n        ${desc}`;
  }
  console.log(log);
}

// render a single consolidated bullet for all Web-Component version bumps
function logWebComponentBumps(commits) {
  const links = commits.map(c => createLink('commit', c.commit.substring(0, 7), '⧉')).join(' ');
  console.log(`    - Increase Web-Component version (${links})`);
}

// log a set of commits, and group by types
function logCommitsByType(commits) {
  if (!commits[0]) return;
  const byType = {};
  commits.forEach(commit => {
    const type = commit.bfp ? 'bfp': commit.breaking ? 'break' : commit.type;
    byType[type] = [...(byType[type] || []), commit];
  });
  Object.keys(keyName).forEach(k => {
    if (byType[k]) {
      console.log(`\n - **${keyName[k]}**:`);
      const bumps = byType[k].filter(c => c.title.includes('Increase Web-Component version'));
      const rest = byType[k].filter(c => !c.title.includes('Increase Web-Component version'));
      bumps.length && logWebComponentBumps(bumps);
      rest.forEach(c => logCommit(c));
    }
  });
}

// log a set of commits, and group by web component
function logByComponent(commits) {
  const byComponent = {};
  function addCommit(commit, components) {
    components.forEach(name => {
      byComponent[name] = [...(byComponent[name] || []), commit];
    });
  }
  commits.forEach(commit => {
    // Group the WC update commit to one category 
    if (commit.title.includes("Increase Web-Component version")){
      commit.components=["All Components"];
    }
    !commit.skip && addCommit(commit, commit.components);
    commit.commits.forEach(c => {
      !c.skip && addCommit(c, commit.components);
    });
  });

  Object.keys(byComponent).sort().forEach(k => {
    k.includes("All Components") ? 
    console.log(`\n#### Changes in \`All Components\``) : console.log(`\n#### Changes in \`vaadin-${k}-flow\``);
    logCommitsByType(byComponent[k]);
  });
}

// Output the release notes for the set of commits
function generateReleaseNotes(commits) {
  console.log(`
## Vaadin ${product} ${version}
This is a release of the Java integration for [Vaadin Components](https://github.com/vaadin/vaadin) to be used from the Java server side with [Vaadin Flow](https://vaadin.com/flow).
  `)

  const includedCommits = commits.filter(c => c.isIncluded);
  if (includedCommits.length) {
    console.log(`
### Changes in ${product} from [${from}](https://github.com/vaadin/flow-components/releases/tag/${from})
    `)
  } else {
    console.log(`
### There are no Changes in ${product} since [${from}](https://github.com/vaadin/flow-components/releases/tag/${from})
    `)
  }
  if (compact) {
    logCommitsByType(includedCommits);
  } else {
    logByComponent(includedCommits);
  }

  console.log(`
### Compatibility
  - This release use Web Components listed in Vaadin Platform [${version}](https://github.com/vaadin/platform/releases/tag/${version})
  - Tested with Vaadin Flow version [${flowVersion}](https://github.com/vaadin/flow/releases/tag/${flowVersion})`);
}


// MAIN
async function main() {
  await getReleases();
  const gitLog = await run(`git log ${from}..${to} --name-only`);
  commits = parseLog(gitLog);
  commits.forEach(commit => parseBody(commit));
  generateReleaseNotes(commits);
}

main();





