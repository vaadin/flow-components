# Block: CellRangeNormalizer

> Phase 3 spec. Drafted from the source (`CellRangeNormalizer.java`), the
> characterization tests (`CellRangeNormalizerTest.java`, 17 tests), and
> the slice that introduced it (slice 89, journal entry 2026-06-21).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Build a {@link CellRangeAddress} from a user-facing input (either a string
like `"B3:C5"` or a 1-based int quartet) and clamp the result to the
active sheet's row/column extents. Two callers exist on
`Spreadsheet` — both `createCorrectCellRangeAddress` overloads now
delegate here.

Two overloads bundled because both reduce to the same shape (reorder
corners → clamp to sheet extents), but they arrive at the result through
slightly different inner arithmetic. The string overload clamps with
`getRows() - 1` / `getCols() - 1` directly. The int overload clamps with
`getRows()` / `getCols()`, then subtracts one when constructing the final
range. Both paths produce the same observable result for in-bounds
input. The asymmetry is preserved verbatim from the pre-extraction code.

## Inputs

### `fromAddressString(String addressString)`

- `addressString` — must be in the form `"<from>:<to>"`. The two corners
  are parsed as POI `CellReference` strings (e.g. `"B3"`, `"C5"`).
- The SUT does NOT validate the format. A missing colon → `split[1]`
  throws `ArrayIndexOutOfBoundsException`. A malformed cell reference
  → POI's `CellReference` constructor throws. Callers are responsible
  for providing well-formed input.

### `fromCoordinates(int row1, int col1, int row2, int col2)`

- 1-based row and column coordinates. The orchestrator reorders them
  so the result is min-to-max, then clamps each to the sheet extents,
  then subtracts one for POI's 0-based output.
- No input validation. Negative or non-monotonic inputs are accepted
  and processed (the reorder normalizes them).

## Outputs

Both overloads return a `CellRangeAddress` with POI 0-based coordinates.
The result is always min-to-max (first <= last on both axes).

## Side effects

None. Both methods are pure functions over the inputs + the active
sheet's `getRows()` / `getCols()` snapshot.

## Invariants

- **Min-to-max reorder.** For both overloads, `result.getFirstRow()` ≤
  `result.getLastRow()` and `result.getFirstColumn()` ≤
  `result.getLastColumn()`, regardless of input order.
- **Sheet-bound clamp.** Both overloads cap every coordinate to the
  active sheet's `(getRows() - 1, getCols() - 1)` cell — out-of-bound
  inputs collapse to the sheet's bottom-right corner.
- **0-based POI output.** Both overloads emit `CellRangeAddress` in
  POI's 0-based coordinate space (where `A1` is row 0, column 0).
- **Cross-overload result equivalence for in-bounds input.** Given the
  same logical range, the string and int overloads produce
  byte-identical `CellRangeAddress` instances (verified by paired
  tests `bothOverloads_orderedRangeInBounds_produceSameResult` and
  `bothOverloads_clampingAtSheetEdge_produceSameResult`).

## Edge cases

### `fromAddressString`

- **`"A1:A1"`** (single cell) — produces a 1×1 range at (0, 0). No
  special-case in the code; the corner-reorder is idempotent on equal
  inputs.
- **`"C5:B3"`** (reversed) — corners swap; result is the same as
  `"B3:C5"`. Pinned by `fromAddressString_reversedRange_isReorderedMinFirst`.
- **`"A1:A15"`** with 10-row sheet — `lastRow` clamps to 9
  (`getRows() - 1`). Pinned by `fromAddressString_rowBeyondSheet_clampsToLastRow`.
- **`"A1:Z1"`** with 10-column sheet — `lastColumn` clamps to 9.
- **`"J10:K15"`** with 5×5 sheet — both corners clamp to (4, 4); the
  result is a 1×1 range at the bottom-right corner. Pinned by
  `fromAddressString_bothCornersBeyondSheet_clampsBoth`.
- **`"A1:A10"`** vs `"A1:A11"` with 10-row sheet — row index 9
  (== `getRows() - 1`) passes through; row 10 (== `getRows()`)
  triggers the `>=` clamp guard and is clipped to 9. The boundary
  test pins the inclusive `>=` comparison (a regression to `>` would
  let row 10 through).
- **`"A1"`** (no colon) — `split[1]` throws
  `ArrayIndexOutOfBoundsException`. Pinned by
  `fromAddressString_noColon_throwsArrayIndexOutOfBounds` as the
  documented current contract; a future input-validation change would
  require updating this test.

### `fromCoordinates`

