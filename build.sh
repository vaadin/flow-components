#!/bin/sh

if [ -n "$1" ]
then
  for i in `echo $*`
  do
    modules=vaadin-$i-flow-parent/vaadin-$i-flow-integration-tests,$modules
    elements="$elements $i"
  done
# else
#   modules="
#   text-field button checkbox combo-box context-menu date-picker date-time-picker
#   dialog form-layout icons radio-button split-layout list-box menu-bar notification
#   ordered-layout progress-bar tabs select time-picker upload iron-list grid
#   "
#   # charts accordion app-layout board confirm-dialog cookie-consent crud
#   # custom-field details grid-pro login rich-text-editor
fi

## TODO: in local 3 is ok, but in TC something fails
processors=3

# open a block in the TC tree output
tcLog() {
  [ -n "$inblock" ] && echo "##teamcity[blockClosed name='$inblock']"
  inblock=$1
  echo "##teamcity[blockOpened name='$inblock']"
}
# log in TC
tcStatus() {
  [ "$1" = "0" ] && status=SUCCESS || status=FAILURE
  echo "##teamcity[buildStatus status='$status' text='$1']"
}

tcLog 'Show info'
java -version
mvn -version
node --version
npm --version
uname -a

tcLog 'Install NPM packages'
npm install --silent --quiet --no-progress

tcLog 'Merge IT modules'
scripts/mergeITs.js `echo $elements`

tcLog 'Compiling and Installing flow components'
cmd="mvn clean install -Drelease -DskipTests -T $processors -q"
echo $cmd
$cmd

args="-B -Dvaadin.pnpm.enable=true"
[ -n "$TBLICENSE" ] && args="$args -Dvaadin.testbench.developer.license=$TBLICENSE"
[ -n "$TBHUB" ] && args="$args -Dtest.use.hub=true -Dcom.vaadin.testbench.Parameters.hubHostname=$TBHUB"
args="$args -Dfailsafe.forkCount=$processors"

### Run IT's in original modules
# if [ -n "$modules" ]
# then
#   tcLog "Running module ITs for $modules"
#   cmd="mvn clean verify $args -pl $modules"
#   echo $cmd
#   $cmd
# fi

### Run IT's in merged module
tcLog 'Running merged ITs'
cmd="mvn clean verify -Drun-it -Drelease -Dtestbench.testsInParalel=1 $args -pl integration-tests"
echo $cmd
$cmd
