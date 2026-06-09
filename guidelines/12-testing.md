# Testing

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

### Serializable test — ALWAYS

```java
@Test
void isSerializable() throws Exception {
    Example c = new Example("value");
    var baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(c);
    var ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    Assertions.assertEquals("value", ((Example) ois.readObject()).getValue());
}
```

Also add `{Component}VariantTest` (each enum value → expected token) and
`{Component}I18nTest` where applicable.

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
for common interactions (`open()`, `selectByText(String)`). Keep it lean.

## Integration tests

JUnit 4 + TestBench, served by Jetty, extending `AbstractComponentIT`. A test
view (`{Component}Page` under `src/main/.../tests`) with `@Route` exposes
elements by `id`; the IT (`{Component}IT` under `src/test/.../tests`) with a
matching `@TestPath` drives a real browser.

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

Run them:

```sh
mvn verify -am -pl vaadin-example-flow-parent/vaadin-example-flow-integration-tests -DskipUnitTests
# single method — append a * (TestBench mangles method names with browser info):
mvn verify -am -pl …-integration-tests -Dit.test='ExampleIT#defaultValue_isEmpty*' -DskipUnitTests
```

Enable an experimental feature flag for ITs via
`src/main/resources/vaadin-featureflags.properties`. When running against an
already-running server, add `-DskipJetty`.
