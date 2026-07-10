> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced — the traffic asymmetry is still present, but it is inherent to how ComponentRenderer works (see Root cause); LitRenderer is the designed lightweight alternative
- **Hypothesis tested:** The bug is that sorting a grid column rendered with `addComponentColumn` produces much heavier update traffic than sorting a plain value column, triggered by any sort-order change, observable as the UIDL response size per sort click.
- **Regression?:** not a regression (inherent to server-side component rendering since ComponentRenderer shipped)
- **Flavor:** Flow
- **Branch:** `repro/1154` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ main (25.3-SNAPSHOT, Flow 25.3-SNAPSHOT)
- **Present on main?:** yes
- **Theme / Browser:** Lumo / Chromium (playwright-cli)

## Observed behavior

Two identical grids with 100 items each; one has a plain `addColumn(SimpleEntity::getId)` id column, the other `addComponentColumn(entity -> new Text(...))`. UIDL response bytes measured per sort-header click (two rounds, so the second round is steady state — components have already been created once):

| Sort click | Plain column | Component column |
| --- | --- | --- |
| round 1, click 1 | 2357 B | 15209 B |
| round 1, click 2 | 2379 B | 15397 B |
| round 2, click 1 | 2420 B | 15479 B |
| round 2, click 2 | 2420 B | 15468 B |

The component column costs **~6.4× more traffic on every sort-order change**, not only on first render. (The 2020 report measured ~4KB vs ~28KB — same phenomenon, ~7×.) Sorting itself works correctly in both grids; no console errors, no server exceptions.

## Expected behavior (per the issue)

Roughly the same traffic as a plain value column, or a way to sort entirely on the client.

## Steps to reproduce

1. Open `http://localhost:8080/repro-1154` with DevTools network tab open.
2. Click the "Id" sort header of the plain grid, then of the component grid.
3. Compare the sizes of the `?v-r=uidl` responses.

## Reproduction

How to run: start the server (`mvn package jetty:run -am -pl vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1154`
- **Scaffold:** `vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests/src/main/java/com/vaadin/flow/component/grid/it/Repro1154View.java`

## Root cause (suspected)

Inherent to server-side component rendering rather than a defect in Grid. With a `ComponentRenderer` column, every data refresh — including a sort-order change of in-memory data — makes the data generator create fresh server-side components for the refreshed range and ship their full element hierarchy in the UIDL (the old ones are detached):

https://github.com/vaadin/flow/blob/1ac6a6e4b156dda1460d63be1fa45b826ef92898/flow-data/src/main/java/com/vaadin/flow/data/provider/AbstractComponentDataGenerator.java#L40-L110

A plain column only resends small JSON row data, hence the ~6× difference.

## Notes

- The designed answer to the reporter's question ("how to make sorting cheap") is `LitRenderer`: it renders cell content client-side from a template plus per-item properties, so a sort only resends row JSON — component-like content without per-row server components.
- Actionable enhancement angle, if the team wants one: reuse existing per-item components in `AbstractComponentDataGenerator` when only the order of an in-memory data set changes (items and keys are unchanged on sort). Until then this is expected behavior with a documented-pattern workaround.
