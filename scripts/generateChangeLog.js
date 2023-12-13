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
    const r = /^(.*)\b(\d+)\b$/.exec(from);
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
    json = await getPlatformVersions('master');
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
    return `**${component}: **${c.components.map(k => '[' + k + '](' + createGHLink('vaadin-' + k) +')').join(', ')}`;
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
  c.body && (log += `\n\n        _${c.body}_`);
  console.log(log);
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
      byType[k].forEach(c => logCommit(c));
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





