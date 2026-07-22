<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is MenuBar rendering empty, triggered by detaching it while an item update is still pending, then a browser refresh (`@PreserveOnRefresh`) and reattach, observable as no items in the menu bar (only the empty overflow button) after reattach.
- **Regression?:** not a regression (inherent in the `updateScheduled` scheduling; no detach reset has ever existed)
- **Fixed by:** n/a (still present on main)
- **Duplicate of:** none found
- **Branch:** `repro/3920` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3-SNAPSHOT)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot** (static bug): ![MenuBar renders empty after detach + refresh + reattach](https://raw.githubusercontent.com/vaadin/flow-components/1ee850245b3e338152479de6100934f00cdc57a6/repro-3920.png) — embeds inline.

## Observed behavior

Using the scaffold below on `main`:

1. Page loads, MenuBar shows `item 1`, `item 2`.
2. Click "modify and detach" — adds `item 3` and removes the MenuBar in the same request (so an item update is scheduled, then the node is detached before the `beforeClientResponse` that resets `updateScheduled` fires).
3. Refresh the browser (`@PreserveOnRefresh` keeps the server-side component, with `updateScheduled` stuck `true`).
4. Click "toggle attached" to re-add the MenuBar.

Result: the MenuBar is present but **empty** — `0` items, only the (empty) overflow `···` button. The `updateScheduled` flag stays `true`, so `resetContent()` → `updateButtons()` returns early and never regenerates the items.

**Minimal pair — the refresh is the trigger:** the exact same modify-and-detach + reattach **without** the browser refresh renders all 3 items correctly. Adding the refresh between detach and reattach is what leaves the menu empty.

## Expected behavior

The MenuBar should show the items that were added, regardless of a browser refresh while it was detached.

## Steps to reproduce

1. Open `http://localhost:8080/repro-3920`.
2. Click "modify and detach".
3. Refresh the browser (F5).
4. Click "toggle attached".
5. Observe the MenuBar renders empty (no items).

## Reproduction

How to run: start the server (`mvn … jetty:run`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-3920`
- **Scaffold:** `vaadin-menu-bar-flow-parent/vaadin-menu-bar-flow-integration-tests/src/main/java/com/vaadin/flow/component/menubar/tests/Repro3920View.java`

```java
@Route("repro-3920")
@PreserveOnRefresh
public class Repro3920View extends Div {
    private final MenuBar menuBar = new MenuBar();
    private final Div holder = new Div();

    public Repro3920View() {
        menuBar.addItem("item 1");
        menuBar.addItem("item 2");
        holder.add(menuBar);

        NativeButton toggleAttached = new NativeButton("toggle attached", e -> {
            if (menuBar.getParent().isPresent()) holder.remove(menuBar);
            else holder.add(menuBar);
        });
        // Schedules an update AND detaches in the SAME request, so the
        // beforeClientResponse that resets updateScheduled never fires.
        NativeButton modifyAndDetach = new NativeButton("modify and detach", e -> {
            menuBar.addItem("item 3");
            holder.remove(menuBar);
        });
        add(toggleAttached, modifyAndDetach, holder);
    }
}
```

## Root cause (suspected)

`updateButtons()` sets `updateScheduled = true` and only resets it to `false` inside a `beforeClientResponse` callback. If the MenuBar is detached before that callback runs (and, with `@PreserveOnRefresh`, the pending callback is dropped when the UI is recreated on refresh), the flag stays `true`. On reattach, `updateButtons()` early-returns and never re-generates the items:

https://github.com/vaadin/flow-components/blob/1ee850245b3e338152479de6100934f00cdc57a6/vaadin-menu-bar-flow-parent/vaadin-menu-bar-flow/src/main/java/com/vaadin/flow/component/menubar/MenuBar.java#L454-L467

`MenuItemsArrayGenerator.generate()` uses the identical pattern and the same stuck-flag exposure:

https://github.com/vaadin/flow-components/blob/1ee850245b3e338152479de6100934f00cdc57a6/vaadin-context-menu-flow-parent/vaadin-context-menu-flow/src/main/java/com/vaadin/flow/component/contextmenu/MenuItemsArrayGenerator.java#L56-L70

The reporter's suggested fix — resetting both flags to `false` in `onDetach` — is not present in the current code.

## Notes

- Neither of the existing IT pages exercises this: `MenuBarPreserveOnRefreshPage` has no detach/reattach, and `MenuBarDetachReattachPage` has no `@PreserveOnRefresh`. The bug needs both plus a pending update at detach time.
- No relevant client console errors (no `jar-resources` connector TypeError).
