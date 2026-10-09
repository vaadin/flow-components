#!/usr/bin/env node

/**
 * Points the `@NpmPackage` annotations of the web components at a pkg.pr.new
 * preview build of vaadin/web-components. Committed on a branch, the change
 * makes its tests and its `snapshot build` snapshot use the web components of
 * that commit.
 *
 * A web component package is a `@vaadin/` package declared at the version of
 * `@vaadin/component-base`. Other npm packages, such as `date-fns`, are left
 * alone.
 *
 * The URL has to be the exact form the preview uses for the dependencies
 * between its own packages, with the full commit hash. Any other form makes
 * npm install a second copy of a package.
 *
 * A branch with preview URLs must not be merged. Revert the commit to return
 * to released versions: `updateNpmVer.js` only reads numeric versions.
 *
 * Usage:
 *   node scripts/useWebComponentsPreview.js <web-components-commit-sha>
 */

const fs = require('fs');
const { execFileSync } = require('child_process');

const PREVIEW_URL = 'https://pkg.pr.new/vaadin/web-components';
const ANNOTATION_REGEX = /@NpmPackage\(value = "(@vaadin\/[^"]+)", version = "([^"]+)"\)/g;

const sha = process.argv[2];
if (!/^[0-9a-f]{40}$/.test(sha || '')) {
  console.error('Usage: node scripts/useWebComponentsPreview.js <full 40 character commit sha>');
  process.exit(1);
}

const files = execFileSync('git', ['grep', '-l', '@NpmPackage(value = "@vaadin/', '--', '*/src/main/java/*.java'], {
  encoding: 'utf8'
})
  .split('\n')
  .filter(Boolean);

const baseVersion = files
  .flatMap((file) => [...fs.readFileSync(file, 'utf8').matchAll(ANNOTATION_REGEX)])
  .find(([, npmName]) => npmName === '@vaadin/component-base')?.[2];
if (!baseVersion) {
  console.error('No @NpmPackage annotation found for @vaadin/component-base');
  process.exit(1);
}

let count = 0;
files.forEach((file) => {
  const content = fs.readFileSync(file, 'utf8');
  const updated = content.replace(ANNOTATION_REGEX, (annotation, npmName, version) => {
    if (version !== baseVersion) {
      return annotation;
    }
    count += 1;
    return `@NpmPackage(value = "${npmName}", version = "${PREVIEW_URL}/${npmName}@${sha}")`;
  });
  if (updated !== content) {
    fs.writeFileSync(file, updated);
  }
});

console.log(`Pointed ${count} @NpmPackage annotations of version ${baseVersion} at ${PREVIEW_URL}/<package>@${sha}`);
