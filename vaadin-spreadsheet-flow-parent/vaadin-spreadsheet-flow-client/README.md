

### Exported Spreadsheet

This module compiles and exposed the client part of the Spreadsheet component which has been developed by using GWT.

### Building

For building it you just need to run `mvn package` and the result JS will be installed in the resources folder of the `vaadin-spreadsheet-flow` component.

### License

This module is not delivered as an artifact, though, the output of this module (the JS resulting of the compilation), is deliver under the `vaadin-spreadsheet-flow` artifact. 

This module depends on the Vaadin Framework v8, but it's used only during the compilation performed to deliver the product. Thus any license term in Vaadin Framework 8.0 it's not propagated to customers using the Vaadin Spreadsheet for Flow.

### Debugging

GWT provides a code server that serves the compiled JS with source maps and recompiles the module on demand after you change Java code, also known as Super Dev Mode (SDM).

SDM is never merged. It lives in [`patches/gwt-sdm-debugging.patch`](patches/gwt-sdm-debugging.patch). To apply it, run it with the IT module, and revert it before committing, follow [`CLAUDE.md`](CLAUDE.md).

**Changes from the patch must not be added to commits / PRs.**

To recompile from the browser instead of with `curl`, open http://localhost:9876 once and install the 'Dev Mode On' bookmark, then click it after each change.

SDM also works with any Vaadin app that contains a spreadsheet and runs on localhost: apply the patch, install the `vaadin-spreadsheet-flow` module, run `mvn -Psdm` from this folder, and start your app.
