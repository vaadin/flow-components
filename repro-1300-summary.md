> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced — columns render in inside-out order when a grid with many columns gets `appendHeaderRow()` in a round-trip after the initial render; without the header row the same deferred configuration is ordered correctly
- **Hypothesis tested:** The bug is that Grid columns render in the wrong order, triggered by configuring the grid (50 columns + `appendHeaderRow()`) after its initial render — from a button click listener (Legioth's simplification; the original used `ui.access` from another thread) — observable as header texts in DOM order differing from 0…49.
- **Regression?:** unknown (reported 2019 against vaadin-grid-flow 3.0.2, still broken on main; no known-good version to anchor a bisect)
- **Flavor:** Flow
- **Branch:** `repro/1300` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ main (25.3-SNAPSHOT, Flow 25.3-SNAPSHOT)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** ![deferred grid renders columns inside-out](https://raw.githubusercontent.com/vaadin/flow-components/repro-1300/repro-1300.png)

## Observed behavior

Three grids, each 50 columns with numbered headers:

| Grid | Configuration | First header row order |
| --- | --- | --- |
| control | 50 columns + `appendHeaderRow()` in the constructor | 0,1,2,…,49 ✓ |
| deferred | same, but from a button click listener | **24,25,23,26,22,27,…,1,48,0,49** ✗ |
| deferred, no header row | 50 columns from the same click listener, no `appendHeaderRow()` | 0,1,2,…,49 ✓ |

The scrambled order is a deterministic inside-out interleaving radiating from the middle. Two further facts, verified in the DOM:

1. The grid's computed column order (`_columnTree`) equals the light-DOM element order — the `vaadin-grid-column` elements themselves are in the wrong order in the light DOM, so the web component renders faithfully; the fault is on the Flow side.
2. The trigger is specifically the header-row wrapping: the same 50-column deferred configuration without `appendHeaderRow()` is ordered correctly.

No console errors, no server exceptions.

## Expected behavior

Column order 0…49 regardless of when the grid is configured.

## Root cause (suspected)

`appendHeaderRow()` on a grid that already has columns wraps every column in its own `vaadin-grid-column-group`. Each wrap is a remove + insert-at-the-same-index on the server, which preserves order index-wise in the server-side state tree:

https://github.com/vaadin/flow-components/blob/6c2510dd66085d0b26ea4d127f857f0a9ff3074f/vaadin-grid-flow-parent/vaadin-grid-flow/src/main/java/com/vaadin/flow/component/grid/ColumnGroupHelpers.java#L94-L107

When this happens in the same round-trip as the initial attach, order survives. When it happens in a later round-trip (button click, `ui.access`), the client applies the batch of 50 remove/insert pairs in an order that produces the inside-out interleaving — pointing at Flow core's client-side application of batched element moves as the layer where the order is lost. (Needs confirmation during the fix; the trigger site above is in flow-components, the suspected mis-application is in Flow core's client engine.)

## Steps to reproduce

1. Open `http://localhost:8080/repro-1300`.
2. Click "Configure".
3. Compare the three grids' header rows: the middle grid shows 24,25,23,26,… instead of 0,1,2,….

## Reproduction

How to run: start the server (`mvn package jetty:run -am -pl vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1300`
- **Scaffold:** `vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests/src/main/java/com/vaadin/flow/component/grid/it/Repro1300View.java`

## Notes

- Legioth's comment from the issue is confirmed: no separate thread is needed — any post-render round-trip triggers it.
- Related mechanism: the `ColumnLayer` wrapping is the same machinery involved in #1267 ("appending footer rows during attach…", whose original symptom is fixed).
