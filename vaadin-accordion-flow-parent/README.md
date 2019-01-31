# Accordion for Vaadin Flow

Accordion for Vaadin Flow is a UI component add-on for Vaadin which provides an accordion component.

### License & Author

Apache License 2.0

Vaadin Accordion is written by Vaadin Ltd.

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

#### Basic use

````java
Accordion accordion = new Accordion();
accordion.add("Red", redContent);
accordion.add("Orange", orangeContent);
accordion.add("Yellow", yellowContent);

accordion.addOpenedChangedListener(event ->
        Notification.show(event.getOpenedPanel().get().getSummaryText() + " opened"));
````


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
