# Block: CustomComponentRangeScanner

> Phase 3 spec. Drafted from the source
> (`CustomComponentRangeScanner.java`) and the characterization
> tests (`CustomComponentRangeScannerTest.java`, slice 61).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Scan a single cell range and ask the configured
`SpreadsheetComponentFactory` for any custom components or
editors to display. Populates four pass-in collections, registers
newly-discovered components as virtual children of the
spreadsheet element.

One range per call; orchestration of viewport panes lives in
[`CustomComponentLoader`](CustomComponentLoader.md).

Skips cells inside merged regions (except the top-left anchor) —
same convention as
[`CellCommentRangeScanner`](CellCommentRangeScanner.md).

## Inputs

### `scan(factory, existingComponents, newCustomComponents, rowsWithComponents, cellKeysToEditorIdMap, componentIDtoCellKeysMap, r1, c1, r2, c2)`

- `factory` — the active `SpreadsheetComponentFactory`. Never
  null (loader filters).
- `existingComponents` — the set of components from the previous
  scan, used to detect "already-registered" cases.
- `newCustomComponents` — output: components seen this scan.
- `rowsWithComponents` — output: rows that contain at least one
  component (consumed by the row sizer).
- `cellKeysToEditorIdMap` / `componentIDtoCellKeysMap` — output:
  the two key maps.

## Outputs

`void`. All output is via the pass-in collections (write-through).

## Side effects

For each cell in the range:

- Skip if inside a merged region (non-anchor).
- Ask the factory for a component / editor.
- If new: register as a virtual child of the Spreadsheet element.
- If retained: reuse the existing `StateNode`.
- Update the four key maps + the rows-with-components set.

## Invariants

- **Pass-by-reference output is the API**. The scanner doesn't
  return anything; the caller's collections are mutated in
  place. A regression that copied the collections would silently
  break the loader's reconciliation pass.
- **Virtual-child registration is idempotent for existing
  components**. The Element API tolerates re-registering the
  same node, but the scanner takes the `existingComponents`
  argument explicitly to avoid the redundant call.
- **Merged-region skip** — same as
  [CellCommentRangeScanner](CellCommentRangeScanner.md).

## Open questions

- The 8-parameter signature is awkward. A future refactor could
  bundle the output collections into a `ScanResult` record, but
  records are blocked here by the pass-through mutation pattern
  (the loader needs to merge multiple scan calls into one
  result, easier with mutable collections than with immutable
  records).
