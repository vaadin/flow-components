# Block: MergedRegionSynchronizer

> Phase 3 spec. Drafted from the source
> (`MergedRegionSynchronizer.java`) and the characterization tests
> (`MergedRegionSynchronizerTest.java`, 7 tests, slice 48).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Bulk-rebuild the in-memory `MergedRegion` cache list from the
active sheet's POI state. Called whenever a structural operation
(row delete, sheet load, etc.) may have changed POI's merged
regions WITHOUT going through
[`MergedRegionOperations`](MergedRegionOperations.md), leaving
the cache stale.

Falls back to `MergedRegionOperations.addRegionToPoiAndCache` for
regions that exist in POI but have no cache entry yet — that
path re-applies the merge cascade so the new region's cells get
the right styling.

Sibling helpers: [`MergedRegionLookup`](MergedRegionLookup.md),
[`MergedRegionCascade`](MergedRegionCascade.md),
[`MergedRegionOperations`](MergedRegionOperations.md).

## Inputs

### `rebuildCacheFromActiveSheet()`

- No arguments. Walks POI's merged regions on the active sheet
  and reconciles against the cache.

## Outputs

`void`. Side effects on the merged-regions cache list.

## Side effects

- Drops cache entries whose `CellRangeAddress` no longer
  matches any POI region.
- Adds cache entries for POI regions without a cache match,
  routing through `MergedRegionOperations.addRegionToPoiAndCache`
  for the styling cascade.

## Invariants

- **Cache is reconciled to POI's truth, not the other way
  around**. POI is the source; the cache lags and the
  synchronizer brings it in sync. A regression that tried to
  push the cache back into POI would corrupt the workbook.
- **Add-path goes through MergedRegionOperations**, not directly
  through the cache list. The cascade (styling, marked-cells)
  is the load-bearing extra work.
- **Drop-path is direct cache mutation**. Removed regions just
  disappear; no cascade.

## Open questions

- The synchronizer is called from "any structural operation"
  paths; a future audit could enumerate the exact call sites and
  consolidate them (e.g. always-run-this-after-row-shift).
  Currently the call sites are scattered; the test pins the
  rebuild's correctness but doesn't pin "every necessary caller
  invokes it."
