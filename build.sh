#!/bin/bash

processors=3
parallel=1

if [ -n "$1" ]
then
  for i in $*
  do
    case $i in
      processors=*)
        processors=`echo $i | cut -d = -f2`;;
      parallel=*)
        parallel=`echo $i | cut -d = -f2`;;
      *)
        modules=vaadin-$i-flow-parent/vaadin-$i-flow-integration-tests,$modules
        elements="$elements $i"
       ;;
     esac
  done
fi
echo "Using processors=$processors parallel=$parallel"

tcMsg() (
  { set +x; } 2> /dev/null
  echo "##teamcity[$1]"
)
# open a block in the TC tree output
tcLog() {
  [ -n "$inblock" ] && tcMsg "blockClosed name='$inblock'"
  inblock="$1"
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

cmd="npm install --silent --quiet --no-progress"
tcLog "Install NPM packages - $cmd"
$cmd

cmd="node scripts/mergeITs.js "`echo $elements`
tcLog "Merge IT modules - $cmd"
$cmd || exit 1

cmd="mvn install -Drelease -B -q -T C$processors"
tcLog "Unit-Testing and Installing flow components - $cmd"
$cmd
if [ $? != 0 ]
then
  tcLog "Unit-Testing and Installing flow components (2nd try) - $cmd"
  sleep 30
  $cmd || exit 1
fi

[ -n "$TBLICENSE" ] && args="$args -Dvaadin.testbench.developer.license=$TBLICENSE"
[ -n "$TBHUB" ] && args="$args -Dtest.use.hub=true -Dcom.vaadin.testbench.Parameters.hubHostname=$TBHUB"

if [ "$TBHUB" = "localhost" ]
then
    tcLog 'Installing docker image with standalone-chrome'
    trap "echo Terminating docker; docker stop standalone-chrome" EXIT
    docker pull selenium/standalone-chrome
    docker image prune -f
    docker run --name standalone-chrome --net=host --rm -d -v /dev/shm:/dev/shm  selenium/standalone-chrome
fi

if [ -n "$modules" ]
then
  ### Run IT's in original modules
  cmd="mvn clean verify $args -pl $modules"
  tcLog "Running module ITs - mvn clean verify -pl ..."
  echo $cmd
  $cmd
elif [ -z "$BUILD" ]
then
  args="$args -Dfailsafe.forkCount=$processors -Dcom.vaadin.testbench.Parameters.testsInParallel=$parallel"
  ### Run IT's in merged module
  cmd="mvn verify  -Drun-it -Drelease $args -pl integration-tests"
  tcLog "Running merged ITs - mvn verify -B -Drun-it -Drelease -pl integration-tests ..."
  echo $cmd
  $cmd 2>&1 | egrep -v 'ProtocolHandshake|Detected dialect|multiple locations|setDesiredCapabilities|empty sauce.options|org.atmosphere|JettyWebAppContext@|Starting ChromeDrive|Only local|ChromeDriver was started|ChromeDriver safe|Ignoring update|Property update'

  ### Second try, Re-run only failed tests
  failed=`egrep '<<< ERROR|<<< FAILURE' integration-tests/target/failsafe-reports/*txt | perl -pe 's,.*/(.*).txt:.*,$1,g' | sort -u | tr '\n' ','`
  if [ -n "$failed" ]
  then
      cmd="mvn verify -Drun-it -Drelease -pl integration-tests -Dit.test=$failed"
      tcLog "Re-Running failed tests - mvn verify -Drun-it -Drelease -pl integration-tests -Dit.test= ..."
      echo $cmd
      $cmd
      exit $?
  fi
  exit 0
fi
