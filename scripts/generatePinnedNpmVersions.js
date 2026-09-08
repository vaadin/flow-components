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
 * The script is automatically run in the Maven build of the modules shipping a
 * versions file.
 *
 * Usage:
 *   node generatePinnedNpmVersions.js <source-dir> <output-file> [mode]
 *
 * The mode is the Vaadin mode the packages apply to, `lit` by default, as the
 * web components of a Flow component are what a Lit application installs; a
 * React application gets them from `@vaadin/react-components` instead.
 *
 * Example
 *   node ../../scripts/generatePinnedNpmVersions.js src/main/java \
 *     target/classes/META-INF/VAADIN/versions/vaadin-text-field-versions.json
 */

const fs = require('fs');
const path = require('path');

if (process.argv.length < 4 || process.argv.length > 5) {
  console.error('Usage: node generatePinnedNpmVersions.js <source-dir> <output-file> [mode]');
  process.exit(1);
}

const sourceDir = process.argv[2];
const outputFile = process.argv[3];
const mode = process.argv[4] || 'lit';

const ANNOTATION_REGEX = /@NpmPackage\s*\(\s*value\s*=\s*"([^"]+)"\s*,\s*version\s*=\s*"([^"]+)"\s*\)/g;

if (!fs.existsSync(sourceDir)) {
  console.error(`Source directory not found: ${sourceDir}`);
  process.exit(1);
}

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

const npmPackages = readNpmPackages();
const npmNames = Object.keys(npmPackages).sort();

if (npmNames.length === 0) {
  console.error(`No @NpmPackage annotation found in ${sourceDir}, so there is no version to pin`);
  process.exit(1);
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

const content = `${JSON.stringify(versions, null, 4)}\n`;

// Leave the file alone when it already says this, so that a rebuild does not
// touch it needlessly
if (fs.existsSync(outputFile) && fs.readFileSync(outputFile, 'utf8') === content) {
  console.log(`Pinned npm versions of ${outputFile} are up to date, skipping`);
  process.exit(0);
}

fs.mkdirSync(path.dirname(outputFile), { recursive: true });
fs.writeFileSync(outputFile, content);

console.log(`Wrote ${outputFile} pinning ${npmNames.join(', ')} for mode ${mode}`);
