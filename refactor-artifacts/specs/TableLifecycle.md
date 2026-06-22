# Block: TableLifecycle

> Phase 3 spec. Drafted from the source (`TableLifecycle.java`) and
> the characterization tests (`TableLifecycleTest.java`, 8 tests,
> slice 83). **Status:** initial draft, not yet reviewed by a human
> operator.

## Purpose

Delete a `SpreadsheetTable` cleanly: unregister from
[`TableRegistry`](TableRegistry.md), tear down its popup buttons
(only if its sheet is currently active), clear filters (for
`SpreadsheetFilterTable` only), and clear the table's content.

A small but coordination-heavy method: 4 side effects across 3
collaborators, with the inactive-sheet branch as an
optimization gate.

Sibling helpers: [`TableRegistry`](TableRegistry.md),
[`TableLoader`](TableLoader.md).

## Inputs

### `delete(SpreadsheetTable table)`

- `table` — the table to delete. The class is dispatched on type
  for the filter-table branch (uses Java 16 pattern matching:
  `if (table instanceof SpreadsheetFilterTable filterTable)`,
  same idiom as slice 100's sweep).

## Outputs

`void`. Side effects on the registry, on the spreadsheet's popup
map, on the filter table's filter map, and on the table's
internal content.

## Side effects

In strict order:

1. **`registry.remove(table)`** — always fires, even if the
   table's sheet is inactive (registration is sheet-agnostic).
2. **Active-sheet check**: `if (!table.isTableSheetCurrentlyActive()) return;`
   — early-return short-circuit. The remaining steps only matter
   for the currently-rendered sheet.
3. **Popup cleanup**: for each popup button the table owns, call
   `spreadsheet.setPopup(popupButton.getCellReference(), null)`.
   This both unregisters the button and triggers the DOM cleanup
   (see [`PopupButtonBinding`](PopupButtonBinding.md)).
4. **Filter cleanup**: pattern-matching dispatch on
   `SpreadsheetFilterTable`. Only filter tables have
   `clearAllFilters()`; the cast is safe.
5. **Content clear**: `table.clear()` — empties the table's cell
   data on the active sheet.

## Invariants

- **Registry-remove is unconditional**. Even if the table's
  sheet is inactive, the registry entry is removed. A regression
  that moved this step inside the active-sheet branch would
  leave dangling entries.
- **Active-sheet check gates the cell-side cleanup**. Popup,
  filter, and content cleanup are all hot-path; they only run
  for the currently-rendered sheet. For inactive-sheet tables,
  the table object itself is GC-able after the registry-remove.
- **The pattern-matching dispatch preserves the filter-only
  behavior.** Non-filter tables silently skip the filter
  cleanup; filter tables get their full reset. A regression
  that lifted the filter call outside the type check would
  ClassCastException on plain SpreadsheetTable instances.

## Open questions

- "Active-sheet check" reads via `table.isTableSheetCurrentlyActive()`,
  which is implemented on the table itself. The implementation
  reads the spreadsheet's active sheet — coupling that the
  helper inherits. A future refactor could hoist the check into
  this class, but the current placement keeps the table's
  invariant (knowing its own sheet's activity) self-contained.
