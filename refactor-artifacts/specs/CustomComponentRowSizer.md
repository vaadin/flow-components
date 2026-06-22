# Block: CustomComponentRowSizer

> Phase 3 spec. Drafted from the source (`CustomComponentRowSizer.java`)
> and the characterization tests (`CustomComponentRowSizerTest.java`,
> slice 60). **Status:** initial draft, not yet reviewed by a human
> operator.

## Purpose

Adjust row heights so rows that contain custom components are
at least `minimumRowHeightForComponents` points tall, and restore
their previous height (POI-derived OR hidden-state) when
components are removed.

Owns the per-call memory of which rows currently have components
— the field `currentRowsWithComponents`, set at the END of each
`applyHeightsFor` call to be the diff-target for the NEXT call.

Sibling helpers:
[`CustomComponentLoader`](CustomComponentLoader.md) (sole
caller), [`SheetDimensions`](SheetDimensions.md) (owns the
`rowH` array this class mutates).

## Inputs

### `applyHeightsFor(Set<Integer> nextRowsWithComponents)`

- The CURRENT set of rows that contain components. Diff'd against
  `currentRowsWithComponents` (the previous set) to compute:
  - Rows in `next` but NOT in `current`: grow to `minHeight` (if
    not hidden, and current height is smaller).
  - Rows in `current` but NOT in `next`: restore — to 0 if
    hidden, to POI's `getHeightInPoints()` otherwise.

## Outputs

`void`. Side effects on the Spreadsheet's `rowH` array (via
`setRowH(updatedHeights)`).

## Side effects

- `Arrays.copyOf(rowH)` — defensive copy.
- Per-row height adjustments (grow / restore).
- `spreadsheet.setRowH(updatedHeights)` — writes back, mirroring
  to the client.
- Updates `this.currentRowsWithComponents = nextRowsWithComponents`
  as the LAST step.

## Invariants

- **Defensive copy on every call**. Same as
  [`SheetScrollMemory`](SheetScrollMemory.md) — the `setRowH`
  contract requires a fresh array.
- **Hidden-row check FIRST**. A hidden row that gains a
  component is not grown (its zero height is intentional). A
  regression that grew hidden rows would visually unhide them.
- **Restore-height-from-POI fallback to `getDefaultRowHeightInPoints`**
  for null rows. A row that had no POI entry (auto-rendered by
  Excel) doesn't know its own height; the default is the right
  restore target.
- **`currentRowsWithComponents` updates LAST**. The diff is
  against the PREVIOUS value; updating it first would lose the
  diff. Pinned by slice 60's test that asserts a two-call
  sequence produces the right restore.

## Open questions

- The minimum-height value (`spreadsheet.getMinimumRowHeightForComponents()`)
  is a single global setting. Per-component minimum heights
  aren't supported. A future enhancement could let factories
  declare a minimum.
- Hidden-then-unhidden rows lose their previous height (restored
  to default on unhide). That's a separate code path
  ([`RowSizeOperations`](RowSizeOperations.md)), not this
  class.
