# Block: CellCommentRangeScanner

> Phase 3 spec. Drafted from the source
> (`CellCommentRangeScanner.java`) and the characterization tests
> (`CellCommentRangeScannerTest.java`, slice 59). **Status:**
> initial draft, not yet reviewed by a human operator.

## Purpose

Scan a single 2D cell range and harvest two things:

1. POI cell comments — `text + author` for client display, plus
   the subset of comments currently visible.
2. Cells marked as having invalid formulas — keys for the
   client-side error indicator.

Pure scanner — one range per call; the orchestration of viewport
panes lives in [`CellCommentLoader`](CellCommentLoader.md).

Skips:

- Cells inside merged regions (except the top-left anchor of the
  region).
- Cells in hidden rows / hidden columns.

## Inputs

### `scan(int r1, int c1, int r2, int c2)`

- 1-based row + column range. Inclusive.
- Reads the active sheet, the current cell-comment maps, the
  merged-region cache, and the invalid-formula tracker.

## Outputs

`void`. Mutates four state maps on the Spreadsheet (in place,
without going through setters — the loader handles the final
mirror).

## Side effects

For each cell in the range:

- Skip if inside a merged region (non-anchor).
- Skip if row / column is hidden.
- Resolve the POI cell. If null, skip.
- If the cell has a comment: extract `text` + `author`, store in
  the maps; if comment.isVisible() add to the visible set.
- If the cell is in `invalidFormulaTracker`'s marked set for the
  active sheet, add its key to the invalid-formula set.

## Invariants

- **Merged-region skip uses the cache, not POI directly**. POI's
  `getMergedRegion` is O(N) per cell; the cache is O(1). Slice
  59's test pins that scanner uses the cache. A regression that
  switched to direct POI lookup would silently make scanning
  O(N²) — invisible but slow.
- **Hidden-row/column skip is checked BEFORE comment extraction**.
  The hidden check is the cheaper of the two (a HashSet contains),
  so the order minimizes wasted POI cell reads.
- **Top-left anchor of a merged region IS scanned**. Only
  non-anchor merged cells are skipped. The anchor carries the
  comment for the whole region.

## Open questions

- The scanner reads + writes the maps directly. A future refactor
  could return a result object and let the loader merge it; that
  would make the scanner pure (testable without spreadsheet
  mocks). Current shape is a write-through optimization.
- Invalid-formula tracking is a separate concern that piggybacks
  on this scanner because the iteration shape is the same. If
  invalid-formula display ever needs a different visibility
  policy (e.g. always show, even when scrolled out), it should
  move to its own scanner.
