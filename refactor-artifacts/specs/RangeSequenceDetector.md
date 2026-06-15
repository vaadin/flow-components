# Block: RangeSequenceDetector

> Sixth Phase 3 spec. Closes the slice-12 trio: `SequenceIncrement` (pure
> math), `SequenceValueExtractor` (POI read), `RangeSequenceDetector` (the
> dispatch that glues them). Drafted from the source (89 LOC, two static
> methods) and `RangeSequenceDetectorTest.java` (13 tests, every behavior in
> this spec pinned).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Decide whether a contiguous run of cells (a row slice or column slice) forms
an arithmetic progression and, if so, return its common difference. The
dispatcher: it reads the leading cell's type, picks the right
`SequenceValueExtractor` overload, and forwards the extracted values to
`SequenceIncrement.compute`.

Used by `DirectionalShiftOperator` to drive Excel autofill — each row (when
shifting columns) or each column (when shifting rows) is probed for a
progression so the autofill can decide between extending the sequence or
literal repetition.

## Inputs

### `getRowIncrement(Sheet activeSheet, int rIndex, int c1, int c2)`

- `activeSheet` — required, non-null. The sheet to read cells from.
- `rIndex` — 1-based row index. Resolved to POI's zero-based via
  `activeSheet.getRow(rIndex - 1)`.
- `c1`, `c2` — 1-based column range. `c1` is the leading column; `c2` is
  the trailing column. Both are inclusive. Passed through to the extractor
  unchanged.

### `getColumnIncrement(Sheet activeSheet, int cIndex, int r1, int r2)`

- `activeSheet` — required, non-null.
- `cIndex` — 1-based column index.
- `r1`, `r2` — 1-based row range. `r1` is the leading row, `r2` is the
  trailing row. Both inclusive.

## Outputs

Both methods: `Double` — the common difference of the values in the range
(via `SequenceIncrement.compute`), or `null` if the range doesn't form a
progression OR the leading cell's type isn't STRING / NUMERIC.

### Null cases (both methods, in evaluation order)

1. Row at `rIndex - 1` (or `r1 - 1` for column dispatch) doesn't exist →
   `null`.
2. Leading cell at the resolved row/column position doesn't exist → `null`.
3. Leading cell's type isn't STRING or NUMERIC (e.g. FORMULA, BOOLEAN,
   BLANK, ERROR) → `null`.
4. Leading cell is STRING / NUMERIC but the extracted values don't form
   a progression → `null` (delegated to `SequenceIncrement.compute`).

### Non-null cases

- Leading cell is STRING → returns `SequenceIncrement.compute(extractString(...))`.
- Leading cell is NUMERIC → returns `SequenceIncrement.compute(extractNumeric(...))`.

## Side effects

None. Pure read on the sheet; no cell mutation, no POI state change. Static
methods on a final class with a private constructor — no instance state.

## Invariants

- The detector is a thin dispatcher: every observable output is exactly what
  `SequenceIncrement.compute` would return for the extracted values, plus
  the four null shortcuts above. No additional transformation.
- The 1-based indexing convention from `CellSelectionShifter` is preserved
  end-to-end: callers pass 1-based row/column indices, and the detector
  converts to zero-based exactly once per dispatch (`activeSheet.getRow(N - 1)`
  / `row.getCell(N - 1)`).
- The leading cell's type fully determines which extractor runs. There is
  no type-mixing: if `firstCell` is STRING, only `extractString` runs; if
  the trailing cells happen to be NUMERIC, the extractor handles the
  mismatch internally (typically by terminating the run early).
- Negative increments propagate. `SequenceIncrement` returns a signed
  `Double`; the detector returns it verbatim.

## Edge cases

- **Missing row** (e.g. `sheet.getRow(0) == null`) → `null` immediately;
  the leading-cell check is short-circuited.
- **Empty row with no cells** → `null` (leading cell is null).
- **Leading cell is a FORMULA whose cached value is numeric** → `null`.
  The detector checks `getCellType() == CellType.STRING / NUMERIC`, not
  the cached-result type. Formula cells never participate in autofill
  detection — Excel matches this.
- **Single-cell string range** (e.g. `c1 == c2`, leading cell `"A1"`) →
  returns `1.0` via `SequenceIncrement`'s single-string-value convention.
- **Single-cell numeric range** → returns `null` (numeric overload requires
  ≥2 values).
- **Decreasing numeric sequence** → returns the negative common difference
  (e.g. `{30, 20, 10}` → `-10.0`).
- **String range with non-uniform prefixes** (e.g. `{"A1", "B2", "C3"}`) →
  returns `null` via `SequenceIncrement`'s prefix check.
