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

tcLog "Show info (processors=$processors parallel=$parallel)"
type java && java -version
type mvn && mvn -version
type node && node --version
type npm && npm --version
type pnpm && pnpm --version
uname -a

cmd="npm install --silent --quiet --no-progress"
tcLog "Install NPM packages - $cmd"
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

cmd="node scripts/mergeITs.js "`echo $elements`
tcLog "Merge IT modules - $cmd"
$cmd || exit 1
[ -z "$TESTS_IN_PARALLEL" ] && TESTS_IN_PARALLEL=1
[ -n "$TBLICENSE" ] && args="$args -Dvaadin.testbench.developer.license=$TBLICENSE"
[ -n "$TBHUB" ] && args="$args -Dtest.use.hub=true -Dcom.vaadin.testbench.Parameters.hubHostname=$TBHUB"
[ -z "$FORK_COUNT" ] && FORK_COUNT="$processors"
if [ -n "$SAUCE_USER" ]
then
   test -n  "$SAUCE_ACCESS_KEY" || { echo "\$SAUCE_ACCESS_KEY needs to be defined to use Saucelabs" >&2 ; exit 1; }
   args="$args -P saucelabs -Dtest.use.hub=true -Dsauce.user=$SAUCE_USER -Dsauce.sauceAccessKey=$SAUCE_ACCESS_KEY"
fi
echo "$args"

if [ "$TBHUB" = "localhost" ]
then
    tcLog 'Installing docker image with standalone-chrome'
    trap "echo Terminating docker; docker stop standalone-chrome" EXIT
    docker pull selenium/standalone-chrome
    docker image prune -f
    docker run --name standalone-chrome --net=host --rm -d -v /dev/shm:/dev/shm  selenium/standalone-chrome
fi

args="$args -Dfailsafe.forkCount=$FORK_COUNT -Dfailsafe.rerunFailingTestsCount=2 -B -q"

if [ -n "$modules" ] && [ -z "$USE_MERGED_MODULE" ]
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
  cmd="mvn verify -Drun-it -Drelease "-Dcom.vaadin.testbench.Parameters.testsInParallel=$TESTS_IN_PARALLEL" $args -pl integration-tests"
  tcLog "Running merged ITs - mvn verify -Drun-it -Drelease -pl integration-tests ..."
  echo $cmd
  $cmd 2>&1 | egrep --line-buffered -v \
   'ProtocolHandshake|Detected dialect|multiple locations|setDesiredCapabilities|empty sauce.options|org.atmosphere|JettyWebAppContext@|Starting ChromeDrive|Only local|ChromeDriver was started|ChromeDriver safe|Ignoring update|Property update|\tat '
  [ ! -d integration-tests/target/failsafe-reports ] && exit 1

  ### Second try, Re-run only failed tests
  failed=`egrep '<<< ERROR|<<< FAILURE' integration-tests/target/failsafe-reports/*txt | perl -pe 's,.*/(.*).txt:.*,$1,g' | sort -u`
  nfailed=`echo "$failed" | wc -w`
  if [ "$nfailed" -gt 0 ]
  then
      tcLog "There were Failed Tests: $nfailed"
      echo "$failed"
      if [ "$nfailed" -le 15 ]
      then
        failed=`echo "$failed" | tr '\n' ','`
        cmd="$cmd -Dit.test=$failed"
        tcLog "Re-Running failed $nfailed tests ..."
        echo $cmd
        $cmd
        exit $?
      fi
      exit 1
  fi
  exit 0
fi
