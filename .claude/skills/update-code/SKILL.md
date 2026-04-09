---
description: Implements a single use case from a Flow component spec file
argument-hint: <ComponentName> <use-case>
---

You are a developer who creates high quality, feature rich Vaadin Flow components.

Earlier, specifications for Flow components were created in the spec/ folder inside the component module, named something-flow-component.md.

Your task is to pick the spec for the given component from `vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-flow/spec/{kebab-name}-flow-component.md` and implement what is needed for the usage example / use case with the given name or number.

Before starting, read the CLAUDE.md file in the repository root to understand the project structure, build commands, and testing approach.

Arguments: [Component name] [UseCase]

- **Component name**: The name of the component (e.g. `Breadcrumb`)
- **UseCase**: A use case number or description from the spec file. This identifies which specific use case / usage example to implement. For example: `1`, `3`, `basic navigation`, `data-driven`.

Derive from the component name:
- **kebab-name**: PascalCase → kebab-case (e.g. `DatePicker` → `date-picker`)
- **package-name**: kebab-name with hyphens removed (e.g. `breadcrumb`, `datepicker`)

## Steps

### 1. Read the spec and identify the use case

Read the spec file and identify the specific use case matching the given UseCase argument. Match by number (e.g. "Use Case 1", "Example 1", the 1st use case listed) or by description (e.g. a heading or keyword match).

### 2. Check the component exists

If the component module at `vaadin-{kebab-name}-flow-parent/` does not exist, stop and ask the user to create it first using `/setup-component {ComponentName}`.

### 3. Read existing implementation

Read all existing Java source files in the component module to understand what is already implemented:
- `vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-flow/src/main/java/com/vaadin/flow/component/{package-name}/`
- `vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-testbench/src/main/java/com/vaadin/flow/component/{package-name}/testbench/`

Also read the corresponding web component spec (if available at `web-components/packages/{kebab-name}/spec/{kebab-name}-web-component.md`) to understand the underlying web component API — this informs how properties, slots, and events map to the Java API.

### 4. Implement the use case

Update the component Java class(es) to support the given use case as defined in the spec. Follow these principles:

- **Only implement the specified use case** — do not implement other use cases from the spec
- **Retain existing features** — any existing features related to other use cases must be preserved
- **Follow existing patterns** — look at similar components (Card, Button, SideNav, Checkbox) for implementation patterns:
  - Slot management: use `SlotUtils.setSlot()`, `SlotUtils.addToSlot()`, `SlotUtils.getChildInSlot()`
  - Properties: use `getElement().setProperty()` / `getElement().getProperty()`
  - Events: use `@DomEvent` annotation and `ComponentEvent<T>` subclass
  - Theme variants: enum implementing `ThemeVariant`, component implementing `HasThemeVariant<T>`
- **Write clean Javadoc** on all public methods and classes
- **Use standard Vaadin Flow interfaces** where appropriate (`HasSize`, `HasComponents`, `HasAriaLabel`, `HasEnabled`, `HasThemeVariant`, `HasText`, `HasPrefix`, `HasSuffix`, etc.)
- **License headers**: All new Java files must have the standard Vaadin license header (copy from an existing file)

### 5. Update TestBench element

If the use case adds new API that should be accessible in integration tests, update the `{ComponentName}Element` class in the testbench module with corresponding methods.

### 6. Add or update unit tests

Add or update unit tests in `vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-flow/src/test/java/com/vaadin/flow/component/{package-name}/`:

- Unit tests use JUnit 5 (`org.junit.jupiter.api`)
- Use `MockUIExtension` for tests that need a UI context:
  ```java
  @RegisterExtension
  final MockUIExtension ui = new MockUIExtension();
  ```
- Test all new public methods: setters, getters, edge cases (null, empty)
- Test event listeners if applicable
- Follow existing test patterns in the repository

### 7. Add or update integration test views

Add or update integration test views in `vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-flow-integration-tests/`:

- Create or update a page class (`{ComponentName}Page.java` or a more specific page) in `src/main/java/.../tests/` with `@Route("vaadin-{kebab-name}")` or a more specific route
- The page should demonstrate the implemented use case in a way that can be visually verified and tested
- Keep the page minimal but sufficient to test all aspects of the implemented use case
- Add interactive controls (buttons, etc.) if the use case involves dynamic behavior

### 8. Run unit tests

Run the unit tests to verify the implementation:

```sh
mvn test -pl vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-flow
```

Fix any failures before proceeding.

### 9. Format code

Run the formatter:

```sh
mvn spotless:apply -pl vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-flow
```

### 10. Start integration test server and verify

Start the integration test server to visually verify the component:

```sh
mvn package jetty:run -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-flow-integration-tests
```

Run this as a background task. Poll the output for "Frontend compiled successfully" to know when the server is ready. Then use Playwright (if available) to test that:
- The page loads without errors
- The component renders correctly
- The implemented use case works as expected

If you started the server yourself, stop it using `TaskStop` when done.

### 11. Iterate until it works

If tests fail or the component doesn't work as expected, fix the issues and repeat the relevant steps.

### 12. Create a commit

When done, create a commit with a descriptive message.

## Important Guidelines

- **Only implement the specified use case** — do not implement other use cases from the spec
- Be thorough in implementing the specified use case — cover all aspects described in the spec
- It is more important to produce a correct, high-quality result than to be quick
- Guessing is forbidden — ask the user for clarification on anything unclear
- Follow the existing conventions in this repository — read similar components for patterns before writing code
- All public API must have Javadoc
- Connector JavaScript (if needed) goes in `src/main/resources/META-INF/resources/frontend` and must be initialized in the attach handler
- When using `Element.executeJs()`, run it in the attach handler so it re-runs when Flow recreates the client-side element
