# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

This is the Vaadin Flow Components repository containing Vaadin Flow wrappers for Vaadin web components. It's a multi-module Maven project with individual component modules following a consistent structure.

### Technologies

- Java 21+
- Maven
- Vaadin Flow 25+
- JUnit 4 for unit testing
- Mockito for mocking in unit tests
- Vaadin TestBench for integration tests
- Jetty for running integration test servers

### Module Structure

Each component module follows a consistent parent-child structure:
```
vaadin-{component}-flow-parent/                 # Parent module for a component
├── vaadin-{component}-flow/                    # Main component implementation
├── vaadin-{component}-flow-integration-tests/  # Integration tests
└── vaadin-{component}-testbench/               # TestBench elements
```

Shared modules used across components:

- `vaadin-flow-components-shared-parent/vaadin-flow-components-base`: Common utilities, mixins, and base classes
- `vaadin-flow-components-shared-parent/vaadin-flow-components-test-util`: Testing utilities

### Component Implementation

- Component classes are located in `src/main/java/com/vaadin/flow/component/{component}/*`
- Extend Flow `Component` and implement mixin interfaces like `HasText`, `HasEnabled`, `ClickNotifier`
- Use `@Tag` and `@JsModule` annotations to link to the corresponding Vaadin web component
- Can have theme variants by implementing the `HasThemeVariant` interface and defining an enum for variants extending from `ThemeVariant`
- Some components use additional client-side JavaScript for integrating with the web component or to add extra functionality. These so-called "connectors" are located in `src/main/resources/META-INF/resources/frontend`
- Connector initialization, as well as any inline JavaScript run with `Element.executeJs()`, are run in the component's attach handler to ensure they are always run again when Flow creates a new element for the same component instance on the client side

### Web Component Integration

- Vaadin Flow components wrap Vaadin web components
- The Vaadin web-components monorepo is located at `../web-components`
- The package name in `@JsModule` indicates the location of the web component in the web-components monorepo (e.g., `@vaadin/button` → `..web-components/packages/button/src/vaadin-button.js`)

### Testing

- Unit tests: Standard JUnit tests, located in component modules
- Integration tests: TestBench-based browser tests, located in separate IT modules
  - Each test consists of a test setup in form of a Vaadin Flow view and a JUnit test class
  - Both are linked using `@Route` and `@TestPath` annotations
  - New integration tests should extend from `com.vaadin.tests.AbstractComponentIT`

## Development Commands

### Building and Testing

```sh
# Build entire project
mvn clean install -DskipTests

# Build a component module
mvn clean install -pl vaadin-{component}-flow-parent -DskipTests

# Run all unit tests for a component
mvn test -pl vaadin-{component}-flow-parent/vaadin-{component}-flow

# Run specific unit tests for a component
mvn test -pl vaadin-{component}-flow-parent/vaadin-{component}-flow -Dtest='{file-pattern}'

# Run all integration tests for a component
mvn verify -am -pl vaadin-{component}-flow-parent/vaadin-{component}-flow-integration-tests -DskipUnitTests

# Run specific integration tests for a component
mvn verify -am -pl vaadin-{component}-flow-parent/vaadin-{component}-flow-integration-tests -Dit.test='{file-pattern}' -DskipUnitTests

# Start integration test server for a component
mvn package jetty:run -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl vaadin-{component}-flow-parent/vaadin-{component}-flow-integration-tests
```

**Notes on test commands**:
- Running a single integration test method requires adding a "*" wildcard after the method name (e.g. `-Dit.test='ButtonIT#textMatches*'`). This is because the TestBench test runner modifies the method names to include browser information (e.g. `textMatches[any_Chrome_]`).
- Integration test server can be used for testing pages manually using Playwright MCP, if installed
- Server needs to be restarted after code changes
- Integration tests can fail if the 8080 port is already in use. At that point stop and ask the user whether to kill the process using that port. If you started the server yourself and want to run tests against it, add `-DskipJetty` to the integration test command.
- When waiting for the server to start, use `TaskOutput` with `block=false` to poll the background task output for the message "Frontend compiled successfully" rather than using arbitrary sleep commands.
- When stopping a server that was started as a background task in the current session, use the `TaskStop` tool with the task ID instead of killing the process directly.

### Code Quality

```sh
# Format code
mvn spotless:apply
```
