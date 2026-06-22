# Block: HyperlinkLoader

> Phase 3 spec. Drafted from the source (`HyperlinkLoader.java`)
> and the characterization tests (`HyperlinkLoaderTest.java`,
> slice 56). **Status:** initial draft, not yet reviewed by a
> human operator.

## Purpose

Orchestrate loading of hyperlink tooltips for the currently
visible cells. Pure dispatcher: reset tooltips map → walk each
viewport pane via [`HyperlinkRangeScanner`](HyperlinkRangeScanner.md).

Twin orchestrator of [`CellCommentLoader`](CellCommentLoader.md);
same pane-walk shape, different harvest.

## Inputs

### `load()`

- No arguments. Reads freeze-split + viewport bounds, walks 4
  panes (or subset, when no freeze split).

## Outputs

`void`. Side effects on `spreadsheet.hyperlinksTooltips`.

## Side effects

1. `spreadsheet.setHyperlinksTooltips(new HashMap<>())` — empty
   the previous map.
2. For each pane that exists (top-left only if both freeze axes,
   top-right if frozenRow > 0, bottom-left if frozenCol > 0,
   main always): `scanner.scan(r1, c1, r2, c2)`.

## Invariants

- **Map RESET, not merge**. A cell that USED to have a hyperlink
  but now doesn't must lose its tooltip on the next load. The
  reset ensures this.
- **Pane-walk order matches the visual quadrants**. Same as
  [`CellCommentLoader`](CellCommentLoader.md): top-left frozen
  → top-right frozen → bottom-left frozen → main.
- **No latch like CellCommentLoader's `topLeftLoaded`**.
  Hyperlinks can change per-load (a hyperlink can be added /
  removed at runtime); the comment loader can latch only
  because the top-left FROZEN pane never scrolls out, so its
  comments are static.

## Open questions

- The pane-walk is duplicated with the comment loader. See
  shared "open questions" in [CellCommentLoader spec](CellCommentLoader.md).
