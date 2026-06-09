# Component Structure

## The main class

A component extends `Component` (or `AbstractSinglePropertyField<C, V>` for
value fields, `AbstractField<C, T>` for richer fields), declares its
annotations, implements the mixin interfaces it needs, and delegates state to
`getElement()` or to helpers like `SignalPropertySupport`.

```java
@Tag("vaadin-example")
@NpmPackage(value = "@vaadin/example", version = "25.2.0-alpha7")
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

### Annotations

- `@Tag("vaadin-{name}")` — maps the class to the web component tag.
- `@NpmPackage(value = "@vaadin/{name}", version = "25.x.y")` — version MUST
  match the published web component.
- `@JsModule("@vaadin/{name}/src/vaadin-{name}.js")` — loads the module.
- `@JsModule("./{name}Connector.js")` — optional connector (see
  [Composition](07-composition.md)).

## Constructors — progressive disclosure

Cover the common combinations with overloads (empty / text / icon / value /
signal / listener and their combinations). Add a `Signal<…>` variant beside each
value parameter:

```java
public Button() {}
public Button(String text) { this(); setText(text); }
public Button(Signal<String> textSignal) { this(); bindText(textSignal); }
public Button(String text, Component icon) { this(); setIcon(icon); setText(text); }
public Button(String text, ComponentEventListener<ClickEvent<Button>> l) { this(); setText(text); addClickListener(l); }
```

Reach for an overload — not a setter chain — for new common-case shortcuts.

## The Element API

Components work through the `Element`:

```java
getElement().setProperty("disabled", true);          // sync property
getElement().getProperty("value", "");               // read property
getElement().setAttribute("theme", "primary");       // attribute
getElement().executeJs("this.focus()");              // run JS
getElement().callJsFunction("open");                 // call a method
getElement().appendChild(child.getElement());        // add child
```

- Scalar state: `setProperty` / `getProperty`.
- State that must throw under an active binding: `Element.bindProperty` or
  `SignalPropertySupport` (see [Signals](05-signals.md)).
- Never reach past `getElement()` into a child's DOM — use child `Component`s.

## Boolean properties — positive form

Use the positive Java form and flip the polarity at the element boundary when
the attribute is negative (see
[Design](02-design.md#prefer-positive-form-boolean-apis)).

```java
// custom boolean state with no pre-built mixin
public void setEditable(boolean editable) {
    getElement().setProperty("readonly", !editable);
}
public boolean isEditable() {
    return !getElement().getProperty("readonly", false);
}
```

`HasEnabled` and `setVisible` already handle their flips. When the attribute is
already natural (`opened`, `required`), keep the same polarity.

## Feature-flag check (experimental components)

```java
private boolean isFeatureFlagEnabled(Feature feature) {
    UI ui = UI.getCurrent();
    return ui != null && FeatureFlags
            .get(ui.getSession().getService().getContext()).isEnabled(feature);
}
```

Enable the flag in tests — `vaadin-featureflags.properties` for ITs,
`EnableFeatureFlagExtension` for unit tests (see [Testing](12-testing.md)).

## Component archetypes

| Archetype          | Template                              | Key interfaces / traits                                                       |
| ------------------ | ------------------------------------- | ----------------------------------------------------------------------------- |
| Simple interactive | `Button`                              | `ClickNotifier`, `Focusable`, `HasText`, `HasThemeVariant`, `HasTooltip`      |
| Field / input      | `TextField`, `ComboBox`, `DatePicker` | a field base; `InputField`, `HasValidationProperties`, `ValidationController` |
| Overlay            | `Dialog`, `Popover`                   | `open()` / `close()` / `isOpened()`, `@Synchronize("opened-changed")`         |
| Data-driven list   | `ComboBox`, `Grid`                    | `HasDataView` family, `DataCommunicator`, JS connector, `setItems` overloads  |
| Navigational       | `SideNav`                             | `Class<? extends Component>` routes, `RouteConfiguration`                     |
| Container / layout | `VerticalLayout`, `Accordion`         | `HasComponents` / `HasOrderedComponents`, `add` / `remove`                    |
