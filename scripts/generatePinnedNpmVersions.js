#!/usr/bin/env node

/**
 * Generates the versions file that pins the npm versions of the packages a
 * module ships, from the `@NpmPackage` annotations of that module.
 *
 * Flow reads every json file of `META-INF/VAADIN/versions/` on the classpath
 * and pins the packages they declare for npm/pnpm/bun, so the version the Java
 * code of a module was written against is the version applications resolve,
 * without the platform having to declare it. Generating the file from the
 * annotations keeps that version in one place, the annotation itself.
 *
 * The script is automatically run in the Maven build of every module declaring
 * `exec-maven-plugin`, which the root pom configures for it. A module that
 * declares no npm package has nothing to pin, and no file is written for it.
 *
 * A package declared by several modules is expected to have the same version
 * everywhere, which is what the annotation update keeps it at. Flow merges the
 * versions files of all modules, warning about a package whose version differs
 * between them and pinning the newest of the two.
 *
 * The packages are pinned for the Lit mode, as the web components of a Flow
 * component are what a Lit application installs; a React application gets them
 * from `@vaadin/react-components` instead.
 *
 * A package the React components do not bring has to be installed in either
 * mode, so it is written without a mode. Whether they bring it is decided by
 * their npm dependencies: everything the components declare is a dependency of
 * `@vaadin/react-components` or `@vaadin/react-components-pro`, except the
 * packages the modules name in the `all-modes` argument. Those are the
 * packages a Flow connector or theme depends on for itself rather than the web
 * component depending on them.
 *
 * Usage:
 *   node generatePinnedNpmVersions.js <source-dir> <output-file> [all-modes]
 *
 * Example
 *   node ../../scripts/generatePinnedNpmVersions.js src/main/java \
 *     target/classes/META-INF/VAADIN/versions/vaadin-map-flow-versions.json proj4
 */

const fs = require('fs');
const path = require('path');

if (process.argv.length < 4 || process.argv.length > 5) {
  console.error('Usage: node generatePinnedNpmVersions.js <source-dir> <output-file> [all-modes]');
  process.exit(1);
}

const sourceDir = process.argv[2];
const outputFile = process.argv[3];
// The packages of the module that no React wrapper brings, so that they are
// installed whichever mode an application uses. Empty for most modules, as the
// argument is unset unless the module declares such a package.
const allModes = (process.argv[4] || '').split(',').map((name) => name.trim()).filter(Boolean);
const mode = 'lit';

const ANNOTATION_REGEX = /@NpmPackage\s*\(\s*value\s*=\s*"([^"]+)"\s*,\s*version\s*=\s*"([^"]+)"\s*\)/g;

function javaFiles(dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const file = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      return javaFiles(file);
    }
    return entry.name.endsWith('.java') ? [file] : [];
  });
}

/**
 * Reads the npm packages the module declares, by package name. A package
 * declared by several components of the module, as the components of one web
 * component are, is expected to be declared with the same version everywhere.
 */
function readNpmPackages() {
  const packages = {};
  javaFiles(sourceDir).forEach((file) => {
    const content = fs.readFileSync(file, 'utf8');
    for (const [, npmName, version] of content.matchAll(ANNOTATION_REGEX)) {
      const declared = packages[npmName];
      if (declared && declared.version !== version) {
        console.error(
          `Conflicting versions for ${npmName}: '${declared.version}' in ${declared.file} and '${version}' in ${file}`
        );
        process.exit(1);
      }
      packages[npmName] = { version, file };
    }
  });
  return packages;
}

const npmPackages = fs.existsSync(sourceDir) ? readNpmPackages() : {};
const npmNames = Object.keys(npmPackages).sort();

// A module without Java sources or without a single annotation ships no npm
// package, so there is nothing for it to pin. Writing an empty versions file
// would only add a file that declares nothing to its jar.
if (npmNames.length === 0) {
  console.log(`No @NpmPackage annotation found in ${sourceDir}, so there is no version to pin`);
  fs.rmSync(outputFile, { force: true });
  process.exit(0);
}

// The name of an entry is only a label; Flow identifies a package by its
// npmName. Use the package name without the scope, as the platform does.
function entryName(npmName) {
  return npmName.replace(/^@[^/]+\//, '');
}

const unknownAllModes = allModes.filter((npmName) => !npmNames.includes(npmName));
if (unknownAllModes.length > 0) {
  console.error(`No @NpmPackage annotation for ${unknownAllModes.join(', ')} in ${sourceDir}, so there is no entry to leave the mode out of`);
  process.exit(1);
}

const versions = {};
npmNames.forEach((npmName) => {
  versions[entryName(npmName)] = {
    jsVersion: npmPackages[npmName].version,
    ...(allModes.includes(npmName) ? {} : { mode }),
    npmName
  };
});

const content = `${JSON.stringify(versions, null, 4)}\n`;

fs.mkdirSync(path.dirname(outputFile), { recursive: true });
fs.writeFileSync(outputFile, content);

const pinnedForMode = npmNames.filter((npmName) => !allModes.includes(npmName));
const pinned = [
  pinnedForMode.length > 0 && `${pinnedForMode.join(', ')} for mode ${mode}`,
  allModes.length > 0 && `${allModes.join(', ')} for every mode`
].filter(Boolean);

console.log(`Wrote ${outputFile} pinning ${pinned.join(' and ')}`);
