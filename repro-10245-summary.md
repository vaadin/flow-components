> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is the connector's "already fully visible" guard in `scrollToItem` being skipped, triggered by calling `Grid.scrollToItem` in the same round trip that first renders the grid (no rows rendered yet), observable as a non-zero `scrollTop` with the target row at the top even though it was inside the initial viewport.
- **Regression?:** not a regression (broken since the visibility guard was introduced in #8332, 25.0.0)
- **Fixed by:** n/a
- **Duplicate of:** none found
- **Branch:** `repro/10245` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.4-SNAPSHOT, 31c6ca7718)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot** (static bug): ![Cold load of ?focus=10 — Item 10 parked at the top](https://raw.githubusercontent.com/vaadin/flow-components/<commit-sha>/repro-10245.png)

## Observed behavior

600px grid, 200 items, 15 rows fully visible at `scrollTop` 0 (Item 0 – Item 14). Measured in the browser after load:

| Case | `scrollTop` | First visible row |
| --- | --- | --- |
| No param (baseline) | 0 | Item 0 |
| Cold load `?focus=10` | 359 | Item 10 |
| Cold load `?focus=20` | 720 | Item 20 |
| Rendered, then button `scrollToItem(Item 10)` (control) | 0 | Item 0 |

Item 10 is inside the initial viewport, yet the cold load scrolls it to the top. The same call after the grid has rendered correctly does nothing. Console clean.

## Expected behavior

`scrollToItem(item)` does not scroll when `item` is already fully visible, also when called in the request that first renders the grid (for example resolving a `?focus=<id>` parameter in `beforeEnter`).

## Steps to reproduce

1. Open `http://localhost:8080/repro-10245?focus=10` directly (full page load).
2. Observe the grid starts at Item 10 (`scrollTop` about 360), not Item 0.
3. Open `http://localhost:8080/repro-10245`, click **scrollToItem(Item 10)**: the grid does not scroll (correct).

## Reproduction

How to run: `mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests` and open the route below.

- **Route / page:** `http://localhost:8080/repro-10245?focus=10`
- **Scaffold:** `vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests/src/main/java/com/vaadin/flow/component/grid/it/Repro10245View.java`

```java
@Route("repro-10245")
public class Repro10245View extends Div implements BeforeEnterObserver {
    private final Grid<String> grid = new Grid<>();

    public Repro10245View() {
        grid.setHeight("600px");
        grid.addColumn(s -> s).setHeader("Item");
        grid.setItems(IntStream.range(0, 200).mapToObj(i -> "Item " + i).toList());
        add(new NativeButton("scrollToItem(Item 10)", e -> grid.scrollToItem("Item 10")), grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getLocation().getQueryParameters().getSingleParameter("focus")
                .map(Integer::valueOf)
                .ifPresent(index -> grid.scrollToItem("Item " + index));
    }
}
```

## Root cause (suspected)

The connector only skips the scroll when the target row is already rendered. On the first response the grid has no rendered rows yet, so `targetRow` is `undefined` and `scrollToIndex` runs unconditionally:

https://github.com/vaadin/flow-components/blob/31c6ca7718a4aedf418755ff027a7405243a6709/vaadin-grid-flow-parent/vaadin-grid-flow/src/main/resources/META-INF/frontend/vaadin-grid/gridConnector.ts#L369-L378

The call is dispatched from `beforeClientResponse`, in the same response that creates the grid:

https://github.com/vaadin/flow-components/blob/31c6ca7718a4aedf418755ff027a7405243a6709/vaadin-grid-flow-parent/vaadin-grid-flow/src/main/java/com/vaadin/flow/component/grid/Grid.java#L5334-L5348

A possible fix: when no row is rendered yet, defer the check until the first render (for example after `requestAnimationFrame` / the grid's next render), or treat an unrendered grid at `scrollTop` 0 by comparing `itemIndex` against the estimated visible row count.

## Notes

- Guard added by #8332 (`refactor!: skip scrolling if item is already fully visible`), first in 25.0.0. Before that, `scrollToItem` always scrolled, so the cold-load path simply never got the new behavior.
- The empty `_getRenderedRows()` at call time is inferred from the code path and the control case, not instrumented directly.
- No IT pom changes needed.
