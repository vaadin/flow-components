#!/bin/bash


if [ -n "$1" ]
then
  for i in $*
  do
    case $i in
      processors=*)
        FORK_COUNT=`echo $i | cut -d = -f2`;;
      parallel=*)
        TESTS_IN_PARALLEL=`echo $i | cut -d = -f2`;;
      *)
        modules=vaadin-$i-flow-parent/vaadin-$i-flow-integration-tests,$modules
        elements="$elements $i"
       ;;
     esac
  done
fi

tcMsg() (
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
  [ "$1" = "0" ] && status=SUCCESS || status=FAILURE
  [ "$1" = "0" ] && text="$3" || status="$2"
  tcMsg "buildStatus status='$status' text='$text'"
  exit $1
}

saveFailed() {
  try=$1
  failed=`egrep '<<< ERROR|<<< FAILURE' integration-tests/target/failsafe-reports/*txt | perl -pe 's,.*/(.*).txt:.*,$1,g' | sort -u`
  nfailed=`echo "$failed" | wc -w`
  if [ "$nfailed" -ge 1 ]
  then
    mkdir -p integration-tests/error-screenshots/$try
    mv integration-tests/error-screenshots/*.png integration-tests/error-screenshots/$try
    for i in $failed
    do
      cp integration-tests/target/failsafe-reports/$i.txt integration-tests/error-screenshots/$try
    done
  fi
}

[ -z "$TESTS_IN_PARALLEL" ] && TESTS_IN_PARALLEL=1
[ -z "$FORK_COUNT" ] && FORK_COUNT="5"

tcLog "Show info (forks=$FORK_COUNT parallel=$TESTS_IN_PARALLEL)"
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

tcLog "Running report watcher for Tests "
tcMsg "importData type='surefire' path='**/*-reports/TEST*xml'";

cmd="mvn install -Drelease -B -q -T $FORK_COUNT"
tcLog "Unit-Testing and Installing flow components - $cmd"
$cmd
if [ $? != 0 ]
then
  tcLog "Unit-Testing and Installing flow components (2nd try) - $cmd"
  sleep 30
  $cmd || tcStatus 1 "Unit-Testing failed"
fi

cmd="node scripts/mergeITs.js "`echo $elements`
tcLog "Merge IT modules - $cmd"
$cmd || tcStatus 1 "Merging ITs failed"
[ -n "$TBLICENSE" ] && args="$args -Dvaadin.testbench.developer.license=$TBLICENSE"
[ -n "$TBHUB" ] && args="$args -Dtest.use.hub=true -Dcom.vaadin.testbench.Parameters.hubHostname=$TBHUB"
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
    tcLog "Starting docker container using the $SELENIUM_DOCKER_IMAGE image"
    set -x
    trap "echo Terminating docker; docker stop $DOCKER_CONTAINER_NAME" EXIT
    docker pull "$SELENIUM_DOCKER_IMAGE" || exit 1
    docker image prune -f || exit 1
    docker run --name "$DOCKER_CONTAINER_NAME" --net=host --rm -d -v /dev/shm:/dev/shm "$SELENIUM_DOCKER_IMAGE" || exit 1
    set +x
fi

args="$args -Dfailsafe.rerunFailingTestsCount=2 -B -q"

reuse_browser() {
    [ -z "$1" ] || echo "-Dcom.vaadin.tests.SharedBrowser.reuseBrowser=$1"
}

if [ -n "$modules" ] && [ -z "$USE_MERGED_MODULE" ]
then
  ### Run IT's in original modules
  cmd="mvn clean verify -Dfailsafe.forkCount=$FORK_COUNT $args -pl $modules $(reuse_browser $TESTBENCH_REUSE_BROWSER)"
  tcLog "Running module ITs - mvn clean verify -pl ..."
  echo $cmd
  $cmd
elif [ -z "$BUILD" ]
then
  mode="-Dfailsafe.forkCount=$FORK_COUNT -Dcom.vaadin.testbench.Parameters.testsInParallel=$TESTS_IN_PARALLEL"
  ### Run IT's in merged module
  cmd="mvn verify -B -q -Drun-it -Drelease $mode $args -pl integration-tests $(reuse_browser $TESTBENCH_REUSE_BROWSER)"
  tcLog "Running merged ITs - mvn verify -B -Drun-it -Drelease -pl integration-tests ..."
  echo $cmd
  $cmd
  error=$?

  [ ! -d integration-tests/target/failsafe-reports ] && exit 1
  saveFailed run-1

  if [ "$nfailed" -gt 0 ]
  then
      tcLog "There were $nfailed Failed Tests: "
      echo "$failed"

      if [ "$nfailed" -le 15 ]
      then
        failed=`echo "$failed" | tr '\n' ','`
        mode="-Dfailsafe.forkCount=2 -Dcom.vaadin.testbench.Parameters.testsInParallel=3"
        cmd="mvn verify -B -q -Drun-it -Drelease $mode $args -pl integration-tests -Dit.test=$failed $(reuse_browser false)"
        tcLog "Re-Running $nfailed failed tests ..."
        echo $cmd
        $cmd
        error=$?
        saveFailed run-2
        tcStatus $error "Test failed: $nfailed" "Success"
      fi
  fi
  exit $error
fi
