# Testing

How components are tested: JUnit unit tests on the server side, TestBench
integration tests in a real browser, and web-test-runner (WTR) tests for
connector JavaScript.

## Choosing the test type

Prefer unit tests over integration tests when the logic is purely server-side
and only involves calling plain Flow framework APIs — mutating the DOM,
setting properties, attributes, or class names. There is nothing a browser
would add: the state is fully observable through the Element API.

This includes the component reacting to client-side events fired by the web
component — fire the event with `ComponentUtil.fireEvent` in a unit test
instead of triggering it in a real browser.

When a component invokes custom JavaScript from the server to modify
client-side state, split coverage by what varies:

- Use unit tests that dump and assert the UI's pending JavaScript invocations
  to cover the full scenario matrix — whether an invocation happens at all,
  and with which arguments.
- Keep a single integration test as a smoke test per distinct client
  operation, to prove the JS reaches the client and produces a real effect.

For JavaScript connectors, the same split applies: prefer WTR tests to cover
the full scenario matrix of the connector logic in isolation, and keep an
integration test for the general case to verify that server component, web
component and connector work together.

## Unit tests

JUnit 6 (Jupiter) with Mockito. Base classes: `AbstractSignalsTest` for
bindings, `MockUIExtension` when a UI / `VaadinSession` is needed,
`EnableFeatureFlagExtension` for flag-gated code.

Cover: every constructor overload; every setter/getter; `setXxx(null)` for
nullable setters; each theme variant; every listener (register + fire); every
`bind*` (binding works, imperative setter throws, side-effects run);
serialisation; i18n round-trip and JSON shape.

```java
class ExampleTest {
    @Test
    void valueCtor() {
        Assertions.assertEquals("foo", new Example("foo").getValue());
    }
}
```

### Serializable test

Each component **must** have a test to verify it is fully serializable. Use
`ClassesSerializableTest` as a base, which automatically verifies all classes
from the component package.

```java
import com.vaadin.flow.testutil.ClassesSerializableTest;

class ExampleSerializableTest extends ClassesSerializableTest {
}
```

In case the package holds classes that are not referenced from the UI,
`ClassesSerializableTest` supports configuration to exclude classes by pattern.

### Signal test

```java
class ExampleSignalTest extends AbstractSignalsTest {
    @Test
    void bindValue_throwsOnSetValue() {
        ValueSignal<String> signal = new ValueSignal<>("foo");
        Example c = new Example(signal);
        UI.getCurrent().add(c);
        Assertions.assertThrows(BindingActiveException.class, () -> c.setValue("bar"));
    }
}
```

### Mixin interface test

Assert a component declares an expected mixin interface:

```java
@Test
void implementsHasThemeVariant() {
    Assertions.assertTrue(
            HasThemeVariant.class.isAssignableFrom(Breadcrumbs.class));
}
```

## TestBench element

A thin DOM wrapper at
`vaadin-{component}-testbench/.../testbench/{Component}Element.java`:

```java
@Element("vaadin-example")
public class ExampleElement extends TestBenchElement {
    public String getValue() { return getPropertyString("value"); }
    public void setValue(String value) { setProperty("value", value); }
}
```

Implement the relevant TestBench `Has*` interfaces and add convenience methods
for common interactions (`open()`, `selectByText(String)`). Keep it lean. Model
the API around common user interactions. The actual implementation of each
method does not need to exactly simulate these user interactions, they can reach
for web component APIs and JavaScript if it speeds up the runtime performance by
reducing Selenium / WebDriver roundtrips.

## Integration tests

JUnit 4 + TestBench, served by Jetty, extending `AbstractComponentIT`. A test
view (`{Component}Page` under `src/main/.../tests`) with `@Route` exposes
elements by `id`; the IT (`{Component}IT` under `src/test/.../tests`) with a
matching `@TestPath` drives a real browser. The module includes a
`TestAppShell` (`AppShellConfigurator`) applying the Lumo theme, so ITs run
against Lumo-styled components.

```java
@TestPath("vaadin-example")
public class ExampleIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-example"));
    }

    @Test
    public void defaultValue_isEmpty() {
        Assert.assertEquals("", $(ExampleElement.class).id("default-example").getValue());
    }
}
```

Enable an experimental feature flag for ITs via
`src/main/resources/vaadin-featureflags.properties`.

## Client-side unit tests (WTR)

Connector JavaScript is tested in isolation with web-test-runner under
`vaadin-{component}-flow-integration-tests/test/*.test.ts`.
