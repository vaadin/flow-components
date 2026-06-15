# Block: MergedRegionOperations

> Ninth Phase 3 spec. Closes the merged-region trio's Phase 3 documentation
> alongside `MergedRegionLookup.md` (slice 44) and
> `MergedRegionCascade.md` (slice 46). Pairs with slice 45's tests
> (`MergedRegionOperationsTest`, 8 tests, every invariant in this spec
> pinned).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

The single-region add/remove driver. Owns validation, ordering, and the
POI write; delegates the cell-level side effects to `MergedRegionCascade`,
the cache-state to the parent's `setMergedRegions`, and the selection-side
notification to `CellSelectionManager`.

Two entry points:

- `addRegion(range)` — merges a `CellRangeAddress` into a new region.
  Validates against existing regions; discards inside-regions; rejects
  overlap or enclose.
- `removeRegion(index)` — removes a region by POI index. Mirrors the
  inverse cascade.

There's a third method, `addRegionToPoiAndCache(range)`, which is the
build-region-from-range half of `addRegion` and is also used by
`MergedRegionSynchronizer` as a fallback when its bulk-rebuild detects a
POI region missing from the cache.

## Inputs

### Constructor

- `spreadsheet` — required, non-null. Provides the active sheet, the
  selection manager, the cached merged-region list (via stateful
  `getMergedRegions` / `setMergedRegions`), the `mergedRegionCounter`
  field for sequential id mint, and the `updateMarkedCells` post-mutation
  flush.
- `cascade` — required, non-null. The `MergedRegionCascade` for side
  effects.

### `addRegion(CellRangeAddress range)`

- `range` — required. POI's range type, 0-based. Converted to 1-based
  when building the `MergedRegion` DTO.

### `removeRegion(int index)`

- `index` — the integer index of the region to remove. Used BOTH as the
  argument to `sheet.removeMergedRegion(index)` AND as the index passed
  to `ArrayList.remove(index)` on the cached list. The caller is
  responsible for ensuring the POI list and cache list are aligned.

### `addRegionToPoiAndCache(CellRangeAddress range)`

- `range` — same as `addRegion`. Used by the synchronizer when only the
  build half is needed (validation is unnecessary because the
  synchronizer already knows POI is the source of truth).

## Outputs

All methods: `void`. Side effects only.

## Side effects

### `addRegion` flow

1. Walk `sheet.getNumMergedRegions()` and compute `CellRangeUtil.intersect`
   between the candidate range and each existing region.
2. If `INSIDE` (an existing region lies wholly inside the candidate):
   call `removeRegionFromPoiAndCache(index)`. Re-read `getNumMergedRegions`
   on the next loop iteration (the count shrinks).
3. If `OVERLAP` or `ENCLOSES`: throw
   `IllegalArgumentException("An existing region <X> overlaps|encloses
   the given region <Y>")`. **The exception thrown before any POI write
   means partial state cannot exist**, but inside-region removals from
   earlier loop iterations are NOT rolled back.
4. Otherwise: increment loop index, continue.
5. After validation: `addRegionToPoiAndCache(range)` →
   `selectionManager.mergedRegionAdded(range)`.

### `addRegionToPoiAndCache` flow

1. `sheet.addMergedRegion(range)` — POI write.
2. Build a fresh `MergedRegion` DTO with 1-based coordinates.
3. Mint the id via `spreadsheet.mergedRegionCounter++` (post-increment).
4. Build a NEW `ArrayList<MergedRegion>` from the existing cache
   (defensive copy — see invariants), append the new region, call
   `spreadsheet.setMergedRegions(updated)`.
5. `cascade.afterMerge(region)`.
6. `spreadsheet.updateMarkedCells()`.

### `removeRegion` flow