- **`(3, 2, 5, 3)`** — produces (1, 1, 4, 2) in 0-based POI space.
  Pinned by `fromCoordinates_orderedRange_returnsZeroBasedRange`.
- **`(5, 3, 3, 2)`** (reversed) — swaps corners; identical output to
  `(3, 2, 5, 3)`. Pinned by `fromCoordinates_reversedRange_isReorderedMinFirst`.
- **`(15, 1, 1, 1)`** with 10-row sheet — row clamps to 10
  (`getRows()`), then `-1` on emit = 9. Pinned by
  `fromCoordinates_rowBeyondSheet_clampsToGetRows_thenSubtractsOne`.
- **`(1, 1, 10, 1)`** vs `(1, 1, 9, 1)` with 10-row sheet — boundary
  case. The 1-based row 10 (== `getRows()`) triggers the clamp (10 >=
  10 is true) and clamps to 10, then emits 9. Row 9 (== `getRows() -
  1`) does NOT trigger the guard, goes through unchanged, emits 8.
  Pinned by `fromCoordinates_atExactSheetBoundary_doesNotClampLastRow`.

## Error modes

- **`null` addressString** — `split(":")` throws NPE. Not guarded.
- **Malformed cell reference** in either corner — POI's `CellReference`
  constructor throws (`IllegalArgumentException` or similar). Not
  caught.
- **`null` Spreadsheet** at construction — would NPE on the first
  `spreadsheet.getRows()` call.

## Non-obvious quirks

- **Different inner arithmetic for the same output.** The string
  overload clamps with `getRows() - 1` directly; the int overload
  clamps with `getRows()` and then subtracts one on emit. Both produce
  the same observable result for any well-formed input. A future
  refactor that unified the two paths would have to preserve the
  cross-overload equivalence — the paired tests
  `bothOverloads_orderedRangeInBounds_produceSameResult` and
  `bothOverloads_clampingAtSheetEdge_produceSameResult` make that
  invariant load-bearing.
- **The `>= getRows()` clamp guard is inclusive on the upper side.**
  A row index of `getRows() - 1` (the last valid row) is INSIDE the
  sheet; an index of `getRows()` triggers the clamp. A regression to
  `>` would let `getRows()` slip through and produce an out-of-bounds
  range. The boundary tests pin this directly.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/CellRangeNormalizer.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/CellRangeNormalizerTest.java` (17 tests; every contract paragraph here is pinned by at least one)
- Slice that introduced it: journal entry 2026-06-21, slice 89 (`CellRangeNormalizer + InvalidFormulaTracker`)
- Callers: `Spreadsheet.createCorrectCellRangeAddress(String)` and `Spreadsheet.createCorrectCellRangeAddress(int, int, int, int)` (both `protected` — thin delegators preserved for subclass extension).

## Contract status

Every contract paragraph maps to at least one passing test in
`CellRangeNormalizerTest.java`. The cross-overload equivalence
invariant is pinned by two paired tests so a future unification of
the two paths can be verified behavior-preserving.

## What the spec doesn't cover

- **Performance.** Both overloads are O(1). The string overload allocates
  two `CellReference` instances; the int overload allocates none.
- **Concurrency.** Not thread-safe in itself, but the operations are
  pure — concurrent calls do not race because no shared state is
  written. The `getRows()` / `getCols()` reads on the host are also
  read-only.
- **Future input validation.** The string overload's
  `ArrayIndexOutOfBoundsException` on missing colon is the current
  contract; a future improvement could throw a more helpful exception
  type, but that would be a behavior change requiring an explicit
  decision.

## Human review

**Status: not yet reviewed.** Per the pipeline's Phase 3 process, this
draft needs a domain expert to verify:

1. **Is the dual-overload split actually justified?** Or should the
   string overload eventually delegate to the int overload (parse the
   string then call `fromCoordinates`)? The cross-overload result
   equivalence is already pinned — a future consolidation is mechanical
   if desired.
2. **Should the string overload validate the input format?** Currently
   a missing colon or malformed cell reference produces a stack trace,
   not a helpful error. Adding validation is a behavior change
   requiring an explicit decision; it would also update the
   `noColon_throwsArrayIndexOutOfBounds` test.
3. **Is `(getRows() - 1, getCols() - 1)` the right clamp ceiling?** A
   3×3 sheet has rows {0, 1, 2}, so the bottom-right cell is (2, 2).
   `getRows() - 1 == 2`. Correct. But the symmetric int-overload
   formulation `getRows()` then `-1` adds a step that could conceivably
   drift in a future refactor — the cross-overload equivalence test is
   what prevents that.
