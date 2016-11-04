# Vaadin Spreadsheet

Vaadin Spreadsheet is a UI component add-on for Vaadin 7 which provides means to view and edit Excel spreadsheets in Vaadin applications.

## License & Author

This Add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3). For license terms, see LICENSE.txt.

Vaadin Spreadsheet is written by Vaadin Ltd.


## Setting up for development:

- Clone the project in GitHub (or fork it if you plan on contributing)
- To build and install the project into the local repository run *mvn install* in the root directory
- To run the UI used for development, run *mvn jetty:run* in vaadin-spreadsheet directory, navigate to http://localhost:9998/SpreadsheetDemoUI
- To set up SuperDevMode, run *mvn vaadin:run-codeserver* in vaadin-spreadsheet. You need to have the UI jetty running as well, then navigate to http://localhost:9998/SpreadsheetDemoUI/?superdevmode