1. `sheet.getMergedRegion(index)` — capture the range BEFORE mutation.
2. `removeRegionFromPoiAndCache(index)`:
   - `sheet.removeMergedRegion(index)` — POI write.
   - Build a fresh `ArrayList` from the existing cache, remove at
     `index`, call `setMergedRegions(updated)`.
   - `cascade.afterUnmerge(removed)`.
3. `spreadsheet.updateMarkedCells()`.
4. `selectionManager.mergedRegionRemoved(capturedRange)`.

## Invariants

- **Defensive new-ArrayList copy on every cache write.** Both
  `addRegionToPoiAndCache` and `removeRegionFromPoiAndCache` build a new
  `ArrayList<MergedRegion>` from the existing cache via the copy
  constructor, mutate the copy, and pass the new list to
  `setMergedRegions`. The prior list is never mutated in place. Load-
  bearing because the cache is a `MirroredProperty.serialized` field —
  in-place mutations would skip the client mirror. Pinned by
  `addRegion_appendsToExistingCache_withoutMutatingPriorList`.
- **Sequential id mint via direct field access.** `mergedRegionCounter`
  is read and written via `spreadsheet.mergedRegionCounter++` (direct
  field, no getter). Each call to `addRegionToPoiAndCache` assigns one
  id and increments the counter once. Even on a Mockito mock the field
  state persists across calls (Mockito mocks are real subclass
  instances; fields default to 0 and direct accesses bypass the
  intercept layer).
- **Validation happens before POI write.** If `addRegion` throws, no
  call to `sheet.addMergedRegion` has happened yet. But earlier
  inside-region removals during the same call DO happen first; they're
  not rolled back.
- **`updateMarkedCells` fires exactly once per `addRegion` call** —
  inside `addRegionToPoiAndCache`. Not also in the outer `addRegion`.
- **`updateMarkedCells` fires exactly once per `removeRegion` call** —
  in the outer `removeRegion` AFTER `removeRegionFromPoiAndCache`. Not
  inside.
- **`selectionManager.mergedRegionAdded` receives the original
  `CellRangeAddress`** — not the `MergedRegion` DTO. Same for
  `mergedRegionRemoved`. Asymmetric with the cascade's `afterMerge` /
  `afterUnmerge`, which receive the DTO.

## Edge cases

- **Empty sheet** → `addRegion` walks zero existing regions, jumps
  straight to `addRegionToPoiAndCache`.
- **Single 1x1 candidate range** → no special handling at this layer;
  the cascade has the 1x1 edge case pinned (slice 43).
- **Candidate range equals an existing region exactly** →
  `CellRangeUtil.intersect` returns `INSIDE` for "equal" (POI's
  intersect considers identical ranges as one being inside the other).
  The existing region is discarded, then re-added. Probably not the
  intent, but the behavior is consistent.
- **Multiple existing regions inside the candidate** → all are
  discarded, in iteration order. The loop's re-read of
  `getNumMergedRegions` after each discard means the index doesn't
  skip entries.
- **Null cache (first add)** → `getMergedRegions()` returns null. The
  null guard in `addRegionToPoiAndCache` constructs a fresh ArrayList
  rather than trying to copy from null.

## Error modes

- **`IllegalArgumentException` for OVERLAP / ENCLOSES** — only error
  thrown directly by this class. Message includes the existing region
  and the candidate.
- **`spreadsheet.getActiveSheet() == null`** → NPE on the first POI
  method call. Not swallowed.
- **`cascade == null` or its callbacks throw** → NPE / propagated. No
  rollback of the POI write.
- **`removeRegion(index)` with `index >= getNumMergedRegions()`** →
  `sheet.getMergedRegion(index)` throws `IndexOutOfBoundsException`.
  The cache list is NOT mutated.
- **`removeRegion(index)` with cache list shorter than POI list** →
  cache `remove(index)` throws `IndexOutOfBoundsException` AFTER
  `sheet.removeMergedRegion(index)` already ran. Partial state:
  POI is updated, cache is not, cascade did not fire. Recovery is the
  caller's problem.

## Non-obvious quirks

