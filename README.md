# Vaadin Flow Components

This is a maven multi-module project including all vaadin flow components.

`master` branch is the latest version of all the components that will be released in the [Vaadin platform](https://github.com/vaadin/platform).

## Compiling all the components and their modules

- `mvn clean compile -DskipTests -T C2` 

## Packaging all the modules

- `mvn clean package -DskipTests -T C2` 

## Running one component demo

- `mvn -am -pl vaadin-checkbox-flow-parent/vaadin-checkbox-flow-demo -Pwar jetty:run`

Then navigate to `http://localhost:9998/vaadin-checkbox` to see the demo.

## Running the ITs of one component

- `mvn -am -pl vaadin-checkbox-flow-parent/vaadin-checkbox-flow-integration-tests verify`

## Running ITs of all components

NOTE: this takes a long while and consumes a lot of resources in your computer

- `mvn clean verify -T C2` 

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-checkbox-flow</artifactId>
    <version>${component.version}</version>
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
