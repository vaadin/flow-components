# Block: InvalidFormulaTracker

> Phase 3 spec. Drafted from the source (`InvalidFormulaTracker.java`), the
> characterization tests (`InvalidFormulaTrackerTest.java`, 21 tests), and the
> slice that introduced it (slice 89, journal entry 2026-06-21).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Track the cells whose user-entered formulas the server has flagged as invalid,
per active sheet. The Spreadsheet's formula bar displays a red-marker
indicator on these cells; clearing the mark is also part of the contract
(re-entering a valid formula or row-shifting the cell removes it).

Two reasons the state is per-sheet rather than per-workbook:

1. A formula valid on one sheet may be invalid on another (e.g. references to
   a row that exists on the active sheet but not on sheet 2).
2. Sheet swaps don't lose marks — when the user re-activates a previously
   touched sheet, its previously-marked cells light up again immediately.

Five operations cover the lifecycle: mark / check / unmark a single cell on
the active sheet, re-key the active sheet's marks after a row shift, and
flush every sheet's marks during a full-sheet reload.

## Inputs

### `mark(int col, int row)`, `isMarked(int col, int row)`, `removeMark(int col, int row)`

- `col`, `row` — 1-based coordinates, as used by
  `SpreadsheetUtil.toKey(col, row)`. The active sheet is resolved each call
  via `spreadsheet.getWorkbook().getActiveSheetIndex()`. No validation of
  range (the caller is responsible for passing sensible coords).

### `shiftActiveSheetRows(int first, int last, int n)`

- `first`, `last` — 0-based POI row indices defining the source range being
  shifted.
- `n` — signed shift delta. Positive = rows move down; negative = rows move
  up (which overwrites the rows in `(first + n, first]`).

### `clearAll()`

- No arguments.

## Outputs

- `isMarked` returns `boolean`. All other methods return `void`.
- `mark` / `removeMark` / `shiftActiveSheetRows` / `clearAll` mutate the
  internal `Map<Integer, HashSet<String>>` (active-sheet → key-set).

## Side effects

- **`mark`** — creates an empty `HashSet` for the active sheet if none exists,
  then adds the `SpreadsheetUtil.toKey(col, row)` string to it.
- **`isMarked`** — pure read; does NOT lazily create a set entry. Returns
  `false` if the active sheet has no entry yet.
- **`removeMark`** — removes the key from the active sheet's set, if a set
  exists. Does NOT remove the (now-empty) set entry from the map.
- **`shiftActiveSheetRows`** — replaces the active sheet's set with a freshly
  computed re-keyed set; returns early without mutation if the active sheet
  has no entry.
- **`clearAll`** — `Map.clear()` on the per-sheet map. After this every
  sheet's `isMarked` returns `false`.

No external side effects (no element-property writes, no event firing). Pure
in-memory state.

## Invariants

- The map is keyed by the **POI active-sheet index** (the value returned by
  `workbook.getActiveSheetIndex()`), NOT by the sheet's visible index nor by
  its name. A POI-level reorder (delete + insert that shifts indices) would
  change which marks are "on" which sheet — that's by design but worth
  knowing.
- Keys use `SpreadsheetUtil.toKey(col, row)` — 1-based. A regression that
  passed 0-based coords directly would silently store a different key and
  the round-trip `mark / isMarked` would still succeed (the key just
  wouldn't match the rest of the codebase's key conventions). Tests pin the
  format indirectly by using 1-based values for both `mark` and `isMarked`.
- `shiftActiveSheetRows` is the only operation that ever **removes** entries
  from the active sheet's set. `removeMark` removes a single key. There is
  no "remove all marks on sheet X" method short of `clearAll`.

## Edge cases

### `mark` / `removeMark` / `isMarked`

- **Fresh tracker**, `isMarked` for any coord → `false`.
- **Idempotent mark** — calling `mark(col, row)` twice has the same effect as
  calling it once; the backing set is a `HashSet`. A single `removeMark`
  clears it.
