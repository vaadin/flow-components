# Repository

## Technology stack

- **Java 21+**, **Maven** (multi-module parent-child), **Vaadin Flow 25+**.
- **JUnit 6 (Jupiter)** for unit tests; **JUnit 4 + TestBench + Selenium** for
  integration tests; **Mockito** for mocking; **Jetty** for the IT server.
- **Jackson 3** for JSON — databind from `tools.jackson.*`, annotations from
  `com.fasterxml.jackson.annotation.*` (they stayed in the legacy package).

## Ground rules

- Components and the data objects they expose are `Serializable`, and JSON is
  built with Jackson (`JacksonUtils`) — see
  [Design → Universal behavioural requirements](02-design.md#universal-behavioural-requirements).
- Connectors and `executeJs` calls run in `attach`, not the constructor — see
  [Composition](07-composition.md).

## Module structure

Each component is a parent Maven module with three children:

```
vaadin-{component}-flow-parent/
├── pom.xml                                    # aggregates children
├── README.md, LICENSE
├── vaadin-{component}-flow/                    # main component
│   └── src/main/java/com/vaadin/flow/component/{name}/
│       ├── {Component}.java
│       ├── {Component}Variant.java             # if it has theme variants
│       └── {Component}I18n.java                # if it renders text
├── vaadin-{component}-testbench/               # TestBench element
└── vaadin-{component}-flow-integration-tests/  # ITs (in the `default` profile)
```

Copy the layout from `vaadin-button-flow-parent` (the reference small
component). The integration-tests module sits under a `default` profile, so it
builds during development but is excluded from releases. Add the new parent
module to the top-level aggregator pom.

## Naming

| Thing             | Pattern                                 | Example                          |
| ----------------- | --------------------------------------- | -------------------------------- |
| Parent module     | `vaadin-{component}-flow-parent`        | `vaadin-date-picker-flow-parent` |
| Package           | `com.vaadin.flow.component.{component}` | `datepicker` (no hyphens)        |
| Component class   | PascalCase, no `Vaadin`/`Flow` affix    | `DatePicker`                     |
| Variant enum      | `{Component}Variant`                    | `ButtonVariant`                  |
| I18n class        | `{Component}I18n`                       | `ComboBoxI18n`                   |
| TestBench element | `{Component}Element`                    | `ButtonElement`                  |
| Web component tag | `vaadin-{component}`                    | `vaadin-date-picker`             |

Test classes: `{Component}Test`, `{Component}SignalTest`,
`{Component}VariantTest`, `{Component}SerializableTest`, `{Component}I18nTest`
(unit); `{Component}IT` + `{Component}Page` (integration). Use the `.tests`
package for both unit and integration tests — `.test` is legacy, avoid it.
`{Component}View` exists in legacy ITs; prefer `{Component}Page` for new ones.

## Maven

The parent pom inherits from the `vaadin-flow-components` aggregator and lists
the IT module under a `default` profile (active when `release` is unset). The
main module's dependencies are minimal — `vaadin-flow-components-base`,
`flow-html-components` (provided), and test-scoped JUnit/Mockito/test-util.
Copy `vaadin-button-flow-parent/vaadin-button-flow/pom.xml` as a starting
point.

## Copyright headers

Every `.java`, `.js`, `.xml`, and `.properties` file carries a license header,
applied by `mvn spotless:apply`. Most components use the Apache 2.0 header;
commercial components (Grid Pro, Charts, Spreadsheet, Dashboard, Board, CRUD,
Map, Rich Text Editor, …) use the Vaadin Commercial License header instead.
