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
 * A module may also declare the React components themselves, in a
 * `REACT_COMPONENTS` map of a Java class, mapping each of their npm packages
 * to the packages it brings. Those are written for the React mode, with what
 * they bring as their `exclusions`, and at the version of the packages the
 * module declares, which the React components are released with.
 *
 * Usage:
 *   node generatePinnedNpmVersions.js <source-dir> <output-file>
 *
 * Example
 *   node ../../scripts/generatePinnedNpmVersions.js src/main/java \
 *     target/classes/META-INF/VAADIN/versions/vaadin-text-field-flow-versions.json
 */

const fs = require('fs');
const path = require('path');

if (process.argv.length !== 4) {
  console.error('Usage: node generatePinnedNpmVersions.js <source-dir> <output-file>');
  process.exit(1);
}

const sourceDir = process.argv[2];
const outputFile = process.argv[3];
const mode = 'lit';

const ANNOTATION_REGEX = /@NpmPackage\s*\(\s*value\s*=\s*"([^"]+)"\s*,\s*version\s*=\s*"([^"]+)"\s*\)/g;

// Matches the whole `static final Map<String, List<String>> REACT_COMPONENTS =
// Map.of(...)` declaration, and then each package of it with what it brings.
const REACT_COMPONENTS_REGEX = /\bREACT_COMPONENTS\s*=\s*Map\.of\(([\s\S]*?)\);/;
const REACT_PACKAGE_REGEX = /"([^"]+)"\s*,\s*List\.of\(([\s\S]*?)\)/g;

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

/**
 * Reads the React components a module declares and the packages each of them
 * brings, from the `REACT_COMPONENTS` map of one of its classes. A module
 * that declares none is the normal case.
 */
function readReactComponents() {
  const declarations = javaFiles(sourceDir)
    .map((file) => ({ file, match: REACT_COMPONENTS_REGEX.exec(fs.readFileSync(file, 'utf8')) }))
    .filter((declaration) => declaration.match);
  if (declarations.length === 0) {
    return {};
  }
  if (declarations.length > 1) {
    console.error(`More than one REACT_COMPONENTS declaration in ${sourceDir}: ${declarations.map((declaration) => declaration.file).join(', ')}`);
    process.exit(1);
  }
  const { file, match } = declarations[0];
  const reactComponents = {};
  for (const [, npmName, brought] of match[1].matchAll(REACT_PACKAGE_REGEX)) {
    reactComponents[npmName] = [...brought.matchAll(/"([^"]+)"/g)].map(([, name]) => name);
  }
  if (Object.keys(reactComponents).length === 0) {
    console.error(`The REACT_COMPONENTS of ${file} declares no package, so it cannot be read`);
    process.exit(1);
  }
  return reactComponents;
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

const versions = {};
npmNames.forEach((npmName) => {
  versions[entryName(npmName)] = {
    jsVersion: npmPackages[npmName].version,
    mode,
    npmName
  };
});

// The React components are released with the components they wrap, so they
// carry the version the module declares for its own packages rather than one
// written down a second time.
const reactComponents = readReactComponents();
const reactNames = Object.keys(reactComponents).sort();
if (reactNames.length > 0) {
  const versionsDeclared = [...new Set(npmNames.map((npmName) => npmPackages[npmName].version))];
  if (versionsDeclared.length !== 1) {
    console.error(
      `The React components take the version of the packages of ${sourceDir}, which declares several: ${versionsDeclared.join(', ')}`
    );
    process.exit(1);
  }
  reactNames.forEach((npmName) => {
    versions[entryName(npmName)] = {
      exclusions: reactComponents[npmName],
      jsVersion: versionsDeclared[0],
      mode: 'react',
      npmName
    };
  });
}

const content = `${JSON.stringify(versions, null, 4)}\n`;

fs.mkdirSync(path.dirname(outputFile), { recursive: true });
fs.writeFileSync(outputFile, content);

const pinned = [
  `${npmNames.join(', ')} for mode ${mode}`,
  reactNames.length > 0 ? `${reactNames.join(', ')} for mode react` : undefined
].filter(Boolean);

console.log(`Wrote ${outputFile} pinning ${pinned.join(' and ')}`);
