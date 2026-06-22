# Block: CustomComponentLoader

> Phase 3 spec. Drafted from the source (`CustomComponentLoader.java`)
> and the characterization tests (`CustomComponentLoaderTest.java`,
> slice 62). **Status:** initial draft, not yet reviewed by a human
> operator.

## Purpose

Orchestrate loading of custom components and custom editors for
the currently visible cells. Walks four viewport panes, asks
[`CustomComponentRangeScanner`](CustomComponentRangeScanner.md)
for each range, then reconciles the new component set against
the previous registration so removed components are unregistered
while retained components keep their `StateNode` identity.

Owns the `customComponents` set + the active
`SpreadsheetComponentFactory`. Also coordinates with
[`CustomComponentRowSizer`](CustomComponentRowSizer.md) to grow
the row heights of rows that now contain components.

## Inputs

### `setFactory(SpreadsheetComponentFactory)` / `getFactory()`

- Registers the factory. If null, `load()` short-circuits to
  "unload everything."

### `load()`

- No args. Multi-stage:
  1. **No-factory branch**: clear the editor maps + unregister
     existing components + reset row heights.
  2. **Pane walk**: scan all 4 viewport panes (same shape as
     [`HyperlinkLoader`](HyperlinkLoader.md)), accumulating
     `nextComponents`, `rowsWithComponents`, and the two key maps.
  3. **Retention reconciliation**: for each component in the
     previous set, KEEP it if either (a) it's in the new scan,
     or (b) its node ID still appears in
     `nextCellKeysToEditorIdMap` (= still registered as an
     editor for some cell). Otherwise unregister + remove.
  4. Push the reconciled state back to the spreadsheet's maps
     and call `rowSizer.applyHeightsFor(rowsWithComponents)`.

### `unloadAll()`

- Drops the editor map + unregisters all components. Used when
  the factory changes before the spreadsheet has bounds.

## Outputs

Both `load()` and `unloadAll()` return `void`. Side effects on
the Spreadsheet's two key maps, on the custom-components set,
and on row heights via the row sizer.

## Side effects

- Cell-key → editor-ID map update (the cell-to-editor binding).
- Component-ID → cell-keys map update (the editor's cell list).
- `spreadsheet.unRegisterCustomComponent(...)` for components
  that lost all their cells.
- `rowSizer.applyHeightsFor(rowsWithComponents)` after the scan.

## Invariants

- **Retention reconciliation is "keep if scanned OR claimed by
  the editor map."** A component that didn't appear in this
  scan (because its cell is off-screen) but is STILL listed as
  a custom editor for some cell keeps its registration. This
  preserves the `StateNode` so Flow doesn't re-create the
  component when the user scrolls back.
- **`unRegisterCustomComponent` is called BEFORE the iterator
  removes the entry.** A regression that swapped the order would
  leave the Spreadsheet with a stale `getCustomComponents()`
  view during the unregister event firing.
- **The no-factory branch ALSO calls `rowSizer.applyHeightsFor(emptySet)`** —
  this is what RESTORES the row heights when a factory is
  unregistered. Without it, rows would stay tall after their
  components are gone.

## Open questions

- The pane-walk pattern is duplicated with the comment +
  hyperlink loaders. See [CellCommentLoader spec](CellCommentLoader.md)
  for the deferred extraction.
- The retention logic is the trickiest part of the loader — a
  future refactor could extract a `CustomComponentReconciler`
  helper to isolate it from the orchestration. Current shape
  trades cohesion for testability.
