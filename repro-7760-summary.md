> [!WARNING]
> Automated reproduction by the Claude Code `repro` skill. Needs human verification.

**Reproduced** on `main` (25.4-SNAPSHOT). Not a regression: the add-then-remove order dates back to the initial import of the component (2020).

**Root cause**: `MultiSelectListBox.updateSelection` adds `addedItems` to the current value and then removes `removedItems`. An item in both sets ends up deselected. The `MultiSelect` contract in Flow says such duplicate items are ignored, and the default `MultiSelect#setValue` relies on that: it passes the new value as `addedItems` and the full current selection as `removedItems`. The bug also shows when `updateSelection` is called directly with overlapping sets.

https://github.com/vaadin/flow-components/blob/c50d2498db454631a31eba2ef52634420164934b/vaadin-list-box-flow-parent/vaadin-list-box-flow/src/main/java/com/vaadin/flow/component/listbox/MultiSelectListBox.java#L149-L155

Reproduction: branch [`repro/7760`](https://github.com/vaadin/flow-components/tree/repro/7760), route `/repro-7760`.

<details>
<summary>Details</summary>

Start state for every case: items 1, 2 and 3 selected.

| Case | Result |
| --- | --- |
| A: reporter's `Composite` wrapper, `setValue({1,3})` through the default `MultiSelect#setValue` | `[]` |
| B: `MultiSelectListBox.setValue({1,3})` (control) | `[1, 3]` |
| C: `MultiSelectListBox.updateSelection(added={1,3}, removed={1,2,3})` | `[]` |

The `MultiSelect` contract and the default `setValue` in Flow:

https://github.com/vaadin/flow/blob/cc4b9e62f4d2b9b240b5410644726596263ed12b/flow-data/src/main/java/com/vaadin/flow/data/selection/MultiSelect.java#L143-L156

https://github.com/vaadin/flow/blob/cc4b9e62f4d2b9b240b5410644726596263ed12b/flow-data/src/main/java/com/vaadin/flow/data/selection/MultiSelect.java#L214-L220

Not verified in the browser: `CheckboxGroup.updateSelection` has the same add-then-remove code. `Grid` and `MultiSelectComboBox` drop or remove the overlapping items first.

</details>
