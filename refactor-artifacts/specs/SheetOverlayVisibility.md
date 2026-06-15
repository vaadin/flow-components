# Block: SheetOverlayVisibility

> Twelfth Phase 3 spec. Pairs with slice 53's tests
> (`SheetOverlayVisibilityTest`, 10 tests). Second member of the slice-4
> overlay family with both Phase 2 + Phase 3 (after `SheetOverlayStorage`).
> **Status:** initial draft, not yet reviewed.

## Purpose

Decide whether a single `SheetOverlayWrapper` is currently visible in
any of the four viewport regions formed by the active sheet's freeze
pane. Owned by `SheetOverlayManager`; the manager queries this on every
overlay during `loadOrUpdate` to choose between attaching, detaching, or
leaving alone.

The visibility decision is layered:

1. **DONT_MOVE_AND_RESIZE workaround** — XSSF anchors of this type
   always return visible, since x/y coordinates can't be matched against
   the viewport.
2. **Hidden-range gate** — non-type-2 anchors whose columns are ALL
   hidden, or whose rows are ALL hidden, are invisible.
3. **4-quadrant viewport check** — visible if the overlay would render
   in any of the four viewport regions formed by the freeze pane.

## Inputs

### Constructor

- `spreadsheet` — required, non-null. The visibility checker reaches
  into `getLastFrozenRow` / `getLastFrozenColumn` / `getFirstRow` /
  `getLastRow` / `getFirstColumn` / `getLastColumn` / `isRowHidden` /
  `isColumnHidden`.

### `isVisible(SheetOverlayWrapper overlay)`

- `overlay` — required, non-null. The visibility checker reads
  `overlay.getAnchor()` and `overlay.isVisible(r1, c1, r2, c2)` (the
  per-quadrant probe).

## Outputs

- `boolean` — `true` if visible (in any quadrant), `false` otherwise.

## Side effects

None. Pure read on the spreadsheet's viewport state and the overlay's
anchor.

## Invariants

