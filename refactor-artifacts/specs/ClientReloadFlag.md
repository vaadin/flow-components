# Block: ClientReloadFlag

> Phase 3 spec. Drafted from the source (`ClientReloadFlag.java`), the
> characterization tests (`ClientReloadFlagTest.java`, 10 tests), and the
> Phase-4 slice that introduced it (slice 93, journal entry 2026-06-21).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Encapsulate the "deferred-flush boolean with client-mirror" pattern. The
single responsibility: track that a full client-side reload is pending and
push the marker to the client through a single `beforeClientResponse`
callback per client round, regardless of how many times the pending state
was set during the server-side processing.

The pattern was previously distributed across **five fragments** on
`Spreadsheet.java`:

1. A `boolean reload` field — the pending state.
2. A private `setReload(boolean)` setter that mirrored a timestamp to the
   client `reload` element property only when the arg was `true`.
3. The public `isRerenderPending()` read.
4. An inline `beforeClientResponse` callback registration block in
   `reloadActiveSheetData`.
5. A private `updateReloadState()` method that wired the callback's flush
   logic, including a default for `initialSheetSelection`.

The architectural rename: ONE class, three methods. The pattern now has a
name and a single home.

## Inputs

### `isPending()`

- No arguments. Returns the current flag state.

### `markPending(Spreadsheet spreadsheet)`

- `spreadsheet` — the host Spreadsheet that the callback should register
  against and that the flush will operate on. Same instance on every call
  in practice (each Spreadsheet has its own `ClientReloadFlag` instance).

### `flush(Spreadsheet spreadsheet)`

- `spreadsheet` — same as above. Package-private; callers are normally
  the `beforeClientResponse` callback that `markPending` registers, but
  it's also reachable directly from tests.

## Outputs

- `isPending()` returns `boolean`.
- `markPending` and `flush` return `void`. Side effects are observable
  on the host Spreadsheet's element + on `spreadsheet.initialSheetSelection`.

## Side effects

### `markPending(spreadsheet)`

1. If `UI.getCurrent()` is non-null, register a `beforeClientResponse`
   callback that — when fired — checks `pending` and calls `flush` if
   still `true`.
2. Set `pending = true` **unconditionally** (outside the `Optional`
   guard). This is load-bearing: a regression that moved the assignment
   inside the lambda would silently no-op in headless contexts.

### `flush(spreadsheet)`

1. Push `System.currentTimeMillis()` to `spreadsheet.getElement()
   .setProperty("reload", ...)`. The long widens to `double` per
   Vaadin's `Element.setProperty(String, double)` overload.
2. Set `pending = false`.
3. If `spreadsheet.initialSheetSelection == null`, seed it from
   `spreadsheet.sheetState.getSelectedCellsOnSheet(activeSheet)` — and
   fall back to `"A1"` if that returns `null`.

## Invariants

- **Coalescing.** Multiple `markPending` calls between flushes produce
  at most one flush. The flag is idempotent on repeated marks (`pending
  = true` stays `true`), and a single flush clears the flag so any
  subsequent `beforeClientResponse` callback finds `pending = false` and
  short-circuits.
- **Flag mutation is unconditional.** `pending = true` runs regardless
  of whether `UI.getCurrent()` returns a UI. Headless contexts still
  observe the correct flag state via `isPending()`.
- **Flush is at-most-once per round.** The `beforeClientResponse`
  callback fires once per client round; the inner `if (pending)` check
  is what gates the flush. If `pending` got cleared earlier in the
  round (by another path), the flush won't fire again.
- **Initial-selection defaulting is a one-time-per-flush operation.**
  The `null` check guards against clobbering a seed that
  `CommandReplayer` (or any other caller) set during the same round.

## Edge cases

- **markPending without a UI** (`UI.getCurrent() == null`) — the
  callback isn't registered (no UI to register against) but the
  `pending = true` mutation still happens. A later `isPending()` call
  correctly returns `true`. A direct `flush(spreadsheet)` invocation
  also still works.
- **flush with `initialSheetSelection` already set** — the seed is
  preserved verbatim; `sheetState` is NOT consulted. Pinned by
  `flush_neverConsultsSheetState_whenInitialSelectionAlreadySet`.
- **flush with `initialSheetSelection` null AND `sheetState` returns
  null** — defaults to `"A1"`. A regression that returned `null` or
  `""` would leave the client without a target cell to render-select.
- **flush with `initialSheetSelection` null AND `sheetState` returns
  a known selection** — uses that selection. The use case is
  "remembered selection from before sheet swap".
- **Multiple back-to-back markPending calls** — the flag stays `true`;
  the `beforeClientResponse` callback is registered each time but is
  idempotent on the flag.
