> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced — duplicate of #6605 (same behavior: components accept a value absent from the data provider); #6605 is the broader, better-triaged umbrella and already cross-references #1546
- **Hypothesis tested:** The bug is that `ComboBox.setValue` accepts a value that is not among the items and reflects it on the client, triggered by `setValue("value_1")` with `setAllowCustomValue(false)`, observable as the value appearing in the input / `selectedItem` and `getValue()` returning it.
- **Regression?:** not a regression (intentional since lazy loading landed — pekam, 2019; unchanged on main)
- **Flavor:** Flow
- **Branch:** `repro/1546` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ main (25.3-SNAPSHOT, `@vaadin/combo-box` 25.3.0-alpha3)
- **Present on main?:** yes (unchanged)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** ![out-of-list value_1 accepted in all three combos](https://raw.githubusercontent.com/vaadin/flow-components/repro-1546/repro-1546.png)

## Observed behavior

`ComboBox`/`MultiSelectComboBox` with items `value_2, value_3`, `setAllowCustomValue(false)`. A button calls `setValue("value_1")` (or `setValue(Set.of("value_1"))`):

| Combo | Client state | `getValue()` |
| --- | --- | --- |
| in-memory single select | input shows `value_1` (`selectedItem` = value_1) | `value_1` |
| lazy (callback) single select | input shows `value_1` | `value_1` |
| multi select | `selectedItems` = `[value_1]` (chip rendered) | `[value_1]` |

The out-of-list value is accepted and reflected on the client for all three — no exception, no revert, no warning. Console clean, no server exceptions.

## Expected behavior (per the issue)

Setting a value not in the items should throw or be ignored — not silently become the value.

## Root cause

`ComboBoxBase#setValue` only guards the "no items at all" case (throws when there is no data provider); it never checks that the value is a member of the data provider, so any value is accepted:

https://github.com/vaadin/flow-components/blob/2bb8f77a60b92f6289c9df10b9349acf9f71d7b1/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/java/com/vaadin/flow/component/combobox/ComboBoxBase.java#L590-L605

As pekam noted in 2019, membership validation was intentionally skipped because a lazy data provider would have to scan the whole data set. #6605 reframes this as a design change (validate at least for in-memory providers, or throw/revert+warn) and is labeled `requires new major` because it is breaking.

## Steps to reproduce

1. Open `http://localhost:8080/repro-1546`.
2. Click any of the three "setValue(value_1)" buttons.
3. The combo shows `value_1` (single) or a `value_1` chip (multi), and the `getValue()` log reads `value_1` — despite `value_1` not being an item and custom values disallowed.

## Reproduction

How to run: start the server (`mvn package jetty:run -am -pl vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1546`
- **Scaffold:** `vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests/src/main/java/com/vaadin/flow/component/combobox/test/Repro1546View.java`

## Duplicate

Same behavior as **#6605** ("Server should not allow setting values that are not in data provider", open, `bug` / `Severity: Minor` / `requires new major`). #6605 is the broader umbrella — it covers Select, RadioButtonGroup, CheckboxGroup **and** ComboBox, links the reverted flow#19310, and already contains `web-padawan`'s "See also #1546" plus `stefanuebe`'s note that MultiSelectComboBox is affected too (confirmed by this reproduction). #1546 is the ComboBox-specific, older instance of exactly this. Recommend closing #1546 as a duplicate of #6605 and consolidating discussion there.

## Notes

- This reproduction (in-memory / lazy / multi side by side) is useful evidence for #6605's scope discussion — it shows ComboBox differs from the Select-family symptom in #6605's description: for ComboBox the client **does** take the out-of-list value, it is not kept at the previous valid value.