- **`removeMark` for a never-marked coord** — no-op, no exception (the set
  exists but doesn't contain the key, so `Set.remove(...)` returns `false`).
- **`removeMark` for a sheet with no map entry** — guarded by
  `containsKey`. No-op, no NPE.
- **`isMarked` for a sheet with no map entry** — returns `false` WITHOUT
  creating an entry (so a subsequent `mark` on the same sheet still
  works without confusion).
- **Active sheet switch** — marks made under sheet 0 are invisible from
  sheet 1; marks on different sheets accumulate independently and survive
  switches back and forth.

### `shiftActiveSheetRows`

For each key on the active sheet's set, the method classifies into one of
three branches (checked in order):

1. **Dropped** — `n < 0 && first + n < row && row <= first`. The row is in
   the half-open range `(first + n, first]` (inclusive at `first`, exclusive
   at `first + n`). When rows are shifted UP by `|n|`, those rows are
   overwritten; their marks are dropped.
2. **Translated** — `first <= row && row <= last`. The row is in the shifted
   range. The new key is `toKey(col + 1, row + n + 1)` — i.e. the row index
   shifts by `n`.
3. **Kept** — the row is OUTSIDE both `[first, last]` and the dropped range.
   The original key is preserved.

The `n < 0` guard on branch 1 makes the drop path **up-shift-only**. With
`n > 0` (down-shift) the drop is never taken; every key is either translated
or kept.

- **Empty active-sheet set** (`null` from `Map.get`) — early return; no map
  mutation; safe to call.
- **Single mark in the translated range** — replaced by its shifted key.
- **Single mark below `first`** — kept (it's the "row above" the shift; not
  affected).
- **Single mark above `last`** — kept (it's the "row below" the shift; not
  affected).
- **Single mark at `row == first`** with `n < 0` — DROPPED (inclusive upper
  bound of the drop range).
- **Single mark at `row == first + n`** with `n < 0` — KEPT (exclusive
  lower bound).
- **Single mark at `row == first`** with `n > 0` — TRANSLATED (no drop
  because `n > 0`; the row is in `[first, last]`).
- **Multiple marks, mixed positions** — each key classified independently;
  one call applies the dispatch to every entry in the active sheet's set.
- **Marks on a different sheet** — UNTOUCHED. Only the active sheet's set
  is re-keyed.

### `clearAll`

- **Empty tracker** — no-op, no exception.
- **Multiple sheets with marks** — every sheet's set is dropped from the
  map. After `clearAll`, the next `isMarked` on any sheet returns `false`.

## Error modes

- **`null` workbook** (i.e. `spreadsheet.getWorkbook() == null`) →
  `NullPointerException` at the active-sheet index lookup. The SUT does NOT
  guard against this; callers must ensure the workbook is initialized.
- **Out-of-range `col` / `row`** — never validated; the key is just a
  string and `mark`/`isMarked` round-trip consistently regardless.
- **`shiftActiveSheetRows` with `first > last`** — not validated; the keep
  branch's `first <= row && row <= last` will never be true, so every key
  falls through to the kept branch unless `n < 0` triggers the drop. The
  effective behavior is the same as a no-op shift (assuming `n` is also
  zero or the drop range is empty).

## Non-obvious quirks

- **The drop range is half-open `(first + n, first]`.** Inclusive at the
  upper end (`row == first` is dropped) and exclusive at the lower end
  (`row == first + n` is kept). The asymmetry is observable only at the
  exact boundary — easy to flip with a one-character regression
  (`<=` ↔ `<`). The characterization test
  `shiftActiveSheetRows_negativeN_rowAtFirstBoundary_isDropped` pins the
  inclusive case; `shiftActiveSheetRows_negativeN_rowAtFirstPlusN_isNotDropped_butTranslated`
  pins the exclusive case.
- **The drop branch's `n < 0` guard.** With positive `n` (down-shift) the
  drop is never taken even for rows in `(first + n, first]` — and indeed
  that range is empty for `n > 0` because `first + n > first`. The guard is
  defensive but not load-bearing for valid inputs.
- **Active-sheet resolution per call.** Every operation re-reads
  `workbook.getActiveSheetIndex()` — the helper does NOT cache the active
  index. A regression that cached it once at construction would silently
  break per-sheet isolation. Pinned by the
  `mark_onSheet0_isNotVisible_afterActiveSheetSwitchesToSheet1` test.
- **`removeMark` doesn't compact empty sets.** After `mark` then `removeMark`
  for the same coord, the active sheet's set is empty but still in the map.
  Subsequent `isMarked` correctly returns `false` because the set is empty,
  but the map entry persists. Not visible from the outside — but worth
  noting if a future caller decides "is any cell marked on this sheet" via
  `map.containsKey(activeSheetIndex)`. That predicate would return `true`
  even when no cells are actually marked.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/InvalidFormulaTracker.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/InvalidFormulaTrackerTest.java` (21 tests; each contract paragraph in this spec is pinned by at least one named test)
- Slice that introduced it: journal entry 2026-06-21, slice 89 (`CellRangeNormalizer + InvalidFormulaTracker`)
- Callers (extant): `Spreadsheet.markInvalidFormula` / `isMarkedAsInvalidFormula` / `removeInvalidFormulaMark` (thin delegators); `Spreadsheet.shiftRows` (calls `shiftActiveSheetRows` after the POI shift); `Spreadsheet.clearSheetServerSide` (calls `clearAll` during the full-sheet reload path)

## Contract status

Every invariant and edge case in this spec maps to at least one passing test
in `InvalidFormulaTrackerTest.java`. The 5-axis self-audit during slice 89
verified each test pins a named 1-line regression in its comment.

## What the spec doesn't cover

- **Persistence.** Marks are in-memory only; a session restart loses them.
  Not part of the contract; consistent with the pre-extraction behavior on
  Spreadsheet.
- **Concurrency.** The tracker is not thread-safe (`HashMap` + `HashSet`
  without synchronization). Spreadsheet operations are expected to run on
  the UI thread; cross-thread access would race.
- **Memory bounds.** No max-size cap. A pathological caller marking
  millions of distinct cells would grow the set without limit; the
  pre-extraction code had the same behavior.

## Human review

**Status: not yet reviewed.** Per the pipeline's Phase 3 process, this draft
needs a domain expert to verify:

1. Is the half-open drop range `(first + n, first]` actually the intended
   semantic, or is the off-by-one a latent bug inherited verbatim from
   `Spreadsheet`? The boundary tests pin the CURRENT behavior; a behavior
   change would need a separate commit and a separate review.
2. The `mark` / `removeMark` asymmetry — `removeMark` leaves the empty
   set entry in the map — should it be compacted? If a caller is added
   later that uses `containsKey` to mean "any marks", this becomes a
   correctness bug. Recommendation: either compact in `removeMark` OR
   document that "any marks" must be checked via `set.isEmpty()`.
3. Should the helper validate the `first`/`last` ordering in
   `shiftActiveSheetRows`? Currently `first > last` silently produces a
   no-op-equivalent. If callers might pass swapped corners, validation
   would catch it; if not, the silence is acceptable.
