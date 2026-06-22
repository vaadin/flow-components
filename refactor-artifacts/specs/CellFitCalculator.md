# Block: CellFitCalculator

> Phase 3 spec. Drafted from the source (`CellFitCalculator.java`)
> and the characterization tests (`CellFitCalculatorTest.java`,
> slice 68). **Status:** initial draft, not yet reviewed by a
> human operator.

## Purpose

Decide whether a formatted cell value fits the cell's column
width given the cell's style-specific character-width ratio.
Used to drive scientific-notation fallback for numeric cells
whose formatted value would overflow.

Owns a single `cellStyleIndex → characterWidthRatio` map sent
from the client side as cells are measured. When the map has no
entry for a style index, the calculator assumes the value fits —
the client will measure on the next sync.

## Inputs

### `onWidthRatioUpdate(HashMap<Integer, Float> ratios)`

- Replaces the entire ratio map. Not a merge — matches the
  original `CellValueManager` field-reassignment behavior.

### `doesValueFit(Cell cell, String formattedValue, int cellWidth)`

- `cell` — POI cell (for its style index).
- `formattedValue` — the would-be-displayed string.
- `cellWidth` — pixel width (computed elsewhere; the calculator
  doesn't know about merged regions).
- Returns `true` if:
  - No ratio entry exists for the cell's style index, OR
  - `formattedValue.length() * ratio <= cellWidth - 4` (4px is
    left+right padding).

## Outputs

`onWidthRatioUpdate` returns `void`. `doesValueFit` returns
`boolean` — `true` = the value fits (or we don't know yet,
assume fit).

## Side effects

`onWidthRatioUpdate` replaces the entire map. No other side
effects.

## Invariants

- **Missing ratio → "fits"**. The calculator assumes fit when the
  client hasn't measured yet, so the cell renders normally on
  first paint. The client measures and pushes a ratio update;
  the next paint uses the new ratio. Pinned by slice 68.
- **Map REPLACE on update, not merge**. A regression that did
  `putAll(...)` would accumulate stale entries from cells whose
  style no longer exists.
- **The `- 4` padding is hardcoded**. Excel's cell-padding is
  visually about 4px; the value is empirical, not derived from
  POI. Pinned by test; a future precision pass might compute it
  per locale / font.
- **The calculator stays free of POI merged-region knowledge**.
  The caller (`CellValueManager.getCellWidth`) computes the
  effective width and passes it in; this class only does the
  fit math.

## Open questions

- The "no entry → fits" default is generous. For very long
  strings, the first paint will display the full value (and may
  visually overflow) before the client measures and triggers
  scientific notation. A conservative default ("no entry →
  doesn't fit, use sci notation immediately") would prevent
  the brief overflow but break cells with short strings that
  don't have a ratio yet. Current shape prefers occasional
  overflow over universal sci-notation.
