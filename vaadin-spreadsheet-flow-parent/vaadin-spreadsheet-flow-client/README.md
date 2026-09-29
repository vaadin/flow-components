# Spreadsheet client (GWT)

This module contains the client part of the Spreadsheet component. It is written in Java and compiled to JavaScript with GWT.

## Building

`mvn install` in this module compiles the client and copies the result to `vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/spreadsheet-export.js`. That file is git-ignored and is shipped in the `vaadin-spreadsheet-flow` jar. Building `vaadin-spreadsheet-flow` or the integration tests with `-am` builds this module too.

Use `mvn clean install` when you need a bundle you can trust. The GWT plugin decides from file timestamps whether to recompile, and an incremental build has been seen to keep an old bundle.

## License

This module is not delivered as an artifact, though, the output of this module (the JS resulting of the compilation), is deliver under the `vaadin-spreadsheet-flow` artifact. 

This module depends on the Vaadin Framework v8, but it's used only during the compilation performed to deliver the product. Thus any license term in Vaadin Framework 8.0 it's not propagated to customers using the Vaadin Spreadsheet for Flow.

## Debugging with SuperDevMode

GWT SuperDevMode (SDM) runs a code server that recompiles the client in a few seconds after a change, instead of a Maven rebuild and a Jetty restart. It also serves source maps, so the browser dev tools show the Java sources.

SDM needs changes that must never be merged: the loader in `vaadin-spreadsheet.js` tries `localhost:9876` on every localhost page, which would slow down every app in development. The changes are kept in [`patches/gwt-sdm-debugging.patch`](patches/gwt-sdm-debugging.patch). Apply the patch locally and revert it before committing.

Run the commands from the repository root unless a step says otherwise.

### Start

1. Apply the patch:
   ```sh
   git apply vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client/patches/gwt-sdm-debugging.patch
   ```
   This adds `SpreadsheetApiXSI.gwt.xml` and changes the client `pom.xml`, `SpreadsheetApi.gwt.xml` and `vaadin-spreadsheet.js`.
2. Start the code server from this folder and keep it running:
   ```sh
   cd vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client
   mvn -Psdm
   ```
   It is ready when the log prints `The code server is ready at http://127.0.0.1:9876/`. Most of its log lines start with `[ERROR]`, even `INFO` messages. They are written to stderr and are not failures.
3. In another terminal, start the integration test server:
   ```sh
   mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-integration-tests
   ```
   Wait for `Frontend compiled successfully`, then open a Spreadsheet view, for example http://localhost:8080/vaadin-spreadsheet/freeze-pane-locale. The browser console shows `Spreadsheet is using GWT SDM at http://localhost:9876/...`. Without that warning, the page uses the bundle from the jar.

### After each client change

Recompile, then reload the page:

```sh
curl -s "http://localhost:9876/recompile/SpreadsheetApi?user.agent=safari&_callback=x"
```

The recompile is done when the response contains `"status":"ok"`. `user.agent=safari` compiles for Chrome and Safari; use `user.agent=gecko1_8` for Firefox.

To recompile from the browser instead, open http://localhost:9876 once and install the "Dev Mode On" bookmark, then click it after each change.

Changes to the server-side Java code in `vaadin-spreadsheet-flow` still need a Jetty restart.

The Java sources are also listed at http://localhost:9876/sourcemaps/SpreadsheetApi/.

### Stop

Stop Jetty with `mvn jetty:stop -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-integration-tests`, and the code server with <kbd>Ctrl</kbd>+<kbd>C</kbd> or `kill $(lsof -tiTCP:9876 -sTCP:LISTEN)`.

### Before committing

**Changes from the patch must not be added to commits / PRs.**

1. Revert the patch:
   ```sh
   git apply -R vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client/patches/gwt-sdm-debugging.patch
   ```
   `git status` should not list `SpreadsheetApiXSI.gwt.xml`, and the diffs of the client `pom.xml`, `SpreadsheetApi.gwt.xml` and `vaadin-spreadsheet.js` should contain only your own changes.
2. Stop Jetty and the code server. `clean` deletes the `target/` folders they run from.
3. Build the production bundle:
   ```sh
   mvn clean install -DskipTests -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client,vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow
   ```
4. Run the unit tests and integration tests against this bundle. A change that was only checked under SDM is not verified.

### Using SDM with another app

SDM works with any Vaadin app that contains a spreadsheet and runs on localhost. Apply the patch, install the flow module so its jar contains the SDM loader, start the code server, and start your app:

```sh
mvn install -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow -DskipTests
```

### When the patch no longer applies

The patch conflicts when `main` changes the client `pom.xml`, `SpreadsheetApi.gwt.xml` or `vaadin-spreadsheet.js`. Refresh it on a new branch from `main`, with no other staged changes:

```sh
P=vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client/patches/gwt-sdm-debugging.patch
git apply --3way "$P"
# resolve the conflicts and `git add` the resolved files, then:
git diff --cached > /tmp/sdm.patch
git apply -R --index /tmp/sdm.patch
cp /tmp/sdm.patch "$P"
```

Check that `git apply --check "$P"` passes, then commit only the patch file in its own PR.
