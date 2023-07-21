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
      pr=*)
        PR=`echo $i | cut -d = -f2`;;
      testMode=*)
        TEST_MODE=`echo $i | cut -d = -f2`;;
      quiet)
        quiet="-q";;
      hub=*)
        TBHUB=`echo $i | cut -d = -f2`;;
      image=*)
        SELENIUM_IMAGE=`echo $i | cut -d = -f2`;;
      *)
        modules=vaadin-$i-flow-parent/vaadin-$i-flow-integration-tests,vaadin-$i-flow-parent/vaadin-$i-flow-integration-tests/pom-bower-mode.xml,$modules
        elements="$elements $i"
       ;;
     esac
  done
fi

args="$args -B $quiet"

## compute modules that were modified in this PR
if [ -z "$modules" -a -n "$PR" ]
then
  ## need to check whether changes in the root or not
  modifiedAll=`curl -s https://api.github.com/repos/vaadin/flow-components/pulls/$PR/files \
    | jq -r '.[] | .filename' | sort -u | tr -d '[:space:]'`
  modifiedComponent=`curl -s https://api.github.com/repos/vaadin/flow-components/pulls/$PR/files \
    | jq -r '.[] | .filename' | grep 'vaadin.*flow-parent' | sort -u | tr -d '[:space:]'`
  modified=`curl -s https://api.github.com/repos/vaadin/flow-components/pulls/$PR/files \
    | jq -r '.[] | .filename' | grep 'vaadin.*flow-parent' | perl -pe 's,^vaadin-(.*)-flow-parent.*,$1,g' | sort -u`

  if [ `echo "$modified" | wc -w` -lt 5 ] && [ `echo ${#modifiedAll}` = `echo ${#modifiedComponent}` ]
  then
    for i in $modified
    do
      modules=vaadin-$i-flow-parent/vaadin-$i-flow-integration-tests,$modules
      elements="$elements $i"
    done
  fi
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
  [ "$1" = "0" ] && text="$3" || text="$2"
  tcMsg "buildStatus status='$status' text='$text'"
  exit $1
}

