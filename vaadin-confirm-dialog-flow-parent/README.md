[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/vaadin-confirm-dialog)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/vaadin-confirm-dialog.svg)](https://vaadin.com/directory/component/vaadin-confirm-dialog)

# Confirm Dialog Component for Vaadin Flow

### Overview
Vaadin Confirm Dialog is an easy to use web component to ask the user to confirm a choice.

### License & Author

This add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3).

To purchase a license, visit http://vaadin.com/pricing

### Installing
Add Confirm Dialog to your project:
```
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-confirm-dialog-flow</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

### Basic Use

```java
ConfirmDialog dialog = new ConfirmDialog("Unsaved changes",
    "Do you want to save or discard your changes before navigating away?",
    "Save", event -> { /* handle confirm */ },
    "Discard", event -> { /* handle discard */ },
    "Cancel", event -> { /* handle cancel */ } );
dialog.open();
```

## Setting up for development

### Checkout the project
Clone the project in GitHub (or fork it if you plan on contributing)

```
git clone git@github.com:vaadin/vaadin-confirm-dialog-flow.git
```

### Building the component
To build and install the project into the local repository run the following command in the root folder:

```mvn install -DskipITs```

### Running demos

To compile and run demos locally execute

```
mvn compile
mvn -pl vaadin-confirm-dialog-flow-demo -Pwar jetty:run
```

### Running integration tests

`-DskipITs` will skip the integration tests, which require a TestBench license. If you want to run all tests as part of the build, run

```
mvn verify -PrunLocally
```


