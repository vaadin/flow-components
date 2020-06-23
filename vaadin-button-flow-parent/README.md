# Button Component for Vaadin

This project is the Component wrapper implementation of [`<vaadin-button>`](https://github.com/vaadin/vaadin-button) element
for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).

`master` branch is the latest version that will at some point be released in the [Vaadin platform](https://github.com/vaadin/platform). See other branches for other framework versions:
 - `1.0` branch is Vaadin 10 LTS (Flow/Flow-component version 1.0)

## Running the component demo
Run from the command line:
- `mvn -pl vaadin-button-flow-demo -Pwar install jetty:run-war`

Then navigate to `http://localhost:9998/vaadin-button` to view the demo.

## Installing the component
Run from the command line:
- `mvn clean install -DskipTests`

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-button-flow</artifactId>
    <version>${component.version}</version>
</dependency>
```

## Flow documentation
Documentation for flow can be found in [Flow documentation](https://github.com/vaadin/flow-and-components-documentation/blob/master/documentation/Overview.asciidoc).

## Contributing
- All contributions should be made for `master` branch, from where those will be picked into any platform LTS versions if necessary.
- Use the coding conventions from [Flow coding conventions](https://github.com/vaadin/flow/tree/master/eclipse)
- [Submit a pull request](https://www.digitalocean.com/community/tutorials/how-to-create-a-pull-request-on-github) with detailed title and description
- Wait for response from one of Vaadin Flow team members

## License
Apache License 2.0
