# Block: TableRegistry

> Phase 3 spec. Drafted from the source (`TableRegistry.java`) and
> the characterization tests (`TableRegistryTest.java`, slice 67).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Hold the set of `SpreadsheetTable`s registered with a Spreadsheet,
plus a sticky **per-active-sheet-render latch** (`tablesLoaded`)
that lets the table-loader skip duplicate reload calls within a
single sheet-render cycle.

The latch is the actual reason this class exists — without it, the
table-load work could happen many times per render (every sheet-
state event could trigger it). The latch defers all work to one
loader pass per sheet-change.

Sibling helpers: [`TableLoader`](TableLoader.md),
[`TableLifecycle`](TableLifecycle.md).

## Inputs

### `add(SpreadsheetTable)` / `remove(SpreadsheetTable)`

- Standard registration. The internal collection is a `HashSet`.

### `getAll()`

- Returns the live `HashSet` — NOT a defensive copy. Used by
  `TableLoader.loadForActiveSheet()` for iteration. Same
  "live-reference-with-comment" pattern as PopupButtonRegistry.

### `getForSheet(Sheet sheet)`

- Filters the registered tables by `table.getSheet().equals(sheet)`
  and returns a fresh `ArrayList`. Used when only the active-sheet
  subset is needed.

### `isLoadedForActiveSheet()` / `markLoadedForActiveSheet()` / `markNeedsReload()`

- The latch trio. `isLoadedForActiveSheet` is the predicate
  TableLoader checks; the two mark-methods set the latch to true
  (after load) or false (on sheet-change).

## Outputs

- `getAll()` returns a `HashSet` (live).
- `getForSheet(Sheet)` returns a `List` (copy).
- `isLoadedForActiveSheet()` returns `boolean`.
- All other methods return `void`.

## Side effects

None outside the internal collection + latch flag.

## Invariants

- **The latch is the load-bearing contract.** Without it, every
  caller would re-iterate all tables on every render-loop event.
  The slice 67 test that pins this checks
  `loadForActiveSheet` is a no-op on the second consecutive call.
- **`markNeedsReload()` must be paired with a sheet-change event.**
  The latch's "false" state is only correct AT a sheet-change
  boundary; if a caller flips it false outside that context, the
  next loader call will redo work.
- **`getAll()` returns the live set, intentional**. Documented in
  slice 67 as a performance choice (table iteration during reload
  is hot-path-adjacent). Same caveat as PopupButtonRegistry: don't
  mutate during iteration.

## Open questions

- The latch + the registry could be split — the latch is one
  boolean and could live on `TableLoader` instead of the
  registry. The current placement keeps `TableLoader` stateless
  but couples the registry to the loader's render-cycle
  semantics. Either placement works; the current choice has a
  test that pins it.
