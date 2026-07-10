> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced — `refreshAll()` fired from inside `DataProvider.fetch()` is silently dropped; the same refresh fired outside fetch works
- **Hypothesis tested:** The bug is that a data change event fired by the data provider during its own fetch pass is ignored by the grid, triggered by growing the reported size and calling `refreshAll()` inside `fetch()` when the last page is requested, observable as the grid's client-side item count staying stale while the server-side size has grown.
- **Regression?:** not a regression (deliberate re-entrancy guard in DataCommunicator; reported 2019, behavior unchanged)
- **Flavor:** Flow (root cause in Flow core `flow-data`)
- **Branch:** `repro/1206` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ main (25.3-SNAPSHOT, Flow 25.3-SNAPSHOT)
- **Present on main?:** yes
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** ![grid stuck at Item 99 while server reports 200](https://raw.githubusercontent.com/vaadin/flow-components/repro-1206/repro-1206.png)

## Observed behavior

Grid with an `AbstractBackEndDataProvider` that reports 100 items and, when the grid fetches the last page, grows the size by 100 and calls `refreshAll()` from within `fetchFromBackEnd` (the reporter's unknown-size emulation). A control button fires the same `refreshAll()` outside fetch.

| Step | Grid client size | Server-side reported size |
| --- | --- | --- |
| initial | 100 | 100 |
| scroll to item 99 (refresh fired **inside** fetch) | **100 — stale** | 200 |
| click "refresh all (outside fetch)" | 200 | 200 |
| scroll to item 199 (refresh inside fetch again) | **200 — stale** | 300 |

The minimal pair isolates the bug precisely: the refresh event itself works, but is lost when fired during the grid's fetch pass. No console errors, no exceptions.

## Expected behavior (per the issue)

Firing a data change event from `fetch()` should make the grid re-query the size and show the new items.

## Root cause (suspected)

Deliberate re-entrancy guard in Flow core's `DataCommunicator`: while a flush is in progress, non-forced flush requests are not registered ("prevents infinite loop in cases including @PreserveOnRefresh"). `refreshAll()` → `reset()` → `requestFlush()` happens exactly during `flushInProgress == true`, so the request is dropped — and nothing re-schedules it after the flush completes, losing the update until some other interaction triggers a flush:

https://github.com/vaadin/flow/blob/1ac6a6e4b156dda1460d63be1fa45b826ef92898/flow-data/src/main/java/com/vaadin/flow/data/provider/DataCommunicator.java#L1214-L1226

A targeted fix would remember that a flush was requested mid-flush and schedule one more flush after the current one finishes (preserving the infinite-loop protection by running it as a new round, not recursion).

## Steps to reproduce

1. Open `http://localhost:8080/repro-1206`.
2. Scroll the grid to the last item ("Item 99") — the label under the grid shows the server now reports 200, but the grid still ends at Item 99.
3. Click "refresh all (outside fetch)" — the grid immediately grows to 200 items.

## Reproduction

How to run: start the server (`mvn package jetty:run -am -pl vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1206`
- **Scaffold:** `vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests/src/main/java/com/vaadin/flow/component/grid/it/Repro1206View.java`

## Notes

- The modern designed alternative for unknown-size lists is Flow's built-in unknown-item-count lazy loading (`grid.setItems(fetchCallback)` without a count callback, `GridLazyDataView#setItemCountEstimate/…`), which did not exist in 2019 and covers the reporter's use case without firing refresh from fetch.
- Still, silently dropping a legal `refreshAll()` is a footgun: the provider contract nowhere forbids firing data change events during fetch, and there is no warning when the event is discarded.
