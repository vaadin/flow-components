> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced — duplicate of #3239 (same root cause, trigger, and symptom); the selected-item caption in the input stays stale after `refreshAll()`, and also after the targeted `refreshItem()` (broader than originally reported)
- **Hypothesis tested:** The bug is that the ComboBox input's selected-item caption is not refreshed when the selected item's label changes via a data-provider refresh, triggered by mutating the item and firing `refreshAll()`/`refreshItem()`, observable as the input showing the old label while the overlay shows the new one.
- **Regression?:** not a regression (reported against Vaadin 14.0.12 in 2019, same behavior on current main; no known-good version)
- **Flavor:** Flow
- **Branch:** `repro/1616` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ main (25.3-SNAPSHOT, `@vaadin/combo-box` 25.3.0-alpha3)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** ![input caption "a" while the overlay item and server value read "aab"](https://raw.githubusercontent.com/vaadin/flow-components/repro-1616/repro-1616.png)

## Observed behavior

ComboBox with one mutable item (identity by `id` via `equals`/`hashCode`), `setItemLabelGenerator(it -> it.name)`, value pre-selected. Two buttons mutate the item's name and fire a refresh:

| Step | Input caption | Overlay item | Server-side value label |
| --- | --- | --- | --- |
| initial | a | — | a |
| mutate → `refreshAll()` | **a (stale)** | aa | aa |
| mutate → `refreshItem(item)` | **a (stale)** | aab | aab |

The overlay always shows the fresh label; the input caption never updates. No console errors, no server exceptions.

## Expected behavior

The input caption should show the item's current label after the data provider signals the item changed.

## Steps to reproduce

1. Open `http://localhost:8080/repro-1616` — the input shows "a".
2. Click "mutate + refreshAll", then open the dropdown: the suggestion reads "aa", the input still reads "a".
3. Click "mutate + refreshItem": same staleness ("aab" in the overlay, "a" in the input).

## Reproduction

How to run: start the server (`mvn package jetty:run -am -pl vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1616`
- **Scaffold:** `vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests/src/main/java/com/vaadin/flow/component/combobox/test/Repro1616View.java`

## Root cause (suspected)

The client caption comes from the `selectedItem` property, which is only rewritten by `ComboBoxBase#refreshValue()`. `refreshValue()` runs on `setValue(...)` and `setItemLabelGenerator(...)` — but on data-provider events the listener routes to `dataCommunicator.refresh(item)` / `reset()`, and the reset-triggered `refreshValue()` is explicitly limited to `MultiSelectComboBox`:

https://github.com/vaadin/flow-components/blob/29d9da5f76b2f8cd8b773e7e42b2601f26ccb1d5/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/java/com/vaadin/flow/component/combobox/ComboBoxDataController.java#L510-L526

https://github.com/vaadin/flow-components/blob/29d9da5f76b2f8cd8b773e7e42b2601f26ccb1d5/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/java/com/vaadin/flow/component/combobox/ComboBoxDataController.java#L634-L647

A fix shape: after a `DataRefreshEvent` matching the current value (or any `reset()`), call `comboBox.refreshValue()` for single-select too.

## Duplicate

Same bug as **#3239** (open, triaged: `bug`, `Severity: Major`, `data provider`): identical trigger (mutate item + `refreshAll()` with an item label generator), identical symptom (overlay updates, input caption stale), and #3239's description names the same root cause this reproduction confirmed in source — `ComboBoxBase.refreshValue` is not run on data-provider refresh. #3239 already cross-references #1616 and additionally covers MultiSelectComboBox chips (not verified by this reproduction). Recommend consolidating: close #1616 as a duplicate of the newer but better-triaged #3239, or vice versa — team's call.

## Notes

- Workaround for affected users: re-set the value (`setValue(null)` + `setValue(item)`) or re-apply the label generator — both paths call `refreshValue()`.
- The staleness on `refreshItem()` (not just `refreshAll()`) widens the original 2019 report.
