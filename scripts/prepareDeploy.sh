#!/bin/bash

# return the major.minor numbers of a version
getBaseVersion() {
   echo $1 | tr - . | cut -d . -f1,2;
}
# get latest released version of an artifact by checking maven repos
getLatest() {
   base=`getBaseVersion $2`
   releases=`curl -s "https://repo.maven.apache.org/maven2/com/vaadin/$1/maven-metadata.xml"`
   prereleases=`curl -s "https://maven.vaadin.com/vaadin-prereleases/com/vaadin/$1/maven-metadata.xml"`

   stable=`echo "$releases" | grep '<version>' | cut -d '>' -f2 |cut -d '<' -f1 | grep "^$base" | tail -1`
   [ -n "$stable" ] && echo $stable && return
   pre=`echo "$prereleases" | grep '<version>' | cut -d '>' -f2 |cut -d '<' -f1 | grep "^$base" | grep 'alpha|beta|rc'`
   [ -n "$pre" ] && echo $pre || echo "$2"
}

### Check that version is given as a parameter and has a valid format
version=$1
! [[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+([\.-](alpha|beta|rc)[0-9]+)?$ ]] && echo Invalid version format: $version && exit 1
[[ $version =~ (alpha|beta|rc) ]] && profile=prerelease || profile=maven-central
pomVersion=`cat pom.xml | grep '<version>' | head -1 | cut -d '>' -f2 | cut -d '<' -f1`

### Extrat major.minor part from version
versionBase=`getBaseVersion $version`
pomBase=`getBaseVersion $pomVersion`

### Compute platform branch based on version to release
[[ $versionBase =~ ^(18|0) ]] && branch=master || branch=$versionBase

### Check that current branch is valid for the version to release
[ $branch != master -a "$versionBase" != "$pomBase" ] && echo "Incorrect pomVersion=$pomVersion for version=$version" && exit 1

### Load versions file for the platform release matching version to release
versions=`curl -s "https://raw.githubusercontent.com/vaadin/platform/$branch/versions.json"`

## Compute flow version for the platform version
flow=`echo "$versions" | jq -r .core.flow.javaVersion`
flow=`getLatest flow $flow`

## Modify poms with the versions to release
echo "Setting version=$version to vaadin-flow-components"
mvn -B -q versions:set -DnewVersion=$version || exit 1
echo "Setting flow.version=$flow in vaadin-flow-components"
mvn -B -q -N versions:set-property -Dproperty=flow.version -DnewVersion=$flow || exit 1

## Compute modules to build and deploy
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





