# Select Component for Vaadin Flow

Vaadin Select for Flow is the Java integration for [`<vaadin-select>`](https://github.com/vaadin/vaadin-select) web component.

### Installing
Add `Select` to your Vaadin Flow Java project by using the `vaadin-core` dependency that contains all Vaadin's open source UI components.
The `vaadin-bom` makes sure you get compatible versions of all the components and the Flow framework.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-bom</artifactId>
            <type>pom</type>
            <scope>import</scope>
            <version>${vaadin.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-core</artifactId>
  </dependency>
</dependencies>
```

Or you can take a specific version of the Select component with
```xml
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-select-flow</artifactId>
    <version>${component.version}</version>
  </dependency>
</dependencies>
```

## Setting up for development

Clone the project in GitHub (or fork it if you plan on contributing)

```
git clone git@github.com:vaadin/vaadin-select-flow.git
```

To build and install the project into the local repository run

```mvn install -DskipITs```

in the root directory. `-DskipITs` will skip the integration tests, which require a TestBench license. If you want to run all tests as part of the build, run

```mvn install```

To compile and run demos locally execute

```
mvn compile
mvn -pl vaadin-select-flow-demo -Pwar jetty:run
```

## License & Author

This component distributed under Apache 2.0 license. For license terms, see LICENSE.txt.

Vaadin Select is written by Vaadin Ltd.
