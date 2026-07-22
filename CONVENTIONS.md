# Conventions

## Public API

New public API should use naming / terminology that matches established equivalents from sibling component modules. When multiple options exist prefer the more common or more recently added one.

Pair public setters with a matching getter.

When a public component API method accepts an object argument that would otherwise surface as a delayed or opaque NullPointerException, guard it at the top with `Objects.requireNonNull(param, "...")` carrying a descriptive message so invalid input fails fast.

Prefer returning unmodifiable collections from public getters when modifying the collection does nothing on its own (e.g. a setter needs to be called again).

Pair String CSS-size setters with a `(float, Unit)` overload, reuse `HasSize.getCssSize(size, unit)` to calculate the CSS string.

When a component sets a custom attribute that does not map to the web component API, prefer an all-lowercase (hyphenated) name.

## Internal API

Class members that facilitate internal logic should have minimum Java visibility. Prefer private for class-internal, package-private for cross-class access in the same package, or protected only when there is a need to override from a class in a different package. If the member must be protected or public, add a 'For internal use only. May be renamed or removed in a future release.' note in the Javadoc.

Internal helper and utils classes should have the minimum Java visibility. If the class is used only within the same package, use package-private. Only use public if the class needs to be shared between multiple packages and there is no way to refactor existing code to be located in the same package.

Internal helper and utils classes should have a 'For internal use only. May be renamed or removed in a future release.' note in the Javadoc.

When adding helper classes to the vaadin-flow-components-base module (e.g. utils, controllers), place them in the `shared.internal` sub-package to indicate these are considered to be internal and not part of the public API.

## Component Implementation

Every component and every data object it exposes is `Serializable`.

When a Flow component calls client-side JavaScript, always call through the component's element (`getElement()`) instead of calling the JS through the UI or the Page. The exception is if the call happens in a static context where no component / element instance is available.

When a Flow component only calls a named method on a web component, prefer `callJsFunction` over `executeJs`.

When server-side code initializes persistent client-side JavaScript via `Element.executeJs()` (e.g. initializing a connector, adding a listener) it must happen in the component's attach hook (`onAttach` override or attach listener). Flow recreates client elements on detach/re-attach and the attach handler guarantees the script is re-applied for every new element. The exceptions are one-time state sync fixes (e.g. force client property to a value when Flow's property tracking would not sync otherwise).

When code clears an element property, prefer `getElement().removeProperty(name)` over `getElement().setProperty(name, null)`.

Components and connectors must import web components from the NPM package's `src` folder (e.g. `@vaadin/text-field/src/vaadin-text-field.js`) instead of importing them from the bare package specifier (e.g. `@vaadin/text-field`) to ensure they stay theme-agnostic.

New frontend resources (e.g. connector .ts/.js) must never be added directly under a module's `META-INF/frontend/` root, place them in a component-named subfolder instead (e.g. `META-INF/frontend/vaadin-popover/popover.ts`). Flow merges all frontend resources into a single `jar-resources` folder at bundle time and generic paths risk filename collisions with application or add-on resources.

When a component may schedule the same client-side update via `Element.executeJs()` multiple times within one server roundtrip and only the last should win, store the returned `PendingJavaScriptResult` in a field and, before scheduling the next call, cancel the previous one if `!isSentToBrowser()` via `cancelExecution()`.

Connector JS that overrides an internal web component method and delegates to the saved original, should call the original with `original.apply(this, arguments)` so the patch stays correct if the web component's signature changes.

When serializing I18N objects to the client-side, strip all properties that the web component does not use (e.g. error messages that are only used by the Flow component validation).

I18N objects must not have default values. Their properties should be left unset / null to ensure the web component's defaults are used instead.

When a component registers listeners to a component that was passed to it (e.g. adding attach listener to a target component reference), the listeners must be removed via their `Registration` when the component is replaced (e.g. setting a new target component). This does not apply when a component manages the full lifecycle of nested / internal components, as we can assume that no other place holds references to those and listeners can be garbage collected together with the components.

When a component implements a mixin interface such as `HasComponents` and overrides its mutation methods to attach a side effect, ensure that all methods of the interface are covered.

Never derive a stable / unique identifier from an Element's `nodeId` (`getElement().getNode().getId()`). `nodeId` is -1 until the node is attached. Prefer an attachment-independent identifier such as a UUID instead.

Prefer the `SlotUtil` helper to assign or query child components in web component slots instead of hand-rolling logic, except when the use case is not covered by the helper.

Avoid accessing `UI.getCurrent`, prefer to access the UI via a component's `getUI` or via an event if it provides the UI (e.g. `AttachEvent`). `UI.getCurrent` should be used as a last resort if there is no component instance available from which to access the UI. Prefer `UI.getCurrentOrThrow()` if the code should fail without a UI.

## Security

Do not synchronize client-side property changes to the server (e.g. with `@Synchronize`) when users can never modify the property through the component's UI to ensure server-side state can not be tampered with. For example, users can not directly modify the validation state or visibility of a component, thus those should never synchronize. Compared to that, users can toggle the opened state of a details component, which is fine to synchronize.

When adding custom logic to handle client-side values (e.g. presentation-to-model parser, `hasValidValue` or `setModelValue` override), prefer to ignore invalid values and fall back to the existing server-side value instead of throwing an exception. If someone tampers with the component through client-side scripting it should not fill the server logs with exceptions.

## Deprecation

