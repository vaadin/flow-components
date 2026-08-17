<!-- Edit any field. This file is committed on the `repro/9883` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is `ComboBox.focusOnSelectedItem()` sending an item index resolved against the **unfiltered** item list, triggered by typing a filter that opens the dropdown while `setFocusSelectedItem(true)` is on and items fit a single page (client-side filtering), observable as the wrong item highlighted (`_focusedIndex` 15 → "Banana 15") and committed as the value when the dropdown closes on outside click.
- **Regression?:** not a regression (broken since `setFocusSelectedItem` was introduced in 25.2, vaadin/flow-components#9239)
- **Fixed by:** n/a — still broken
- **Duplicate of:** none found (`gh search issues "combo-box focusSelectedItem"` across `flow-components` and `web-components` returns no other issue)
- **Branch:** `repro/9883` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3-SNAPSHOT), commit `79f402a48087f4138edb5e21af9d07f14418a1b5`
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot** (static bug): ![Dropdown filtered to the 20 "Banana" items with "Banana 15" highlighted while the selected value is "Banana 5"](https://raw.githubusercontent.com/vaadin/flow-components/9506bdc7e4806f13ec15fa460c6869098fd25406/repro-9883.png)

## Observed behavior

With `setFocusSelectedItem(true)`, value `"Banana 5"` (index 15 of 30 unfiltered items, index 5 among the 20 `"Banana"` items), after typing the filter `Banana`:

```js
comboBox._focusedIndex;                                 // 15
comboBox._dropdownItems.length;                         // 20
comboBox._dropdownItems[comboBox._focusedIndex].label;  // "Banana 15"
comboBox.selectedItem.label;                            // "Banana 5"  (still correct)
```

The only dropdown row carrying the `focused` attribute is `"Banana 15"`, and the dropdown is scrolled to it (see screenshot).

After closing the dropdown with an outside click:

```js
comboBox.selectedItem.label;   // "Banana 15"
```

The server-side value follows: the `Span` bound to the value-change listener changes from `Banana 5` to `Banana 15`, i.e. a value change event fires for an item the user never picked.

The side-by-side control with `setFocusSelectedItem(false)` keeps `_focusedIndex` at `-1` through the same steps, and the value stays `Banana 5` before and after the outside click.

Browser console is clean — 0 errors (only the dev-mode Lit warning and a favicon 404).

## Expected behavior

Typing a filter and clicking outside keeps the value unchanged. If an item is focused while a filter is active, it should be the selected item, matched by identity rather than by index.

## Steps to reproduce

1. Open the route below.
2. Focus the combo box input **without clicking it** (e.g. Tab into it) and select all its text.
3. Type `Banana` in one quick burst, so the dropdown opens with the 20 "Banana" items.
4. "Banana 15" is highlighted instead of "Banana 5".
5. Click outside the combo box to close the dropdown.
6. The value changes to "Banana 15" and a value change event fires.

**Timing matters** — see Notes. Clicking the input first, or typing slowly character by character, hides the bug.

## Reproduction

How to run: start the server (`mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-9883`
- **Scaffold:** `vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests/src/main/java/com/vaadin/flow/component/combobox/test/Repro9883View.java`

```java
private Div comboBoxSection(String id, boolean focusSelectedItem,
        String description) {
    ComboBox<String> comboBox = new ComboBox<>();
    comboBox.setId(id);
    comboBox.setItems(items()); // "Apple 0".."Apple 9", "Banana 0".."Banana 19"
    comboBox.setFocusSelectedItem(focusSelectedItem);
    comboBox.setValue("Banana 5"); // index 15 unfiltered, index 5 among "Banana"

    Span value = new Span(comboBox.getValue());
    value.setId(id + "-value");
    comboBox.addValueChangeListener(
            event -> value.setText(String.valueOf(event.getValue())));

    NativeButton reset = new NativeButton("Reset value",
            event -> comboBox.setValue("Banana 5"));
    reset.setId(id + "-reset");

    return new Div(new Paragraph(description), comboBox,
            new Div(new Span("Server-side value: "), value), reset);
}
```

The view puts the failing case (`true`) next to a control (`false`) so the trigger is isolated.

## Root cause (suspected)

`focusOnSelectedItem()` resolves the index through the data view, which builds its query from the data communicator. With in-memory items that fit a single page, the combo box filters on the client and the server never learns about the filter, so the index is computed against the full item list and then applied to the filtered list on the client:

https://github.com/vaadin/flow-components/blob/79f402a48087f4138edb5e21af9d07f14418a1b5/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/java/com/vaadin/flow/component/combobox/ComboBox.java#L426-L449

The web component's `__focusIndex` only rejects indexes past the end of the filtered list, so an index that happens to be in range is applied as is. Its own javadoc states the commit side effect is the caller's responsibility:

https://github.com/vaadin/web-components/blob/main/packages/combo-box/src/vaadin-combo-box-focus-index-mixin.js#L12-L37

That points the fix at the Flow side: either skip focusing while a client-side filter is active, or pass the item and let the client match it by identity within `_dropdownItems` instead of by index.

## Notes

- **The bug is timing-sensitive**, which is worth knowing when writing a regression IT. Because items fit one page, the only server round trip is the `opened` property change; the client keeps re-filtering meanwhile. The web component recomputes `_focusedIndex` on every items update ([`vaadin-combo-box-mixin.js#L536-L546`](https://github.com/vaadin/web-components/blob/main/packages/combo-box/src/vaadin-combo-box-mixin.js#L536-L546)), so:
  - typing `Banana` character by character with ~80 ms delays → the `__focusIndex(15)` response lands mid-typing and a later items update resets `_focusedIndex` to `-1`: **no bug**;
  - typing the filter in one burst (realistic fast typing or paste), so the round trip returns after the filter settles → `_focusedIndex` stays `15`: **bug**.
- Clicking the input to focus it also hides the bug: the click opens the dropdown with an empty filter, `__focusIndex(15)` is applied to the unfiltered list (correctly focusing "Banana 5"), and the subsequent filter keystrokes reset `_focusedIndex`. The input must be focused without opening the dropdown, as in the issue's steps.
- The existing IT `ComboBoxFocusSelectedItemIT.inMemory_filterActive_doesNotErrorOut` cannot catch this: it selects "Item 30" and filters with `"Item 1"`, leaving 11 items, so `__focusIndex` returns early on the out-of-range check.
- No dependency changes were needed in the IT module pom.
- Related: vaadin/flow-components#9239 (feature), vaadin/flow-components#9396 (earlier fix for wrapped in-memory data providers).
