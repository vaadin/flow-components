<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is that a `MenuItem` checked-state change made from the opened-change-listener is not reflected in the just-opened menu, triggered by calling `setChecked()` during the open round-trip while the overlay renders from an already-built items array, observable as the visible checkmark lagging exactly one open behind the state Java reports.
- **Regression?:** not a regression (broken since introduction — inherent to how `setChecked` updates an open menu)
- **Fixed by:** n/a
- **Duplicate of:** none found
- **Branch:** `repro/1010` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `25.3-SNAPSHOT` (main)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot** (static bug): ![Checkmark on item 0 while status says item 1 is checked](https://raw.githubusercontent.com/vaadin/flow-components/a93bce998cc5e7e14c2a0f2c79af84fd88fdc5bd/repro-1010.png) — embeds inline.

## Observed behavior

Menu items are checkable; the opened-change-listener advances the "currently checked" index and calls `setChecked` on the items each time the menu opens.

- **First open:** the status line reads `Item 1 should be checked: Java says true`, but the visible checkmark (`menu-item-checked` attribute) is on **item 0**.
- **Second open:** status reads `Item 2 should be checked: Java says true`, but the checkmark is on **item 1**.

The displayed checkmark is always exactly one open behind the server-side state — matching the reporter's "visible lazily, as if reflected when the menu is opened for a second time".

## Expected behavior

When `setChecked` is called from the opened-change-listener, the menu that is being opened should show the checkmark on the item Java reports as checked — not the item that was checked before the listener ran.

## Steps to reproduce

1. Open `http://localhost:8080/repro-1010`.
2. Click the "Say hello" button to open the context menu.
3. Note the status line says item 1 should be checked (`Java says true`), but the checkmark is on item 0.
4. Press Escape, click the button again: status says item 2, checkmark is on item 1.

## Reproduction

How to run: start the server (`mvn … jetty:run`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1010`
- **Scaffold:** `vaadin-context-menu-flow-parent/vaadin-context-menu-flow-integration-tests/src/main/java/com/vaadin/flow/component/contextmenu/it/Repro1010View.java`

```java
ContextMenu cm = new ContextMenu(button);
cm.setOpenOnClick(true);
for (int i = 0; i < 10; i++) {
    final MenuItem menuItem = cm.addItem("item " + i);
    menuItem.setCheckable(true);
    menuItems.put(i, menuItem);
}
cm.addOpenedChangeListener(e -> {
    if (e.isOpened()) {
        currentlyChecked++;
    }
    updateCurrentlyChecked(); // calls setChecked(...) on each item
});
```

## Root cause (suspected)

`MenuItem.setChecked` sends the new state to the client via `contextMenuConnector.setChecked`, which updates only the internal `_item.checked` value. It toggles the visible `menu-item-checked` attribute (and thus the checkmark) **only for `keepOpen` items**; for a normal item it relies on the next full menu re-render. When the checked state changes during the open round-trip, the overlay has already rendered from the items array built before the listener ran, so the checkmark is not refreshed and lags one open behind:

https://github.com/vaadin/flow-components/blob/893da700e059a21361c5aa7b622151775c461805/vaadin-context-menu-flow-parent/vaadin-context-menu-flow/src/main/resources/META-INF/frontend/contextMenuConnector.js#L79-L89

## Notes

- Faithful port of the reporter's Vaadin 14.5.1 example, modernized to the current API (`NativeButton` instead of `Button` to avoid adding a cross-component dependency to the IT module; `currentlyChecked` wraps at 10 so the demo cycles).
- No console errors beyond dev-server noise (favicon 404, Lit dev-mode warning).
