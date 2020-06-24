# Vaadin Details for Flow

Vaadin Details for Flow is a UI component add-on for Vaadin.

## License & Author

Apache License 2.0

Vaadin Details is written by Vaadin Ltd.

### Installing
Add Details to your project
```xml
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-details-flow</artifactId>
    <version>${vaadin.details.version}</version>
  </dependency>
</dependencies>
```

### Using Vaadin Details

#### Basic use
```java
Details component = new Details("Heading", new Span("Details"));
```

## Setting up for development

Clone the project in GitHub (or fork it if you plan on contributing)

```
git clone git@github.com:vaadin/vaadin-details-flow.git
```

To build and install the project into the local repository run

```mvn install -DskipITs```

in the root directory. `-DskipITs` will skip the integration tests, which require a TestBench license. If you want to run all tests as part of the build, run

```mvn install```

To compile and run demos locally execute

```
mvn compile
mvn -pl vaadin-details-flow-demo -Pwar jetty:run
```
