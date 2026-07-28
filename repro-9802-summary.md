<!-- Edit any field. This file is committed on the `repro/9802` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is the virtualizer keeping a stale (too small) height on the grid's items container, triggered by the data provider size growing back (filter reset) while the amortization threshold is not met, observable as the footer row and the last rows staying at the filtered-state position — a gap at the bottom of the grid with the footer in the middle.
- **Regression?:** unknown — the amortization logic is inherited from `iron-list` and predates 25.x; the issue names no working version.
- **Fixed by:** n/a — `vaadin/web-components#12146` fixes the same underlying mechanism but only for `allRowsVisible` grids, so it does not cover this case.
- **Duplicate of:** none found
- **Branch:** `repro/9802` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (`@vaadin/grid` 25.3.0-alpha7, i.e. **including** web-components#12146)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot** (static bug): ![Footer row "43 entries" stuck in the middle of the grid after clearing the filter](https://raw.githubusercontent.com/vaadin/flow-components/daccdf3e9546943a53261c177cff62f66cfc28f0/repro-9802-bug.png)

## Observed behavior

At a browser height of 955px (grid height 929px), with 43 items filtered down to 20 and then unfiltered:

| step | `#items` inline height | footer row top | grid bottom | `grid.size` |
| --- | --- | --- | --- | --- |
| initial (43 items) | `1547px` | 918 | 955 | 43 |
| filtered (20 items) | `719px` | 802 | 955 | 20 |
| **filter cleared (43 items)** | **`719px` (stale)** | **802** | 955 | 43 |

The server-side size is correct (43) and the rows are rendered, but the items container keeps the height computed for 20 rows. In the reporter's setup (`#items { flex-grow: 0; flex-shrink: 1 }` + `#footer { flex-grow: 1 }`) this puts the footer row in the middle of the grid and clips the rows below it — see the screenshot: the "43 entries" footer sits right after `Street 19`, with `Street 21`+ rendered underneath it.

The failure depends on the viewport height, exactly as reported. Sweep of the same steps (43 → 20 → 43 items):

| viewport height | items height after clearing | stale? |
| --- | --- | --- |
| 700 | `1547px` | no |
| 800 | `1547px` | no |
| 900 | `719px` | **yes** |
| 955 | `719px` | **yes** |
| 1100 | `719px` | **yes** |
| 1400 | `1547px` | no |

A plain grid (no shadow DOM tweak, no `allRowsVisible`) is affected too, just less visibly: after clearing the filter its scroll range is `1279px` instead of `1639px`, so the last rows cannot be reached in one scrollbar drag — it took two scroll-to-bottom attempts to reach row 42. Scrolling makes the virtualizer re-apply the height, which is why the problem "snaps back" on scroll or resize.

`allRowsVisible` grids are **not** affected on `main` (fixed by web-components#12146).

Console: clean (only the dev-server favicon 404).

## Expected behavior

When the data provider size grows, the items container height must be recalculated right away, so all rows are visible/reachable and the footer stays at the bottom of the grid — without a scroll or resize.

## Steps to reproduce

1. Set the browser window height so the grid is ~870–1080px tall (e.g. window height 955px).
2. Open `http://localhost:8080/repro-9802` — a 43-item grid with a footer row, in a full-height layout.
3. Click **Show only Italy** → 20 items, the grid shrinks correctly.
4. Click **Clear filter** → 43 items again.
5. The footer row stays in the middle of the grid and the rows below it are clipped. Scrolling or resizing the window fixes it.

## Reproduction

How to run: `mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests`, then open the route below.

- **Route / page:** `http://localhost:8080/repro-9802` (query parameters: `mode=hack|allrows|plain`, `total`, `filtered`, `gridHeight`)
- **Scaffold:** `vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests/src/main/java/com/vaadin/flow/component/grid/it/Repro9802View.java`

```java
Grid<Item> grid = new Grid<>();
var streetColumn = grid.addColumn(Item::street).setHeader("Street");
// ... four more columns ...

// Reporter's tweak: items container shrinks to its content, footer takes the rest
grid.getElement().executeJs(
        "this.shadowRoot.querySelector('#items').style.flexGrow = '0';"
                + "this.shadowRoot.querySelector('#items').style.flexShrink = '1';"
                + "this.shadowRoot.querySelector('#footer').style.flexGrow = '1';");

GridListDataView<Item> dataView = grid.setItems(items); // 43 items, 20 of them "Italien"
FooterRow.FooterCell footerCell = grid.appendFooterRow().getCell(streetColumn);

add(new NativeButton("Show only Italy",
        e -> dataView.setFilter(item -> "Italien".equals(item.country()))));
add(new NativeButton("Clear filter", e -> dataView.removeFilters()));

setSizeFull();
add(grid);
setFlexGrow(1, grid); // grid fills the viewport
```

## Root cause (suspected)

The virtualizer amortizes items-container height updates: the new height is only applied when the change is at least one viewport tall (or when the scroll position is already at the end). When the grid grows from 20 back to 43 rows, the growth here is 828px while the scroller is 872–1072px tall, so the update is skipped and the container keeps the 20-row height until a scroll or resize forces a recalculation:

https://github.com/vaadin/web-components/blob/74c69d862e24d9a9f6750d0afcf325a820ec7bcf/packages/component-base/src/iron-list-core.js#L617-L634

Confirmed in the browser: calling `grid.__virtualizer.__adapter._updateScrollerSize(true)` after clearing the filter immediately restores `#items` to `1547px` and moves the footer back to the bottom. So the amortization gate is the only cause.

web-components#12146 added an opt-out from the amortization, but the grid only enables it in `allRowsVisible` mode:

https://github.com/vaadin/web-components/blob/74c69d862e24d9a9f6750d0afcf325a820ec7bcf/packages/grid/src/vaadin-grid-mixin.js#L249-L256

A fix likely belongs in `vaadin/web-components`: force a scroller-size update when the virtualizer size changes (`set size(size)` in `virtualizer-iron-list-adapter.js`), instead of only when `allRowsVisible` is set. Reproducing it there is out of scope for this report.

## Notes

- The reporter's example changes the grid's shadow DOM (`#items` / `#footer` flex settings), which is not a supported API. That tweak only makes the stale height *visible*; the stale height itself also happens in a plain scrolling grid (short scroll range, last rows unreachable until you scroll again), so the underlying problem is not caused by the tweak.
- The reported version (Vaadin 25.2.3) predates web-components v25.2.5, which contains #12146 — but since the reporter's grid does not use `allRowsVisible`, upgrading does not help. Verified on `main`, which includes the fix.
- The repro view supports `?mode=allrows` (control: works) and `?mode=plain` (control: stale scroll range) so the trigger can be isolated without editing Java.
- No IT pom changes were needed — the grid IT module already depends on `vaadin-ordered-layout-flow`.
