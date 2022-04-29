#!/bin/sh
#
# This script displays a menu for selecting maven actions
# to run in this project.
#

## by default 4 forks are used (set in pom), but it can be changed
[ -n "$FORKS" ] && args="-Dfailsafe.forkCount=$FORKS";
## bu default local tests are runn in headless, but can be disabled
[ "$HEADLESS" = false ] && args="$args -DdisableHeadless" && quiet="" || quiet="-q"

## Speedup installation of frontend stuff
verify="verify -Dvaadin.pnpm.enable"
jettyrun="jetty:run -Dvaadin.pnpm.enable"

## List all modules and ask for one to the user
askModule() {
  [ -z "$modules" ] && modules=`cat pom.xml | grep 'module>vaadin.*flow-parent</module' | sed -e 's,.*<module>,,g' | sed -e 's,-flow-parent.*,,g' | sort | cat -n -`
  max=`echo "$modules" | wc -l`
  printf "\nList of Modules\n$modules\nType the number of component:  "
  read module
  if [ -n "$module" -a "$module" -ge 1 -a "$module" -le $max ]
  then
    module=`echo "$modules" | head -$module | tail -1 | awk '{print $2}'`
  else
   printf "Incorrect option $module it should be in the range 1 - %s\n" $max && exit 1
  fi
}
## Check whether there are sauce credentials, otherwise ask for them
askSauce() {
  if [ -z "$SAUCE_USER" -a -z "$SAUCE_ACCESS_KEY" ]
  then
     printf "You need to set SAUCE_USER and SAUCE_ACCESS_KEY env vars"
     printf "Otherwise type your SauceLabs credentials SAUCE_USER:SAUCE_ACCESS_KEY "
     read sauce
     SAUCE_USER=`echo $sauce | cut -d ":" -f1`
     SAUCE_ACCESS_KEY=`echo $sauce | cut -d ":" -f2`
  fi
  # used internally in TB classes
  export TESTBENCH_GRID_BROWSERS=${TESTBENCH_GRID_BROWSERS:-edge,safari-13,firefox}
}
## Ask for a list of tests to run
askITests() {
  printf "Specify classes to test (separated by comma), empty for all: [all]: "
  read itests
  [ -n "$itests" ] && itests="-Dit.test=$itests"
}
## Ask whether run unit tests before ITs
askUTests() {
  utests="-Dtest=none"
  [ -n "$itests" ] && return
  printf "Do you want to run also Unit Tests y/n [y]: "
  read run
  [ "$run" != n ] && utests=""
}
## Run the mergeITs script if it was not run or there are modifications in modules
mergeITs() {
  [ ! -d node_modules ] && npm install
  pom=integration-tests/pom.xml
  modified=`[ -f $pom ] && find vaadin*parent/*integration-tests/src -mnewer $pom`
  [ -f $pom -a -z "$modified" ] || node ./scripts/mergeITs.js $MODULES
}
## Ask whether to run dev-server before running ITs
askJetty() {
  printf "Start jetty (answer n if jetty is running in background) ? y/n [y]: "
  read run
  [ "$run" = "n" ] && jetty="-DskipJetty"
}
## Decide whether to run frontend compilation
runFrontend() {
  [ -n "$module" ] && folder=$module-flow-parent/$module-flow-integration-tests || folder=integration-tests
  bundle="$folder/target/classes/META-INF/VAADIN/build/vaadin-bundle*js"
  modified=`[ -f "$bundle" ] && find $folder/src -mnewer "$bundle"`
  [ -f "$bundle" -a -z "$modified" ] && frontend="-DskipFrontend"
}

## Ask for run options
clear
printf "List of Options\n"
cat -n <<EOF
  one compile  - Compile one component (including tests)                     - mvn clean test-comple -pl component...
  one test  IT - Verify Integration-Tests of one component                   - mvn verify -pl component -Dit.Test=...
  one jetty IT - Start Jetty Server on one component IT module               - mvn jetty:run -pl component ...
  one sauce IT - Verify Integration-Tests of one component in SauceLabs      - mvn verify -pl component -Dsauce.user ...
  all compile  - Compile all components (including tests)                    - mvn clean test-compile ...
  all install  - Install modules in local maven (demos, addons & testbenchs) - mvn install ...
  all test  IT - Verify merged IT's of all component (takes a while)         - mvn verify -pl integration-tests -Dit.Test=...
  all jetty IT - Start Jetty Server on merged IT's module                    - mvn jetty:run -pl integration-tests ...
  all sauce IT - Run Integration-Tests of all component in SauceLabs         - mvn verify -pl integration-tests -Dsauce.user ...
EOF
printf "Your option:  "
read option
## compose the mvn cli command based on the selected option
case $option in
   1) askModule; cmd="mvn clean test-compile -amd -B $quiet -DskipFrontend -pl $module-flow-parent";;
   2) askModule; askITests; askUTests; askJetty; runFrontend; cmd="mvn $verify $quiet -am -B -pl $module-flow-parent/$module-flow-integration-tests $utests $itests $frontend $jetty $args";;
   3) askModule; cmd="mvn package $jettyrun -am -B $quiet -DskipTests -pl $module-flow-parent/$module-flow-integration-tests"; browser=true;;
   4) askSauce; askModule; askITests; askUTests; askJetty; runFrontend; cmd="mvn $verify -am -B $quiet -pl $module-flow-parent/$module-flow-integration-tests $utests $itests $frontend $jetty $args -Dtest.use.hub=true -Psaucelabs -Dsauce.user=$SAUCE_USER -Dsauce.sauceAccessKey=$SAUCE_ACCESS_KEY";;
   5) cmd="mvn clean test-compile -DskipFrontend -B $quiet -T 1C";;
   6) cmd="mvn install -B -DskipTests -Drelease -T 1C";;
   7) mergeITs; askITests; askUTests; askJetty; runFrontend; cmd="mvn $verify $quiet -am -B -Drun-it -pl integration-tests $utests $itests $frontend $jetty $args";;
   8) mergeITs; cmd="mvn package $jettyrun -am -B $quiet -DskipTests -Drun-it -pl integration-tests"; browser=true;;
   9) askSauce; mergeITs; askITests; askUTests; askJetty; runFrontend; cmd="mvn $verify -am -B $quiet -pl integration-tests -Drun-it $utests $itests $frontend $jetty $args -Dtest.use.hub=true -Psaucelabs -Dsauce.user=$SAUCE_USER -Dsauce.sauceAccessKey=$SAUCE_ACCESS_KEY";;
esac

## execute mvn command and check error status
printf "\nRunning: $TESTBENCH_GRID_BROWSERS\n$cmd\n\n"
[ -n "$browser" ] && printf "Wait until server starts, then open the URL: http://localhost:8080/$module\n\n"
start=`date +%s`
$cmd
printf "\n$cmd\nexited with status: $?, after "$(expr `date +%s` - $start)" secs.\n\n"


