# Block: SheetChangeDispatcher

> Phase 3 spec. Drafted from the source (`SheetChangeDispatcher.java`)
> and the characterization tests (`SheetChangeDispatcherTest.java`,
> slice 84). **Status:** initial draft, not yet reviewed by a human
> operator.

## Purpose

Translate three client-side sheet events (`tabSelected`,
`newSheetCreated`, `sheetRenamed`) into the right sequence of
scroll-position stash + sheet activation + sheet creation +
listener notification.

Pure orchestrator: every move is delegated to a sibling.
[`SheetScrollMemory`](SheetScrollMemory.md) records the scroll
position before the change; [`ActiveSheetSelector`](ActiveSheetSelector.md)
performs the activation cascade; the Spreadsheet's `fireSheetChangeEvent`
fires the listener; the rename path goes through
`Spreadsheet.setSheetNameWithPOIIndex`.

Sibling helpers: [`SheetScrollMemory`](SheetScrollMemory.md),
[`ActiveSheetSelector`](ActiveSheetSelector.md),
[`SheetMetadataOperations`](SheetMetadataOperations.md).

## Inputs

### `onSheetSelected(int tabIndex, int scrollLeft, int scrollTop)`

- `tabIndex` — the new visible index to activate.
- `scrollLeft` / `scrollTop` — current scroll position of the
  outgoing sheet.
- Steps:
  1. Compute `oldIndex` (defensively `Math.abs` because the
     initial state may carry a negative sentinel).
  2. `scrollMemory.rememberForSheetAtIndex(oldIndex, scrollLeft,
     scrollTop)` — stash the outgoing scroll position.
  3. Record `oldSheet`, call
     `spreadsheet.setActiveSheetIndex(tabIndex)`.
  4. Record `newSheet`, call
     `spreadsheet.fireSheetChangeEvent(oldSheet, newSheet)`.

### `onNewSheetCreated(int scrollLeft, int scrollTop)`

- Same scroll stash, then `spreadsheet.createNewSheet(null,
  defaultRows, defaultCols)`. The newly-created sheet auto-
  activates inside `createNewSheet`, so the dispatch doesn't fire
  a separate change event.

### `onSheetRename(int sheetIndex, String sheetName)`

- Translates visible index to POI index and calls
  `setSheetNameWithPOIIndex(poiIndex, sheetName)`. No history
  recording — comment in source: "if Excel doesn't keep these in
  history, neither will we."

## Outputs

`void`. Side effects on the scroll-position arrays + the active
sheet + the listener registrations + the workbook (rename).

## Side effects

- `SheetScrollMemory.rememberForSheetAtIndex` (defensive copy of
  both arrays + index update + setter).
- `ActiveSheetSelector.setActiveSheetIndex` (via Spreadsheet's
  wrapper) — the full 8-step cascade pinned in
  [ActiveSheetSelector spec](ActiveSheetSelector.md).
- `fireSheetChangeEvent` — fires the
  `SheetChangeListener.SheetChangeEvent` to listeners.

## Invariants

- **Scroll stash BEFORE active-sheet change.** Otherwise the
  stash would record the new sheet's scroll (which is zero) for
  the old sheet, and returning to the old sheet would lose its
  scroll position. The order is load-bearing.
- **`onSheetSelected` defensively `Math.abs`-s the sheet index.**
  The legacy comment names a "very rare occasion when the
  selected sheet value is still negative" (initial state before
  the first activation). The `Math.abs` is unconditional; a
  regression that dropped it would crash on negative-index init
  states.
- **Rename intentionally doesn't fire a change event.** The
  source comment names this: history-tracking parity with Excel.
- **New-sheet creation auto-activates inside `createNewSheet`**.
  The dispatcher doesn't re-fire the change event — the
  `createNewSheet` path on `SheetLifecycleOperations` fires it
  directly.

## Open questions

- The `Math.abs` defensive read is opaque without the comment
  next to it. A future cleanup could replace it with an explicit
  `oldIndex = Math.max(0, ...)` to make the intent clearer.
- `setActiveSheetIndex(tabIndex)` is called without validation
  here — the validation lives on
  [`ActiveSheetSelector`](ActiveSheetSelector.md). A regression
  that bypassed the selector would lose the validation; the
  current routing is correct.
