# Block: MergedRegionCascade

> Eighth Phase 3 spec. Pairs with slice 43's tests
> (`MergedRegionCascadeTest`, 11 tests). The second leg of the
> merged-region trio's Phase 3 documentation — `MergedRegionLookup` was
> slice 44; `MergedRegionOperations` is the next pending spec.
> **Status:** initial draft, not yet reviewed by a human operator.
> **Open question for review:** the asymmetry between null-row and
> null-cell handling in `afterUnmerge` (see "Non-obvious quirks").

## Purpose

Applies the cell-level side effects that follow when a region is merged or
unmerged. Two-method orchestrator owned by `MergedRegionOperations` (the
add/remove driver) and `MergedRegionSynchronizer` (the bulk-rebuild
fallback). Drives:

- Style invalidation on affected cells (and the row / column just past the
  region — see "wider-by-one cascade area" below).
- Marking cells for client-cache removal when merging (the inside-non-anchor
  cells, which become hidden by the merge).
- Marking cells for client-cache update when unmerging (the previously-
  hidden cells, which become visible again).
- Terminal `loadCustomBorderStylesToState` to refresh custom borders once
  per cascade.

## Inputs

### Constructor

- `spreadsheet` — required, non-null. The cascade reaches into
  `getActiveSheet()`, `getSpreadsheetStyleFactory()`, and
  `getCellValueManager()` for every operation.

### `afterMerge(MergedRegion region)`

- `region` — the merged-region DTO that was just added. The cascade reads
  `col1` / `col2` / `row1` / `row2` (all 1-based) to drive the loop bounds.

### `afterUnmerge(MergedRegion region)`

- `region` — the merged-region DTO that was just removed. Same field
  contract as `afterMerge`.

## Outputs

Both methods: `void`. Side effects only.

## Side effects

### Common pattern (both methods)

1. Walk the **wider-by-one cascade area**: rows from `row1` to `row2 + 1`,
   columns from `col1` to `col2 + 1`. The `+1` extends the loop into the
   row/column just past the bottom-right of the region — these cells need
   their border styles recalculated because the merge changes their
   neighbor.
2. Skip null rows (`sheet.getRow(r - 1) == null` → `continue`).
3. Per-method behavior on the cells within each row (detailed below).
4. After both nested loops complete, fire
   `spreadsheet.getSpreadsheetStyleFactory().loadCustomBorderStylesToState()`
   exactly once.

### `afterMerge` per-cell behavior

- If the cell exists: `cellStyleUpdated(cell, false)` AND, if the cell is
  strictly inside the merge but not the anchor
  (`c <= col2 && r <= row2 && (c, r) != (col1, row1)`),
  `markCellForRemove(cell)`.
- If the cell does not exist: skip entirely. No marker, no synthesized
  removal.

### `afterUnmerge` per-cell behavior

- If the cell exists: `cellStyleUpdated(cell, false)` AND
  `markCellForUpdate(cell)`. Unlike `afterMerge`, the update marker is
  set for **every** existing cell in the cascade area, not just the
  inside-non-anchor cells.
- If the cell does not exist: `clearCellStyle(r, c)` — by 1-based
  coordinates, NOT by Cell object. The cascade explicitly invalidates any
  cached style state for cells that never had a POI Cell object.

## Invariants

- **`updateCustomBorders=false` per-cell, then bulk reload at the end.**
  Both methods always call `cellStyleUpdated(cell, false)`, never
  `(cell, true)`. The custom-border refresh happens exactly once per
  cascade via the terminal `loadCustomBorderStylesToState()`. This avoids
  quadratic style work for large merges. Pinned by
  `neitherCascade_callsUpdateCustomBorders_perCell`.
- **`loadCustomBorderStylesToState` fires exactly once per call**, even
  when no cells exist. Pinned by `afterMerge_loadCustomBorderStyles_firesOnce`
  and `afterUnmerge_loadCustomBorderStyles_firesOnce`.
- **`afterMerge` and `afterUnmerge` are not symmetric.** Merge marks only
  the inside-non-anchor cells for removal; unmerge marks every existing
  cell in the cascade area for update. Pinned by the count assertions on
  both methods (3 for-remove vs 9 for-update on a 2x2 merge).
- The `markCellForUpdate` calls fire for cells along the row/column just
  past the region too (the cascade area is wider by one). Their styles
  may need to re-render because the absence of the merge changes their
  bordering neighbor.

## Edge cases

- **1x1 "merge"** (`col1 == col2 && row1 == row2`). `afterMerge` walks a
  2x2 area; only the anchor is inside the merge, so 0 markCellForRemove
  calls fire. `afterUnmerge` walks the same 2x2; every existing cell
  gets cellStyleUpdated + markCellForUpdate. Pinned by
  `afterMerge_singleCellRegion_marksZeroForRemove`.
- **Sparse rows in the cascade area.** Null rows are skipped (continue);
  null cells WITHIN an existing row are handled differently per method.
- **Region with `row2 + 1` past the last row in the sheet** → POI returns
  null for that row → the row is skipped.
