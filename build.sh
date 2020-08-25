#!/bin/bash 

processors=5

if [ -n "$1" ]
then
  for i in $*
  do
    case $i in
      [0-9]|[0-9][0-9])
        processors=$i
        ;;
      *)
        modules=vaadin-$i-flow-parent/vaadin-$i-flow-integration-tests,$modules
        elements="$elements $i"
       ;;
     esac
  done
fi

tcMsg() (
  { set +x; } 2> /dev/null
  echo "##teamcity[$1]"
)

# open a block in the TC tree output
tcLog() {
  [ -n "$inblock" ] && tcMsg "blockClosed name='$inblock'"
  inblock=$1
  tcMsg "blockOpened name='$inblock'"
}
# log in TC
tcStatus() {
  { set +x; } 2> /dev/null
  [ "$1" = "0" ] && status=SUCCESS || status=FAILURE
  tcMsg "buildStatus status='$status' text='$1'"
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
[ -n "$TBHUB" ] && TBHUB=localhost
[ -n "$TBLICENSE" ] && args="$args -Dvaadin.testbench.developer.license=$TBLICENSE"
[ -n "$TBHUB" ] && args="$args -Dtest.use.hub=true -Dcom.vaadin.testbench.Parameters.hubHostname=$TBHUB"
args="$args -Dfailsafe.forkCount=$processors -Dfailsafe.reuseForks=false"

### Run IT's in original modules
# if [ -n "$modules" ]
# then
#   tcLog "Running module ITs for $modules"
#   cmd="mvn clean verify $args -pl $modules"
#   echo $cmd
#   $cmd
# fi
### Run IT's in merged module
if [ "$TBHUB" = "localhost" ]
then
    tcLog 'Installing docker image with standalone-chrome'
    trap "echo Terminating docker; docker stop standalone-chrome" EXIT
    set -x
    docker pull selenium/standalone-chrome
    docker image prune -f
    docker run --name standalone-chrome --net=host --rm -d -v /dev/shm:/dev/shm  selenium/standalone-chrome
    set +x
fi
tcLog "Running merged ITs (processors=$processors)"
cmd="mvn clean verify -Drun-it -Drelease -Dcom.vaadin.testbench.Parameters.testsInParallel=1 $args -pl integration-tests"
echo $cmd
$cmd
