# Events

Every component event corresponds to something real: either a client
interaction on the web component, or a change to state the server tracks.
Which of the two it reports determines how the event is wired.

## Defining an event

A nested `public static class` extending `ComponentEvent<T>`. `fromClient`
marks whether the event came from the browser or a server-side fire.

```java
public static class OpenedChangeEvent extends ComponentEvent<Accordion> {
    private final Integer index;

    public OpenedChangeEvent(Accordion source, boolean fromClient,
            Integer index) {
        super(source, fromClient);
        this.index = index;
    }

    public OptionalInt getOpenedIndex() {
        return index == null ? OptionalInt.empty() : OptionalInt.of(index);
    }
}
```

How the event is fired depends on what it reports — see the two sections
below.

## Interaction events

Interactions that only the client can cause and that don't change
server-tracked state: clicks, cell focus, a custom value being entered. Map
the web component's DOM event with `@DomEvent` and pull payload with
`@EventData`:

```java
@DomEvent("custom-value-set")
public static class CustomValueSetEvent<TComponent extends ComboBoxBase<TComponent, ?, ?>>
        extends ComponentEvent<TComponent> {
    private final String detail;

    public CustomValueSetEvent(TComponent source, boolean fromClient,
            @EventData("event.detail") String detail) {
        super(source, fromClient);
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }
}
```

The payload may still be resolved into server-side objects — e.g.
`Grid.ItemClickEvent` receives the item key via `@EventData` and resolves it
to the item on the server. Needing server-side data is not a reason to avoid
`@DomEvent`.

## State-change events

Events that report a change to state the server tracks: opened, selection,
expansion, column order, upload progress. Such state changes from two
origins — a user interaction on the client or a server API call — and the
event must fire for both, exactly once per change, after the server state is
updated, with `fromClient` reporting the origin. A plain `@DomEvent` cannot
provide this: a DOM event only reaches the server for client-originated
changes. State-change events are therefore fired programmatically from
wherever the server applies the change:

- State synced as an element property: fire from
  `Element.addPropertyChangeListener`, passing `isUserOriginated()` as
  `fromClient`. Both client and server changes go through the same listener:

  ```java
  getElement().addPropertyChangeListener("opened", event -> {
      OptionalInt openedIndex = getOpenedIndex();
      fireEvent(new OpenedChangeEvent(this, event.isUserOriginated(),
              openedIndex.isPresent() ? openedIndex.getAsInt() : null));
  });
  ```

- State managed by server-side logic: fire from the code path that applies
  the change — `fromClient = false` on server API paths, `true` on
  client-triggered paths (`TreeGrid` expand/collapse, `Upload` lifecycle
  events).

An event class can combine both mechanisms when the client reports the change
as a DOM event: `Grid.ColumnReorderEvent` is `@DomEvent`-mapped for
client-side reorders and additionally fired from `setColumnOrder(...)` with
`fromClient = false`.

## Registering listeners

Provide a method to register an event listener that returns a registration for
unregistering the listener:

```java
public Registration addOpenedChangeListener(
        ComponentEventListener<OpenedChangeEvent> listener) {
    return ComponentUtil.addListener(this, OpenedChangeEvent.class, listener);
}
```

The registration is the conventional way to unregister listeners, there is no
need to provide methods to remove listeners.

## Built-in listeners

Prefer the listeners that come from mixins over a parallel custom event:
`ClickNotifier` → `addClickListener`, `Focusable` → `addFocusListener` /
`addBlurListener`, `HasValue` → `addValueChangeListener`.
