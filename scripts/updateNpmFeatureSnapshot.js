#!/usr/bin/env node
/**
 * Repoint every @vaadin NpmPackage annotation at a web-components *feature
 * snapshot* published to npm by the web-components `publishFeatureSnapshot.sh`
 * script.
 *
 * That script publishes all monorepo packages under a mutable dist-tag
 * `dev-<feature>` (backed by an immutable `<base>-dev.<hash>` version). This
 * script writes the dist-tag itself into the annotation, so every flow build
 * resolves the latest build behind that tag, like a Java `-SNAPSHOT` dependency
 * - re-publishing the snapshot needs no change here.
 *
 * Only packages that actually publish the tag are updated; packages outside the
 * web-components monorepo (which never got the snapshot) are left untouched.
 *
 * Usage:
 *   ./scripts/updateNpmFeatureSnapshot.js disable-dates
 *   ./scripts/updateNpmFeatureSnapshot.js --tag dev-disable-dates
 *
 * Revert with the regular version bump once the feature is released:
 *   ./scripts/updateNpmVer.js
 */

const { execFileSync, execSync } = require('child_process');
const fs = require('fs');

function featureToTag(feature) {
  // Same normalization as publishFeatureSnapshot.sh in the web-components repo.
  const name = feature
    .toLowerCase()
    .replace(/[^a-z0-9-]+/g, '-')
    .replace(/^-+|-+$/g, '');
  if (!name) {
    console.error(`Feature name '${feature}' has no usable characters`);
    process.exit(1);
  }
  return `dev-${name}`;
}

function parseArgs() {
  const args = process.argv.slice(2);
  let feature;
  let tag;
  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--tag') {
      tag = args[i + 1];
      i += 1;
    } else if (!feature) {
      feature = args[i];
    }
  }
  if (tag) {
    return tag;
  }
  if (!feature) {
    console.error('Usage: updateNpmFeatureSnapshot.js <feature-name>   (or --tag <dist-tag>)');
    process.exit(1);
  }
  return featureToTag(feature);
}

const ANNOTATION = /@NpmPackage\(value = "(@vaadin\/[^"]+)", version = "([^"]+)"\)/g;

function javaFilesWithAnnotations() {
  const out = execSync('grep -rl --include=*.java -e \'@NpmPackage(value = "@vaadin/\' .', {
    encoding: 'utf8',
    maxBuffer: 64 * 1024 * 1024,
  });
  return out.split('\n').filter(Boolean);
}

// Whether the package publishes the given dist-tag, cached per package so the
// many repeated annotations only trigger one `npm view` each.
const publishesTag = {};
function tagExists(pkg, tag) {
  if (!(pkg in publishesTag)) {
    try {
      const version = execFileSync('npm', ['view', `${pkg}@${tag}`, 'version'], {
        encoding: 'utf8',
        stdio: ['ignore', 'pipe', 'ignore'],
      }).trim();
      publishesTag[pkg] = version.length > 0;
    } catch {
      // No such package/tag on the registry -> not part of this snapshot.
      publishesTag[pkg] = false;
    }
  }
  return publishesTag[pkg];
}

function main() {
  const tag = parseArgs();
  console.log(`Repointing @vaadin NpmPackage annotations to the '${tag}' snapshot tag.\n`);

  const files = javaFilesWithAnnotations();
  const skipped = new Set();
  let matched = 0;
  let changed = 0;

  for (const file of files) {
    const src = fs.readFileSync(file, 'utf8');
    const out = src.replace(ANNOTATION, (match, pkg, version) => {
      matched += 1;
      if (!tagExists(pkg, tag)) {
        skipped.add(pkg);
        return match;
      }
      if (version === tag) {
        return match;
      }
      changed += 1;
      return `@NpmPackage(value = "${pkg}", version = "${tag}")`;
    });
    if (out !== src) {
      fs.writeFileSync(file, out);
    }
  }

  [...skipped].sort().forEach((pkg) => {
    console.log('\x1b[90m%s\x1b[0m', `skip ${pkg}: no '${tag}' version published`);
  });

  const updatedPackages = Object.keys(publishesTag).filter((pkg) => publishesTag[pkg]).length;
  if (updatedPackages === 0) {
    console.error('\n\x1b[31m%s\x1b[0m', `No @vaadin package publishes the '${tag}' dist-tag - nothing updated.`);
    process.exit(1);
  }
  console.log(
    '\n\x1b[32m%s\x1b[0m',
    `Pointed ${matched} annotation(s) across ${updatedPackages} package(s) at '${tag}' (${changed} changed).`,
  );
}

main();
