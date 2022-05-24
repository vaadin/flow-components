#!/bin/bash
set -o pipefail

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
   pre=`echo "$prereleases" | grep '<version>' | cut -d '>' -f2 |cut -d '<' -f1 | grep "^$base" | grep -v "SNAPSHOT" | egrep 'alpha|beta|rc' | tail -1`
   [ -z "$pre" ] && pre=`echo "$prereleases" | grep '<version>' | cut -d '>' -f2 |cut -d '<' -f1 | egrep 'alpha|beta|rc' | tail -1`
   [ -z "$pre" ] && pre="$2"
   expr "$pre" : ".*SNAPSHOT" >/dev/null && echo "Releases cannot depend on SNAPSHOT: $1 - $pre" && exit 1 || echo $pre
}

getPlatformVersion() {
  [ "$1" = vaadin-iron-list ] && name="iron-list" || name=$1

  echo "$versions" | jq -r ".core, .vaadin | .[\"$name\"]| .javaVersion" | grep -v null
}

getNextVersion() {
  [ -z "$1" ] && return
  prefix=`echo $1 | perl -pe 's/[0-9]+$//'`
  number=`echo $1 | perl -pe 's/.*([0-9]+)$/$1/'`
  number=`expr $number + 1` || exit 1
  echo $prefix$number
}

setPomVersion() {
  [ -z "$1" ] && return
  key=`echo $1 | tr - .`".version"
  echo "Setting $key=$2 in pom.xml"
  mvn -B -q -N versions:set-property -Dproperty=$key -DnewVersion=$2 || exit 1
}

### Check that version is given as a parameter and has a valid format
version=$1
! [[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+([\.-](alpha|beta|rc)[0-9]+)?$ ]] && echo Invalid version format: $version && exit 1
[[ $version =~ (alpha|beta|rc) ]] && profile=prerelease || profile=maven-central
pomVersion=`cat pom.xml | grep '<version>' | head -1 | cut -d '>' -f2 | cut -d '<' -f1`

### Extrat major.minor part from version
versionBase=`getBaseVersion $version`
pomBase=`getBaseVersion $pomVersion`

### Get the master branch version for components
masterPom=`curl -s "https://raw.githubusercontent.com/vaadin/flow-components/master/pom.xml"`
masterMajorMinor=`echo "$masterPom" | grep '<version>' | cut -d '>' -f2 |cut -d '<' -f1 | grep "^$base" | head -1 | cut -d '-' -f1`

### Load versions file for this platform release
branch=$versionBase
[ $branch = $masterMajorMinor ] && branch=master
versions=`curl -s "https://raw.githubusercontent.com/vaadin/platform/$branch/versions.json"`
[ $? != 0 ] && branch=master && versions=`curl -s "https://raw.githubusercontent.com/vaadin/platform/$branch/versions.json"`

### Check that current branch is valid for the version to release
[ $branch != master -a "$versionBase" != "$pomBase" ] && echo "Incorrect pomVersion=$pomVersion for version=$version" && exit 1

### Compute flow version
flow=`getPlatformVersion flow`
flow=`getLatest flow $flow`

## Modify poms with the versions to release
echo "Setting version=$version to vaadin-flow-components"
mvn -B -q versions:set -DnewVersion=$version || exit 1
setPomVersion flow $flow || exit 1

## Compute modules to build and deploy
### collect the component modules from the root pom, remove the shared parent from the list, as it will be added separately (line 109) 
modules=`grep '<module>' pom.xml | grep parent | grep -v shared-parent | cut -d '>' -f2 | cut -d '<' -f1 | perl -pe 's,-flow-parent,,g'`

if [ "$versionBase" = 14.4 -o "$versionBase" = 17.0 ]
then
for i in $modules
  do
    modVersion=`getPlatformVersion $i`
    setPomVersion $i $modVersion
  done
  git pull origin $branch --tags --ff-only --quiet
  lastTag=`git tag --merged $branch --sort=-committerdate | head -1`
  if [ -n "$lastTag" ]
  then
    shift
    ## allow setting modules to build from command line or via env var
    [ -n "$modified" ] || modified=$*
    ## otherwise utilise git history to figure out modified modules
    [ -n "$modified" ] || modified=`git log $lastTag..HEAD --name-only | egrep '\-flow/|-testbench/|parent/pom.xml' | sed -e 's,-flow-parent.*,,g' | sort -u`
    modules="$modified"
    echo "Increasing version of the modified modules since last release $lastTag"
    for i in $modules
    do
      modVersion=`getPlatformVersion $i`
      nextVersion=`getNextVersion $modVersion`
      [ "$modVersion" = "$nextVersion" ] && echo Error Increasing version && exit 1
      setPomVersion $i $nextVersion
    done
  fi
fi

echo "Deploying "`echo $modules | wc -w`" Modules from branch=$branch to profile=$profile"
## '.' points to the root project, 'vaadin-flow-components-shared-parent' has the common dependencies for components
build=.,vaadin-flow-components-shared-parent,vaadin-flow-components-shared-parent/vaadin-flow-components-base,vaadin-flow-components-shared-parent/vaadin-flow-components-test-util
for i in $modules
do
  if [ -d "$i" -o -d "$i-flow-parent" ]
  then
    build="$build,$i-flow-parent,$i-flow-parent/$i-flow"
    [ -d "$i-flow-parent/$i-testbench" ] && build="$build,$i-flow-parent/$i-testbench"
    [ -d "$i-flow-parent/$i-flow-demo" ] && build="$build,$i-flow-parent/$i-flow-demo"
    [ -d "$i-flow-parent/$i-flow-svg-generator" ] && build="$build,$i-flow-parent/$i-flow-svg-generator"
    [ -d "$i-flow-parent/$i-flow-client" ] && build="$build,$i-flow-parent/$i-flow-client"
  fi
done

## Inform TC about computed parameters
echo "##teamcity[setParameter name='components.branch' value='$branch']"
echo "##teamcity[setParameter name='maven.profile' value='$profile']"
echo "##teamcity[setParameter name='flow.version' value='$flow']"
echo "##teamcity[setParameter name='build.modules' value='$build']"
echo "##teamcity[setParameter name='vaadin.flow.components.shared.parent.version' value='$version']"