- **Range with mid-stream type change** (e.g. `c1` is NUMERIC, middle cell
  is STRING) → the extractor handles this internally; typically yields a
  shorter values array, then `SequenceIncrement.compute` decides on the
  truncated input. Behavior is "use whatever clean prefix exists" with the
  termination semantics defined by `SequenceValueExtractor`.

## Error modes

- **`activeSheet == null`** → `NullPointerException` on `.getRow(...)`. Not
  swallowed.
- **`c1 > c2`** (row dispatch) → the extractor returns an empty/degenerate
  array, `SequenceIncrement` returns `null`. No bounds check at the
  dispatcher level.
- **`r1 > r2`** (column dispatch) → same as above.
- **`rIndex == 0`** → `activeSheet.getRow(-1)` returns `null` (POI is
  defensive), detector returns `null`. No explicit guard.

## Non-obvious quirks

- **Leading-cell type is the only type check.** Trailing cells of a
  different type than the leading cell don't cause a dispatch decision
  change; they just produce a shorter extracted array. The detector
  doesn't "see" them.
- **The 1-based convention is enforced here, not in the extractor.** The
  extractor takes its arguments 1-based too (the call chain preserves the
  convention), but the row/cell lookup arithmetic (`-1` decrement) is
  visible at the dispatcher level for the `getRow` / `getCell` calls in
  the dispatch itself. Anyone reading just the extractor wouldn't see
  this — it's a contract layered on top.
- **FORMULA bypass is intentional.** The detector's two type checks
  (`CellType.STRING`, `CellType.NUMERIC`) deliberately don't include
  FORMULA. A formula cell with a cached numeric value isn't treated as a
  numeric for autofill purposes, even though its `getNumericCellValue`
  would return a number. The autofill semantics match Excel's: dragging a
  formula cell drags the formula text (handled elsewhere), not its cached
  result, so trying to infer an arithmetic progression from formula cells
  is meaningless.
- **The column dispatch resolves the leading cell via `activeSheet.getRow(r1 - 1)`
  not `activeSheet.getRow(rIndex - 1)`.** Subtle but important: for a column
  walk, the leading cell lives in the *first row* of the range, in the
  designated column. The extractor then walks down the column.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/RangeSequenceDetector.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/RangeSequenceDetectorTest.java` (13 tests pin every invariant in this spec)
- Slice that introduced it: slice 12c (`DirectionalShiftOperator` + `RangeSequenceDetector`)
- Companion blocks: `SequenceIncrement` (the math leg), `SequenceValueExtractor`
  (the read leg), `DirectionalShiftOperator` (the dispatch's only caller),
  `CellValueShifter` (downstream — applies the detected increment per cell)

## Contract status

Every invariant in this spec maps to at least one passing test in
`RangeSequenceDetectorTest.java`. The spec is the contract; the tests are
the enforcement. The slice-12 trio (`SequenceIncrement`,
`SequenceValueExtractor`, `RangeSequenceDetector`) is now fully specced.

## What the spec doesn't cover

- Performance characteristics. The detector is O(c2-c1) or O(r2-r1) plus
  the constant cost of the type check; trivially fast at any practical
  range size. Not relevant for the autofill use case.
- Thread safety. The detector has no state; safe to call concurrently. The
  underlying `Sheet` is not thread-safe, but that's a POI contract, not a
  detector contract.
- Interactions with merged regions. The detector reads cells via
  `Row.getCell(col)` and `Sheet.getRow(row).getCell(col)`; merged regions
  affect what POI returns from those calls (typically only the top-left
  cell of a merge carries a value). Whether autofill should treat merged
  regions specially is a higher-level concern handled by
  `DirectionalShiftOperator`'s callers.

## Human review

**Status: not yet reviewed.** Per pipeline §13.2, this draft needs a domain
expert to verify:

1. Is the FORMULA-bypass truly intentional, or an oversight? A formula
   cell with a cached numeric result that the user *expects* to extend
   would silently fall through to no-autofill today. The
   `getRowIncrement_leadingCellIsFormula_returnsNull` test pins the
   current behavior, but the *intent* should be confirmed against Excel
   reference behavior.
2. Are BOOLEAN cells (and the rarer ERROR / BLANK types) deliberately
   in the same bucket as FORMULA? They share the dispatcher's "neither
   STRING nor NUMERIC" fallthrough.
3. Should `c1 > c2` / `r1 > r2` be explicit precondition violations or
   tolerated as silent no-progressions? Current behavior is silent — the
   extractor produces an empty array, `SequenceIncrement` returns null.
