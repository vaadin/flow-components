# Block: CellDeletionGuard

> Phase 3 spec. Drafted from the source (`CellDeletionGuard.java`)
> and the characterization tests (`CellDeletionGuardTest.java`, 17
> tests, slice 77). **Status:** initial draft, not yet reviewed by
> a human operator.

## Purpose

Decide whether the default cell-deletion path runs, or whether
the application's custom
`Spreadsheet.CellDeletionHandler` has taken responsibility. Three
deletion shapes, three predicates — each returning `true` when the
default path should run.

## Inputs

### `passesDeletionCheck(CellReference selectedCellReference)`

- `selectedCellReference` — the single-cell selection. May be
  `null` (which short-circuits to `true`).
- Reads `parent.getCustomCellDeletionHandler()`. If null, short-
  circuits to `true`.
- Resolves the row / cell on the active sheet. If the row is null
  OR the cell is null, short-circuits to `true` (no cell to
  delete → default path runs).
- Otherwise: forwards to `handler.cellDeleted(cell, sheet, col,
  row, formulaEvaluator, dataFormatter,
  conditionalFormattingEvaluator)` and returns whatever the
  handler returns.

### `passesIndividualDeletionCheck(List<CellReference>)`

- Empty list or null handler → `true`.
- Else: forwards to `handler.individualSelectedCellsDeleted(...)`
  with the full collaborator set; returns the handler's verdict.

### `passesRangeDeletionCheck(List<CellRangeAddress>)`

- Empty list or null handler → `true`.
- Else: forwards to `handler.cellRangeDeleted(...)` with the
  full collaborator set; returns the handler's verdict.

### `isInRange(CellReference, List<CellRangeAddress>)`

- Predicate. Returns `true` iff the cell sits inside ANY of the
  ranges. Short-circuits on first match (standard linear scan).

## Outputs

All four methods return `boolean`. The two-state interpretation:
`true` = default path should run; `false` = the custom handler
took responsibility, default path should NOT run.

## Side effects

None directly. The forwarded handler calls MAY produce side
effects (user code can do anything inside `cellDeleted` etc.),
but this class doesn't mutate any state.

## Invariants

- **"Pass when nobody intercepts."** All three predicates return
  `true` if there's no handler OR the handler verb didn't take
  ownership. The semantic is "the guard passes (= default runs)
  unless the custom handler explicitly handled it" — load-bearing
  for backward compatibility (a user who doesn't register a
  handler gets the default behavior).
- **Handler return value propagates verbatim (no inversion).**
  Pinned by slice 77's tests. A regression that flipped the
  return value would silently change semantics for every existing
  custom handler.
- **`passesDeletionCheck` has TWO short-circuit paths to default.**
  Null-row OR null-cell-at-resolved-column both return `true`
  without consulting the handler. The semantic: "you can't delete
  what's not there," so the default path runs cheaply. Both
  short-circuits are pinned by separate tests.
- **The full collaborator set is forwarded.** Custom handlers
  receive `formulaEvaluator`, `dataFormatter`, and
  `conditionalFormattingEvaluator` to make context-aware
  decisions (e.g., "delete if cell evaluates to zero"). Dropping
  any of the three would break documented handler API.

## Open questions

- The `isInRange` method is currently package-private but isn't
  called from outside `CellSelectionShifter` and this class.
  Could be inlined; the extraction was for testability.
- The parent reference (`CellValueManager`) carries 5 cross-cutting
  values; a future refactor could narrow the dependency to a
  smaller `DeletionContext` value object.
