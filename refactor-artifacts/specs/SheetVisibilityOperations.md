# Block: SheetVisibilityOperations

> Phase 3 spec. Drafted from the source
> (`SheetVisibilityOperations.java`) and the characterization tests
> (`SheetVisibilityOperationsTest.java`, slice 86). **Status:**
> initial draft, not yet reviewed by a human operator.

## Purpose

Toggle sheet visibility (visible / hidden / very-hidden) and
sheet-level protection on the workbook, with the active-sheet
shift cascade when the currently-active sheet is hidden.

Sibling helpers: [`SheetLifecycleOperations`](SheetLifecycleOperations.md),
[`SheetMetadataOperations`](SheetMetadataOperations.md).

## Inputs

### `setSheetHidden(int sheetPOIIndex, SheetVisibility visibility)`

- `sheetPOIIndex` — 0-based POI index.
- `visibility` — VISIBLE / HIDDEN / VERY_HIDDEN.
- Guard: if hiding would leave zero visible sheets, throws
  `IllegalArgumentException` (POI tolerates "all hidden"; this
  component does not).
- **No-op shortcut**: if the visibility didn't actually change
  (e.g. already visible and setting visible again), the method
  early-returns AFTER the POI call. The POI call is unconditional
  to keep the workbook authoritative; the reload skip is the
  optimization.

### `setSheetProtected(int sheetPOIIndex, String password)`

- Validates the POI index range. Calls
  `workbook.getSheetAt(...).protectSheet(password)`. Reloads
  styles (lock state affects the cell-style cache).

## Outputs

`void`. Side effects on the workbook + on the Spreadsheet's
sheet-state arrays + on the reload cascade.

## Side effects

### setSheetHidden — multi-branch cascade

If visibility actually changed:

1. **`sheetPOIIndex != activeSheetIndex`** branch (we hid /
   unhid a non-active sheet):
   - `spreadsheet.reloadSheetNames()` — sheet-names array
     changes (hidden / very-hidden sheets are excluded).
   - `spreadsheet.setSheetIndex(...)` — visible-index needs to
     shift if the hide / unhide is before the active.
2. **`sheetPOIIndex == activeSheetIndex` AND hiding** branch:
   - Choose a new active visible sheet. If the active was the
     last sheet and is being hidden, fall back to the previous.
   - `workbook.setActiveSheet(newPoiIndex)`.
   - Full reload: `reloadActiveSheetData` +
     `SpreadsheetFactory.reloadSpreadsheetData` +
     `reloadSheetStyles(false, false)`.

### setSheetProtected

POI `Sheet.protectSheet(password)` + style reload.

## Invariants

- **"At least one sheet always visible" is a component-level
  contract**, not POI's. The throw enforces it before any
  workbook mutation.
- **POI sheet-visibility setter is called UNCONDITIONALLY** even
  when the visibility didn't change. The "no-op skip" is only on
  the reload cascade. Pinned by slice 86 tests.
- **Hiding the last visible sheet shifts to the previous, not
  the next**. The code path:
  `oldVisibleSheetIndex-- if hiding && activeSheetIndex ==
  workbook.getNumberOfSheets() - 1`. Counter-intuitive: it's the
  visible-index that decrements, but only when the active is at
  the end. A regression that simplified this would break the
  "active follows the visible sequence" expectation.
- **`reloadSheetStyles(false, false)` skips lock-state and
  conditional-formatting recomputation**. Used when visibility
  changed but cell styles didn't. The two `false` args are
  load-bearing — `true, true` would force a full restyling pass
  that the visibility change doesn't need.

## Open questions

- The "no-op skip" check tests `changed` AFTER the POI mutation
  has already happened. If POI's setter has side effects (e.g.
  it dirty-flags the workbook), those are NOT skipped. A future
  audit could pre-check and skip the POI call too, saving a
  small amount of work on idempotent calls.
- `setSheetProtected` doesn't validate the password format. POI
  accepts any string (including null); Excel's behavior with
  null vs empty differs. Worth flagging if the password contract
  becomes important downstream.
