# Vaadin Flow Components

This is a maven multi-module project including all vaadin flow components.

`master` branch is the latest version of all the components that will be released in the [Vaadin platform](https://github.com/vaadin/platform).

## Compiling all the components and their modules including ITs

- `mvn clean compile -DskipTests -T C2`

## Compiling all modules but excluding ITs

- `mvn clean compile -DskipTests -Drelease -T C2`

## Installing all modules

- `mvn clean install -DskipTests -Drelease -T C2`

## Running one component demo

- `mvn -am -pl vaadin-checkbox-flow-parent/vaadin-checkbox-flow-demo -Pwar jetty:run`

Then navigate to `http://localhost:9998/vaadin-checkbox` to see the demo.

## Running the ITs of one component

- `mvn -am -pl vaadin-checkbox-flow-parent/vaadin-checkbox-flow-integration-tests verify`

## Running ITs of all components

NOTE: this takes a long while and consumes a lot of resources in your computer, it's better to run tests in the merged repo as it is indicated in the following sections

- `mvn clean verify -T C2`

## Merging ITs of all components in one module

There is a able to visit all IT modules and merge then into one unique module.
It does substitutions in sources so as routes do no conflict, and also adjust ports etc.

- `scripts/mergeITs.js`

By default it merges all modules, but it's also possible to merge certain modules by passing arguments

- `scripts/mergeITs.js button text-field crud`

## Running ITs of all components in the merged module

It should take around 15-20 minutes depending on the computer capabilities.

- `mvn verify -Drun-it -Dfailsafe.forkCount=5 -Dcom.vaadin.testbench.Parameters.testsInParallel=1 -pl integration-tests`

NOTE: that we need to activate the module with the `-Drun-it` property, and to speed up tests we enable parallel execution of classes by setting `-Dfailsafe.forkCount=5`, in addition `-Dcom.vaadin.testbench.Parameters.testsInParallel=1` makes TB to reuse browser instances.

## Updating modules from original master branches

By running `./scripts/updateFromMaster.sh` all components are replaced with their origin master branches.
It also aligns component poms and folder naming.

## Build script

The `./build.sh` script is though to be run in CI, it compiles all modules, merge IT's and run those.
It expects `TBLICENSE` and `TBHUB` variables when run in the CI server.
Optionally it's possible to run just a bunch of modules e.g. `./build.sh grid combo-box`


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

## License
Apache License 2.0
