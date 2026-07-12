> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced — a `sortersChanged` call reaching the server for a just-removed sorted column throws `IllegalArgumentException: Received a sorters changed call from the client for a non-existent column`
- **Hypothesis tested:** The bug is that removing a sorted column does not fully clear the client sorter state, so a `sortersChanged` call references the removed column, triggered by column removal coinciding with sort activity (multi-sort), observable as the server-side `IllegalArgumentException` from `Grid.sortersChanged`.
- **Regression?:** unknown (reported against Vaadin 14.0.15 in 2019, confirmed on Vaadin 24 by a commenter, still present on main; no known-good version stated)
- **Flavor:** Flow
- **Branch:** `repro/1201` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ main (25.3-SNAPSHOT, Flow 25.3-SNAPSHOT)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** ![grid after the failing remove+sort race](https://raw.githubusercontent.com/vaadin/flow-components/repro-1201/repro-1201.png)

## Observed behavior

Multi-sort grid (`setMultiSort(true)`) with three sortable keyed columns A/B/C, plus "remove column A" and "remove all columns" buttons.

| Sequence | Result |
| --- | --- |
| sort A+B, wait, remove A, wait, sort C | **no exception** — removeColumn cleared A's sorter cleanly |
| multi-sort A+B, wait, `removeAllColumns()` | **no exception** |
| sort A, `removeColumn(A)` **and immediately** emit `sortersChanged` (re-click sorters before the round-trip) | **`IllegalArgumentException: … non-existent column`** on the server (9 exceptions logged, client console errors) |

So the clean sequential paths are handled, but when a `sortersChanged` reaches the server referencing a column that was just removed — the race the reporters describe (column removal coinciding with sort round-trips) — the server still throws.

## Expected behavior

Removing a column should clear its client-side sorter state; a `sortersChanged` referencing a removed column should be ignored, not throw a server-side exception.

## Steps to reproduce

1. Open `http://localhost:8080/repro-1201`.
2. Click the "A" column sorter (sort by A).
3. Click "remove column A" and immediately re-trigger sorting (click the remaining sorters) before the round-trip settles.
4. The server log shows `IllegalArgumentException: Received a sorters changed call from the client for a non-existent column`.

## Reproduction

How to run: start the server (`mvn package jetty:run -am -pl vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1201`
- **Scaffold:** `vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests/src/main/java/com/vaadin/flow/component/grid/it/Repro1201View.java`

## Root cause (suspected)

`Grid.sortersChanged` looks each incoming sorter's path up in `idToColumnMap` and **throws** when the column is absent, rather than skipping the stale sorter:

https://github.com/vaadin/flow-components/blob/2bb8f77a60b92f6289c9df10b9349acf9f71d7b1/vaadin-grid-flow-parent/vaadin-grid-flow/src/main/java/com/vaadin/flow/component/grid/Grid.java#L4025-L4048

A `sortersChanged` from the client can legitimately reference a column removed on the server between the client emitting the event and the server handling it. A fix shape: skip (ignore) sorters whose column is no longer present instead of throwing — matching the reporter's suggestion.

## Notes

- The basic "sort then remove then sort" (with round-trips settling) no longer throws — the still-broken path is the concurrent one where `sortersChanged` and the column removal cross on the wire, which is exactly what the reporters hit in real apps (sorting while columns are being removed).
- finaris-cs (2024) reports the same on Vaadin 24, including with `removeAllColumns()`; the workaround in the wild is to swallow the exception in a custom error handler.
