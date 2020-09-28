# Avatar Component for Vaadin Flow

This project is the Component wrapper implementation of [`<vaadin-avatar>`](https://github.com/vaadin/vaadin-avatar)
element for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).

## Running the component demo
Run from the command line:
- `mvn -pl vaadin-avatar-flow-demo -Pwar install jetty:run`

Then navigate to `http://localhost:9998/vaadin-avatar` to view the demo.

## Installing the component
Run from the command line:
- `mvn clean install -DskipTests`

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-avatar-flow</artifactId>
    <version>${component.version}</version>
</dependency>
```

## Flow documentation
Documentation for Flow can be found on [vaadin.com website](https://vaadin.com/docs/v14/flow/Overview.html) or on [GitHub](https://github.com/vaadin/flow-and-components-documentation/blob/master/documentation/Overview.asciidoc).

## Contributing
- Use the coding conventions from [Flow coding conventions](https://github.com/vaadin/flow/tree/master/eclipse)
- [Submit a pull request](https://www.digitalocean.com/community/tutorials/how-to-create-a-pull-request-on-github)
  with detailed title and description
- Wait for response from one of Vaadin CE team members

## License
Apache License 2.0
