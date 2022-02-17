
#!/usr/bin/env bash

# Update poms of all modules from templates.
# Usage:
#   ./scripts/updateJavaPOMs.sh
#

set -e
pom='pom.xml'
mods=`grep '<module>' $pom  | egrep -v 'integration-tests|shared|helpers' | cut -d ">" -f2 | cut -d "<" -f1`

for i in $mods
do
  node scripts/updateJavaPOMs.js $i
done
