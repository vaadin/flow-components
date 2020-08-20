# TextField component for Vaadin Flow

This project is the Component wrapper implementation of [`<vaadin-text-field>`](https://github.com/vaadin/vaadin-text-field) element
for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).

The repository contains implementations for `TextField`, `PasswordField` and `TextArea`.

`master` branch is the latest version that will at some point be released in the [Vaadin platform](https://github.com/vaadin/platform). See other branches for other framework versions:
 - `1.0` branch is Vaadin 10 LTS (Flow/Flow-component version 1.0)


## Running the component demo
Run from the command line:
- `mvn -pl vaadin-text-field-flow-demo -Pwar install jetty:run`

Then navigate to `http://localhost:9998/vaadin-text-field` for the demo of TextField,
to `http://localhost:9998/vaadin-password-field` for the demo of PasswordField and to
`http://localhost:9998/vaadin-text-area` for the demo of TextArea 


## Running Integration tests

For running integration tests demos execute one of the following lines depending on the desired mode
- `mvn -pl vaadin-text-field-flow-integration-tests clean jetty:run`
- `mvn -pl vaadin-text-field-flow-integration-tests clean jetty:run -Dvaadin.bowerMode`

Then navigate to integration tests URLs for see integration tests views.

For running all integration tests execute
- `mvn clean install verify`

## Installing the component
Run from the command line:
- `mvn clean install -DskipTests`

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-text-field-flow</artifactId>
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
