<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced on the 24.x line; fixed on 25.x
- **Hypothesis tested:** The bug is Grid computing its all-rows-visible height from the default row-height estimate and never applying the corrected height, triggered by `setAllRowsVisible(true)` + a component column whose content makes rows taller than the estimate + a footer row, observable as a too-short grid that clips rows and internally scrolls, with the footer drawn mid-grid.
- **Regression?:** not a regression (broken since the virtualizer's amortized height update; reported on 24.5.1, still present on 24.10)
- **Fixed by:** vaadin/web-components#12146 (virtualizer now always applies the scroller height in `allRowsVisible` mode instead of amortizing it; merged 2026-07-15, backported to 25.2 and 25.1 — **not** to 24.x)
- **Duplicate of:** none found
- **Branch:** `repro/8023` (branched from flow-components `24.10`)
- **Reproduced on:** flow-components `24.10` (@vaadin/grid 24.10.4), Chromium
- **Present on main?:** no — not reproducible on 25.3-SNAPSHOT, which also handles late row-height growth correctly (grid resized 1048→2488px with footer staying pinned when cell content was grown after render)
- **Theme / Browser:** Lumo / Chromium
- **Screenshot:** ![Footer rendered mid-grid between rows 9 and 10 in the component-column grid; control grid below renders correctly](https://raw.githubusercontent.com/vaadin/flow-components/adec1b3bc3853c49befc79d261e930e0442c8410/repro-8023.png)

## Observed behavior

On flow-components `24.10` (@vaadin/grid 24.10.4), measured with Playwright immediately after load:

- The failing grid is stuck at **644px** — the height matching 15 *default-height* (35px) rows — while its rows are actually 62px tall (component content present from the first frame). The internal table has `scrollHeight` 937px vs `clientHeight` 642px: **295px hidden overflow despite `allRowsVisible`**, with only 14 of 15 rows rendered. The state does not self-correct (unchanged for 4+ seconds).
- Because the grid is internally scrollable when it shouldn't be, scrolling detaches the footer from the grid bottom: after a 100px internal scroll the footer renders **101px above the grid's bottom edge**, visually mid-grid between data rows (see screenshot).
- The control grid on the same page (identical setup, plain text columns) renders correctly: no overflow, all 15 rows, footer at the bottom.
- On 25.3-SNAPSHOT the same view is correct from the first observable frame, and even artificially growing all cells to 150px after render resizes the grid immediately with the footer staying pinned.

## Expected behavior

With `allRowsVisible`, the grid should be tall enough to show all rows with the footer at the bottom, right from the start (per the issue).

## Steps to reproduce

1. Check out flow-components `24.10` and start the grid IT server.
2. Open `http://localhost:8080/repro-8023`.
3. Observe the first grid: it is too short, clips rows, and can be scrolled internally; the footer appears mid-grid while scrolling. Compare with the control grid below it.

## Reproduction

How to run: start the server (`mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -pl vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-8023`
- **Scaffold:** `vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests/src/main/java/com/vaadin/flow/component/grid/it/Repro8023View.java` (faithful port of the reporter's example, plus a text-column control grid)

```java
Grid<String> grid = new Grid<>();
Grid.Column<String> str = grid.addColumn(s -> s).setHeader("STR");
grid.addComponentColumn(s -> {
    VerticalLayout cellLayout = new VerticalLayout();
    cellLayout.add(new Span("ORG1A"));
    return cellLayout;
});
grid.setItems(IntStream.rangeClosed(1, 15).mapToObj(String::valueOf).toList());
grid.setAllRowsVisible(true);
FooterRow footerRow = grid.prependFooterRow();
footerRow.getCell(str).setComponent(new Paragraph("It works!"));
```

## Root cause (suspected)

The virtualizer's `_updateScrollerSize` amortizes height updates: it only applies a new items-container height when forced, when scrolled near the end, or when the difference exceeds the viewport height. Here the corrected estimate (937px, from the real 62px row average) differs from the stale height (642px, from the 35px default estimate) by 295px — less than the 642px viewport — so the update is skipped forever. In `allRowsVisible` mode the grid's own height tracks this stale value, and the footer is positioned against it:

https://github.com/vaadin/web-components/blob/db2b55d7760803aacfff86694c3124a563d782c3/packages/component-base/src/iron-list-core.js#L618-L635

Fixed on 25.x by vaadin/web-components#12146, which makes the grid tell the virtualizer to always apply the scroller size when `allRowsVisible` is set:

https://github.com/vaadin/web-components/blob/a3265b00f4cbdb79f902817788a5a05cedc9db7a/packages/grid/src/vaadin-grid-mixin.js#L250-L256

A backport of #12146 to the 24.x line would likely resolve this issue there.

## Notes

- vaadin/web-components#12146 was written for a different trigger (tree-grid deep expand not showing all rows) but fixes the same stale-height mechanism; it has no linked issue, so this issue was never closed by it.
- Verified the environment before trusting the result: a first attempt served `@vaadin/grid` from a local web-components checkout (via `LOCAL_WEB_COMPONENTS_PATH`), producing a `Grid.prototype._updateItem` TypeError from `gridConnector.ts` — a build artifact, not the bug. Re-verified on a clean install of @vaadin/grid 24.10.4.
- No pom edits were needed (`vaadin-ordered-layout-flow` is already a grid IT dependency).
- Duplicate search across vaadin/web-components, vaadin/flow-components, and vaadin/flow found no matching issue.
