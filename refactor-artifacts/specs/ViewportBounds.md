# Block: ViewportBounds

> Third Phase 3 spec. Smallest block to date (70 LOC of source, 10 unit tests).
> Tests a state-holder block at the simplest end of the spectrum — four
> primitive ints + atomic update + change-detection predicate. Drafted from
> the source (`ViewportBounds.java`) + `ViewportBoundsTest.java` (10 tests) +
> the slice that introduced it (slice 14, journal entry 2026-06-05).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

A server-side memo of the cell range the client is currently displaying.
Four 1-based indices: first row, last row, first column, last column. Updated
from `Spreadsheet.onSheetScroll` when the client reports a new viewport; read
by `Spreadsheet`'s public getters and by code that decides what cells to push
to the client.

Holds no POI references and no Spreadsheet reference — it's a pure state
container plus an equality predicate for cheap change detection.

## Inputs

### `getFirstRow()`, `getLastRow()`, `getFirstColumn()`, `getLastColumn()`

- No arguments. Return the corresponding 1-based index from the last
  `update()` call, or `0` (primitive int default) if never updated.

### `hasChanged(int firstRow, int lastRow, int firstColumn, int lastColumn)`

- Four ints — the candidate viewport. Returns `true` if any of the four
  differs from the currently-stored viewport. Caller uses this to decide
  whether to re-push cell data.

### `update(int firstRow, int lastRow, int firstColumn, int lastColumn)`

- Four ints — replaces all four indices atomically. No validation, no
  sign check, no range check. The `-1, -1, -1, -1` sentinel is treated
  as a regular state (no special handling).

## Outputs

- Getters return `int`.
- `hasChanged` returns `boolean`.
- `update` returns `void`.

## Side effects

None outside the block's own state.

## Invariants

- `getFirstRow()` returns the value passed as the first argument to the
  most recent `update()` call. Same for the other three accessors.
- Immediately after `update(a, b, c, d)`,
  `hasChanged(a, b, c, d)` returns `false`. Calling `hasChanged` with
  any other combination returns `true`.
- Before any `update()` call, all four getters return `0`. (Primitive
  int field defaults.)
- The block is stateless beyond the four ints. There's no observation
  ordering, no mutex, no derived state.

## Edge cases

- **Never-updated bounds**: all four getters return `0`. `hasChanged`
  with any non-zero arg returns `true`, with all zeros returns `false`.
- **`update(-1, -1, -1, -1)`**: pinned as Spreadsheet's "viewport not yet
  known" sentinel. `Spreadsheet.reloadActiveSheetData` calls this to
  reset viewport; `setSpreadsheetComponentFactory` checks
  `viewportBounds.getFirstRow() != -1` to decide whether to load
  components now or defer.
- **Negative arguments to `update`**: accepted without complaint.
  Negative values become the new state and are returned by getters.
- **Identical repeated `update` calls**: behavior is the same — the
  state is replaced (idempotent on equal values).
- **One axis updated to differ, three same**: `hasChanged` correctly
  flags this as changed.

## Error modes

- None. The block cannot throw — it has no inputs that fail, no
  collections that overflow, no IO. Even an `Integer.MIN_VALUE` /
  `MAX_VALUE` argument is accepted and stored.

## Non-obvious quirks

- **The atomic-update semantic matters for `hasChanged`.** `update`
  always writes all four. Partial-update operations don't exist on this
  block; callers that want to change just one index must read the other
  three first and pass them all through `update`. This is intentional:
  it makes the change-detection predicate cheap (single comparison) and
  avoids race conditions where two partial updates land in the wrong
  order.
- **No event firing.** Unlike many state holders in the codebase, this
  block doesn't fire change events. Spreadsheet polls via `hasChanged`
  inside its `onSheetScroll` handler instead.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/ViewportBounds.java`
- Tests: `ViewportBoundsTest.java` (10 tests, slice 22)
- Slice that introduced it: journal entry 2026-06-05, slice 14
- Companion blocks: `FreezePaneState` (sibling viewport state — owns
  scroll positions + split coordinates); `ViewportBufferConfig` (sibling
  — owns row/column buffer sizes); `Spreadsheet.onSheetScroll` (the
  primary `update` caller)

## Contract status

Every invariant maps to a passing test in `ViewportBoundsTest.java`. The
sentinel-update for "viewport not yet known" is explicitly covered by
`update_withNegativeOnes_marksViewportAsUnknown`. The atomic-update
semantic and the per-axis change detection are each covered by their own
`hasChanged_*` test.

## What the spec doesn't cover

- Performance characteristics. Trivially O(1) for all operations.
- Thread safety. Not thread-safe; HashSet/Map-style "request-thread
  serializes access" applies.
- Serialization. The block is `Serializable`; the four int fields
  serialize trivially.

## Human review

**Status: not yet reviewed.** Per pipeline §13.2, this draft needs a domain
expert to verify:

1. Is the `update(-1, -1, -1, -1)` "viewport not yet known" sentinel really
   the contract, or is it accidental behavior that just happens to work?
   `setSpreadsheetComponentFactory` checks `getFirstRow() != -1` — if the
   sentinel changes, this caller breaks silently.
2. Should `hasChanged` document its tie-break behavior when called before
   any `update`? Currently it returns `false` only for the all-zeros case.
   That's arguably surprising for a "has the viewport changed?" predicate
   when the viewport is "unknown."
3. Are there other callers of `update` besides `onSheetScroll` and
   `reloadActiveSheetData`? The grep at slice-write time showed only two,
   but a re-grep before approving would be cheap insurance.
