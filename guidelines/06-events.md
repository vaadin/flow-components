# Events

## Defining an event

A nested `public static class` extending `ComponentEvent<T>`, annotated
`@DomEvent` with the client DOM event name; pull payload with `@EventData`:

```java
@DomEvent("opened-changed")
public static class OpenedChangeEvent extends ComponentEvent<Accordion> {
    private final Integer index;

    public OpenedChangeEvent(Accordion source, boolean fromClient,
            @EventData("event.detail.value") Integer index) {
        super(source, fromClient);
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }
}
```

`fromClient` marks whether the event came from the browser or a server-side
fire.

## Registering listeners

```java
public Registration addOpenedChangeListener(
        ComponentEventListener<OpenedChangeEvent> listener) {
    return ComponentUtil.addListener(this, OpenedChangeEvent.class, listener);
}
```

- Name methods `add{EventName}Listener`.
- Return `Registration`; callers unregister with `registration.remove()`.
- Do NOT add a `remove…Listener` method.

## Built-in listeners

Prefer the listeners that come from mixins over a parallel custom event:
`ClickNotifier` → `addClickListener`, `Focusable` → `addFocusListener` /
`addBlurListener`, `HasValue` → `addValueChangeListener`.
