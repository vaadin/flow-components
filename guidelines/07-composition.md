# Composition

## Prefix / suffix slots

Form-style components implement `HasPrefix` / `HasSuffix`:

```java
field.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
field.setSuffixComponent(new Div("USD"));
```

Slots are managed by `SlotUtils.setSlot(this, "prefix", component)` — don't set
`slot=` attributes by hand when a shared utility exists.

## Manual slot management

For a component-specific slot (e.g. Button's `icon` slot moving between prefix
and suffix), use the `Element` API directly:

```java
iconComponent.getElement().setAttribute("slot", iconAfterText ? "suffix" : "prefix");
```

## Container components

Components holding children implement `HasComponents` (or a narrower
`Has…Items`) and add typed convenience overloads so callers can chain setup:

```java
public AccordionPanel add(String summary, Component content) {
    return add(new AccordionPanel(summary, content));
}
// accordion.add("Details", body).setOpened(true);
```

When children are computed at runtime, expose `setItems(...)` instead — see
[Data & Validation](08-data-and-validation.md).

## Connectors (JavaScript glue)

Add a connector only when bridging `DataCommunicator`, renderers, or dynamic DOM
needs custom JS. Place it at
`src/main/resources/META-INF/resources/frontend/{name}Connector.js` and load it
with `@JsModule("./{name}Connector.js")`.

Use the `initLazy` + `$connector`-guard pattern, and initialise it in the attach
handler:

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
element for the same server-side instance after detach/re-attach. The connector
must re-initialise each time; running it once in the constructor leaves the
second client element without a connector.

Typical connectors: `comboBoxConnector.js` (lazy `dataProvider` ↔
`DataCommunicator`), `contextMenuConnector.js` / `menuBarConnector.js` (nested
menu structure), `flow-component-renderer.js` (server components inside cells).
