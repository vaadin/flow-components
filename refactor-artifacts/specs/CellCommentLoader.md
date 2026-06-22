# Block: CellCommentLoader

> Phase 3 spec. Drafted from the source (`CellCommentLoader.java`)
> and the characterization tests (`CellCommentLoaderTest.java`, 9
> tests, slice 58). **Status:** initial draft, not yet reviewed by
> a human operator.

## Purpose

Orchestrate loading of cell comments + invalid-formula markers
for the currently visible cells. Pure dispatcher: reset state
maps → walk each viewport pane via
[`CellCommentRangeScanner`](CellCommentRangeScanner.md) → mirror
to the client through `Spreadsheet`'s `setCellComments` /
`setCellCommentAuthors` setters.

Owns a single bit of state — `topLeftLoaded`, the
"frozen-top-left-corner is loaded once per lifetime" latch. The
frozen-pane corner never scrolls out, so re-scanning it is
wasted work.

Sibling helpers: [`CellCommentRangeScanner`](CellCommentRangeScanner.md),
[`HyperlinkLoader`](HyperlinkLoader.md) (parallel orchestrator
for hyperlinks).

## Inputs

### `load()`

- No arguments. Early-returns if `getFirstColumn() == -1`
  (spreadsheet not yet loaded; method will be called again).
- Resets the four state maps to empty maps (preserves the
  property-write mirror semantics).
- Walks 4 viewport panes:
  - Top-left frozen quadrant: `scanner.scan(1, 1, frozenRow,
    frozenCol)` ONLY if `!topLeftLoaded`, then flips the latch.
  - Top-right frozen row (if frozenRow > 0): scan above the
    split.
  - Bottom-left frozen col (if frozenCol > 0): scan left of the
    split.
  - Main viewport: `scanner.scan(firstRow, firstCol, lastRow,
    lastCol)`.

### `resetTopLeftLoaded()`

- No arguments. Flips the latch to false. Called by the
  Spreadsheet on sheet change.

## Outputs

`void`. Side effects on the four state maps + the latch.

## Side effects

- `spreadsheet.setCellComments(new HashMap<>())` — clears + mirrors.
- `spreadsheet.setCellCommentAuthors(new HashMap<>())` — same.
- `spreadsheet.setInvalidFormulaCells(new HashSet<>())` — same.
- Possibly: `spreadsheet.setVisibleCellComments(new ArrayList<>())` —
  re-init the visible-set.
- Per-pane scans via the range scanner (which mutates the four
  maps in place — see [scanner spec](CellCommentRangeScanner.md)).

## Invariants

- **`firstColumn == -1` short-circuit before any work**. The
  spreadsheet's initial state has uninitialized viewport bounds;
  trying to load comments before bounds are set would scan a
  nonsense range. Documented in the source comment.
- **`topLeftLoaded` is "load-once per active-sheet lifetime"**.
  The latch is reset on sheet change (via `resetTopLeftLoaded`).
  Between sheet changes, the top-left corner is scanned exactly
  once.
- **The four state maps are RESET, not merged**. A fresh load
  produces a fresh map, not an additive update. Pinned by the
  slice 58 test that asserts `cellComments` after a comment is
  removed from POI no longer contains the stale key.

## Open questions

- The pane-walk pattern is duplicated across
  [`HyperlinkLoader`](HyperlinkLoader.md), this class, and
  `CustomComponentLoader`. A future refactor could extract a
  `ViewportPaneWalker` helper that takes a `BiConsumer` for the
  per-pane work; deferred until a fourth loader appears.
