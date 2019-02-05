# Vaadin GridPro for Flow

Vaadin GridPro for Flow is a high quality data grid / data table UI component add-on for Vaadin.

## License & Author

This Add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3). For license terms, see LICENSE.txt.

Vaadin GridPro is written by Vaadin Ltd.

To purchase a license, visit http://vaadin.com/pricing

### Installing
Add GridPro to your project
```xml
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-grid-pro-flow</artifactId>
    <version>${vaadin.gridpro.version}</version>
  </dependency>
</dependencies>
```

### Using Vaadin GridPro

[<img src="https://raw.githubusercontent.com/vaadin/vaadin-grid-pro/master/screenshot.png" width="700" alt="Screenshot of vaadin-grid-pro">](https://vaadin.com/components/vaadin-grid-pro)

#### Basic use
```java
GridPro<> grid = new GridPro<>();
```

## Setting up for development

Clone the project in GitHub (or fork it if you plan on contributing)

```
git clone git@github.com:vaadin/vaadin-grid-pro-flow.git
```

To build and install the project into the local repository run

```mvn install -DskipITs```

in the root directory. `-DskipITs` will skip the integration tests, which require a TestBench license. If you want to run all tests as part of the build, run

```mvn install```

To compile and run demos locally execute

```
mvn compile
mvn -pl vaadin-grid-pro-flow-demo -Pwar jetty:run
```
