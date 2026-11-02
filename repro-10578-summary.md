<!-- Edit any field. This file is committed on the `repro/10578` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is a redundant `resetDataCommunicator` call sent from `$connector.reset()`, triggered by `setItems()` in a focus listener while the dropdown is opening via the toggle button, observable as `_dropdownItems` full of `ComboBoxPlaceholder` while real items render, and the item click being dropped.
- **Regression?:** worked in 25.2.9 / broke in 25.2.11 (per reporter; consistent with #10360 backport #10373 first tagged in 25.2.11, and #10374 in 25.3.2). Older versions not built here.
- **Fixed by:** n/a
- **Duplicate of:** none found
- **Branch:** `repro/10578` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.4-SNAPSHOT, e7a5d2cdc9)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot** (static bug): ![Dropdown showed real items, but the click on "Option A" left the field blank](https://raw.githubusercontent.com/vaadin/flow-components/<commit-sha>/repro-10578.png)

## Observed behavior

Opening the "Lazy on focus" combo box with the toggle button always sends one `resetDataCommunicator` RPC. In failing runs:

- The dropdown renders `" "`, `Option A`, `Option B`.
- `_dropdownItems` holds 3 `ComboBoxPlaceholder` entries.
- Clicking `Option A` closes the dropdown. The value stays `" "` and no client value-change event reaches the server.

| Variant | Runs | `resetDataCommunicator` sent | Selection lost |
| --- | --- | --- | --- |
| Lazy, toggle click, no added latency | 19 | 19 | 4 |
| Lazy, toggle click, +40 ms on UIDL | 3 | 3 | 3 |
| Lazy, toggle click, +100 ms on UIDL | 2 | 2 | 2 |
| Lazy, `$server.resetDataCommunicator` no-op during `$connector.reset()` | 4 | 0 | 0 |
| Control (all items up front, no focus listener) | 4 | 0 | 0 |

Every lost selection had placeholders in `_dropdownItems`. Every successful run had none. The failure rate grows with round-trip latency. The console has no bug-related errors.

## Expected behavior

Clicking an item selects it, also when `setItems()` was called while the dropdown was opening.

## Steps to reproduce

1. Open `http://localhost:8080/repro-10578`. Do not focus the first combo box.
2. Click the toggle button of "Lazy on focus".
3. Run `document.querySelector('#lazy')._dropdownItems` in the console. In failing runs it contains placeholders.
4. Click "Option A".
5. The dropdown closes, the field stays blank, and the value label does not show `[Option A] fromClient=true`.
6. Reload and repeat if the selection worked. Adding network latency makes it fail almost every time.

## Reproduction

How to run: start the server (`mvn package jetty:run -am -DskipTests -pl vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-10578`
- **Scaffold:** `vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests/src/main/java/com/vaadin/flow/component/combobox/test/Repro10578View.java`

```java
ComboBox<String> combo = new ComboBox<>("Lazy on focus");
combo.setItems(List.of(" "));
combo.setValue(" ");
combo.addFocusListener(e -> {
    String previous = combo.getValue();
    combo.setItems(ALL); // " ", "Option A", "Option B"
    if (ALL.contains(previous)) {
        combo.setValue(previous);
    }
});
```

## Root cause (suspected)

The analysis in the issue matches the code on `main`. `reset()` calls `comboBox.clearCache()`, and the connector's `clearCache` override sets `#needsDataCommunicatorReset = true`:

https://github.com/vaadin/flow-components/blob/e7a5d2cdc922cfcd9b1da868786327573419fdb3/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/resources/META-INF/frontend/vaadin-combo-box/comboBoxConnector.ts#L40-L46

While the dropdown is open, `clearCache()` synchronously requests a page. `#requestPage()` sees the flag and calls `$server.resetDataCommunicator()` before `reset()` clears the flag:

https://github.com/vaadin/flow-components/blob/e7a5d2cdc922cfcd9b1da868786327573419fdb3/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/resources/META-INF/frontend/vaadin-combo-box/comboBoxConnector.ts#L124-L136

https://github.com/vaadin/flow-components/blob/e7a5d2cdc922cfcd9b1da868786327573419fdb3/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/resources/META-INF/frontend/vaadin-combo-box/comboBoxConnector.ts#L242-L245

The second server response then runs `clear()`, which writes placeholders into `filteredItems` in place without a re-render. `set()` only fills `#cache`, and `confirm()` finds no pending page, so `filteredItems` keeps the placeholders. The web component then drops the click because `_dropdownItems.indexOf(item)` is `-1`.

Two candidate fixes:

- Call the prototype `clearCache` directly in `reset()`, as the reporter suggests. Clearing the flag after `clearCache()` is too late, because the page request already ran.
- Make `clear()` and `confirm()` restore `filteredItems` from `#cache` when no page is pending.

## Notes

- Introduced by #10360 (fix for #10351), backported in #10373 (25.2) and #10374 (25.3).
- No pom changes were needed.
- Latency was added with a Playwright route delay on UIDL requests.
