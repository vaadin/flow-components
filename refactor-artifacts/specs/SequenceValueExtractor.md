# Block: SequenceValueExtractor

> Tenth Phase 3 spec. Closes the slice-12 trio's documentation:
> `SequenceIncrement` (slice 25, pure math), `RangeSequenceDetector`
> (slice 41, dispatcher), `SequenceValueExtractor` (this, the POI read
> leg). All 14 existing tests in `SequenceValueExtractorTest` map to
> the invariants here.
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Read a contiguous run of cell values out of a POI row or column, stopping
at the first cell that doesn't match the requested type (numeric or
string), is `null`, or sits on a missing row.

Four static methods, two for rows and two for columns, paired by type
(numeric / string). Pure POI reads — no `Spreadsheet` dependency, no
mutations. Fed by `RangeSequenceDetector` after it decides which type
overload to call based on the leading cell's type; the extracted values
then drive `SequenceIncrement`'s arithmetic-progression detection.

## Inputs

All four methods take 1-based indices. They convert to POI's 0-based
internally on every cell-read (`getCell(i - 1)`).

### `extractRowNumeric(Row row, int c1, int c2)` / `extractRowString(...)`

- `row` — required, non-null. The POI row to read from.
- `c1`, `c2` — 1-based column range, inclusive.

### `extractColumnNumeric(Sheet activeSheet, int columnIndex, int r1, int r2)` / `extractColumnString(...)`

- `activeSheet` — required, non-null. The POI sheet to read from.
- `columnIndex` — 1-based column index.
- `r1`, `r2` — 1-based row range, inclusive.

## Outputs

All four methods return an array of length `c2 - c1 + 1` (for row
variants) or `r2 - r1 + 1` (for column variants):

- Numeric variants return `Double[]`.
- String variants return `String[]`.

Indexing in the returned array is **left-justified** — `result[0]`
corresponds to the leading position (`c1` or `r1`), `result[length-1]` to
the trailing.

Entries after the first non-matching cell are left as the default array
value: `null` for both numeric and string overloads. The caller must
treat the trailing `null`s as "no value", which is what
`SequenceIncrement.compute` does (it aborts on first null).

## Side effects

None. Pure POI reads.

## Invariants

- **Returned array length is exactly `range + 1`** — even when iteration
  stops early. The trailing positions are populated with `null`. This is
  load-bearing for the downstream `SequenceIncrement.compute`, which
  scans the array linearly and exits on the first null.