- **flush called when `pending` is already false** — pushes the
  timestamp anyway, clears the (already-clear) flag, and seeds the
  selection if applicable. The pre-extraction code had an explicit
  `if (reload) { ... } else { setReload(reload); }` shape where the
  else branch was a no-op (`setReload(false)` did nothing); the
  post-extraction code drops the conditional because flush is now
  only invoked from the `if (pending)` callback path. Direct flush
  calls outside that path are not part of the contract.

## Error modes

- **`null` spreadsheet** to `markPending` or `flush` — NPE at the
  first dereference. Not guarded.
- **`null` UI returned from `UI.getCurrent()`** — handled by the
  `Optional.ifPresent` guard. No exception.
- **Vaadin `beforeClientResponse` callback failures** — propagate to
  the caller. The SUT doesn't catch them.
- **Mid-round state mutation race** — the flag is not thread-safe
  (`boolean` without synchronization). Spreadsheet operations are
  UI-thread-only; cross-thread access would race.

## Non-obvious quirks

- **The seed-on-flush coupling.** Two concerns are bundled in
  `flush`: pushing the timestamp AND defaulting the initial
  selection. They're load-bearing together because both fire at the
  same lifecycle point ("client is about to reload the active sheet")
  — splitting them would create a window where the client could
  reload before the selection is seeded, producing a 1-frame "wrong
  selection" flicker. Pre-extraction, they were already coupled in
  `updateReloadState`; the helper preserves that coupling.
- **`setReload(false)` was a deliberate no-op pre-extraction.** The
  original `setReload(boolean reload) { if (reload) setProperty(...);
  }` guarded against pushing a timestamp when the flag was already
  cleared. Post-extraction, the equivalent behavior is achieved by
  the callback's `if (pending)` check: `flush` is only called when
  the flag is `true`. If a future caller invokes `flush` directly
  with `pending` already false, they'll push a redundant timestamp
  (likely benign on the client, but worth noting).
- **Long-to-double timestamp widening.** `System.currentTimeMillis()`
  returns `long`, but Vaadin's `Element.setProperty` overload for
  numerics takes `double`. The Java auto-widening means a tiny
  precision-loss window exists at very large timestamps (years from
  now), but it's not load-bearing for the lifecycle of this code.
- **`pending = true` is OUTSIDE the `Optional.ifPresent`.** This is
  load-bearing for headless / unit-test contexts. A reorder that
  moved the assignment inside would silently fail in
  `UI.getCurrent() == null` environments. The characterization test
  `markPending_setsFlagToTrue_evenWhenUIIsNotPresent` pins this.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/ClientReloadFlag.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/ClientReloadFlagTest.java` (10 tests; each contract paragraph here is pinned by at least one)
- Slice that introduced it: journal entry 2026-06-21, slice 93 (`Phase 4 architectural extract: ClientReloadFlag`)
- Callers: `Spreadsheet.isRerenderPending()` (thin delegator to `isPending()`); `Spreadsheet.reloadActiveSheetData()` (calls `clientReloadFlag.markPending(this)`); the `beforeClientResponse` callback registered by `markPending` (calls `flush` when the flag is still `true` at client-flush time).

## Contract status

Every contract paragraph in this spec maps to at least one passing test
in `ClientReloadFlagTest.java` OR to the end-to-end golden snapshot in
`SpreadsheetHistoryManagerPayloadTest`. The split is intentional: unit
tests pin per-method observable behavior with mocks; the snapshot pins
the wire-format byte-shape across the full lifecycle.

## What the spec doesn't cover

- **Performance.** All three methods are O(1). The `beforeClientResponse`
  callback registration is a Vaadin internal operation; its cost is
  measured in microseconds.
- **Concurrency.** Not thread-safe. UI-thread only.
- **Persistence.** The flag is in-memory; a session restart resets it
  to `false`.

## Human review

**Status: not yet reviewed.** Per the pipeline's Phase 3 process, this
draft needs a domain expert to verify:

1. **Is the seed-on-flush coupling actually intentional?** The bundling
   of "push timestamp" + "seed initialSheetSelection" inherits the
   pre-extraction behavior verbatim. The argument FOR keeping them
   coupled is the "no-flicker" rationale above. The argument AGAINST is
   that splitting them might enable use cases that need just one or
   just the other (e.g. a "selection-only re-seed" without a reload).
   Worth documenting one way or the other.
2. **Should `flush` early-return when `pending` is already false?** The
   current contract says it pushes the timestamp regardless. The
   pre-extraction code had explicit handling for the false case. With
   the post-extraction architecture, direct-flush callers are
   conceptually wrong but not actually blocked. Adding an early-return
   would make the helper more idiot-proof at the cost of one extra
   branch. Recommendation: defer until a real caller surfaces.
3. **The `setReload(false)` no-op behavior is now dropped.** No
   regression observed (the test suite passes, the payload snapshot is
   byte-identical), but worth confirming that no external client-side
   code path depended on the absence of a `"reload"` timestamp push
   when the flag was already cleared.
