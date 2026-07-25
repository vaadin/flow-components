#!/usr/bin/env node
/**
 * Repoint every @vaadin NpmPackage annotation at a web-components *feature
 * snapshot* published to npm by the web-components `publishFeatureSnapshot.sh`
 * script.
 *
 * That script publishes all monorepo packages under an immutable
 * `<base>-dev.<hash>` version. This script writes that exact version into the
 * annotation, pinning every flow build to that specific snapshot.
 *
 * Only packages that actually publish the version are updated; packages outside
 * the web-components monorepo (which never got the snapshot) are left untouched.
 *
 * Usage:
 *   ./scripts/updateNpmFeatureSnapshot.js 25.1.0-dev.a1b2c3d
 *
 * Revert with the regular version bump once the feature is released:
 *   ./scripts/updateNpmVer.js
 */

const { execFileSync, execSync } = require('child_process');
const fs = require('fs');

function parseArgs() {
  const args = process.argv.slice(2);
  const version = args[0];
  if (!version) {
    console.error('Usage: updateNpmFeatureSnapshot.js <npm-version>');
    process.exit(1);
  }
  return version;
}

const ANNOTATION = /@NpmPackage\(value = "(@vaadin\/[^"]+)", version = "([^"]+)"\)/g;

function javaFilesWithAnnotations() {
  const out = execSync('grep -rl --include=*.java -e \'@NpmPackage(value = "@vaadin/\' .', {
    encoding: 'utf8',
    maxBuffer: 64 * 1024 * 1024,
  });
  return out.split('\n').filter(Boolean);
}

// Whether the package publishes the given version, cached per package so the
// many repeated annotations only trigger one `npm view` each.
const publishesVersion = {};
function versionExists(pkg, version) {
  if (!(pkg in publishesVersion)) {
    try {
      const found = execFileSync('npm', ['view', `${pkg}@${version}`, 'version'], {
        encoding: 'utf8',
        stdio: ['ignore', 'pipe', 'ignore'],
      }).trim();
      publishesVersion[pkg] = found.length > 0;
    } catch {
      // No such package/version on the registry -> not part of this snapshot.
      publishesVersion[pkg] = false;
    }
  }
  return publishesVersion[pkg];
}

function main() {
  const version = parseArgs();
  console.log(`Repointing @vaadin NpmPackage annotations to the '${version}' snapshot version.\n`);

  const files = javaFilesWithAnnotations();
  const skipped = new Set();
  let matched = 0;
  let changed = 0;

  for (const file of files) {
    const src = fs.readFileSync(file, 'utf8');
    const out = src.replace(ANNOTATION, (match, pkg, current) => {
      matched += 1;
      if (!versionExists(pkg, version)) {
        skipped.add(pkg);
        return match;
      }
      if (current === version) {
        return match;
      }
      changed += 1;
      return `@NpmPackage(value = "${pkg}", version = "${version}")`;
    });
    if (out !== src) {
      fs.writeFileSync(file, out);
    }
  }

  [...skipped].sort().forEach((pkg) => {
    console.log('\x1b[90m%s\x1b[0m', `skip ${pkg}: no '${version}' version published`);
  });

  const updatedPackages = Object.keys(publishesVersion).filter((pkg) => publishesVersion[pkg]).length;
  if (updatedPackages === 0) {
    console.error('\n\x1b[31m%s\x1b[0m', `No @vaadin package publishes the '${version}' version - nothing updated.`);
    process.exit(1);
  }
  console.log(
    '\n\x1b[32m%s\x1b[0m',
    `Pointed ${matched} annotation(s) across ${updatedPackages} package(s) at '${version}' (${changed} changed).`,
  );
}

main();
