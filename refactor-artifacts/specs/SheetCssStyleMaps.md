# Block: SheetCssStyleMaps

> Phase 3 spec. Drafted from the source (`SheetCssStyleMaps.java`)
> and the characterization tests (`SheetCssStyleMapsTest.java`, slice
> 88). **Status:** initial draft, not yet reviewed by a human
> operator.

## Purpose

Holds the six per-sheet CSS-style index maps that
[`SpreadsheetStyleFactory`](CellBorderStyleBuilder.md) populates
during a style-load pass and mirrors to the client. Each setter is
a get-and-mirror pair — the client's source of truth for cell
appearance.

The class is **state-with-mirror**, the same pattern named in
`MirroredProperty<T>` and the deferred-flush `ClientReloadFlag`.
Here the mirror is per-setter inline (not via `MirroredProperty`)
because each field has a slightly different serialization shape
that wouldn't fit cleanly into the generic helper.

## Inputs

Six fields, six getter/setter pairs:

- `rowIndexToStyleIndex` / `columnIndexToStyleIndex` —
  `HashMap<Integer, Integer>` mapping row / column index to POI
  style index (row-level / column-level overrides).
- `lockedColumnIndexes` / `lockedRowIndexes` — `Set<Integer>`
  for the locked-for-editing indexes.
- `shiftedCellBorderStyles` — `ArrayList<String>` of CSS rules
  for the marching-ants border around a copy / paste selection.
- `conditionalFormattingStyles` — `HashMap<Integer, String>`
  mapping rule index to CSS body.

## Outputs

Getters return the live collection / value. Setters return `void`.

## Side effects

Every setter executes:

1. `this.field = value` — local cache assignment.
2. `spreadsheet.getElement().setProperty("fieldName",
   Serializer.serialize(value))` — mirror the value to the client.
   Primitive shapes go through `setProperty` directly; collections
   go through `Serializer.serialize` (Jackson NON_DEFAULT
   inclusion).

## Invariants

- **`cellStyleToCSSStyle` is intentionally NOT here.** That field
  is `protected` on `Spreadsheet` and would break downstream
  subclasses if moved. The other six fields were uniformly
  `private` before the extraction, so moving them was safe.
- **Setter contract = mirror + cache.** A caller that wants to
  push a new style index without mirroring has no API to do so —
  every write is both. This is load-bearing for the
  "server-cached value is what the client sees" invariant.
- **`Serializer` is the wire-format owner.** Any change to its
  Jackson config (NON_DEFAULT, etc.) affects every value mirrored
  through this class. Pinned by `DtoWireFormatTest` indirectly.

## Open questions

- All six fields could be generalized via
  `MirroredProperty<HashMap<Integer, Integer>>` /
  `MirroredProperty<Set<Integer>>` once the generic helper
  supports the per-type serialization. The current direct setters
  are a deliberate deferral — the journal entry for slice 88 names
  this.
