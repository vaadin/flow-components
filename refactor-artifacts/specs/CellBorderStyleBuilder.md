# Block: CellBorderStyleBuilder

> Phase 3 spec. Drafted from the source (`CellBorderStyleBuilder.java`)
> and the characterization tests (`CellBorderStyleBuilderTest.java`,
> slice 81). **Status:** initial draft, not yet reviewed by a human
> operator.

## Purpose

Compose the per-cell-style CSS for borders, including the
**transferred-border** caches that route top / left borders to the
bottom / right of the cell ABOVE / TO THE LEFT (Excel's drawing
convention is to render those borders on the adjacent cell, not the
source cell — a CSS quirk needed because the browser draws each
cell's own borders independently and double-renders would produce
visible doubling at the shared edge).

Owns five caches:

- `shiftedBorderTopStyles` / `shiftedBorderLeftStyles` — cell-style
  index → ready-to-emit CSS rule for the cells that should pick up
  the transferred bottom / right border from the cells above /
  to-the-left of them.
- `shiftedBorderTopStylesMap` / `shiftedBorderLeftStylesMap` — temp
  accumulators used during a styling pass to collect the
  row / column index lists that will eventually become the CSS
  selectors. Flattened to selectors by `flattenAccumulators()`.
- `mergedCellBorders` — CSS selector (the merged region's top-left
  cell) → border CSS rules that need to apply to it (the right /
  bottom borders of cells inside the merged region that would
  otherwise be invisible inside the region).

## Inputs

### `appendBorderStyles(StringBuilder sb, CellStyle cellStyle)`

- `sb` — caller-owned StringBuilder for the cell-style's CSS. The
  method appends 4 border-side properties to it.
- `cellStyle` — the POI CellStyle (XSSF or HSSF) being processed.
- For each of the 4 sides (top/right/bottom/left): reads
  `cellStyle.getBorderXxx()`, maps it to `BorderStyle.NONE` /
  `SOLID_THIN` / etc. via `SpreadsheetStyleFactory.BORDER`. For
  XSSF cells where the parent style's `ApplyBorder == false`
  (Excel theme indicator), reads the theme-resolved border via
  `CTBorder` reflection — same pattern in 3 of the 4 side helpers
  (top is the one with the inverted-form rewrite from slice 100).

### `processCustomStyling(Cell cell)`

- `cell` — the POI cell being evaluated. Used to identify the
  cell's row + column for the transferred-border accumulator maps.
- The cell's style is checked for "should transfer top border" and
  "should transfer left border" via the same XSSF theme-resolved
  border path. If yes, the cell's row / column index is recorded in
  the accumulator for the OWNING cell-style's index (NOT the
  cell's index — multiple cells share styles).

### `flattenAccumulators()`

- No arguments. Walks the two accumulator maps and produces the
  final `shiftedBorderTopStyles` / `shiftedBorderLeftStyles` map
  entries by composing CSS selectors from the accumulated
  row / column index lists.

### `removeCellFromShiftedStyles(...)` / `removeCellFromInternalShiftedMaps(...)` / `stripLeadingSelectorsFromShiftedMaps()`

- The shift-row family of mutation methods. Called after row /
  column shifts to drop selectors that no longer match.

### `buildMergedCellBorderCss(String selector, String rules)`

- Adds (or replaces) an entry in the `mergedCellBorders` map. The
  selector is the merged region's top-left cell, the rules string
  is the accumulated right + bottom CSS for the cells inside the
  region.

### `appendShiftedStylesToOutput(...)` / `resetAll()` / `resetAccumulators()`

- Output assembly + reset helpers.

## Outputs

- `appendBorderStyles` appends CSS to the StringBuilder passed in.
  Side-effect-on-arg, no return value.
- `processCustomStyling` writes into the accumulator maps. No
  return value.
- `flattenAccumulators` writes into the shifted-styles maps. No
  return value.
- Getter methods return the live maps (NOT defensive copies — used
  by `SpreadsheetStyleFactory` for the final CSS emission).

## Side effects

- Mutation of all five internal maps.
- Reflection-style access to POI's CTBorder XML in the
  XSSF-theme-resolved path. This is the same reflection seam as
  `POIReflectionTools.isBorderSet` (the slice 103 audit confirmed
  it can't be replaced with public POI API yet).

## Invariants

- **The transferred-border indirection is load-bearing for
  rendering.** Without it, the browser double-draws shared edges
  (1px instead of 1px, but smearing on subpixel boundaries) — the
  CSS pass that resolves "this cell's top border belongs to the
  cell above" exists precisely to avoid that.
- **Cell-style index is the cache key, NOT cell position.** Two
  cells with the same `cellStyle.getIndex()` share the same CSS
  rule; the transferred-border maps store `cellStyleIndex →
  selector list` for that reason.
- **XSSF theme borders use `ApplyBorder == false` as the
  "use theme, not workbook border" signal.** The
  `appendBorderStyles` per-side helpers (and `XSSFColorConverter`)
  all share this branch — slice 100 collapsed three of them into
  the inverted-pattern form `if (cellStyle instanceof XSSFCellStyle
  xssfCellStyle && !xssfCellStyle.getCoreXf().getApplyBorder())`.
- **The accumulator maps are temp state — they're reset at the
  start of every full-workbook style pass via `resetAccumulators`,
  populated by `processCustomStyling`, and consumed exactly once
  by `flattenAccumulators`.** Any caller that mutates the
  shifted-styles maps without first calling
  `flattenAccumulators` is observing stale state.
- **Merged-cell borders use the region's top-left as the
  selector.** Inside-the-region borders would otherwise be
  invisible (`border-collapse: collapse` on the cell grid hides
  inner borders unless they're declared on the outermost cell).

## Open questions

- The five-map design predates the slice-1 extraction; the
  characterization tests (14, slice 81) pin observable CSS output
  but don't fully exercise the
  `removeCellFromShiftedStyles` /
  `stripLeadingSelectorsFromShiftedMaps` mutation interplay during
  row-shift scenarios. A future test would extend coverage.
- `mergedCellBorders` is a `HashMap<String, String>` — the value
  is concatenated CSS rules. There's no per-rule structure for
  invalidation; the entire entry is replaced on
  `buildMergedCellBorderCss`. A more structured representation
  would let partial invalidation reuse the unaffected rules.
