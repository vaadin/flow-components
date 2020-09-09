
#!/usr/bin/env bash

# Remove all component modules, checkout them from their
# origin branch in github, consolidate folders so as
# all of them follow the same pattern, and update their
# POMs to be aligned.
#
# Usage:
#   ./scripts/updateFromMaster.sh
#

set -e
pom='pom.xml'
mods=`grep '<module>' $pom  | grep -v '>integration-tests<' | grep -v shared | cut -d ">" -f2 | cut -d "<" -f1`

checkoutProject() {
  mod=$1
  branch=$2
  prj=`echo $mod | sed -e 's/-parent//'`
  # check that the branch exist otherwise branch=master
  curl --output /dev/null --silent --head --fail "https://github.com/vaadin/$prj/tree/$branch" || branch=master
  echo cloning $prj branch=$branch into $mod
  rm -rf $mod
  git clone -q git@github.com:vaadin/$prj.git --branch $branch $mod
  rm -rf $mod/.??*
}

renameModule() {
  mod=$1
  old=$2
  new=$3
  [ ! -d $mod/$old ] && return
  echo Renaming $mod/$old $mod/$new
  [ -d $mod/$new ] && rm -rf $mod/$new
  mv $mod/$old $mod/$new
  perl -pi -e "s,>$old<,>$new<,g" $mod/pom.xml $mod/$new/pom.xml
}

consolidateProject() {
  mod=$1
  prj=`echo $mod | sed -e 's/-parent//'`
  oldtb="$prj-testbench"
  newtb=`echo $oldtb | sed -e 's/-flow//'`
  renameModule $mod $oldtb $newtb
  newit="$prj-integration-tests"
  oldit=`echo $newit | sed -e 's/-flow//'`
  renameModule $mod $oldit $newit
}

consolidateCharts() {
  mod=$1
  prj=`echo $mod | sed -e 's/-parent//'`
  tb=`echo $prj | sed -e 's/-flow/-testbench/'`
  renameModule $mod addon $prj
  renameModule $mod examples $prj-demo
  perl -pi -e "s,>$prj-examples<,>$prj-demo<,g" $mod/*/pom.xml
  renameModule $mod integration-test $prj-integration-tests
  renameModule $mod testbench $tb
}

consolidatePoms() {
  node scripts/updateJavaPOMs.js $1
}

consolidateSources() {
  node scripts/updateSources.js $1
}

versions=`node scripts/getVersions.js`

for i in $mods
do
  branch=`echo "$versions" | grep $i | cut -d ":" -f2`
  checkoutProject $i $branch
  [ $i = 'vaadin-charts-flow-parent' ] && consolidateCharts $i || consolidateProject $i
  consolidatePoms $i
  consolidateSources $i
done
