> [!WARNING]
> Automated reproduction by the Claude Code `repro` skill. Needs human verification.

**Reproduced** on `24.10` (24.10-SNAPSHOT). Fixed on `main` by #8700, released in 25.0.6 and 25.1.0. The fix is not backported to 24.x. Not a regression.

**Root cause**: On 24.x, `TabSheet` copies the selected index to its own `selected` property only in a `SelectedChangeListener`. Removing a tab before the selected one lowers the index, but the selected tab stays the same, so `Tabs` fires no `SelectedChangeEvent`. The `TabSheet` element keeps the old index on the server. On refresh with `@PreserveOnRefresh`, Flow sends that old index to the new `vaadin-tabsheet` element, which then points past the last tab and shows the loading spinner.

https://github.com/vaadin/flow-components/blob/e0c069358e1f97d0a9f947968dce0cd787df24b1/vaadin-tabs-flow-parent/vaadin-tabs-flow/src/main/java/com/vaadin/flow/component/tabs/TabSheet.java#L67-L70

Reproduction: branch [`repro/7882`](https://github.com/vaadin/flow-components/tree/repro/7882) (based on `24.10`), route `/vaadin-tabs/repro-7882`.

<details>
<summary>Details</summary>

Steps: select the second tab, remove the first tab from the server, reload the page.

| Case | After reload |
| --- | --- |
| `TabSheet` on `24.10` | server `getSelectedIndex()` = 0, client `vaadin-tabsheet.selected` = 1, no tab selected, loading spinner |
| Plain `Tabs` on `24.10` | server 0, client 0, remaining tab selected |
| `TabSheet` on `main` | server 0, client 0, remaining tab selected with its content |

`Tabs` returns early when the selected tab does not change, so no event reaches the `TabSheet` listener:

https://github.com/vaadin/flow-components/blob/e0c069358e1f97d0a9f947968dce0cd787df24b1/vaadin-tabs-flow-parent/vaadin-tabs-flow/src/main/java/com/vaadin/flow/component/tabs/Tabs.java#L681-L683

#8700 removed the property copy for another bug (#8699).

</details>
