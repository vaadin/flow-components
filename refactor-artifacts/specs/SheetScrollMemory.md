# Block: SheetScrollMemory

> Phase 3 spec. Drafted from the source (`SheetScrollMemory.java`)
> and the characterization tests (`SheetScrollMemoryTest.java`,
> slice 66). **Status:** initial draft, not yet reviewed by a human
> operator.

## Purpose

Records the current scroll position into the per-sheet vertical /
horizontal scroll-position arrays before a sheet switch, so the
client can restore the right scroll offset when the user returns
to a previously visited sheet.

The single public method
([`SheetChangeDispatcher`](SheetChangeDispatcher.md) is the only
caller) stashes both axes simultaneously.

## Inputs

### `rememberForSheetAtIndex(int oldIndex, int scrollLeft, int scrollTop)`

- `oldIndex` — visible-sheet index of the outgoing sheet.
- `scrollLeft` / `scrollTop` — the scroll position to record.

## Outputs

`void`. Side effects on the vertical + horizontal scroll-position
arrays on the Spreadsheet.

## Side effects

For each axis:

1. `Arrays.copyOf(spreadsheet.getXxxScrollPositions(), len)` —
   defensive copy of the existing array.
2. Set the entry at `oldIndex` to the new value.
3. `spreadsheet.setXxxScrollPositions(updated)` — write the new
   array back, mirroring to the client property.

## Invariants

- **Defensive copy on EVERY remember call**, both axes. A
  regression that mutated in place would break the
  "scroll-positions array is immutable from any reader's POV"
  contract. Pinned by slice 66's test that asserts the original
  array reference is preserved post-call.
- **Both axes are stashed UNCONDITIONALLY**, even if the caller
  only cares about one. The orchestrator
  ([`SheetChangeDispatcher`](SheetChangeDispatcher.md)) passes
  both — the API requires both args. If a future caller only
  changes vertical scroll, it would still record (unchanged)
  horizontal. Documentary, not a bug.

## Open questions

- The two-axis stash could be split into two single-axis methods,
  letting callers update one without recording the other. The
  current monolithic API is simpler at the cost of a few
  unnecessary array copies on partial updates.
