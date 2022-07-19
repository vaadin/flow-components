#!/bin/bash

## Read Arguments
if [ -n "$1" ]
then
  for i in "$@"
  do
    case $i in
      processors=*)
        FORK_COUNT=`echo $i | cut -d = -f2`;;
      parallel=*)
        TESTS_IN_PARALLEL=`echo $i | cut -d = -f2`;;
      pr=*)
        PR=`echo $i | cut -d = -f2`;;
      quiet)
        quiet="-q";;
      hub=*)
        TBHUB=`echo $i | cut -d = -f2`;;
      image=*)
        SELENIUM_IMAGE=`echo $i | cut -d = -f2`;;
      *)
        modules="vaadin-$i-flow-parent/vaadin-$i-flow-integration-tests,$modules"
        elements="$elements $i"
       ;;
     esac
  done
fi

args="$args -B $quiet"
verify="verify -Dvaadin.pnpm.enable"

## compute modules that were modified in this PR
if [ -z "$modules" -a -n "$PR" ]
then
  title=`curl -s "https://api.github.com/repos/vaadin/flow-components/pulls/$PR" | jq -r '. | .title'`
  ## need to check whether changes in the root or not
  modifiedAll=`curl -s "https://api.github.com/repos/vaadin/flow-components/pulls/$PR/files" \
    | jq -r '.[] | .filename' | sort -u | tr -d '[:space:]'`
  modifiedComponent=`curl -s "https://api.github.com/repos/vaadin/flow-components/pulls/$PR/files" \
    | jq -r '.[] | .filename' | grep 'vaadin.*flow-parent' | sort -u | tr -d '[:space:]'`
  modified=`curl -s "https://api.github.com/repos/vaadin/flow-components/pulls/$PR/files" \
    | jq -r '.[] | .filename' | grep 'vaadin.*flow-parent' | perl -pe 's,^vaadin-(.*)-flow-parent.*,$1,g' | sort -u`

  nmods=`echo "$modified" | wc -w`
  [ "$nmods" -eq 1 ] && echo "$title" | grep -q " vaadin-$modified-flow " && partial=yes
  [ -z "$partial" ] && [ $nmods -lt 5 ] && [ `echo ${#modifiedAll}` = `echo ${#modifiedComponent}` ] && partial=yes

  if [ -n "$partial" ]
  then
    for i in $modified
    do
      modules="vaadin-$i-flow-parent/vaadin-$i-flow-integration-tests,$modules"
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
    mkdir -p integration-tests/error-screenshots/$try
    mv integration-tests/error-screenshots/*.png integration-tests/error-screenshots/$try
    for i in $failed
    do
      cp integration-tests/target/failsafe-reports/$i.txt integration-tests/error-screenshots/$try
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

## Set default build paramters
[ -z "$TESTS_IN_PARALLEL" ] && TESTS_IN_PARALLEL=1
[ -z "$FORK_COUNT" ] && FORK_COUNT="5"
[ -z "$SELENIUM_IMAGE" ] && SELENIUM_IMAGE="latest"

## Show info about environment
tcLog "Show info (forks=$FORK_COUNT parallel=$TESTS_IN_PARALLEL)"
echo $SHELL
type java && java -version
type mvn && mvn -version
type node 2>/dev/null && node --version
type npm 2>/dev/null && npm --version
type pnpm 2>/dev/null && pnpm --version
uname -a
[ -n "$elements" ] && echo "Run: $elements" || echo "Run all"

## Compile all java files including tests in ITs modules
cmd="mvn clean test-compile -DskipFrontend $args"
tcLog "Compiling flow components - $cmd"
$cmd || tcStatus 1 "Compilation failed"

## Notify TC that we are going to run maven tests
tcLog "Running report watcher for Tests "
tcMsg "importData type='surefire' path='**/*-reports/TEST*xml'";

