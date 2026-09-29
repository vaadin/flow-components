# Spreadsheet client (GWT)

The Spreadsheet client is GWT Java. Its `package` phase compiles it and
copies the result to `vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/spreadsheet-export.js`,
which is git-ignored. The flow jar ships that bundle.

## Iterate with SuperDevMode

Iterate on client changes with GWT SuperDevMode (SDM). Recompiling takes
about 3 s, compared with about 45 s for a Maven rebuild plus Jetty restart.
It also serves the Java sources for source maps. SDM is never merged: it
lives in `patches/gwt-sdm-debugging.patch`, which you apply locally and revert
before committing. Run the `git apply` commands from the repository root.

1. Apply it:
   ```sh
   git apply vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client/patches/gwt-sdm-debugging.patch
   ```
   Done when `SpreadsheetApiXSI.gwt.xml` exists in the client's `src/main/resources`.
2. Install the flow module so its jar contains the SDM loader:
   ```sh
   mvn install -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow -DskipTests
   ```
3. Start the code server in the background from the client module:
   `mvn -B -Psdm`. Done when the log prints `The code server is ready`.
   Its `[ERROR] ... INFO` lines are logging on stderr, not failures.
4. Start the IT Jetty server as described in the root `CLAUDE.md`. On a localhost
   page the browser console warns `Spreadsheet is using GWT SDM`; that warning
   confirms the client is served from port 9876.
5. After each client edit, recompile and reload the page:
   ```sh
   curl -s "http://localhost:9876/recompile/SpreadsheetApi?user.agent=safari&_callback=x"
   ```
   Done when the response contains `"status":"ok"`. Server-side Java edits
   still need the flow `install` and a Jetty restart.

Java sources for debugging are at `http://localhost:9876/sourcemaps/SpreadsheetApi/`.
Stop the code server with `kill $(lsof -tiTCP:9876 -sTCP:LISTEN)`.

## Before committing

1. Revert SDM:
   ```sh
   git apply -R vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client/patches/gwt-sdm-debugging.patch
   ```
   Done when `git status` lists no `SpreadsheetApiXSI.gwt.xml`, and the diffs
   of the client `pom.xml`, `SpreadsheetApi.gwt.xml` and
   `vaadin-spreadsheet.js` contain only your own changes.
2. Stop Jetty. It holds `target/` and makes `clean` fail. Then rebuild the
   production bundle:
   ```sh
   mvn clean install -DskipTests \
     -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client,vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow
   ```
   `clean` is required, because an incremental `package` can skip the GWT
   compile and keep the old bundle.
3. Run the unit tests and ITs against this bundle. A change that only worked
   under SDM is unverified.

## When the patch no longer applies

The patch conflicts when `main` changes the client `pom.xml`,
`SpreadsheetApi.gwt.xml` or `vaadin-spreadsheet.js`. Refresh it on a new
branch from `main`, starting with no other staged changes:

```sh
P=vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client/patches/gwt-sdm-debugging.patch
git apply --3way "$P"
# resolve the conflicts and `git add` the resolved files, then:
git diff --cached > /tmp/sdm.patch
git apply -R --index /tmp/sdm.patch
cp /tmp/sdm.patch "$P"
```

Check that `git apply --check "$P"` passes, then commit only the patch file
in its own PR.
