#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// Check command line arguments
if (process.argv.length < 3) {
    console.error('Usage: node copy-npm-theme-files.js <path-to-theme-java-file>');
    process.exit(1);
}

const javaFilePath = process.argv[2];
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

// Create temporary directory for npm install
const tempDir = path.join(projectRoot, 'target', 'npm-workspace');
fs.mkdirSync(tempDir, { recursive: true });

// Create package.json with all dependencies
const packageJson = {
    name: 'theme-extractor',
    version: '1.0.0',
    dependencies: {}
};

packages.forEach(pkg => {
    packageJson.dependencies[pkg.name] = pkg.version;
});

fs.writeFileSync(
    path.join(tempDir, 'package.json'),
    JSON.stringify(packageJson, null, 2)
);

// Run npm install
console.log('Running npm install...');
execSync('npm install', { 
    cwd: tempDir,
    stdio: 'inherit'
});

// Copy files recursively
function copyRecursive(src, dest) {
    const stats = fs.statSync(src);
    
    if (stats.isDirectory()) {
        fs.mkdirSync(dest, { recursive: true });
        const files = fs.readdirSync(src);
        
        files.forEach(file => {
            copyRecursive(path.join(src, file), path.join(dest, file));
        });
    } else {
        fs.copyFileSync(src, dest);
    }
}

// Copy files from node_modules to target/classes for each package
packages.forEach(pkg => {
    const sourceDir = path.join(tempDir, 'node_modules', pkg.name);
    const targetDir = path.join(projectRoot, 'target/classes/META-INF/resources', pkg.name);

    if (!fs.existsSync(sourceDir)) {
        console.warn(`Warning: Package ${pkg.name} not found in node_modules, skipping...`);
        return;
    }

    // Create target directory
    fs.mkdirSync(targetDir, { recursive: true });

    console.log(`Copying ${pkg.name} from ${sourceDir} to ${targetDir}`);
    copyRecursive(sourceDir, targetDir);
});

console.log('Successfully extracted all NPM packages to resources');