Pair `@Deprecated` annotations with a matching `@deprecated` Javadoc tag. The annotation must carry `since` in major.minor form (e.g. "25.0", not "25.0.0"). The version can be determined from the project version in the root POM.

When deprecating API, ensure there is a concrete replacement API in place and cite it from the `@deprecated` tag via `{@link ...}`. The exception is when an API turns out to be broken, in which case the tag should mention that the API is not supported.

Deprecations must not change the member's behavior and test coverage for the deprecated member must be preserved.

When the intent is to deprecate a mixin interface, ensure to cover all methods of it.

## JavaDoc

JavaDoc should describe the public contract and behavior, not internal implementation details on how the behavior is achieved.

JavaDoc should address readers in second person as "you", not in third person such as "allows developers to...".

JavaDoc should wrap literal values that are not referenced anywhere in a symbol (e.g. `true`, `false`, a constant string) in `{@code}`. Use `{@link}` if there is a symbol to reference.

A JavaDoc for a property should state what the default value is.

JavaDoc for component events and methods for registering such event listeners should state in which scenario the event fires, not which web component event the event maps to.

When a component setter overrides state set by a different setter (e.g. `setTitleText` overrides `setTitleComponent`), the JavaDoc should mention the interaction.

## Testing

Name tests by scenario and observable outcome. Simple tests covering a feature or property can just be called after those. Do not use "assert" or "expect" as a prefix for a test as those are reserved for test helpers.

When adding a new component module, add a serializable test that extends from `com.vaadin.flow.testutil.ClassesSerializableTest`.

Component getters and setters should be covered by unit tests. When those delegate to Element APIs, tests should verify via the Element API that a value was properly applied. Simple properties can be covered with a single unit test instead of splitting up testing over multiple tests.

When a component mutates a child's slot attribute, unit tests must verify that the slot attribute is added when the child is added, and removed when the child is removed from the component.

Use unit tests over integration tests when the logic is purely on the server-side and only involves calling plain Flow framework APIs (mutating DOM, setting properties, attributes, class names).

Use unit tests to cover the component reacting to client-side events fired by the web component. Use `ComponentUtil.fireEvent` to fire events in unit tests.

When a component invokes custom JavaScript from the server to modify client-side state, split test coverage by what varies: use unit tests that dump and assert pending JavaScript invocations from the UI to cover the full scenario matrix (e.g. whether invocation happens or not, different arguments to the invocation). Use a single integration test as smoke test per distinct client operation to prove the JS reaches the client and produces a real effect.

For coverage of JavaScript connectors, prefer web-test-runner tests under `vaadin-{component}-flow-integration-tests/test/*.test.ts` to cover the full scenario matrix. As above, integration tests should exist for the general case to verify integration of server component, web component and connector.

Do not add tests that cover behavior of mixin interfaces in each component that use them. Keep only a test to verify the mixin interface is implemented. The exception is when methods are overridden to add custom logic.

`@Route` and `@TestPath` annotations on integration tests views should use routes prefixed with the component element name, e.g. "vaadin-markdown/sub-route".

In TestBench element classes, methods that query for an element should return null for absent elements instead of throwing an exception.

When a TestBench element or integration test needs to query elements by a specific characteristic (text content, class name, attribute value), use the respective TestBench query API if it exists. Do not simply query all elements and do the filtering from Java as that results in N+1 WebDriver round-trips when accessing each element's data. When such a lookup can not be implemented with standard TestBench query APIs, or logic is otherwise complex and would require excessive web-driver round-trips, prefer a single `executeScript` that implements the logic via JavaScript.

Prefer using component-specific TestBench element classes in integration tests (e.g. `$(ButtonElement.class)`), instead of using raw string tag names (e.g. `$("vaadin-button")`).

Prefer NativeButton and other base HTML elements from the Flow framework when building integration test fixtures to avoid pulling in extra component-module dependencies into the test app.

Use `MockUIExtension` to set up a mocked UI and other Flow framework thread locals in unit tests. Avoid rolling your own setup / teardown logic as those have tended to be brittle or end up interfering with other tests.

Use `EnableFeatureFlagExtension` to properly enable, or temporarily disable, a feature flag in unit tests.

## Code Style

Keep the getter and setter for each individual property next to each other, the getter comes before the setter.

Place signal binding methods (`bind*`) after their getter / setter.

Prefer Java multiline strings over string concatenation when writing longer multi-line / multi-statement strings (e.g. JavaScript invocations).

Do not use Java instance initializer blocks to initialize fields, use the constructor instead.

Loggers should be created by passing the class reference, not by passing the class name or a custom string.

## Build & Dependencies

When adding a dependency to a module POM, do not use inline versions. Instead, declare the version in the root POM's dependency management section using a property, similar to how other dependencies are managed there. Flow framework dependency versions are managed through the Flow BOM.

When adding new published Maven modules to the project (a `-flow` or `-testbench` module), register them with a matching dependency in the BOM under `flow-components-bom/pom.xml`.

Keep POM dependencies minimal, only add what is really needed by the respective module. Do not simply copy dependencies from existing modules or add the whole range of Flow framework dependencies.

## Miscellaneous

Components and integration test fixtures must not use `Label` or `NativeLabel` just to display text. Labels must only be used when they are associated with an input.

To distinguish an in-memory data provider from a lazy / backend data provider, use `DataProvider.isInMemory()` instead of relying on instanceof checks.

When implementing a new feature flag, duplicate the setup from an existing component that uses it ( SPI provider, exception class, checking feature flag in `onAttach`). There is no need to extract common helpers as the code is trivial.
