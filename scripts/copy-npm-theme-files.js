#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// Check command line arguments
if (process.argv.length < 4) {
  console.error('Usage: node copy-npm-theme-files.js <path-to-theme-java-file> <css-file-name1> [css-file-name2] ...');
  process.exit(1);
}

const javaFilePath = process.argv[2];
const cssFileNames = process.argv.slice(3);
const projectDir = path.dirname(javaFilePath);

// Find the project root (where pom.xml is located)
let currentDir = projectDir;
while (currentDir !== path.dirname(currentDir)) {
  if (fs.existsSync(path.join(currentDir, 'pom.xml'))) {
    break;
  }
  currentDir = path.dirname(currentDir);
}

if (!fs.existsSync(path.join(currentDir, 'pom.xml'))) {
  console.error('Could not find project root with pom.xml');
  process.exit(1);
}

const projectRoot = currentDir;

// Read Java file and extract NPM package info
if (!fs.existsSync(javaFilePath)) {
  console.error(`Java file not found: ${javaFilePath}`);
  process.exit(1);
}

const javaContent = fs.readFileSync(javaFilePath, 'utf8');

// Extract all package names and versions using regex
const packageRegex = /@NpmPackage\s*\(\s*value\s*=\s*"([^"]+)"\s*,\s*version\s*=\s*"([^"]+)"/g;
const packages = [];
let match;

while ((match = packageRegex.exec(javaContent)) !== null) {
  packages.push({
    name: match[1],
    version: match[2]
  });
}

if (packages.length === 0) {
  console.error(`Could not extract NPM package information from ${javaFilePath}`);
  process.exit(1);
}

console.log(`Found ${packages.length} NPM package(s) to extract:`);
packages.forEach(pkg => {
  console.log(`  - ${pkg.name}@${pkg.version}`);
});

// Find the package that contains the CSS file
let themePackage = null;
for (const pkg of packages) {
  // For now, assume the CSS file is in one of the packages
  // We'll check which one actually has it after npm install
  themePackage = pkg;
  if (pkg.name.includes('styles') || pkg.name.includes('aura')) {
    // Prefer packages with 'styles' or 'aura' in the name
    break;
  }
}

// Create temporary directory for npm install
const tempDir = path.join(projectRoot, 'target', 'npm-workspace');
fs.mkdirSync(tempDir, { recursive: true });

// Create package.json with all dependencies plus PostCSS
const packageJson = {
  name: 'theme-extractor',
  version: '1.0.0',
  dependencies: {}
};

packages.forEach(pkg => {
  packageJson.dependencies[pkg.name] = pkg.version;
});

// Add PostCSS and plugins for minification
packageJson.dependencies['postcss'] = '^8.4.31';
packageJson.dependencies['postcss-cli'] = '^10.1.0';
packageJson.dependencies['cssnano'] = '^6.0.1';
packageJson.dependencies['postcss-import'] = '^15.1.0';
packageJson.dependencies['postcss-url'] = '^10.1.3';

fs.writeFileSync(
  path.join(tempDir, 'package.json'),
  JSON.stringify(packageJson, null, 2)
);

// Create PostCSS config
const postcssConfig = `
module.exports = {
    plugins: [
        require('postcss-import')(),
        require('postcss-url')({ 
            url: 'copy',
            assetsPath: '.',
            useHash: false
        }),
        require('cssnano')({
            preset: 'default',
        })
    ]
}
`;

fs.writeFileSync(
  path.join(tempDir, 'postcss.config.js'),
  postcssConfig
);

// Run npm install
console.log('Running npm install...');
execSync('npm install', {
  cwd: tempDir,
  stdio: 'inherit'
});

// Function to process a single CSS file
function processCssFile(cssFileName) {
  console.log(`\nProcessing ${cssFileName}...`);

  // Find the CSS file in the installed packages
  let cssSourcePath = null;
  let cssPackage = null;

  for (const pkg of packages) {
    const possiblePath = path.join(tempDir, 'node_modules', pkg.name, cssFileName);
    if (fs.existsSync(possiblePath)) {
      cssSourcePath = possiblePath;
      cssPackage = pkg;
      console.log(`Found ${cssFileName} in package ${pkg.name}`);
      break;
    }
  }

  if (!cssSourcePath) {
    console.error(`CSS file '${cssFileName}' not found in any of the installed packages`);
    process.exit(1);
  }

  // Create target directory
  const targetDir = path.join(projectRoot, 'target/classes/META-INF/resources', cssPackage.name);
  fs.mkdirSync(targetDir, { recursive: true });

  // Run PostCSS to minify the CSS
  const outputPath = path.join(targetDir, cssFileName);
  console.log(`Minifying ${cssFileName}...`);

  try {
    execSync(`npx postcss ${cssSourcePath} -o ${outputPath}`, {
      cwd: tempDir,
      stdio: 'inherit'
    });
    console.log(`Successfully minified and saved ${cssFileName} to ${outputPath}`);
  } catch (error) {
    console.error(`Error during minification of ${cssFileName}:`, error);
    process.exit(1);
  }
}

// Process each CSS file
cssFileNames.forEach(processCssFile);
