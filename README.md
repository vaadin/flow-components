# Vaadin Flow Components

This is a maven multi-module project including all vaadin flow components.

`master` branch is the latest version of all the components that will be released in the [Vaadin platform](https://github.com/vaadin/platform).

## Quick start

It's provided a script that facilitates running most common tasks for running or testing components.

Execute `./scripts/run.sh` and select the appropriate menu options.

NOTE: a valid unix terminal with a regular shell is needed for running the utility.

## Compiling all modules but excluding ITs

- `mvn clean compile -Drelease -T C2`

## Compiling all the components and their modules including ITs

- `mvn clean test-compile -DskipFrontend -T C2`

## Installing all modules

- `mvn clean install -DskipTests -Drelease -T C2`

## Running one component demo

- `mvn -am -pl vaadin-checkbox-flow-parent/vaadin-checkbox-flow-demo -Pwar jetty:run`

Then navigate to `http://localhost:9998/vaadin-checkbox` to see the demo.

## Running ITs of one component

- `mvn -am -pl vaadin-checkbox-flow-parent/vaadin-checkbox-flow-integration-tests verify`

## Running ITs of all components

NOTE: this takes a long while and consumes a lot of resources in your computer, it's better to run tests in the merged repo as it is indicated in the following sections

- `mvn clean verify -T C2`

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

## Build script

The `./scripts/build.sh` script is though to be run in CI, it compiles all modules, merge IT's and run those.
It expects `TBLICENSE` and `TBHUB` variables when run in the CI server.
Optionally it's possible to run just a bunch of modules e.g. `./scripts/build.sh grid combo-box`

## Update package version for `@NpmPackage` in all files
There are scripts available for updating the `@NpmPackage` annotation to its latest patch version:
- `./scripts/updateNpmVer.js`

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

## Flow documentation
Documentation for flow can be found in [Flow documentation](https://github.com/vaadin/flow-and-components-documentation/blob/master/documentation/Overview.asciidoc).

## Contributing
- Use the coding conventions from [Flow coding conventions](https://github.com/vaadin/flow/tree/master/eclipse)
- [Submit a pull request](https://www.digitalocean.com/community/tutorials/how-to-create-a-pull-request-on-github) with detailed title and description
- Wait for response from one of Vaadin Flow team members

## Bug and enhancement tickets
- Bug tickets and enhancement requests that are specific to a certain Vaadin component should be posted in the component's Web Component repostory (e.g. https://github.com/vaadin/vaadin-button for Button).
- Issues that are not component-specific (e.g. requests for new components) or encompass multiple components should be posted in this repository.

## LICENSE
For specific module(s), check the LICENSE file under the parent module.
