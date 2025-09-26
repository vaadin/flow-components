#!/usr/bin/env node

/**
 * Downloads the given NPM theme package with the version specified in the given Java theme class,
 * and copies its distribution files to the target directory to expose them as static resources in
 * a web application. The script is automatically run in the Maven build for the Aura and Lumo
 * theme modules.
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// Check command line arguments
if (process.argv.length !== 4) {
  console.error('Usage: node copy-theme-distribution.js <path-to-theme-class> <npm-theme-package>');
  process.exit(1);
}

const javaThemeClass = process.argv[2];
const npmThemePackage = process.argv[3];

// Read Java file and extract NPM package info
if (!fs.existsSync(javaThemeClass)) {
  console.error(`Theme class not found: ${javaThemeClass}`);
  process.exit(1);
}

const javaContent = fs.readFileSync(javaThemeClass, 'utf8');

// Find version from NpmPackage annotation for the specific package
const packageRegex = new RegExp(`@NpmPackage\\s*\\(\\s*value\\s*=\\s*"${npmThemePackage.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}"\\s*,\\s*version\\s*=\\s*"([^"]+)"`);
const match = javaContent.match(packageRegex);

if (!match) {
  console.error(`Could not extract NPM package version for ${npmThemePackage} from ${javaThemeClass}`);
  process.exit(1);
}

const npmThemePackageVersion = match[1];

console.log(`Extracting theme distribution for ${npmThemePackage}@${npmThemePackageVersion}`);

// Create temporary directory for npm install
const tempDir = path.join('target', 'npm-workspace');
fs.mkdirSync(tempDir, { recursive: true });

// Install the theme package using npm
const packageJson = {
  name: 'theme-extractor',
  version: '1.0.0',
  dependencies: {
    [npmThemePackage]: npmThemePackageVersion
  }
};

fs.writeFileSync(
  path.join(tempDir, 'package.json'),
  JSON.stringify(packageJson, null, 2)
);

execSync('npm install', {
  cwd: tempDir,
  stdio: 'inherit'
});

// Create target directory
const targetDir = path.join('target/classes/META-INF/resources', npmThemePackage);
fs.mkdirSync(targetDir, { recursive: true });

// Copy the distribution files
const sourceDir = path.join(tempDir, 'node_modules', npmThemePackage, 'dist');
if (!fs.existsSync(sourceDir)) {
  console.error(`Distribution directory not found: ${sourceDir}`);
  process.exit(1);
}

fs.cpSync(sourceDir, targetDir, { recursive: true });

console.log(`Copied theme distribution to ${targetDir}`);
