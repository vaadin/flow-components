# TimePicker Component for Vaadin Flow

This project is the Component wrapper implementation of [`<vaadin-time-picker>`](https://github.com/vaadin/vaadin-time-picker) element
for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).

## Running the component demo
Run from the command line:
- `mvn -pl vaadin-time-picker-flow-demo -Pwar install jetty:run`

Then navigate to `http://localhost:9998/vaadin-time-picker` to view the demo.

## Running Integration tests

For running integration tests demos execute one of the following lines depending on the desired mode
- `mvn -pl vaadin-time-picker-flow-integration-tests clean jetty:run`
- `mvn -pl vaadin-time-picker-flow-integration-tests clean jetty:run -Dvaadin.bowerMode`

Then navigate to integration tests URLs for see integration tests views.

## Installing the component
Run from the command line:
- `mvn clean install -DskipTests`

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-time-picker-flow</artifactId>
    <version>${component.version}</version>
</dependency>
```

## Flow documentation
Documentation for flow can be found in [Flow documentation](https://github.com/vaadin/flow-and-components-documentation/blob/master/Overview.asciidoc).

## Contributing
- Use the coding conventions from [Flow coding conventions](https://github.com/vaadin/flow/tree/master/eclipse)
- [Submit a pull request](https://www.digitalocean.com/community/tutorials/how-to-create-a-pull-request-on-github) with detailed title and description
- Wait for response from one of Vaadin Flow team members

## License

Vaadin Commercial License and Service Terms
