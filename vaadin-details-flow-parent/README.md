# Vaadin Details for Flow

Vaadin Details for Flow is a UI component add-on for Vaadin.

## License & Author

This Add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3). For license terms, see LICENSE.txt.

Vaadin Details is written by Vaadin Ltd.

To purchase a license, visit http://vaadin.com/pricing

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

[<img src="https://raw.githubusercontent.com/vaadin/vaadin-details/master/screenshot.gif" width="700" alt="Screenshot of vaadin-details">](https://vaadin.com/components/vaadin-details)

#### Basic use
```java
Component component = new Component();
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
