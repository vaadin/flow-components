#!/bin/sh

[ -z "$1" -o -z "$GITHUB_TOKEN" ] && exit 1

base=`curl -s -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/vaadin/vaadin-flow-components/pulls/$1 | jq -r .base.sha`

git fetch origin
folders=`git diff --name-status $base | awk '{print $2}' | cut -d '/' -f1 | sort -u`

modules=""
for i in $folders
do
  grep -q "<module>$i</module>" pom.xml && modules="$i,$modules"
done
[ -z "$modules" ] && exit

echo $folders 
mvn package --also-make-dependents -DskipTests -DskipITs -pl $modules
