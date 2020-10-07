# Vaadin Charts

Vaadin Charts for Flow is a UI component add-on for Vaadin 10 which provides means to create multiple different types of charts in Vaadin applications.

## License & Author

This Add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3). For license terms, see LICENSE.txt.

Vaadin Charts is written by Vaadin Ltd.


## Setting up for development:

Clone the project in GitHub (or fork it if you plan on contributing) and required submodules

```
git clone git@github.com:vaadin/vaadin-charts-flow.git
```

To build and install the project into the local repository run 

```mvn install -DskipITs```

in the root directory. `-DskipITs` will skip the integration tests, which require a TestBench license. If you want to run all tests as part of the build, run

```mvn install```
