# Block: CellMarkUpdater

> Phase 3 spec. Drafted from the source (`CellMarkUpdater.java`), the
> characterization tests (`CellMarkUpdaterTest.java`, 11 tests), and the
> slice that introduced it (slice 90, journal entry 2026-06-21).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Orchestrate the cell-state update lifecycle on the active sheet. The five
methods all share the same concern: *a cell changed — push the change to the
client*. They differ in **scope** and **trigger**:

| Method | Scope | Trigger |
|--------|-------|---------|
| `markCellAsUpdated` | one cell | server-side code modified a value |
| `markCellAsDeleted` | one cell | server-side code removed a cell |
| `deleteCell` | one cell | locate-by-coords-then-delete (high-level API) |
| `updateMarkedCells` | session | flush at end of a server-side change pass |
| `refreshAllCellValues` | sheet | full re-evaluation of every cell's value |

Pure orchestrator — no own state. Every method dispatches into existing
helpers (valueManager, styler, selectionManager, conditionalFormatter,
cellCommentLoader, formulaEvaluator, conditionalFormattingEvaluator) or
back to Spreadsheet (refreshCells, reloadVisibleCellContents,
updateRowAndColumnRangeCellData).

## Inputs

### `markCellAsUpdated(Cell cell, boolean cellStyleUpdated)`, `markCellAsDeleted(Cell cell, boolean cellStyleUpdated)`

- `cell` — the POI Cell that changed. Not null in the typical contract;
  passing null would NPE on `cellUpdated` / `cellDeleted`.
- `cellStyleUpdated` — when true, propagates a style change to the
  `SpreadsheetStyleFactory`; when false, the styler call is skipped.

### `deleteCell(int row, int col)`

- `row`, `col` — 0-based POI coordinates. The active sheet is resolved
  via `workbook.getActiveSheetIndex()` (not hardcoded). Out-of-range
  inputs aren't validated; the SUT lets POI's `getRow` / `getCell` return
  null, which short-circuits the dispatch to a no-op.

### `updateMarkedCells()`, `refreshAllCellValues()`

- No arguments.

## Outputs

- All methods return `void`. The outputs are observable only through the
  collaborator calls they trigger.

## Side effects

### `markCellAsUpdated(cell, cellStyleUpdated)`

- `valueManager.cellUpdated(cell)` — always.
- `styler.cellStyleUpdated(cell, true)` — only when `cellStyleUpdated`.

### `markCellAsDeleted(cell, cellStyleUpdated)`

- `valueManager.cellDeleted(cell)` — always.
- `styler.cellStyleUpdated(cell, true)` — only when `cellStyleUpdated`.
- `spreadsheet.refreshCells(cell)` — always (independent of the flag;
  the refresh is what re-renders the deleted cell on the client).

### `deleteCell(row, col)`

When the resolved cell is **non-null** (in order, all four happen):

1. `styler.cellStyleUpdated(cell, true)` — must be before the cell is
   removed from POI, so the styler can still see the cell's pre-delete
   style map.
2. `activeSheetRow.removeCell(cell)` — removes the cell from POI.
3. `valueManager.cellDeleted(cell)` — pushes the delete to the value
   manager.
4. `spreadsheet.refreshCells(cell)` — re-renders.

When the resolved cell is **null** — no-op. None of the four fire.

### `updateMarkedCells()`

Five-step orchestration, in order:

1. `conditionalFormatter.createConditionalFormatterRules()` — rebuild
   CF rules first so the next step picks up updated styling.
2. `valueManager.updateMarkedCellValues()` — flush queued cell-value
   changes to the client.
3. `selectionManager.reSelectSelectedCell()` — refresh the formula bar
   in case the selected cell's formula changed.
4. `cellCommentLoader.load()` — re-publish cell comments so new ones
   show immediately.
5. `spreadsheet.reloadVisibleCellContents()` — re-instantiate custom
   components / editors for visible cells.

The order is load-bearing: see *Non-obvious quirks*.

### `refreshAllCellValues()`

Three-step prologue, always:

1. `formulaEvaluator.clearAllCachedResultValues()`
2. `conditionalFormattingEvaluator.clearAllCachedValues()`
3. `valueManager.clearCachedContent()`

Then a conditional epilogue:

- If `spreadsheet.getFirstColumn() == -1` (viewport never loaded):
  early return. NO `updateRowAndColumnRangeCellData`, NO
  `reSelectSelectedCell`.
- Otherwise: `spreadsheet.updateRowAndColumnRangeCellData(1, 1,
  spreadsheet.getRows(), spreadsheet.getColumns())` then
  `selectionManager.reSelectSelectedCell()`.

## Invariants

- **Pure orchestrator.** No mutable state on the helper; every dispatch
  is read-through to a collaborator. Two instances of `CellMarkUpdater`
  with the same parent `Spreadsheet` would behave identically.
- **`deleteCell` resolves the active sheet per call** via
  `workbook.getActiveSheetIndex()`. A regression that cached the sheet
  reference would silently break operations across sheet swaps.
- **`refreshAllCellValues`'s clear order** — formula and conditional-
  formatting evaluators are cleared BEFORE the value cache. If reversed,
  `valueManager.clearCachedContent()` could trigger a re-evaluation that
  resurrects stale formula results from the evaluator caches that were
  about to be cleared next.
- **`updateMarkedCells`'s 5-call order** — see *Non-obvious quirks*.

## Edge cases

