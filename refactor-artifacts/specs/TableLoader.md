# Block: TableLoader

> Phase 3 spec. Drafted from the source (`TableLoader.java`) and
> the characterization tests (`TableLoaderTest.java`, slice 82).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Reload each registered `SpreadsheetTable` that belongs to the
currently active sheet, and latch the registry so duplicate calls
within the same sheet-render cycle are a no-op.

Pure dispatcher: latched-skip → iterate → reload-per-table →
mark-latched. The latch state lives on
[`TableRegistry`](TableRegistry.md); this class only reads + writes
it.

Sibling helpers: [`TableRegistry`](TableRegistry.md),
[`TableLifecycle`](TableLifecycle.md).

## Inputs

### `loadForActiveSheet()`

- No arguments. Steps:
  1. **Latch check**: if `registry.isLoadedForActiveSheet()`,
     return immediately.
  2. **Iterate**: for each table in `registry.getAll()`, call
     `table.reload()` ONLY if its sheet equals the active sheet.
     Inactive-sheet tables are silently skipped.
  3. **Latch**: `registry.markLoadedForActiveSheet()` after the
     loop.

## Outputs

`void`. Side effects on each active-sheet table (via `reload()`)
plus the latch flip.

## Side effects

- `SpreadsheetTable.reload()` per active-sheet table — the table
  rebuilds its filter state, repopulates its popup buttons,
  re-applies styles.
- Registry latch flip.

## Invariants

- **Latch-first short-circuit.** The latched-skip check comes
  BEFORE any iteration. A regression that moved the latch check
  inside the iteration would silently lose the deduplication.
- **Active-sheet filter is `table.getSheet().equals(...)`, not
  reference equality.** POI's `Sheet` implementations may return
  the same logical sheet from different code paths; equals-based
  comparison is the right one.
- **Latch flips to "loaded" UNCONDITIONALLY after the loop, even
  on an empty registry.** This is the load-bearing semantic for
  "a render cycle ran, regardless of whether there was work":
  the next sheet-change is the only event that can flip it back
  to false.

## Open questions

- The latch is conceptually "have we run the loader for the
  current sheet?" — but `TableRegistry.markNeedsReload()` is the
  only way to invalidate it. Callers that mutate tables outside
  a sheet-change (e.g. adding a popup mid-session) don't
  currently invalidate; that's by design (table additions
  cascade differently), but a future test could pin the
  expectation.
