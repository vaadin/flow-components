# Block: SheetGrouping

> Phase 3 spec. Drafted from the source (`SheetGrouping.java`) and
> the characterization tests (`SheetGroupingTest.java`, slice 87).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Holds the row + column grouping (outline) metadata mirrored to the
client. Five fields representing the two grouping axes:

- `colGroupingData` — list of column-grouping entries (start / end
  column indices, group level).
- `colGroupingMax` / `rowGroupingMax` — deepest nesting level per
  axis; used by the client to size the grouping gutter.
- `colGroupingInversed` / `rowGroupingInversed` — collapse / expand
  control position (start = true, end = false).

Same state-with-mirror pattern as
[`SheetCssStyleMaps`](SheetCssStyleMaps.md).

## Inputs

### Per-field getter/setter pairs

- `getColGroupingData()` / `setColGroupingData(List<GroupingData>)`
  — the column-axis grouping array, mirrored as JSON.
- `setRowGroupingData(List<GroupingData>)` — **WRITE-ONLY**.
  Mirrors to the client but does NOT cache server-side. This is
  the row/column asymmetry preserved from the legacy
  `Spreadsheet.setRowGroupingData`.
- `colGroupingMax` / `rowGroupingMax` getter/setter pairs — `int`,
  mirrored directly.
- `colGroupingInversed` / `rowGroupingInversed` getter/setter
  pairs — `boolean`, mirrored directly.

## Outputs

Getters return cached values. `setRowGroupingData` has no getter
(write-only). All setters return `void`.

## Side effects

Each setter: cache (where applicable) + `element.setProperty(...)`
mirror.

## Invariants

- **The column-vs-row asymmetry is load-bearing.**
  `setColGroupingData` caches its arg into `colGroupingData`, but
  `setRowGroupingData` does NOT cache. The asymmetry is
  intentional and pinned by the slice-87 audit-fix comment in
  `SheetGroupingTest`. A regression that added row-side caching
  would change the observable behavior (a server-side `getter`
  would suddenly exist).
- **Inversed flags default to `false`**. The collapse / expand
  control sits at the end of each range by default. Excel's
  default is also end-of-range; a regression that flipped the
  default would invert every existing grouping render.

## Open questions

- The row-grouping-write-only asymmetry could be eliminated by
  adding the field + getter — would the GWT client tolerate a
  server-side query of the row-grouping array? The journal
  entry for slice 87 names this as deferred; no concrete blocker.
- `colGroupingData` is a `List<GroupingData>`. The DTO wire
  format is pinned by `DtoWireFormatTest` (slice 106).
