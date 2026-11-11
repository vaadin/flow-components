> [!WARNING]
> Automated reproduction by the Claude Code `repro` skill. Needs human verification.

**Reproduced** on `main` (25.4-SNAPSHOT). Not a regression.

`accordion.close()` called at creation does give an accordion with only closed panels. Only the `panel.setOpened(false)` route described in the report fails.

**Root cause**: The `Accordion` constructor sets the `opened` index to `0`, and `AccordionPanel.setOpened(false)` does not change that index. On the client, `vaadin-accordion` treats its `opened` index as the single source of truth. Its `_updateItems()` sets `item.opened = true` on panel 0, so Flow syncs the panel back to opened. A later `setOpened(false)` works because the panel then changes from `true` to `false` on the client, and `_updateOpened()` resets the accordion index to `null`.

https://github.com/vaadin/flow-components/blob/5389f45fdcde73bac743420d625c68b3c75ceaf7/vaadin-accordion-flow-parent/vaadin-accordion-flow/src/main/java/com/vaadin/flow/component/accordion/Accordion.java#L64-L67

https://github.com/vaadin/web-components/blob/bb73ce26e10cffe1d016d9192c87755039d408fd/packages/accordion/src/vaadin-accordion-mixin.js#L137-L147

Reproduction: branch [`repro/1638`](https://github.com/vaadin/flow-components/tree/repro/1638), route `/repro-1638`.

<details>
<summary>Details</summary>

Each accordion has 2 panels. "Server" is the state after a round trip.

| Case | Client `opened` / panels | Server `getOpenedIndex()` / `isOpened()` |
| --- | --- | --- |
| First panel `setOpened(false)` in the constructor | `0` / `[true, false]` | `0` / `[true, false]` |
| `accordion.close()` in the constructor | `null` / `[false, false]` | empty / `[false, false]` |
| First panel `setOpened(false)` from a button click | `null` / `[false, false]` | empty / `[false, false]` |

</details>
