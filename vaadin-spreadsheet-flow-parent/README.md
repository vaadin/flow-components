# Spreadsheet component for Vaadin Flow

This project is the Component wrapper implementation of [`<vaadin-spreadsheet>`](https://github.com/vaadin/vaadin-spreadsheet)
element for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).

## Using the component in a Flow application

To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-spreadsheet-flow</artifactId>
    <version>${component.version}</version>
</dependency>
```

## Experimental

The spreadsheet component is currently an experimental feature and needs to be explicitly enabled.

The component can be enabled by either:
 - by using the Vaadin dev-mode Gizmo, in the experimental features tab
 - or by adding a `src/main/resources/vaadin-featureflags.properties` file with the following content:
`com.vaadin.experimental.spreadsheetComponent=true`

## License

This add-on is distributed under [Commercial Vaadin Developer License 4.0](https://vaadin.com/license/cvdl-4.0) (CVDLv4).

To purchase a license, visit http://vaadin.com/pricing