saveFailedTests() {
  try=$1
  failedMethods=`egrep '<<< ERROR!$|<<< FAILURE!$' integration-tests/target/failsafe-reports/*txt | perl -pe 's,.*:(.*)\((.*)\).*,$2.$1,g' | sort -u`
  failed=`egrep '<<< ERROR!$|<<< FAILURE!$' integration-tests/target/failsafe-reports/*txt | perl -pe 's,.*:(.*)\((.*)\).*,$2,g' | sort -u`
  nfailed=`echo "$failed" | wc -w`
  ### collect tests numbers for TC status
  ncompleted=`grep 'Tests run: ' vaadin*/*flow/target/surefire-reports/*.txt integration-tests/target/failsafe-reports/*txt | awk '{SUM+=$3} END { print SUM }'`
  nskipped=`grep 'Tests run: ' vaadin*/*flow/target/surefire-reports/*.txt integration-tests/target/failsafe-reports/*txt | awk '{SUM+=$9} END { print SUM }'`
  if [ "$nfailed" -ge 1 ]
  then
    mkdir -p integration-tests/error-screenshots/$testFolder/$try
    mv integration-tests/error-screenshots/*.png integration-tests/error-screenshots/$testFolder/$try
    for i in $failed
    do
      cp integration-tests/target/failsafe-reports/$i.txt integration-tests/error-screenshots/$testFolder/$try
    done
  fi
}

computeFastBuild() {
  [ -z "$PR" ] && return 1
  ghUrl="https://api.github.com/repos/vaadin/flow-components/pulls/$PR"
  prTitle=`curl -s $ghUrl | jq -r .title`
  echo "$prTitle" | grep -v '\[skip ci\]' >/dev/null || return 0
  prMessages=`curl -s $ghUrl/commits | jq -r '.[] | .commit.message'`
  echo "$prMessages" | grep -v '\[skip ci\]' >/dev/null || return 0
  return 1
}

[ -z "$TESTS_IN_PARALLEL" ] && TESTS_IN_PARALLEL=1
[ -z "$FORK_COUNT" ] && FORK_COUNT="5"
### By default, run test under npm-it
[ -z "$TEST_MODE" ] && TEST_MODE="npm-it"
[ -z "$SELENIUM_IMAGE" ] && SELENIUM_IMAGE="latest"

tcLog "Show info (forks=$FORK_COUNT parallel=$TESTS_IN_PARALLEL)"
echo $SHELL
type java && java -version
type mvn && mvn -version
type node && node --version
type npm && npm --version
type pnpm && pnpm --version
uname -a

## Compile all java files including tests in ITs modules
cmd="mvn clean test-compile -DskipFrontend $args"
tcLog "Compiling flow components - $cmd"
$cmd || tcStatus 1 "Compilation failed"

## Notify TC that we are going to run maven tests
tcLog "Running report watcher for Tests "
tcMsg "importData type='surefire' path='**/*-reports/TEST*xml'";

## Compile and install all modules excluding ITs
cmd="mvn install -Drelease -B -T $FORK_COUNT $args"
tcLog "Unit-Testing and Installing flow components - $cmd"
$cmd
if [ $? != 0 ]
then
  tcLog "Unit-Testing and Installing flow components (2nd try) - $cmd"
  sleep 15
  $cmd || tcStatus 1 "Unit-Testing failed"
fi

tcLog "Checking for skip-ci labels"
if computeFastBuild
then
  echo "$prTitle"
  echo "$prMessages"
  tcStatus 0 "" "Success - skip-ci"
fi

cmd="npm install --silent --quiet --no-progress"
tcLog "Install NPM packages - $cmd"
$cmd || exit 1

cmd="node scripts/mergeITs.js "`echo $elements`
tcLog "Merge IT modules - $cmd"
$cmd || tcStatus 1 "Merging ITs failed"
[ -n "$TBLICENSE" ] && args="$args -Dvaadin.testbench.developer.license=$TBLICENSE"
[ -n "$TBHUB" ] && args="$args -Dtest.use.hub=true -Dcom.vaadin.testbench.Parameters.hubHostname=$TBHUB"
if [ -n "$SAUCE_USER" ]
then
   test -n "$SAUCE_ACCESS_KEY" || { echo "\$SAUCE_ACCESS_KEY needs to be defined to use Saucelabs" >&2 ; exit 1; }
   args="$args -P saucelabs -Dtest.use.hub=true -Dsauce.user=$SAUCE_USER -Dsauce.sauceAccessKey=$SAUCE_ACCESS_KEY"
fi
echo "$args"

args="$args -Dfailsafe.rerunFailingTestsCount=2"

## Install a selenium hub in local host to run tests against chrome
if [ "$TBHUB" = "localhost" ]
then
    DOCKER_CONTAINER_NAME="selenium-container"
    [ -n "$SELENIUM_DOCKER_IMAGE" ]  || SELENIUM_DOCKER_IMAGE="selenium/standalone-chrome:$SELENIUM_IMAGE"
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

testMode=$TEST_MODE
if [ "$testMode" = "bower-it" ]
then
  pomFile="pom-bower-mode.xml"
elif [ "$testMode" = "npm-it" ]
then
  pomFile="pom.xml"
fi

if [ -n "$modules" ] && [ -z "$USE_MERGED_MODULE" ]
then
  ### Run IT's in original modules
  cmd="mvn clean verify -Dfailsafe.forkCount=$FORK_COUNT $args -pl $modules -Dtest=none $(reuse_browser $TESTBENCH_REUSE_BROWSER)"
  tcLog "Running module ITs ($elements) - mvn clean verify -pl ..."
  echo $cmd
  $cmd
else
  mode="-Dfailsafe.forkCount=$FORK_COUNT -Dcom.vaadin.testbench.Parameters.testsInParallel=$TESTS_IN_PARALLEL"
  ### Run IT's in merged module
  cmd="mvn verify -D$TEST_MODE -Drelease -Dvaadin.productionMode -Dfailsafe.rerunFailingTestsCount=2 $mode $args -pl integration-tests/$pomFile -Dtest=none $(reuse_browser $TESTBENCH_REUSE_BROWSER)"
  tcLog "Running merged ITs - mvn verify -B -D$TEST_MODE -Drelease -pl integration-tests/$pomFile ..."
  echo $cmd
  $cmd
  error=$?

  [ ! -d integration-tests/target/failsafe-reports ] && return 1
  saveFailedTests run-1-$testMode

  if [ "$nfailed" -gt 0 ]
  then
      ## Give a second try to failed tests
      tcLog "There were $nfailed failed IT classes in first round."
      echo "$failedMethods"

      rerunFailed=$nfailed
      if [ "$nfailed" -le 15 ]
      then
        failed=`echo "$failed" | tr '\n' ','`
        mode="-Dfailsafe.forkCount=2 -Dcom.vaadin.testbench.Parameters.testsInParallel=3"
        cmd="mvn verify -D$TEST_MODE -Drelease -Dvaadin.productionMode -DskipFrontend $mode $args -pl integration-tests/$pomFile -Dtest=none -Dit.test=$failed $(reuse_browser false)"
        tcLog "Re-Running $nfailed failed IT classes ..."
        echo $cmd
        $cmd
        error=$?
        tcLog "Re-Run exited with code $error"
        saveFailedTests run-2-$testMode
        tcStatus $error "(IT2)Test failed: $nfailed" "(IT2)Tests passed: $ncompleted ($rerunFailed retried, $nfailed failed), ignored: $nskipped"
      else
        tcStatus $error "(IT1)Test failed: $nfailed" "(IT1)Tests passed: $ncompleted (more than 15 failed), ignored: $nskipped"
      fi
  fi
  exit $error
fi
