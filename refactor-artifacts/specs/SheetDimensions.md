# Block: SheetDimensions

> Phase 3 spec. Drafted from the source (`SheetDimensions.java`) and
> the characterization tests (`SheetDimensionsTest.java`, slice 69).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Holds the per-sheet row-height + column-width + hidden-index arrays
plus the default row-height and column-width values. Single source
of truth for cell geometry — operations
([`RowSizeOperations`](https://...), `ColumnSizeOperations`,
`ColumnAutofitter`) read and write through this class, never
directly accessing the underlying state.

Same state-with-mirror pattern as
[`SheetCssStyleMaps`](SheetCssStyleMaps.md): every setter caches +
mirrors to the corresponding element property.

## Inputs / fields

Six fields:

- `defRowH` / `defColW` — defaults applied to cells without
  per-row / per-column overrides.
- `rowH` / `colW` — `float[]` / `int[]` arrays of per-index
  sizes.
- `hiddenColumnIndexes` / `hiddenRowIndexes` —
  `ArrayList<Integer>` of 1-based indexes that are currently
  hidden.

Each field has a `get` + `set` pair.

## Outputs

Getters return the live value (array reference, not copy).
Setters return `void`.

## Side effects

- `setDefRowH(float)` / `setDefColW(int)` — primitive mirror via
  `element.setProperty(...)`.
- `setRowH(float[])` / `setColW(int[])` — serialized mirror via
  `Serializer.serialize`.
- `setHiddenColumnIndexes(ArrayList)` / `setHiddenRowIndexes` —
  serialized mirror.

## Invariants

- **Get returns live, set takes ownership.** The caller passes
  in a fresh array on set; the getter returns the same array
  reference. No defensive copies. Callers (RowSizeOperations,
  etc.) are responsible for not mutating the returned reference
  after a getter call — they always set a fresh array.
- **The "single source of truth" contract**. Operations classes
  read here before computing changes and write here as the final
  step. A regression that bypassed this class (e.g. wrote
  directly to a sibling field on Spreadsheet) would silently
  drift the server-side cache from the client.
- **`hidden*Indexes` uses 1-based indexing**. Standard POI is
  0-based; this class follows the Spreadsheet's UI convention.
  Pinned by slice 71's row-size tests (`hiddenRowIndexes`
  contains `index+1`, not POI's `index`).

## Open questions

- The "live reference" contract is unenforceable by the type
  system. A `Collections.unmodifiableXxx` wrapper would surface
  accidental mutation; current shape relies on convention.
- All six fields could be migrated to
  `MirroredProperty<T>` / `MirroredArray<T>` once the helper
  supports primitive-array serialization. Deferred.
