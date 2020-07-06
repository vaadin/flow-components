# IronList component for Vaadin Flow

This project is the Component wrapper implementation of [`<iron-list>`](https://github.com/PolymerElements/iron-list) element
for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).

This branch is for Vaadin 10 maintenance. See other branches for other framework versions:

 - `master` branch is Vaadin 11 (Flow/Flow-component version 1.1)
 - `1.0` branch is Vaadin 10 (Flow/Flow-component version 1.0)

## Running the component demo
Run from the command line:
- `mvn jetty:run -PrunTests`

Then navigate to `http://localhost:9998/iron-list` for the demo.

## Installing the component
Run from the command line:
- `mvn clean install -DskipTests`

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-iron-list-flow</artifactId>
    <version>${component.version}</version>
</dependency>
```

## Flow documentation
Documentation for flow can be found in [Flow documentation](https://github.com/vaadin/flow/blob/master/flow-documentation/Overview.asciidoc).

## Contributing
- Use the coding conventions from [Flow coding conventions](https://github.com/vaadin/flow/tree/master/eclipse)
- [Submit a pull request](https://www.digitalocean.com/community/tutorials/how-to-create-a-pull-request-on-github) with detailed title and description
- Wait for response from one of Vaadin Flow team members

## License
Apache License 2.0