- **`addRegion` validation order matters.** `INSIDE` is handled by
  removal-and-continue; `OVERLAP` / `ENCLOSES` are handled by throw.
  An existing region that's both partially-overlapping AND inside
  another existing region can't happen (POI guarantees uniqueness),
  but the loop's re-read of count after each discard makes the
  ordering explicit.
- **The `MergedRegion.id` field is monotonically increasing across the
  lifetime of the Spreadsheet instance.** Removed-then-re-added regions
  get different ids. This matters because the client-side cache keys
  off the id; the same logical merge after an unmerge/re-merge cycle
  is a different cache entry to the client.
- **`removeRegion` index aliasing assumption.** The integer index passed
  in is used both for POI and for the cache list. POI keeps a sorted
  internal list; the cache is appended in mint-order. The two orderings
  are NOT guaranteed equal. The current code works because callers
  invoke `removeRegion` with the cache-list index, and the
  synchronizer (which knows the orderings can diverge) calls the
  lower-level `addRegionToPoiAndCache` directly rather than going
  through this driver. **Worth pinning more explicitly** — see "Human
  review".
- **The `selectionManager` callbacks receive the POI range, not the DTO.**
  The selection-manager uses the range to shift selection boundaries;
  it doesn't care about the cache id.
- **`addRegionToPoiAndCache` is package-private and used by the
  synchronizer**, not just by `addRegion`. Renaming or changing the
  signature breaks the synchronizer too.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/MergedRegionOperations.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/MergedRegionOperationsTest.java` (8 tests; every invariant in this spec maps to at least one assertion)
- Slice that introduced it: slice 2 (the merged-region decomposition)
- Companion blocks: `MergedRegionLookup` (read-only query side),
  `MergedRegionCascade` (cell-level cascade — the primary downstream),
  `MergedRegionSynchronizer` (bulk-rebuild fallback — re-uses
  `addRegionToPoiAndCache`), `Spreadsheet.addMergedRegion` /
  `removeMergedRegion` (the thin public-API delegators that route
  through this driver)

## Contract status

Every invariant in this spec maps to at least one passing test in
`MergedRegionOperationsTest.java`. The merged-region trio
(`MergedRegionLookup`, `MergedRegionCascade`, `MergedRegionOperations`)
is now **fully tested AND fully specced** — Phase 2 + Phase 3 both green
for all three members.

## What the spec doesn't cover

- Performance characteristics. `addRegion` is O(existing regions) for
  validation; `removeRegion` is O(1) for the actual remove (POI does
  its own bookkeeping). Both build a fresh ArrayList (O(N) copy on
  every call). Not relevant for typical merge counts.
- Thread safety. Relies on Vaadin Flow's request-thread serialization
  for the parent's `mergedRegionCounter` and the cache getter/setter.
- The synchronizer's interaction with this driver. The synchronizer
  has its own bulk-rebuild contract; this spec only documents the
  add/remove driver's behavior.

## Human review

**Status: not yet reviewed.** Per pipeline §13.2, this draft needs a domain
expert to verify:

1. **`removeRegion` index aliasing.** Is the assumption "POI index ==
   cache index" stated anywhere in the wider system, or is it implicit?
   If the synchronizer ever swaps the cache list to a different
   ordering, `removeRegion` callers would silently remove the wrong
   region. Worth pinning explicitly OR refactoring `removeRegion` to
   take a region reference instead.
2. **Partial-state recovery when an inside-region discard happens but
   a later `OVERLAP` check throws.** The existing region was already
   removed by the time the exception fires. Should the driver
   accumulate the discards in a pending list and only apply them after
   validation succeeds? Currently the partial state is observable.
3. **Monotonic `mergedRegionCounter` across removes.** Should the
   counter rewind when a region is removed? Currently it doesn't —
   the next add always gets a fresh id. This is probably correct
   (id stability for the client cache) but worth confirming the
   intent vs. an accidental behavior.
