> [!WARNING]
> Automated bug reproduction by Claude Code. It reproduces the reported behavior and points at a likely root cause, but has not been reviewed by a maintainer. Treat the root-cause pointer as a hypothesis.

## Verdict

**Reproduced.** A `TreeGrid` placed inside a parent `Grid`'s component column does not render its row content after attach. The content only appears after interacting with the parent Grid (e.g. sorting a column).

## Branch

`repro/9674` on `vaadin/flow-components` — runnable reproduction View.

## Environment

- Repo: `vaadin/flow-components`, branch `main` (`6e49f21`), Flow `25.3-SNAPSHOT`, `@vaadin/grid` 25.3.0-alpha2
- Theme: Lumo
- Browser: Chromium (Playwright)

## Observed behavior

After attach, the inner TreeGrid renders its column header (`Item`) but **not** its single data row (`dummy text`). Inspecting the DOM: the inner grid has a non-zero size (`offsetHeight` 400, `#table` 398) and one physical row element exists in `tbody#items`, yet the row's `vaadin-grid-cell-content` is empty — only the header cell content is present:

```
cellTexts: ["Item"]        // "dummy text" missing
```

Clicking the sortable **Number** column header on the outer Grid makes the row content appear immediately:

```
cellTexts: ["Item", "dummy text"]   // after sort
```

Console is clean (only the dev-server favicon 404 and the Lit dev-mode warning). This is a silent rendering bug — no exception.

## Expected behavior

The inner TreeGrid's row content (`dummy text`) should render right after attach, without any interaction with the outer Grid.

## Steps to reproduce

1. Open `/repro-9674`.
2. Observe the Tree Grid Column: the inner TreeGrid shows its `Item` header but no row content.
3. Click the **Number** column header to sort — the `dummy text` row now appears in the inner TreeGrid.

## Reproduction

Route `@Route("repro-9674")`, View `Repro9674View` (module `vaadin-grid-flow-integration-tests`):

```java
var grid = new Grid<String>();
grid.addColumn(item -> "1").setHeader("Number").setSortable(true);
grid.addComponentColumn(item -> createTreeGrid()).setHeader("Tree Grid Column");
grid.setItems(List.of("row"));
add(grid);

// ...
private TreeGrid<String> createTreeGrid() {
    var treeGrid = new TreeGrid<String>();
    treeGrid.addHierarchyColumn(s -> s).setHeader("Item");
    treeGrid.setItems(List.of("dummy text"), item -> List.of());
    return treeGrid;
}
```

## Root cause

The bug lives in the shared web component, not in Flow. A grid renders its rows through the virtualizer, and the virtualizer's initial render is skipped whenever its scroll target has zero height at the moment the render is scheduled:

https://github.com/vaadin/web-components/blob/v25.3.0-alpha2/packages/component-base/src/iron-list-core.js#L473-L476

A TreeGrid mounted inside another grid's component-column cell is measured/updated while its shadow `#table` is still 0-height, so the initial row pool is never created. The only initial-render self-heal is a single `requestAnimationFrame(() => this._resizeHandler())` that can still observe 0 height inside a not-yet-laid-out parent cell:

https://github.com/vaadin/web-components/blob/v25.3.0-alpha2/packages/component-base/src/virtualizer-iron-list-adapter.js#L404-L410

After that, rendering only recovers when the adapter's `ResizeObserver` fires with a non-zero `#table`. Sorting the outer Grid clears its cache and forces a layout/reflow of the row containing the cell, which finally gives the inner grid's table a non-zero size within an observation cycle — so the inner virtualizer renders and the row appears. Any resize/refresh does the same; sorting is just a convenient trigger. A likely fix: when the grid transitions to visible (`__hostVisible` false→true) in `vaadin-grid-resize-mixin.js` / `vaadin-grid-mixin.js`, force a virtualizer re-render instead of relying solely on the adapter's internal `ResizeObserver`.
