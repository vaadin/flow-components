# Vaadin Flow Components

This is a maven multi-module project including all vaadin flow components.

`main` branch is the latest version of all the components that will be released in the [Vaadin platform](https://github.com/vaadin/platform).

## Quick start

It's provided a script that facilitates running most common tasks for running or testing components.

Execute `./scripts/run.sh` and select the appropriate menu options.

NOTE: a valid unix terminal with a regular shell is needed for running the utility.

## Compiling all modules but excluding ITs

- `mvn clean compile -Drelease -T 2C`

## Compiling all the components and their modules including ITs

- `mvn clean test-compile -DskipFrontend -T 2C`

## Installing all modules

- `mvn clean install -DskipTests -Drelease -T 2C`

## Serving the IT pages of a component

- `mvn -am -pl vaadin-checkbox-flow-parent/vaadin-checkbox-flow-integration-tests -DskipTests package jetty:run`

Then navigate to `http://localhost:8080/vaadin-checkbox/checkbox-test` to see the IT page.

## Running ITs of one component

- `mvn -am -pl vaadin-checkbox-flow-parent/vaadin-checkbox-flow-integration-tests verify`

## Running ITs of all components

NOTE: this takes a long while and consumes a lot of resources in your computer, it's better to run tests in the merged repo as it is indicated in the following sections

- `mvn clean verify -T 2C`

## Merging ITs of all components in one module

There is a able to visit all IT modules and merge then into one unique module.
It does substitutions in sources so as routes do no conflict, and also adjust ports etc.

- `./scripts/mergeITs.js`

NOTE: By default it merges all modules, but it's also possible to merge certain modules by passing arguments

- `./scripts/mergeITs.js button text-field crud`

## Running ITs of all components in the merged module

It should take around 15-20 minutes depending on the computer capabilities.

- `mvn verify -Drun-it -pl integration-tests`

NOTE: that we need to activate the module with the `-Drun-it` property. By default it runs 4 tests in parallel but you can change it by setting `-Dfailsafe.forkCount=5`.

## Running in Sauce Labs

The time it takes depends on the number of browsers and the modules tested.

To select which browsers to test, set the `TESTBENCH_GRID_BROWSERS` environment variable with a list of browsers.
```
TESTBENCH_GRID_BROWSERS=edge,safari-13,firefox
```



Then run the following command, replacing the `***` with your Sauce Labs credentials.

For testing one component run:

- `mvn verify -Dsauce.user=*** -Dsauce.sauceAccessKey=*** -pl vaadin-checkbox-flow-parent/vaadin-checkbox-flow-integration-tests`

For running all merged components execute:

- `mvn verify -Dsauce.user=*** -Dsauce.sauceAccessKey=*** -Drun-it -pl integration-tests`

## Debugging web-test-runner tests of a component

Make sure the root level dependencies are installed

- `npm install`

Serve the IT pages of the component whose tests you want to debug

- See "Serving the IT pages of a component" above

Run the tests for the component once to have the necessary dependencies installed

- `node ./scripts/wtr.js grid`

Move to the integration tests module of the component

- `cd vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests`

Start the test runner in watch mode

- `npx web-test-runner --playwright test/**/*.test.ts --node-resolve --watch`

NOTE: The tests actually import the client module under test from `..integration-tests/frontend/generated/jar-resources`.
For faster feedback loop you can work on the generated file directly. Just be careful not to lose your changes to it since it's not under version control.

## Bumping version for all Maven modules

To update the version for all modules for a new major or minor, run the following command:
```
mvn versions:set -DnewVersion=<next-version> -DprocessAllModules=true -DgenerateBackupPoms=false
```
where you replace `<next-version>` with the version that you want to set.

## Build script

The `./scripts/build.sh` script is though to be run in CI, it compiles all modules, merge IT's and run those.
It expects `TBLICENSE` and `TBHUB` variables when run in the CI server.
Optionally it's possible to run just a bunch of modules e.g. `./scripts/build.sh grid combo-box`

## Using the component in a Flow application

To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-checkbox-flow</artifactId>
    <version>${platform.version}</version>
</dependency>
```

## Contributing

For submitting pull requests to this repo please check our [contributing guidelines](https://vaadin.com/docs/latest/contributing/pr).

### Update package version for `@NpmPackage` in all files

There are scripts available for updating the `@NpmPackage` annotation to its latest patch version:

- `./scripts/updateNpmVer.js`

### Modify `pom.xml` files in modules

In order to align all component buids, maven pom files are generated from templates placed in `scripts/templates`.

If you need to make any modification in a component, consider whether it is convenient to add the same to all components, then modify files under the template and run:

- `./scripts/updateJavaPOMs.sh` script.


### Formatting

Run `mvn spotless:apply` before pushing your code.

## Bug and enhancement tickets
- Bug tickets and enhancement requests for the web component implementations should go to the Vaadin web components monorepo https://github.com/vaadin/web-components/.
- Issues that are not component-specific (e.g. requests for new components) or encompass multiple Flow components should be posted in this repository.

## LICENSE
For specific module(s), check the LICENSE file under the parent module.
