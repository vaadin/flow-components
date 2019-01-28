# Vaadin Accordion for Flow

Vaadin Accordion for Flow is a UI component add-on for Vaadin which provides an accordion component.

## License & Author

This Add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3). For license terms, see LICENSE.txt.

Vaadin Accordion is written by Vaadin Ltd.

To purchase a license, visit http://vaadin.com/pricing

### Installing
Add Accordion to your project
```xml
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-accordion-flow</artifactId>
    <version>${vaadin.accordion.version}</version>
  </dependency>
</dependencies>
```

### Using Vaadin Accordion

[<img src="https://raw.githubusercontent.com/vaadin/vaadin-accordion/master/screenshot.gif" width="700" alt="Screenshot of vaadin-accordion">](https://vaadin.com/components/vaadin-accordion)

#### Basic use
In the most basic use case, Vaadin Accordion requires...

#### Reacting to events
The events are...


#### Disabling a panel
Accordion panels can be...

## Setting up for development

Clone the project in GitHub (or fork it if you plan on contributing)

```
git clone git@github.com:vaadin/vaadin-accordion-flow.git
```

To build and install the project into the local repository run 

```mvn install -DskipITs```

in the root directory. `-DskipITs` will skip the integration tests, which require a TestBench license. If you want to run all tests as part of the build, run

```mvn install```

To compile and run demos locally execute

```
mvn compile
mvn -pl vaadin-accordion-flow-vaadincom-demo -Pwar jetty:run
```
