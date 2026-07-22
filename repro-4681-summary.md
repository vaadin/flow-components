<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is `ListBox`/`MultiSelectListBox` clearing the current selection, triggered by calling `removeItem`/`addItem` on a *non-selected* item inside a `ValueChangeListener`, observable as no item selected (server value `null`/empty) after clicking an item.
- **Regression?:** not a regression (broken since introduction — data change has always reset the value)
- **Fixed by:** n/a (still present on main)
- **Duplicate of:** none found
- **Branch:** `repro/4681` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3-SNAPSHOT)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot** (static bug): ![List box selection cleared after item change in listener](https://raw.githubusercontent.com/vaadin/flow-components/21d20a141b7ade2da21c445bffc20e4138d128ad/repro-4681.png) — embeds inline.

## Observed behavior

Clicking `Hans` in each list box fires the `ValueChangeListener`, which calls `removeItem("Dorothee")` + `addItem("Dorothee")` (a *different*, non-selected item). Result:

- **A) single-select `ListBox`** — selection is lost. No item stays selected and `getValue()` returns `null` (the value span reads `null`).
- **B) `MultiSelectListBox`** (default `SelectionPreservationMode.DISCARD`) — selection is lost. `getSelectedItems()` is empty.
- **C) `MultiSelectListBox`** with `SelectionPreservationMode.PRESERVE_EXISTING` — selection is kept: `Hans` stays selected (value = `Hans`). This is the only working path.

## Expected behavior

Selection of the clicked item should be preserved when `removeItem`/`addItem` is called on another item — matching how `ComboBox`/`MultiSelectComboBox` behave.

## Steps to reproduce

1. Open `http://localhost:8080/repro-4681`.
2. Click `Hans` in list box A (single-select `ListBox`).
3. Observe: nothing is selected and the value span shows `null`.
4. Same for list box B (`MultiSelectListBox`, default mode); list box C (`PRESERVE_EXISTING`) keeps the selection.

## Reproduction

How to run: start the server (`mvn … jetty:run`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-4681`
- **Scaffold:** `vaadin-list-box-flow-parent/vaadin-list-box-flow-integration-tests/src/main/java/com/vaadin/flow/component/listbox/test/Repro4681View.java`

```java
// A) single-select ListBox — selection cleared (reporter: buggy)
ListBox<String> listBox = new ListBox<>();
listBox.setItems("Hans", "Franz", "Dorothee");
listBox.addValueChangeListener(e -> {
    var dv = listBox.getListDataView();
    dv.removeItem("Dorothee");
    dv.addItem("Dorothee");
    // listBox.getValue() is already null here
});

// C) MultiSelectListBox — selection kept only with PRESERVE_EXISTING
MultiSelectListBox<String> ms = new MultiSelectListBox<>();
ms.setItems("Hans", "Franz", "Dorothee");
ms.setSelectionPreservationMode(SelectionPreservationMode.PRESERVE_EXISTING);
```

## Root cause (suspected)

For any non-refresh `DataChangeEvent`, `ListBoxBase.handleDataChange` unconditionally calls `clear()` (resetting the value) before `rebuild()`. Single-select `ListBox` has no way to opt out:

https://github.com/vaadin/flow-components/blob/21d20a141b7ade2da21c445bffc20e4138d128ad/vaadin-list-box-flow-parent/vaadin-list-box-flow/src/main/java/com/vaadin/flow/component/listbox/ListBoxBase.java#L130-L137

`MultiSelectListBox` overrides this via a `SelectionPreservationHandler`, but its default is `DISCARD` → `onDiscard()` → `clear()`, so default behavior still clears:

https://github.com/vaadin/flow-components/blob/21d20a141b7ade2da21c445bffc20e4138d128ad/vaadin-list-box-flow-parent/vaadin-list-box-flow/src/main/java/com/vaadin/flow/component/listbox/MultiSelectListBox.java#L69-L100

## Notes

- The `SelectionPreservationMode` API (added 24.4) exists **only on `MultiSelectListBox`**. Single-select `ListBox` has no selection-preservation option at all, so its case cannot be worked around today — the more significant gap.
- The reporter's example mislabels the ComboBox variants (out of scope for this repo's list-box module); the list-box cases are what the `vaadin-list-box` label covers and are reproduced here.
- No relevant client console errors (only a favicon 404 and dev-mode notices).
