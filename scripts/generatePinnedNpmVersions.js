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
 * The class declaring the packages says it with `@ReactComponents`, next to
 * the `@NpmPackage` annotations it applies to.
 *
 * Usage:
 *   node generatePinnedNpmVersions.js <source-dir> <output-file>
 *
 * Example
 *   node ../../scripts/generatePinnedNpmVersions.js src/main/java \
 *     target/classes/META-INF/VAADIN/versions/vaadin-map-flow-versions.json
 */

const fs = require('fs');
const path = require('path');

if (process.argv.length !== 4) {
  console.error('Usage: node generatePinnedNpmVersions.js <source-dir> <output-file>');
  process.exit(1);
}

const sourceDir = process.argv[2];
const outputFile = process.argv[3];

// The npm packages of the React components, by the name the annotation gives
// them. A class naming anything else would send a React application to a
// package that does not exist, which nothing downstream would notice.
const REACT_COMPONENTS = {
  'ReactComponents.CORE': '@vaadin/react-components',
  'ReactComponents.PRO': '@vaadin/react-components-pro'
};
const mode = 'lit';

const ANNOTATION_REGEX = /@NpmPackage\s*\(\s*value\s*=\s*"([^"]+)"\s*,\s*version\s*=\s*"([^"]+)"\s*\)/g;

// Matches `@ReactComponents`, with the React components it names and the
// packages it lists, both of which may be left out.
const REACT_COMPONENTS_REGEX = /@ReactComponents\b\s*(?:\(([\s\S]*?)\))?/;


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


// Read the sources once, for the annotations of all of them.
const sources = fs.existsSync(sourceDir)
  ? javaFiles(sourceDir).map((file) => ({ file, content: fs.readFileSync(file, 'utf8') }))
  : [];

/**
 * Reads what a class says the React components bring, from its
 * `@ReactComponents` annotation: the React components it names, the core ones
 * by default, and the packages it lists, all of the ones the class declares by
 * default.
 */
function readReactComponents(file, content, declared) {
  const annotation = REACT_COMPONENTS_REGEX.exec(content);
  if (!annotation) {
    return {};
  }
  const args = annotation[1] || '';
  const named = /(ReactComponents\.\w+)/.exec(args);
  if (named && !REACT_COMPONENTS[named[1]]) {
    console.error(`${file} names ${named[1]}, which is not one of the React components ${Object.keys(REACT_COMPONENTS).join(' and ')}`);
    process.exit(1);
  }
  const reactComponents = named ? REACT_COMPONENTS[named[1]] : REACT_COMPONENTS['ReactComponents.CORE'];
  const listed = /packages\s*=\s*(?:\{([\s\S]*?)\}|("[^"]+"))/.exec(args);
  const packages = listed
    ? [...(listed[1] || listed[2]).matchAll(/"([^"]+)"/g)].map(([, name]) => name)
    : declared;
  const unknown = packages.filter((npmName) => !declared.includes(npmName));
  if (unknown.length > 0) {
    console.error(`${file} says the React components bring ${unknown.join(', ')}, which it does not declare with @NpmPackage`);
    process.exit(1);
  }
  return packages.reduce((brought, npmName) => {
    brought[npmName] = reactComponents;
    return brought;
  }, {});
}

const npmPackages = readNpmPackages(sources);

// What each package of the module says brings it, by package name.
const includedIn = sources.reduce((brought, { file, content }) => {
  const declared = [...content.matchAll(ANNOTATION_REGEX)].map(([, npmName]) => npmName);
  return declared.length === 0 ? brought : Object.assign(brought, readReactComponents(file, content, declared));
}, {});
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

const brought = npmNames.filter((npmName) => includedIn[npmName]);

const versions = {};
npmNames.forEach((npmName) => {
  versions[entryName(npmName)] = {
    ...(includedIn[npmName] ? { includedIn: includedIn[npmName] } : {}),
    jsVersion: npmPackages[npmName].version,
    mode,
    npmName
  };
});

const content = `${JSON.stringify(versions, null, 4)}\n`;

fs.mkdirSync(path.dirname(outputFile), { recursive: true });
fs.writeFileSync(outputFile, content);

console.log(
  `Wrote ${outputFile} pinning ${npmNames.join(', ')} for mode ${mode}` +
    (brought.length > 0 ? `, ${brought.map((npmName) => `${npmName} brought by ${includedIn[npmName]}`).join(', ')}` : '')
);