- **Region at row 1 / col 1 of the sheet** → the cascade still walks from
  row1 (i.e. 1) outward; there's no row-0 issue because indexing is
  1-based and the loop converts to 0-based POI indexing inside.

## Error modes

- **`region == null`** → NPE on `region.row1` access. Not swallowed.
- **`spreadsheet.getActiveSheet()` returns null** → NPE on
  `.getRow(...)`. Not swallowed.
- **`spreadsheet.getSpreadsheetStyleFactory()` / `getCellValueManager()`
  returns null** → NPE on the first call. Not swallowed.
- **`cellStyleUpdated` / `markCellForRemove` / `markCellForUpdate` throws**
  → propagates up; the terminal `loadCustomBorderStylesToState` doesn't run.
  No try/finally cleanup.

## Non-obvious quirks

- **Null-row vs null-cell asymmetry in `afterUnmerge`.** A null row aborts
  the row entirely (continue) — no `clearCellStyle` calls fire for any
  column in that row. A null cell INSIDE an existing row triggers
  `clearCellStyle(r, c)` by coordinate. This asymmetry is plausibly a
  bug (why should a null row escape the clear-by-coordinate path?), but
  the IT tests are stable, so slice 43 pins it as-is. **Flagged for
  domain-expert review** — see "Human review" below.
- **The wider-by-one cascade area is intentional.** A merge changes the
  bordering neighbors of the merge, so the row immediately below
  `row2` and the column immediately right of `col2` need their styles
  re-rendered too. Without the `+1`, the visible borders would be stale.
- **`cellStyleUpdated(cell, false)` per-cell is critical for performance.**
  Passing `true` would trigger a per-cell custom-border refresh, which
  would be O(cells × borders) — quadratic. The cascade's design — false
  per-cell + one terminal `loadCustomBorderStylesToState()` — pulls the
  custom-border work out of the inner loop.
- **`markCellForRemove` vs `markCellForUpdate` is what tells the client
  cache to evict vs re-fetch.** The semantic distinction lives on
  `CellValueManager`; the cascade just routes per per-method.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/MergedRegionCascade.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/MergedRegionCascadeTest.java` (11 tests; every invariant in this spec maps to at least one assertion)
- Slice that introduced it: slice 2 (the merged-region decomposition)
- Companion blocks: `MergedRegionLookup` (read-only query side), `MergedRegionOperations` (add/remove driver — the primary caller),
  `MergedRegionSynchronizer` (bulk-rebuild fallback — also calls this cascade per region),
  `SpreadsheetStyleFactory.cellStyleUpdated` / `clearCellStyle` /
  `loadCustomBorderStylesToState` (the style-side downstream),
  `CellValueManager.markCellForRemove` / `markCellForUpdate` (the cache-side downstream)

## Contract status

Every invariant in this spec maps to at least one passing test in
`MergedRegionCascadeTest.java`. The slice-43 test file caught the
clearCellStyle parameter order on the first run — the asymmetry between
null-row (no-op) and null-cell (clear-by-coordinate) is now explicitly
pinned by two separate tests (`afterUnmerge_missingRow_skippedEntirely`
+ `afterUnmerge_missingCell_clearsStyleByCoordinatesInstead`).

## What the spec doesn't cover

- Performance characteristics. O(cascade area) per call. The +1
  widening doesn't meaningfully change the asymptotic; it's a small
  constant on top of the region size.
- Thread safety. Relies on Vaadin Flow's request-thread serialization
  for access to `Spreadsheet`'s style factory + value manager.
- Whether the wider-by-one extension is sufficient at the right and
  bottom edges of the sheet. If `row2 + 1` exceeds the sheet's row
  range, POI returns null and the cascade silently skips — which means
  the actual rendering relies on the absence of a neighbor to be
  visually correct. Probably correct, but worth verifying.

## Human review

**Status: not yet reviewed.** Per pipeline §13.2, this draft needs a domain
expert to verify:

1. **Is the null-row vs null-cell asymmetry in `afterUnmerge` intentional?**
   A null row escapes the `clearCellStyle(r, c)` path entirely; a null
   cell inside an existing row triggers it. If a row was unmerged after
   a previous merge had stored cached style state for cells that never
   had POI Cell objects, those cached styles only get cleared if the
   ROW exists. A row that was created during the merge but became null
   after the unmerge would leak stale style state. Unlikely in practice
   (POI rows usually outlive merges), but the contract is implicit.
2. **Should `markCellForUpdate` fire for the +1 cells in `afterUnmerge`?**
   The cells in the row past `row2` and the column past `col2` weren't
   hidden by the merge, so their values haven't changed. Marking them
   for update sends a redundant value to the client. Their *styles*
   have changed (the merge's removal affects their borders), but those
   are handled separately via `cellStyleUpdated`. Confirming whether
   the +1 cells should be in the update-mark set is a 5-minute
   conversation that could prune a small amount of client-side work.
3. **Should the cascade survive a thrown exception mid-loop?** Currently
   it doesn't — the terminal `loadCustomBorderStylesToState` is skipped
   on exception, so the next style refresh would be stale. A try/finally
   would fix that, but might mask the actual error. Decide based on the
   error-handling philosophy elsewhere in the spreadsheet codebase.
