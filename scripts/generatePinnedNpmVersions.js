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
 * A package the React components bring says so itself, in the `includedIn`
 * field naming the npm package of the React components that brings it. A
 * React application installs that one instead of the packages it brings, so
 * it is the package that says which one covers it, rather than the React
 * components listing every package they cover.
 *
 * The module names the React components in the `included-in` argument, and
 * the packages of its own that they do not bring in `not-included`.
 *
 * Usage:
 *   node generatePinnedNpmVersions.js <source-dir> <output-file> \
 *     [included-in] [not-included]
 *
 * Example
 *   node ../../scripts/generatePinnedNpmVersions.js src/main/java \
 *     target/classes/META-INF/VAADIN/versions/vaadin-map-flow-versions.json \
 *     @vaadin/react-components-pro ol,proj4
 */

const fs = require('fs');
const path = require('path');

if (process.argv.length < 4 || process.argv.length > 6) {
  console.error('Usage: node generatePinnedNpmVersions.js <source-dir> <output-file> [included-in] [not-included]');
  process.exit(1);
}

const sourceDir = process.argv[2];
const outputFile = process.argv[3];
// The npm package of the React components bringing the packages of this
// module, empty for a module they do not cover, such as a theme.
const includedIn = (process.argv[4] || '').trim();
// The packages of this module that the React components do not bring, which
// an application installs whichever mode it uses.
const notIncluded = (process.argv[5] || '').split(',').map((name) => name.trim()).filter(Boolean);
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
function readNpmPackages(sources) {
  const packages = {};
  sources.forEach(({ file, content }) => {
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


// Read every source once: both the annotations and the React components come
// out of the same files.
const sources = fs.existsSync(sourceDir)
  ? javaFiles(sourceDir).map((file) => ({ file, content: fs.readFileSync(file, 'utf8') }))
  : [];

const npmPackages = readNpmPackages(sources);
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

const unknownNotIncluded = notIncluded.filter((npmName) => !npmNames.includes(npmName));
if (unknownNotIncluded.length > 0) {
  console.error(`No @NpmPackage annotation for ${unknownNotIncluded.join(', ')} in ${sourceDir}, so it cannot be left out of the React components`);
  process.exit(1);
}

const versions = {};
npmNames.forEach((npmName) => {
  const brought = includedIn && !notIncluded.includes(npmName);
  versions[entryName(npmName)] = {
    ...(brought ? { includedIn } : {}),
    jsVersion: npmPackages[npmName].version,
    mode,
    npmName
  };
});

const content = `${JSON.stringify(versions, null, 4)}\n`;

fs.mkdirSync(path.dirname(outputFile), { recursive: true });
fs.writeFileSync(outputFile, content);

const broughtNames = includedIn ? npmNames.filter((npmName) => !notIncluded.includes(npmName)) : [];

console.log(
  `Wrote ${outputFile} pinning ${npmNames.join(', ')} for mode ${mode}` +
    (broughtNames.length > 0 ? `, ${broughtNames.join(', ')} brought by ${includedIn}` : '')
);
