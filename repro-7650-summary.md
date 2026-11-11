> [!WARNING]
> Automated reproduction by the Claude Code `repro` skill. Needs human verification.

**Reproduced** on `main` (25.4-SNAPSHOT). Not a regression.

Same root cause and trigger as vaadin/flow#24292 (open). The cause is in Flow core, so recommend closing this issue as a duplicate of vaadin/flow#24292.

**Root cause**: `AbstractDataView.addItemCountChangeListener` registers the listener on the `Grid` instance. `DataCommunicator.fireItemCountEvent` fires the event on `Element.getComponent()` of the grid's state node. `Composite` remaps the content element to itself, so that call returns the `Composite`, and the event never reaches the listeners registered on the `Grid`.

https://github.com/vaadin/flow/blob/cc4b9e62f4d2b9b240b5410644726596263ed12b/flow-data/src/main/java/com/vaadin/flow/data/provider/DataCommunicator.java#L1387-L1399

Reproduction: branch [`repro/7650`](https://github.com/vaadin/flow-components/tree/repro/7650), route `/vaadin-grid/repro-7650`.

<details>
<summary>Details</summary>

Initial render, then one item added through `getListDataView().addItem(...)`:

| Case | Events received |
| --- | --- |
| `Composite<Grid>`, listener on `getContent().getListDataView()` | none |
| `Composite<Grid>`, listener on the `Composite` with `ComponentUtil.addListener` | `0`, `1`, both with `source` = the `Composite` subclass |
| Plain `Grid`, listener on `getListDataView()` | `0`, `1`, both with `source` = `Grid` |

Listener registration on the `Grid`:

https://github.com/vaadin/flow/blob/cc4b9e62f4d2b9b240b5410644726596263ed12b/flow-data/src/main/java/com/vaadin/flow/data/provider/AbstractDataView.java#L82-L89

`Composite` maps the content element to the outermost composite:

https://github.com/vaadin/flow/blob/cc4b9e62f4d2b9b240b5410644726596263ed12b/flow-server/src/main/java/com/vaadin/flow/component/Composite.java#L153-L158

</details>
