# Reproduction: vaadin-grid — IllegalArgumentException when a row is dropped on a header filter (#1453)

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Issue:** https://github.com/vaadin/flow-components/issues/1453
- **Verdict:** reproduced
- **Hypothesis tested:** The bug is that `GridDropEvent`'s constructor cannot be created, triggered by dropping a row on a header filter field (the client sends a `grid-drop` event with an empty `dropLocation`), observable as a server-side `IllegalArgumentException` caused by `NoSuchElementException` at `GridDropEvent.<init>`.
- **Branch:** `repro/1453` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `24.10` (flow 24.10-SNAPSHOT, `@vaadin/grid` 24.10.2 and earlier)
- **Present on main?:** no — fixed by the web component in [vaadin/web-components#10351](https://github.com/vaadin/web-components/pull/10351) (released in 25.0.0, backported to 24.8.x / 24.9.x / 24.10.3). The server-side fragility in `GridDropEvent` is still present on `main`, but the client no longer emits the empty event that triggers it.
- **Theme / Browser:** Lumo / Chromium (matches the reporters: Chrome-only)

## Observed behavior

Dropping a dragged row onto the header filter `TextField` makes the client dispatch a `grid-drop` event whose `detail.dropLocation` is empty. The server fails to construct the event and logs the exact stack trace from the issue:

```
ERROR com.vaadin.flow.server.DefaultErrorHandler - Unexpected error: Unable to create an event object of type com.vaadin.flow.component.grid.dnd.GridDropEvent
java.lang.IllegalArgumentException: Unable to create an event object of type com.vaadin.flow.component.grid.dnd.GridDropEvent
	at com.vaadin.flow.component.ComponentEventBus.createEventForDomEvent(ComponentEventBus.java:543)
	...
Caused by: java.util.NoSuchElementException: No value present
	at java.base/java.util.Optional.get(Optional.java:143)
	at com.vaadin.flow.component.grid.dnd.GridDropEvent.<init>(GridDropEvent.java:86)
```

There is no client-visible symptom — the page keeps working; the failure is server-side only.

## Expected behavior

Dropping a row on a header filter field (or any non-row target that yields no drop location) should be ignored gracefully, not crash the server with an unhandled exception.

## Steps to reproduce

1. Create a grid that is both draggable (`setRowsDraggable(true)`) and a drop target (`setDropMode(GridDropMode.BETWEEN)`), with a `TextField` filter in the column header and a `addDropListener`.
2. Drag a row and drop it on the header filter `TextField` (the text cursor appears over the field).
3. Observe the server log: `IllegalArgumentException` / `NoSuchElementException` at `GridDropEvent.<init>`.

## Reproduction

How to run: start the server and open the route below.

```bash
cd flow-components
CI=true mvn package jetty:run -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -DskipTests \
  -pl vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests
```

- **Route / page:** `http://localhost:8080/repro-1453`
- **Scaffold:** `vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests/src/main/java/com/vaadin/flow/component/grid/it/Repro1453View.java`

```java
@Route("repro-1453")
public class Repro1453View extends Div {
    public Repro1453View() {
        Grid<String> grid = new Grid<>();
        grid.setId("grid");

        TextField filter = new TextField();
        filter.setId("filter");
        filter.setPlaceholder("Filter");

        grid.addColumn(item -> item).setHeader(filter);
        grid.setItems(IntStream.range(0, 5).mapToObj(i -> "Item " + i)
                .collect(Collectors.toList()));

        grid.setRowsDraggable(true);
        grid.setDropMode(GridDropMode.BETWEEN);

        Div dropMessage = new Div();
        dropMessage.setId("drop-message");
        grid.addDropListener(e -> dropMessage.setText("drop: "
                + e.getDropLocation() + " on " + e.getDropTargetItem().orElse(null)));

        add(grid, dropMessage);
    }
}
```

Because the buggy client (`@vaadin/grid` ≤ 24.10.2) is no longer pulled by current maintenance lines, the empty `grid-drop` event was reproduced by dispatching the exact payload the pre-fix client emits when dropping on the header — verified against the [#10351](https://github.com/vaadin/web-components/pull/10351) diff:

```js
document.querySelector('#grid').dispatchEvent(new CustomEvent('grid-drop', {
  bubbles: true, cancelable: true,
  detail: { dropTargetItem: undefined, dropLocation: undefined,
            dragData: [{ type: 'text', data: 'Item 0' }] }
}));
```

This drives the real Flow client→server round-trip and the real `GridDropEvent` constructor, producing the reported stack trace.

## Root cause (suspected)

`vaadin-grid-flow-parent/vaadin-grid-flow/src/main/java/com/vaadin/flow/component/grid/dnd/GridDropEvent.java:86` —

```java
this.dropLocation = Arrays.asList(GridDropLocation.values()).stream()
        .filter(dl -> dl.getClientName().equals(dropLocation))
        .findFirst().get();   // <-- throws NoSuchElementException when dropLocation is empty/unknown
```

When the client sends an empty/unknown `dropLocation`, no enum value matches and the bare `.get()` on an empty `Optional` throws. The server has no defensive handling for a missing/unrecognized drop location.

The client-side cause was fixed in [vaadin/web-components#10351](https://github.com/vaadin/web-components/pull/10351): `_onDrop` now only dispatches `grid-drop` when `_dropLocation` is set (`if (this.dropMode && this._dropLocation)`), so dropping on a header/input no longer emits an empty event. The server-side `.get()` remains unguarded, so the defect could resurface from any future client that sends an empty location.

## Notes

- **Version-specific.** Affected: `@vaadin/grid` ≤ 24.10.2 (and the 24.x lines before the #10351 backport). Fixed: 25.0.0, and backported to 24.8.x / 24.9.x / 24.10.3. The current `24.10` branch build pulls `@vaadin/grid` 24.10.3, which already contains the fix, so a plain run there does **not** reproduce — this is why the genuine pre-fix `grid-drop` payload had to be emitted explicitly.
- Reporters noted it is Chrome-only and tied to `GridDropMode.BETWEEN` / the filter field showing a text cursor on dragover — consistent with the header early-return path that leaves `_dropLocation` undefined.
- No client-visible symptom; the only evidence is the server log.
