# Block: FreezePaneState

> Phase 3 spec. Drafted from the source (`FreezePaneState.java`)
> and the characterization tests (`FreezePaneStateTest.java`,
> slice 33). **Status:** initial draft, not yet reviewed by a
> human operator.

## Purpose

Holds the freeze-pane split coordinates + per-pane scroll
positions, mirrored to the client. A frozen pane divides the
sheet into up to four scroll regions (top-left, top-right,
bottom-left, bottom-right). The two split positions name where
the divider sits; the two scroll-position arrays remember each
region's last-seen scroll offset so a sheet switch can restore
them.

Same state-with-mirror pattern as
[`SheetCssStyleMaps`](SheetCssStyleMaps.md) and
[`SheetDimensions`](SheetDimensions.md).

## Inputs

Four fields, each with a get/set pair:

- `verticalSplitPosition` — row index where the horizontal
  divider sits.
- `horizontalSplitPosition` — column index where the vertical
  divider sits.
- `verticalScrollPositions` / `horizontalScrollPositions` —
  `int[]` arrays indexed by visible-sheet-index. Each entry is
  the last-seen scroll offset for that sheet's pane.

## Outputs

Getters return cached values. Setters return `void`.

## Side effects

- Primitive setters mirror directly via `element.setProperty`.
- Array setters mirror via `Serializer.serialize`.

## Invariants

- **Split positions are POI-aligned (1-based for non-zero,
  0 = no split)**. A regression that flipped the convention
  would silently shift every freeze pane by one cell.
- **Scroll-position arrays are sized by the visible-sheet
  count**. The setter doesn't validate length; the caller
  ([`SheetScrollMemory`](SheetScrollMemory.md)) is responsible
  for size consistency.
- **The two axes are independent**. A vertical-only freeze
  (horizontalSplit = 0) means only the top-vs-bottom panes
  exist; the corresponding scroll-position array is still
  sized normally because per-sheet scroll memory is independent
  of the freeze axis.

## Open questions

- The 4-field layout could be folded into a record (`record
  FreezePaneState(int verticalSplit, int horizontalSplit,
  int[] vScroll, int[] hScroll)`) — but `int[]` doesn't satisfy
  record equality cleanly (record auto-equals on array fields
  is reference equality, not deep equality). Documented in
  slice 107's records audit; FreezePaneState rejected as a
  record candidate because of the int[] equality issue plus
  the per-setter side-effect (mirror) pattern.