- **`deleteCell` for a cell that doesn't exist** — the row exists but
  the cell at the given column is `null` (a sparse row). The method
  short-circuits silently; the dispatch chain is skipped. A regression
  removing the null guard would NPE on `styler.cellStyleUpdated`.
- **`deleteCell` for a row that doesn't exist** — would NPE on
  `activeSheet.getRow(row).getCell(col)`. The SUT does NOT guard
  against missing rows; callers are expected to validate.
- **`markCellAsDeleted` with `cellStyleUpdated == false`** — the styler
  is skipped but `refreshCells` still fires. The "refresh always fires"
  contract is independent of style propagation; this asymmetry is
  intentional because client-side rendering needs the refresh even when
  the cell's style is unchanged (the cell itself was deleted).
- **`refreshAllCellValues` on a never-rendered Spreadsheet** —
  `getFirstColumn() == -1` is the sentinel for "viewport not yet
  loaded". The early return prevents an unwanted reload before the
  client has even requested cells. A regression that always reloaded
  would cause duplicate work AND a redundant selection event during
  initial Spreadsheet construction.
- **`updateMarkedCells` with no marks pending** — the SUT does NOT
  early-exit on "no marks". All five collaborator calls fire
  unconditionally. `valueManager.updateMarkedCellValues()` handles the
  empty-queue case internally. The other four calls are harmless on
  empty input. A regression that added a "marks.isEmpty() → return"
  optimization would break this contract.

## Error modes

- **`null` cell** in `markCellAsUpdated` / `markCellAsDeleted` — NPE
  inside the value manager. Not swallowed.
- **`null` workbook** at `deleteCell` — NPE at `workbook.getSheetAt(...)`.
  Not swallowed.
- **Out-of-bounds row in `deleteCell`** — NPE at `getRow(row).getCell(col)`
  when `getRow` returns null. Not guarded; callers must ensure the row
  exists.
- **Collaborator failure** (e.g. `styler.cellStyleUpdated` throws) —
  propagates. The SUT does not catch exceptions to leave partial state.

## Non-obvious quirks

- **`updateMarkedCells`'s 5-call order is load-bearing.** Two ordering
  invariants are pinned by `InOrder` in the test:
  1. CF rule rebuild **before** value flush — `valueManager.updateMarkedCellValues`
     reads CF-applied styles when serializing cell payloads. If CF
     rules are stale, the cell payload would carry the wrong style.
  2. `reloadVisibleCellContents` **last** — it depends on the freshly
     loaded value cache for custom-component / editor placement. A
     pre-flush call would render against the old cache.
- **`refreshAllCellValues`'s evaluator-before-value-cache order is
  load-bearing.** `valueManager.clearCachedContent()` may trigger a
  re-evaluation when the next update cycle requests cell values; if
  the evaluator caches still hold stale formula results at that point,
  the re-evaluation would return them instead of computing fresh
  values.
- **`deleteCell`'s 4-call order is load-bearing.** The styler is
  notified BEFORE `row.removeCell(cell)` so the styler's internal
  cell→style map can resolve the cell. After removal, POI would no
  longer return it and the styler's lookup would fail. Pinned by
  `deleteCell_cellExists_dispatchesFullChain` via `InOrder`.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/CellMarkUpdater.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/CellMarkUpdaterTest.java` (11 tests; every contract paragraph here is pinned by at least one)
- Slice that introduced it: journal entry 2026-06-21, slice 90 (`CellMarkUpdater for cell mark/refresh orchestration`)
- Callers (extant): `Spreadsheet.markCellAsUpdated` / `markCellAsDeleted` / `deleteCell` / `updateMarkedCells` / `refreshAllCellValues` (all thin delegators); plus internal calls from `CellValueShifter`, `DirectionalShiftOperator`, and the merge-region pipeline.

## Contract status

Every observable behavior in this spec maps to at least one passing test
in `CellMarkUpdaterTest.java`. The InOrder-based tests pin three separate
sequencing contracts: the 4-call delete chain, the 5-call update flush,
and the evaluator-before-value-cache clear sequence in
`refreshAllCellValues`.

## What the spec doesn't cover

- **Performance characteristics.** All five methods are O(n) in the
  size of the affected cell set or the visible viewport. Not relevant
  for typical interactive use.
- **Concurrency.** Not thread-safe. UI-thread only.
- **Memory pressure during `refreshAllCellValues`.** The clear-and-
  reload sequence allocates fresh cell payloads for the entire visible
  viewport. Large sheets with custom-component-heavy viewports may see
  brief GC pressure; the legacy code had the same behavior.

## Human review

**Status: not yet reviewed.** Per the pipeline's Phase 3 process, this
draft needs a domain expert to verify:

1. **Is the "refresh always fires" contract in `markCellAsDeleted`
   actually intentional**, or was the inconsistency vs `markCellAsUpdated`
   (which has no refresh call) an accident inherited from the
   pre-extraction code? The current contract is "delete = needs refresh,
   update = doesn't" — defensible but worth confirming with the original
   author or with manual UI testing.
2. **Should `updateMarkedCells` short-circuit when the value-manager
   queue is empty?** Currently all 5 calls fire even for an empty
   change set. The cost is negligible (each is itself a guarded no-op)
   but a future "no-op session" optimization could be valuable for
   reducing UI-thread work.
3. **Is the `getFirstColumn() == -1` sentinel for "viewport not loaded"
   robust?** The check is observable through `viewportBounds`. A
   subclass of `Spreadsheet` that overrides `getFirstColumn()` could
   inadvertently break the early-return. The contract here depends on
   `viewportBounds` being the source of truth for that value.
