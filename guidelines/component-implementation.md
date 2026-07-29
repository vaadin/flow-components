# Component Implementation

How a component works on the inside: the class and its annotations, the
`Element` API, connectors, property synchronization, and code style. A
component is a Java class that delegates its state to its `Element`.

The server/client split behind that Element is an implementation detail and must 
not leak into the public API. To developers the API looks like ordinary Java
object state, with the synchronization machinery invisible in the API, Javadoc
and behavior.

## The main class

A component extends `Component` (or `AbstractSinglePropertyField<C, V>` for
value fields, `AbstractField<C, T>` for richer fields), declares its
annotations, implements the mixin interfaces it needs, and delegates state to
`getElement()`.

```java
@Tag("vaadin-example")
@NpmPackage(value = "@vaadin/example", version = "25.3.0-alpha1")
@JsModule("@vaadin/example/src/vaadin-example.js")
public class Example extends Component
        implements HasEnabled, HasSize, HasStyle, HasThemeVariant<ExampleVariant> {

    public Example() {
    }

    public Example(String value) {
        this();
        setValue(value);
    }

    public void setValue(String value) {
        getElement().setProperty("value", value == null ? "" : value);
    }

    public String getValue() {
        return getElement().getProperty("value", "");
    }
}
```

## Annotations

- `@Tag("vaadin-{name}")` — maps the class to the web component tag.
- `@NpmPackage(value = "@vaadin/{name}", version = "25.x.y")` — declare the NPM 
  dependency that contains the wrapped web component. Version should match those
  of other components.
- `@JsModule("@vaadin/{name}/src/vaadin-{name}.js")` — loads the web component
  module.
- `@JsModule("./vaadin-{name}/{name}Connector.js")` — optional connector.

## The Element API

Components work through the `Element` — reading and writing properties and
attributes, managing children, and calling into client-side JavaScript:

```java
getElement().getProperty("value", "");                                          // read property
getElement().setProperty("disabled", true);                                     // sync property
getElement().setAttribute("img", url);                                          // attribute
getElement().callJsFunction("open");                                            // call a method
getElement().executeJs("window.Vaadin.Flow.exampleConnector.initLazy(this)");   // run JS
getElement().appendChild(child.getElement());                                   // add child
```

## Connectors (JavaScript glue)

Add a connector only when bridging `DataCommunicator`, renderers, or dynamic
DOM needs custom JS. Place it in a component-named subfolder,
`src/main/resources/META-INF/frontend/vaadin-{name}/{name}Connector.js`, and
load it with `@JsModule("./vaadin-{name}/{name}Connector.js")`.

Use the `initLazy` + `$connector`-guard pattern, and initialise it in the
attach handler:

```javascript
window.Vaadin.Flow.exampleConnector = {
  initLazy: (element) => {
    if (element.$connector) return;
    element.$connector = {};
    // wire up data provider, event handlers, etc.
  }
};
```

```java
@Override
protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    getElement().executeJs("window.Vaadin.Flow.exampleConnector.initLazy(this)");
}
```

**Why `onAttach`, not the constructor?** Flow creates a fresh client-side
element for the same server-side instance after detach/re-attach. The
connector must re-initialise each time; running it once in the constructor
leaves the second client element without a connector.

Typical connectors: `comboBoxConnector.js` (lazy `dataProvider` ↔
`DataCommunicator`), `contextMenuConnector.js` / `menuBarConnector.js` (nested
menu structure), `flow-component-renderer.js` (server components inside cells).
These older connectors still sit directly under `META-INF/frontend/` — that
placement is legacy; new files always go in a component-named subfolder.

## Property synchronization & trust (security)

Everything arriving from the browser is user-controllable. A property may only
synchronize to the server if the user can legitimately change it through the
component's UI; anything else is a tampering channel. For example, users can
toggle the opened state of a details component, so `opened` may synchronize —
but they can never legitimately change a component's validation state or
visibility, so those must not.

The same applies to values: input tampered with through client-side scripting
should be ignored, not turned into server-side exceptions that fill the logs.
