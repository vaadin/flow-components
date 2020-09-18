#!/bin/bash

# return the major.minor numbers of a version
getBaseVersion() {
   echo $1 | tr - . | cut -d . -f1,2;
}
# get latest released version of an artifact by checking releases or prereleases repos
getLatest() {
   base=`getBaseVersion $2`
   releases=`curl -s "https://maven.vaadin.com/vaadin-prereleases/com/vaadin/$1/maven-metadata.xml"`
   prereleases=`curl -s "https://repo.maven.apache.org/maven2/com/vaadin/$1/maven-metadata.xml"`

   stable=`echo "$releases" | grep '<version>' | cut -d '>' -f2 |cut -d '<' -f1 | grep "^$base" | tail -1`
   [ -n "$stable" ] && echo $stable && return
   pre=`echo "$prereleases" | grep '<version>' | cut -d '>' -f2 |cut -d '<' -f1 | grep "^$base" | grep 'alpha|beta|rc'`
   [ -n "$pre" ] && echo $pre || echo $1
}

### Check that version comes as a parameter and has a valid format
version=$1
! [[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+([\.-](alpha|beta|rc)[0-9]+)?$ ]] && echo Invalid version format: $version && exit 1
[[ $version =~ (alpha|beta|rc) ]] && profile=prerelease || profile=maven-central

### Compute platform branch based on version
branch=`getBaseVersion $version`
[[ $branch =~ ^(18|0) ]] && branch=master

### Load versions file for this platform release
versions=`curl -s "https://raw.githubusercontent.com/vaadin/platform/$branch/versions.json"`

## Compute flow version
flow=`echo "$versions" | jq -r .core.flow.javaVersion`
flow=`getLatest flow $flow`

## Modify poms with the versions to release
echo "Setting version=$version to vaadin-flow-components"
mvn -B -q versions:set -DnewVersion=$version
echo "Setting flow.version=$flow in vaadin-flow-components"
mvn -B -q -N versions:set-property -Dproperty=flow.version -DnewVersion=$flow

## Compure modules to build and deploy
modules=`grep '<module>' pom.xml | grep parent | cut -d '>' -f2 |cut -d '<' -f1 | perl -pe 's,-flow-parent,,g'`
build=vaadin-flow-components-shared
for i in $modules
do
  build=$build,$i-flow-parent/$i-flow,$i-flow-parent/$i-testbench,$i-flow-parent/$i-flow-demo
done

## Inform TC about computed parameters
echo "##teamcity[setParameter name='components.branch' value='$branch']"
echo "##teamcity[setParameter name='maven.profile' value='$profile']"
echo "##teamcity[setParameter name='flow.version' value='$flow']"
echo "##teamcity[setParameter name='build.modules' value='$build']"