- **Stop conditions are first-mismatch.** Numeric variants stop at:
  null cell OR non-NUMERIC cell type. String variants stop at: null
  cell OR non-STRING cell type. Column variants additionally stop at:
  null row (the entire row doesn't exist in POI).
- **Cell-type check is `getCellType()`, not `getCachedFormulaResultType()`.**
  A FORMULA cell with a cached numeric result will stop the numeric
  variant (because its `getCellType()` returns `FORMULA`, not `NUMERIC`).
  Matches the FORMULA-bypass quirk pinned in `RangeSequenceDetector`'s
  spec — the entire slice-12 stack treats formula cells as non-sequence
  cells, regardless of cached value.

## Edge cases

- **`c1 == c2`** (or `r1 == r2`) → length-1 array. Returns the leading
  value, or `[null]` if the leading cell doesn't match.
- **`c1 > c2`** (or `r1 > r2`) → length-0 or negative-length array.
  `new Double[c2 - c1 + 1]` with `c2 < c1 - 1` would throw
  `NegativeArraySizeException`. **Worth flagging** — the extractor
  doesn't guard. Callers (currently only `RangeSequenceDetector`) are
  expected to pass valid ranges.
- **Empty row in the column variants** (`row.getRowNum()` exists but
  `getCell` returns null for `columnIndex`) → first iteration fills
  `result[0]` with null and breaks. Effective: length-N array with one
  meaningful position (the first) and N-1 trailing nulls.
- **Mixed-type sequence** (e.g. row with numeric, numeric, string) →
  numeric variant returns `[n1, n2, null]`. The string at position 2
  stops the iteration; positions 2+ are null.
- **All cells null** → returns array full of nulls.
- **Single populated cell at the leading position, rest empty** → returns
  the value followed by nulls.

## Error modes

- **`row == null`** in row variants → NPE on `row.getCell(...)`. Not
  swallowed. Caller responsibility (`RangeSequenceDetector` guards via
  its own null-row check before dispatching, so the extractor doesn't
  see null rows on the call path).
- **`activeSheet == null`** in column variants → NPE on
  `activeSheet.getRow(...)`. Same caller-responsibility logic.
- **Negative array size** (when `c2 < c1 - 1` etc.) →
  `NegativeArraySizeException`. Not swallowed.
- **POI throws** (e.g. `getNumericCellValue` on a malformed cell) →
  propagates up. The extractor has no try/catch.

## Non-obvious quirks

- **The break-on-mismatch is silent.** A caller looking at the returned
  array can't distinguish "stopped at position 3 because cell 3 was
  null" from "stopped at position 3 because cell 3 was a string"
  (when the caller wanted numeric). The trailing nulls look the same.
  The downstream `SequenceIncrement.compute` only cares about whether
  the run is uniform, not why it terminated.
- **Cell-type uniformity is per-overload, not negotiated.** The
  numeric extractor demands NUMERIC; the string extractor demands
  STRING. A mixed sequence (e.g. `"A"`, `"B"`, `1.0`) is not handled
  by some hybrid extractor — the caller picks one type based on the
  leading cell, gets the prefix run, and gives up if the run isn't
  long enough.
- **The +1 in `c2 - c1 + 1` matters.** A range like `c1=2, c2=2` is a
  single cell, so the array length is 1, not 0. Off-by-one bugs in
  this arithmetic would silently truncate the array; the existing
  tests pin the correct length.
- **The `result[i - c1]` indexing is left-justified.** A range of
  `c1=5, c2=7` reads three cells; the result array is indexed 0, 1, 2.
  The cell at column 5 lands at `result[0]`, not `result[5]`.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/SequenceValueExtractor.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/SequenceValueExtractorTest.java` (14 tests; every invariant in this spec maps to at least one assertion)
- Slice that introduced it: slice 12a (`SequenceIncrement +
  SequenceValueExtractor`)
- Companion blocks: `SequenceIncrement` (downstream math), `RangeSequenceDetector` (upstream dispatcher), `CellValueShifter#shiftString` (parallel string-prefix-detection logic that uses the same `TRAILING_DIGITS_PATTERN`)

## Contract status

Every invariant in this spec maps to at least one passing test in
`SequenceValueExtractorTest.java`. The slice-12 trio
(`SequenceIncrement`, `SequenceValueExtractor`, `RangeSequenceDetector`)
is now **fully tested AND fully specced** — Phase 2 + Phase 3 both green
for all three members.

## What the spec doesn't cover

- Performance characteristics. O(c2-c1) or O(r2-r1) per call; called
  once per autofill drag. Not relevant.
- Thread safety. Stateless; safe to call concurrently. The underlying
  `Sheet` / `Row` is not thread-safe, but that's POI's contract.
- The choice of `Double[]` (boxed) over `double[]` (primitive). Boxed
  is needed for the null-sentinel semantic — a primitive `double[]`
  would default to 0.0, which is a valid numeric value. The boxing
  cost is negligible at the call rate.

## Human review

**Status: not yet reviewed.** Per pipeline §13.2, this draft needs a domain
expert to verify:

1. **Should `c1 > c2` be a precondition or tolerated?** The current code
   silently throws `NegativeArraySizeException` if the range is invalid.
   A guard would be friendlier but the caller (only
   `RangeSequenceDetector` today) already passes valid ranges.
2. **Should the four methods be unified into a single
   templated `<T>` method?** The four methods are near-identical
   structurally; only the type predicate (`CellType.NUMERIC` vs
   `CellType.STRING`) and the value-extractor lambda
   (`getNumericCellValue` vs `getStringCellValue`) differ. A
   generic method with a `Class<T>` token and a `Function<Cell, T>`
   would eliminate the duplication, at the cost of an indirection. Pin
   the duplication-vs-indirection trade-off as a deliberate choice.
3. **Should column variants take a `Row` slice precomputed by the
   caller, rather than re-reading `getRow(...)` per iteration?** Same
   caller (`RangeSequenceDetector`) already has access to the sheet,
   but precomputing rows would let the extractor be uniform between
   row and column variants. Today the asymmetry is observable.
