# Block: HyperlinkRangeScanner

> Phase 3 spec. Drafted from the source (`HyperlinkRangeScanner.java`)
> and the characterization tests (`HyperlinkRangeScannerTest.java`,
> slice 57). **Status:** initial draft, not yet reviewed by a human
> operator.

## Purpose

Scan a single cell range and harvest tooltips for hyperlink cells.
Two kinds of hyperlink are checked:

1. **Cells with a direct `Hyperlink` attached** — tooltip taken
   from the link's tooltip text; falls back to the link's address.
2. **Cells with a `HYPERLINK(...)` formula** — tooltip resolved
   through the active `HyperlinkCellClickHandler`.

XSSF and HSSF hyperlinks both supported. The XSSF path catches
`XmlValueDisconnectedException` (an XMLBeans quirk that can fire
when reading a hyperlink whose underlying XML node was detached
mid-iteration).

Sibling helper: [`HyperlinkLoader`](HyperlinkLoader.md).

## Inputs

### `scan(int r1, int c1, int r2, int c2)`

- 1-based row + column range. Inclusive.
- Reads the active sheet, the current click handler, and the
  hyperlinks-tooltips map.

## Outputs

`void`. Mutates `spreadsheet.hyperlinksTooltips` in place.

## Side effects

- Per cell with a direct hyperlink: add tooltip to the map.
  Hyperlink-tooltip-text wins; fall back to hyperlink-address.
- Per cell with a `HYPERLINK(...)` formula AND a registered
  click handler: resolve via the handler; add to the map.
- `XmlValueDisconnectedException` is caught and logged; the
  affected cell is silently skipped (best-effort scan).

## Invariants

- **Direct hyperlink wins over formula hyperlink** — if a cell
  has both, only the direct one's tooltip is recorded. Slice 57
  tests don't pin this explicitly (the case may not exist in
  practice), but the code's order makes it the observable
  behavior.
- **The XmlValueDisconnectedException catch is at the per-cell
  granularity**. One bad cell doesn't abort the scan. The catch
  log is at info level (not error) because the exception is
  benign — the cell just doesn't get a tooltip this round.
- **Click-handler-formula path is OPT-IN**. If
  `spreadsheet.getHyperlinkCellClickHandler() == null`, the
  formula path is skipped. Direct hyperlinks still work.

## Open questions

- `XmlValueDisconnectedException` catch — is it still needed on
  POI 5+? Could be a stale guard. A future audit could try
  removing it and seeing if any tests fail.
- The formula-resolution path calls into the handler, which can
  do arbitrary work (DB lookup, etc.). If the scan range is
  large, the handler is called many times. A future optimization
  could batch-resolve formulas; currently it's per-cell.