- **`DONT_MOVE_AND_RESIZE` on `XSSFClientAnchor` is always visible.** Pin
  via [issue #3261](https://github.com/vaadin/flow-components/issues/3261).
  No way to match absolute x/y coords against the viewport, so the
  checker errs on showing the overlay.
- **The XSSF type check is concrete-type, not anchor-type.** A non-XSSF
  anchor (e.g. HSSF) with `DONT_MOVE_AND_RESIZE` type takes the normal
  path — the workaround is XSSF-specific.
- **Type-2 anchors skip the hidden-range gate.** An anchor with
  `col2 == 0 && row2 == 0` doesn't size with cells, so the hidden-range
  semantics are undefined and the check is bypassed.
- **All-hidden columns/rows means invisible.** The check is "every
  column in `[col1, col2]` is hidden" — partial hiding doesn't trigger.
  Symmetric for rows.
- **4-quadrant OR.** The overlay is visible if it would render in ANY
  of the four quadrants (top-left frozen corner, top-right frozen rows,
  bottom-left frozen columns, bottom-right main viewport). Short-circuit
  evaluation — if quadrant 1 returns true, quadrants 2-4 aren't queried.
- **Quadrants 1-3 only fire when their split is > 0.** Quadrant 4 (main
  viewport) is unconditional.

## Edge cases

- **No freeze pane** (`horizontalSplit == 0 && verticalSplit == 0`) →
  only quadrant 4 is queried. Single OR clause; equivalent to
  "overlay visible in the main viewport".
- **Only horizontal split** → quadrant 1 skipped (needs BOTH),
  quadrant 2 skipped (needs vertical), quadrant 3 fires, quadrant 4
  always.
- **Only vertical split** → quadrant 1 skipped, quadrant 2 fires,
  quadrant 3 skipped, quadrant 4 always.
- **Type-2 anchor with all-hidden columns** → still passes the gate
  (gate is skipped for type-2) and proceeds to the 4-quadrant check.
- **Anchor spanning multiple hidden columns + visible columns** →
  treated as visible (hidden-range gate requires ALL).
- **Anchor at `col2 == 0 && row2 > 0`** — partial type-2? The check
  is `col2 == 0 && row2 == 0` (AND), so this would NOT be treated as
  type-2 and the hidden-range gate would apply normally.

## Error modes

- **`overlay == null`** → NPE on `overlay.getAnchor()`. Not swallowed.
- **`overlay.getAnchor()` returns null** → NPE on `anchor.getAnchorType()`.
- **`spreadsheet.*` methods return null** → not directly tested, but
  the int-returning getters wouldn't return null. `getLastFrozen*`
  returning negative values would silently skip quadrants 1-3 (the
  `> 0` guard).

## Non-obvious quirks

- **The hidden-range loops short-circuit on first non-hidden.** A range
  is "all hidden" only if every column/row in `[col1, col2]` is hidden.
  As soon as the loop finds a non-hidden column, it returns false and
  the gate doesn't trigger. So the gate only catches overlays that are
  entirely hidden.
- **POI's `ClientAnchor.getCol1/2` returns `short`, `getRow1/2` returns
  `int`.** This is a POI API quirk — the column variants are short, the
  row variants are int. The visibility checker assigns to local `int`
  variables, getting implicit widening. Worth knowing when writing
  tests (the Mockito stub for `getCol1` needs a `(short)` cast).
- **The 4 quadrants use slightly different anchor formulations:**
  - Q1 (frozen × frozen): `(1, 1, verticalSplit, horizontalSplit)`
  - Q2 (frozen rows × all cols): `(1, firstColumn, verticalSplit, lastColumn)`
  - Q3 (all rows × frozen cols): `(firstRow, 1, lastRow, horizontalSplit)`
  - Q4 (main): `(firstRow, firstColumn, lastRow, lastColumn)`
  Notice Q1 starts at (1,1) — the frozen corner is always at the
  top-left. Q2 / Q3 / Q4 use the firstRow / firstColumn from the
  spreadsheet's scrolled position.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/SheetOverlayVisibility.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/SheetOverlayVisibilityTest.java` (10 tests; every invariant maps to at least one assertion)
- Slice that introduced it: slice 4
- Companion blocks: `SheetOverlayStorage`, `SheetOverlayInfoBuilder`,
  `SheetOverlayManager` (the orchestrator), `SheetOverlayWrapper`
  (the value type — its `isVisible(r1, c1, r2, c2)` is what this
  block delegates to per-quadrant)

## Contract status

Every invariant maps to at least one passing test in slice 53. The
4-quadrant logic is exercised by the both-splits and single-split tests;
the hidden-range gate is exercised by the all-columns-hidden /
partial-column-hidden / all-rows-hidden trio; the DONT_MOVE_AND_RESIZE
workaround is exercised by two tests (XSSF vs non-XSSF).

## What the spec doesn't cover

- Performance characteristics. O(quadrant count × overlay isVisible
  cost). The per-quadrant isVisible on the overlay is itself O(1) for
  rectangle-overlap math.
- Thread safety. Stateless (no mutable fields); safe to call
  concurrently. The underlying `Spreadsheet` state is request-thread-
  serialized per Vaadin Flow.

## Human review

**Status: not yet reviewed.**

1. **Is the type-2 hidden-range bypass intentional?** A type-2 anchor
   pinned to a hidden column still doesn't size with cells, but it's
   rendered at the column's position. If the column is hidden, should
   the overlay also be hidden? Today it's not, because the bypass
   skips the hidden-range gate.
2. **Should `getLastFrozen*` returning negative values be a precondition
   violation?** Today negative values silently skip quadrants 1-3 due
   to the `> 0` check; equivalent to "no freeze pane". A negative value
   is a programmer error and could be a precondition assertion.
3. **Is the all-hidden definition (every column hidden) the right one?**
   A wide overlay spanning 10 columns where 9 are hidden but 1 is
   visible currently shows. A future product decision might want
   "most of the columns hidden" → hidden, but that's a contract change.
