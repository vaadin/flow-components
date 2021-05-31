[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/vaadin-spreadsheet)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/vaadin-spreadsheet.svg)](https://vaadin.com/directory/component/vaadin-spreadsheet)


# Vaadin Spreadsheet for Flow

Vaadin Spreadsheet is a UI component add-on for Vaadin Flow which provides means to view and edit Excel spreadsheets in Vaadin applications.

Currently a pre-release version is available by using following "Maven coordinates":

    <repositories>
		  <repository>
			  <id>vaadin-prereleases</id>
			  <name>Vaadin Pre-releases</name>
			  <url>https://maven.vaadin.com/vaadin-prereleases</url>
		  </repository>
    </repositories>

    <dependencies>
      <dependency>
        <groupId>com.vaadin</groupId>
        <artifactId>vaadin-spreadsheet-flow</artifactId>
        <version>1.0.0.alpha1</version>
      </dependency>
    </dependencies>


## Request for early feedback

The add-on is a port of the original Vaadin Spreadsheet add-on for Vaadin Framework 7 & 8. Most of the functionality is already there, but you can't yet for example add Vaadin components to cells. We'd really appreciate feedback about this new version. Use the Issues tab in this GitHub project (preferred) or use the feedback from in vaadin.com. Share the version of Vaadin and the version of the add-on in your feedback. Thanks in advance!

## License & Author

This Add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3). For license terms, see LICENSE.txt.

Vaadin Spreadsheet is written by Vaadin Ltd.


## Setting up for development:

Clone the project in GitHub (or fork it if you plan on contributing)

To build and install the project into the local repository run 

```mvn install -DskipITs```

in the root directory. `-DskipITs` will skip the integration tests, which require a TestBench license. If you want to run all tests as part of the build, run

```mvn install```

To run the UI used for development, run *mvn jetty:run* in vaadin-spreadsheet directory, navigate to http://localhost:9998/SpreadsheetDemoUI

To set up SuperDevMode, run *mvn vaadin:run-codeserver* in vaadin-spreadsheet. You need to have the UI jetty running as well, then navigate to http://localhost:9998/SpreadsheetDemoUI/?superdevmode

