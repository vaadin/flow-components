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

saveFailed() {
  try=$1
  failed=`egrep '<<< ERROR|<<< FAILURE' integration-tests/target/failsafe-reports/*txt | perl -pe 's,.*/(.*).txt:.*,$1,g' | sort -u`
  if [ -n $failed ]
  then
    mkdir -p integration-tests/error-screenshots/$try
    mv integration-tests/error-screenshots/*.png integration-tests/error-screenshots/$try
    for i in $failed
    do
      cp integration-tests/target/failsafe-reports/$i.txt integration-tests/error-screenshots/$try
    done
  fi
}

tcLog "Show info (processors=$processors parallel=$parallel)"
echo $SHELL
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
    DOCKER_CONTAINER_NAME="selenium-container"
    [ -n "$SELENIUM_DOCKER_IMAGE" ]  || SELENIUM_DOCKER_IMAGE="selenium/standalone-chrome"
    tcLog "Installing docker image with "
    trap "echo Terminating docker; docker stop $DOCKER_CONTAINER_NAME" EXIT
    docker pull "$SELENIUM_DOCKER_IMAGE"
    docker image prune -f
    docker run --name "$DOCKER_CONTAINER_NAME" --net=host --rm -d -v /dev/shm:/dev/shm "$SELENIUM_DOCKER_IMAGE"
fi

args="$args -Dfailsafe.rerunFailingTestsCount=2 -B -q"

if [ -n "$modules" ] && [ -z "$USE_MERGED_MODULE" ]
then
  ### Run IT's in original modules
  cmd="mvn clean verify -Dfailsafe.forkCount=$FORK_COUNT $args -pl $modules"
  tcLog "Running module ITs - mvn clean verify -pl ..."
  echo $cmd
  $cmd
elif [ -z "$BUILD" ]
then
  mode="-Dfailsafe.forkCount=$FORK_COUNT -Dcom.vaadin.testbench.Parameters.testsInParallel=$TESTS_IN_PARALLEL"
  ### Run IT's in merged module
  cmd="mvn verify -B -Drun-it -Drelease $mode $args -pl integration-tests"
  tcLog "Running merged ITs - mvn verify -B -Drun-it -Drelease -pl integration-tests ..."
  cmd="mvn verify -Drun-it -Drelease "-Dcom.vaadin.testbench.Parameters.testsInParallel=$TESTS_IN_PARALLEL" $args -pl integration-tests"
  tcLog "Running merged ITs - mvn verify -Drun-it -Drelease -pl integration-tests ..."
  echo $cmd
  # set -o pipefail

  $cmd 2>&1 | (egrep --line-buffered -v \
   'ProtocolHandshake|Detected dialect|multiple locations|setDesiredCapabilities|empty sauce.options|org.atmosphere|JettyWebAppContext@|Starting ChromeDrive|Only local|ChromeDriver was started|ChromeDriver safe|Ignoring update|Property update|\tat ' \
   || true)
  error=$?

  [ ! -d integration-tests/target/failsafe-reports ] && exit 1
  saveFailed run-1

  nfailed=`echo "$failed" | wc -w`
  if [ "$nfailed" -gt 0 ]
  then
      tcLog "There were Failed Tests: $nfailed"
      echo "$failed"

      if [ "$nfailed" -le 15 ]
      then
        failed=`echo "$failed" | tr '\n' ','`
        mode="-Dfailsafe.forkCount=2 -Dcom.vaadin.testbench.Parameters.testsInParallel=3"
        cmd="mvn verify -B -Drun-it -Drelease $mode $args -pl integration-tests -Dit.test=$failed"
        tcLog "Re-Running failed $nfailed tests ..."
        echo $cmd
        TESTBENCH_REUSE_BROWSER=false $cmd
        error=$?
        saveFailed run-2
        exit $error
      fi
  fi
  exit $error
fi
