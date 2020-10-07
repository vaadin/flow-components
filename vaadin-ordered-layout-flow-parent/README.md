# Layout components for Vaadin Flow

This project is the Component wrapper implementation of [`<vaadin-ordered-layout>`](https://github.com/vaadin/vaadin-ordered-layout) element
for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).
The repository contains implementations for `FlexLayout`, `HorizontalLayout` and `VerticalLayout`.

`master` branch is the latest version that will at some point be released in the [Vaadin platform](https://github.com/vaadin/platform). See other branches for other framework versions:
 - `1.0` branch is Vaadin 10 LTS (Flow/Flow-component version 1.0)

## Running the component demo
Run from the command line:
- `mvn  -pl vaadin-ordered-layout-flow-demo -Pwar install jetty:run`

Then navigate to `http://localhost:9998/vaadin-ordered-layout` for the demo.

## Installing the component
Run from the command line:
- `mvn clean install -DskipTests`

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-ordered-layout-flow</artifactId>
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
