

### Exported Spreadsheet

This module compiles and exposed the client part of the Spreadsheet component which has been developed by using GWT.

### Building

For building it you just need to run `mvn package` and the result JS will be installed in the resources folder of the `vaadin-spreadsheet-flow` component.

### License

This module is not delivered as an artifact, though, the output of this module (the JS resulting of the compilation), is deliver under the `vaadin-spreadsheet-flow` artifact. 

This module depends on the Vaadin Framework v8, but it's used only during the compilation performed to deliver the product. Thus any license term in Vaadin Framework 8.0 it's not propagated to customers using the Vaadin Spreadsheet for Flow.

### Debugging

GWT provides a code-server for serving the generated JS with their source-maps as well as for recompiling the module on demand once you do changes in java code, also known as Super Dev Mode.

To enable debugging for GWT, first apply the Git patch in `./patches/gwt-sdm-debugging.patch`.
The patch must be applied from the repository root.
As the patch is not continuously maintained, merge conflicts must be resolved.
**Changes from the patch must not be added to commits / PRs.**

After applying the patch, for debugging the GWT code you have two options:

**Running the IT module:**

- Run the following commands from the repository root:
  ```sh
  # Install the spreadsheet-flow module
  mvn -B -q -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow -DskipTests install
  # Start the GWT SuperDevMode code server (in a separate terminal)
  mvn -B -q -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client -Psdm
  # Start the Jetty server for integration tests
  mvn package jetty:run -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -B -q -DskipTests -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-integration-tests
  ```
- If not already, you need to install the bookmark as it is indicated in the next block

**Running any Vaadin app with a spreadsheet:**

- run `mvn -Psdm` from this folder
- open this module in your favourite java IDE
- open the url http://localhost:9876 and install the 'Dev Mode On' bookmark in your browser (this only need to be performed once)
- run your application containing the `vaadin-spreadsheet` in localhost
- visit your application in localhost e.g. http://localhost:8080
- perform changes in Java code, and push the bookmark when ready.
