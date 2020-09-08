#!/usr/bin/env bash

grep -r @NpmPackage ../* >> info.json
node updateNpmVer.js 
rm info.json
