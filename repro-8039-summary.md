> [!WARNING]
> Automated reproduction by the Claude Code `repro` skill. Needs human verification.

**Reproduced** on `main` (25.4-SNAPSHOT). Not a regression: broken since `ListBox` got `HasTooltip` in #3719.

The bug is wider than the call order in the report. Any later `setItems()` or `getDataProvider().refreshAll()` also drops a tooltip that was set after the items.

**Root cause**: `setTooltipText()` puts a `<vaadin-tooltip slot="tooltip">` element in the light DOM of the list box. Each `setItems()` or `refreshAll()` runs `rebuild()`, which calls `removeAll()`. That clears every child element, including the tooltip. The tooltip handle stays cached on the component, so a later `setTooltipText()` only updates the removed element and never adds it back.

https://github.com/vaadin/flow-components/blob/5389f45fdcde73bac743420d625c68b3c75ceaf7/vaadin-list-box-flow-parent/vaadin-list-box-flow/src/main/java/com/vaadin/flow/component/listbox/ListBoxBase.java#L290-L291

https://github.com/vaadin/flow-components/blob/5389f45fdcde73bac743420d625c68b3c75ceaf7/vaadin-flow-components-shared-parent/vaadin-flow-components-base/src/main/java/com/vaadin/flow/component/shared/Tooltip.java#L215-L217

Reproduction: branch [`repro/8039`](https://github.com/vaadin/flow-components/tree/repro/8039), route `/repro-8039`.

<details>
<summary>Details</summary>

| Case | `<vaadin-tooltip>` in list box |
| --- | --- |
| `ListBox`: `setTooltipText()` then `setItems()` | missing, no tooltip on hover |
| `MultiSelectListBox`: `setTooltipText()` then `setItems()` | missing |
| `ListBox`: `setItems()` then `setTooltipText()` | present, tooltip opens on hover |
| Same, then `setItems()` again | missing |
| Same, then `getDataProvider().refreshAll()` | missing |
| Failing case, then `setTooltipText()` again | missing |

</details>
