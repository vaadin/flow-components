> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced (styling limitation still present on main)
- **Hypothesis tested:** The bug is that a full-width component (e.g. a filter TextField) set as the header of a sortable Grid column does not stretch to the column width, triggered by the sortable header content being wrapped in a content-sized `vaadin-grid-sorter`, observable as the TextField rendering at a fraction of the header cell width.
- **Regression?:** not a regression (behavior present since sortable headers wrap content in `vaadin-grid-sorter`; reported 2021, unchanged)
- **Flavor:** Flow (root cause shared with the web component's sorter styling)
- **Branch:** `repro/1675` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ main (25.3-SNAPSHOT, `@vaadin/grid` 25.3.0-alpha3)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** ![sortable vs plain header width](https://raw.githubusercontent.com/vaadin/flow-components/repro-1675/repro-1675.png)

## Observed behavior

Grid (900px) with two columns, each with a `TextField` header set to `width: 100%`:

| Column | Header cell width | Rendered TextField width |
| --- | --- | --- |
| sortable (`setSortable(true)`) | 449px | **192px** (inside a 209px `vaadin-grid-sorter`) |
| plain (not sortable) | 449px | 417px (full width minus cell padding) |

The full-width field in the sortable column fills less than half the available header width. Measured with `offsetWidth` in Chromium; no console errors, no server exceptions.

## Expected behavior

The filter component should be able to fill the column header width also when the column is sortable — `setWidth("100%")` on the header component should resolve against the header cell, as it does for non-sortable columns.

## Steps to reproduce

1. Open `http://localhost:8080/repro-1675`.
2. Compare the two filter fields: the sortable column's field is shrunk to content width, the plain column's field fills the cell.

## Reproduction

How to run: start the server (`mvn package jetty:run -am -pl vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1675`
- **Scaffold:** `vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests/src/main/java/com/vaadin/flow/component/grid/it/Repro1675View.java`

## Root cause (suspected)

Two halves, one in each repo:

1. Flow's grid connector renders the header component **inside** the `vaadin-grid-sorter` element when the column is sortable:

https://github.com/vaadin/flow-components/blob/6c2510dd66085d0b26ea4d127f857f0a9ff3074f/vaadin-grid-flow-parent/vaadin-grid-flow/src/main/resources/META-INF/frontend/gridConnector.ts#L433-L446

2. The web component's sorter host is content-sized (`display: inline-flex`), so a `width: 100%` child resolves against the shrink-wrapped sorter instead of the header cell:

https://github.com/vaadin/web-components/blob/v25.3.0-alpha3/packages/grid/src/styles/vaadin-grid-sorter-base-styles.js#L9-L12

## Notes

- Workarounds from the issue still apply: theme `vaadin-grid-sorter` with `:host { width: 100% }` (styling module), or set the wrapper width with a JS call after attach.
- As noted in the issue's comments, making the sorter full-width by default would also stretch text-only sortable headers (moving the sort indicator to the far right), so a default change has UX trade-offs — possibly better addressed with an opt-in (e.g. a sorter part/attribute or documented styling recipe).