## Compile and install all modules excluding ITs
cmd="mvn install -Drelease -T $FORK_COUNT $args"
tcLog "Unit-Testing and Installing flow components - $cmd"
if ! $cmd
then
  ## Some times install fails because of maven multithread race condition
  ## running a second time it is mitigated
  tcLog "Unit-Testing and Installing flow components (2nd try) - $cmd"
  sleep 15
  $cmd || tcStatus 1 "Unit-Testing failed"
fi

## Skip IT's if developer passed [skip ci] labels in commit messages
tcLog "Checking for skip-ci labels"
if computeFastBuild
then
  echo "$prTitle"
  echo "$prMessages"
  tcStatus 0 "" "Success - skip-ci"
fi

## Install node modules used for merging ITs
cmd="npm install --silent --quiet --no-progress"
tcLog "Install NPM packages - $cmd"
$cmd || exit 1

## Create the integration-tests by coping all module ITs
cmd="node scripts/mergeITs.js "`echo $elements`
tcLog "Merge IT modules - $cmd"
$cmd || tcStatus 1 "Merging ITs failed"

## Compute variable to run tests
[ -n "$TBLICENSE" ] && args="$args -Dvaadin.testbench.developer.license=$TBLICENSE"
[ -n "$TBHUB" ] && args="$args -Dtest.use.hub=true -Dcom.vaadin.testbench.Parameters.hubHostname=$TBHUB"
if [ -n "$SAUCE_USER" ]
then
   test -n "$SAUCE_ACCESS_KEY" || { echo "\$SAUCE_ACCESS_KEY needs to be defined to use Saucelabs" >&2 ; exit 1; }
   args="$args -P saucelabs -Dtest.use.hub=true -Dsauce.user=$SAUCE_USER -Dsauce.sauceAccessKey=$SAUCE_ACCESS_KEY"
fi

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

if [ -n "$modules" ] && [ -z "$USE_MERGED_MODULE" ]
then
  ### Run IT's in original modules
  cmd="mvn clean $verify -Dfailsafe.forkCount=$FORK_COUNT $args -pl $modules -Dtest=none"
  tcLog "Running module ITs ($elements) - mvn clean $verify -pl ..."
  echo $cmd
  $cmd
else
  mode="-Dfailsafe.forkCount=$FORK_COUNT -Dcom.vaadin.testbench.Parameters.testsInParallel=$TESTS_IN_PARALLEL"
  ### Run IT's in merged module
  cmd="mvn $verify -Drun-it -Drelease -Dvaadin.productionMode -Dfailsafe.rerunFailingTestsCount=2 $mode $args -pl integration-tests -Dtest=none"
  tcLog "Running merged ITs - mvn $verify -B -Drun-it -Drelease -pl integration-tests ..."
  echo $cmd
  $cmd
  error=$?

  [ ! -d integration-tests/target/failsafe-reports ] && exit 1
  saveFailedTests run-1

  if [ "$nfailed" -gt 0 ]
  then
      ## Give a second try to failed tests
      tcLog "There were $nfailed failed IT classes in first round. (error=$error)"
      echo "$failedMethods"

      rerunFailed=$nfailed
      if [ "$nfailed" -le 15 ]
      then
        failed=`echo "$failed" | tr '\n' ','`
        mode="-Dfailsafe.forkCount=2 -Dcom.vaadin.testbench.Parameters.testsInParallel=3"
        cmd="mvn $verify -Drun-it -Drelease -Dvaadin.productionMode -DskipFrontend $mode $args -pl integration-tests -Dtest=none -Dit.test=$failed"
        tcLog "Re-Running $nfailed failed IT classes ..."
        echo $cmd
        $cmd
        error=$?
        saveFailedTests run-2

        tcLog "There where $nfailed failed IT classes in second round. (error=$error)"
        echo "$failedMethods"

        tcStatus $error "(IT2)Test failed: $nfailed" "(IT2)Tests passed: $ncompleted ($rerunFailed retried, $nfailed failed), ignored: $nskipped"
      else
        tcStatus $error "(IT1)Test failed: $nfailed" "(IT1)Tests passed: $ncompleted (more than 15 failed), ignored: $nskipped"
      fi
  fi
  exit $error
fi
