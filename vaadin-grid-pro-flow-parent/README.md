# Vaadin ProGrid for Flow

Vaadin ProGrid for Flow is a UI component add-on for Vaadin.

## License & Author

This Add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3). For license terms, see LICENSE.txt.

Vaadin ProGrid is written by Vaadin Ltd.

To purchase a license, visit http://vaadin.com/pricing

### Installing
Add ProGrid to your project
```xml
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-pro-grid-flow</artifactId>
    <version>${vaadin.progrid.version}</version>
  </dependency>
</dependencies>
```

### Using Vaadin ProGrid

[<img src="https://raw.githubusercontent.com/vaadin/vaadin-pro-grid/master/screenshot.gif" width="700" alt="Screenshot of vaadin-pro-grid">](https://vaadin.com/components/vaadin-pro-grid)

#### Basic use
```java
Component component = new Component();
```

## Setting up for development

Clone the project in GitHub (or fork it if you plan on contributing)

```
git clone git@github.com:vaadin/vaadin-pro-grid-flow.git
```

To build and install the project into the local repository run

```mvn install -DskipITs```

in the root directory. `-DskipITs` will skip the integration tests, which require a TestBench license. If you want to run all tests as part of the build, run

```mvn install```

To compile and run demos locally execute

```
mvn compile
mvn -pl vaadin-pro-grid-flow-demo -Pwar jetty:run
```
