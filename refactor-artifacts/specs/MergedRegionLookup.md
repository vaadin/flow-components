# Block: MergedRegionLookup

> Seventh Phase 3 spec. Pairs with slice 42's tests
> (`MergedRegionLookupTest`, 12 tests, every invariant pinned). The first
> spec for a block that implements an existing client-side interface
> (`MergedRegionUtil.MergedRegionContainer`) — the contract is part of an
> existing API, not a new internal one. Drafted from
> `MergedRegionLookup.java` (63 LOC) + `MergedRegionUtil.MergedRegionContainer`
> (the interface) + the slice-2 journal entry that introduced the
> decomposition.
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Read-only queries over the live list of merged regions on the active sheet.
Implements `MergedRegionUtil.MergedRegionContainer` — a frozen
client-side interface used by `MergedRegionUtil.findIncreasingSelection`
to walk the edges of a user selection and grow it to swallow any partially
overlapped merges.

Two queries:

- **Exact-corner match** (`getMergedRegionStartingFrom`) — the cell at
  `(col, row)` must be the *top-left anchor* of the merge.
- **Containment match** (`getMergedRegion`) — the cell at `(col, row)` lies
  anywhere inside the merge's inclusive bounding rectangle.

The lookup owns no state. It holds a `Supplier<List<MergedRegion>>` and
consults it on every call — so it always sees the latest list without
holding a stale reference.

## Inputs

### Constructor

- `regionsSupplier` — required, non-null. Yields the live list of regions
  to query. May yield `null` itself (treated as "no regions"). May yield
  the same `List` instance across calls; the lookup re-reads via the
  supplier each time rather than caching the reference.

### `getMergedRegionStartingFrom(int column, int row)`

- `column`, `row` — 1-based cell coordinates. Both inclusive. No bounds
  check — out-of-range coordinates simply don't match anything.

### `getMergedRegion(int column, int row)`

- `column`, `row` — 1-based cell coordinates. Same convention.

## Outputs

Both methods: `MergedRegion` reference (from the supplier's list, returned
by identity), or `null` if no region matches.

### `getMergedRegionStartingFrom` matching rule

- Returns the first region in iteration order with `region.col1 == column`
  AND `region.row1 == row`. The match is strictly on the top-left anchor.
- A cell inside the merge but not at the anchor does NOT match — pinned
  by the `getMergedRegionStartingFrom_innerCell_returnsNull` test.

### `getMergedRegion` matching rule

- Returns the first region in iteration order with
  `col1 ≤ column ≤ col2` AND `row1 ≤ row ≤ row2`. Containment is inclusive
  on all four bounds.
- A cell at `(col2+1, row)` or `(col, row2+1)` is OUTSIDE — pinned by
  `getMergedRegion_cellJustOutside_returnsNull`.

## Side effects

None. The lookup reads from its supplier and returns; no mutations to the
supplier's list, no I/O, no element-property writes.

## Invariants

- **Live view, no caching.** The lookup does not cache the supplier's
  result between calls. Mutations to the underlying list (via the
  caller's API) are visible on the next query — pinned by
  `queries_seeMutationsOfTheBackingList`.
- **First match wins.** Both methods walk the list in iteration order and
  return the first hit. If duplicate-anchor regions or overlapping merges
  exist (degenerate but technically possible), the earlier one is
  returned. Callers that need deterministic disambiguation must order
  the list upstream.
- **Null-supplier-result is silent.** If the supplier returns `null`, the
  lookup returns `null` cleanly instead of throwing — this matters because
  the backing source may not be initialized at first query time.

## Edge cases

- **Empty list** → both methods return `null` (the loop body never runs).
- **Null list from supplier** → both methods return `null` immediately.
- **Single-cell "merge"** (`col1 == col2 && row1 == row2`) → both methods
  treat it normally: starting-from matches the single anchor cell;
  getMergedRegion matches the same single cell.
- **Overlapping merges** (a degenerate state — POI doesn't normally allow
  this, but the data type does) → first-match-wins, as pinned by
  `getMergedRegion_firstContainingRegionWins`. Nested merge: outer is
  added first, inner is added second; query for a cell inside both
  returns the outer.
- **Two merges sharing an anchor** (also degenerate) → first-match-wins for
  `getMergedRegionStartingFrom`. Pinned by
  `getMergedRegionStartingFrom_firstMatchWins`.

## Error modes

- **Null supplier passed to constructor** → NPE on the first query (the
  constructor doesn't validate). Caller responsibility.
- **Supplier throws** → propagates up. The lookup has no error handling.
- **MergedRegion field mutation between query and use** → the lookup
  returns a reference, not a copy. If a caller mutates the returned
  region's `col1`/`col2`/`row1`/`row2` fields, the lookup's next query
  sees the mutation. Not the lookup's responsibility, but worth knowing
  for callers that hold references across operations.

## Non-obvious quirks

- **Supplier indirection enables a non-Mockito test.** Because the lookup
  takes a `Supplier<List<...>>` rather than a list directly, slice-42's
  tests inject behavior with a one-line lambda and override mid-test with
  `AtomicReference::set`. No Mockito needed. Captured as a general
  lesson — supplier seams give test ergonomics for free.
- **`getMergedRegionStartingFrom` vs `getMergedRegion` is a load-bearing
  distinction.** They look similar but the caller decides which one to
  use based on whether they're asking "is this the corner of a merge"
  (e.g. for resize-handle placement) or "is this cell anywhere inside a
  merge" (e.g. for navigation / selection expansion). Pinned by the
  inner-cell-returns-null test on the starting-from method.
- **The two methods walk the list independently — there's no shared
  iteration state.** Two back-to-back calls do two independent linear
  scans. O(N) per call; N is typically small (≤ a few hundred merges on
  a normal sheet), so not worth caching.
- **No defensive copying of the returned region.** The caller gets the
  same object that's in the supplier's list. This is intentional —
  callers that need to grow a selection edit the region in place during
  the cascade, and the lookup must see those edits on subsequent calls
  within the same operation.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/MergedRegionLookup.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/MergedRegionLookupTest.java` (12 tests; every invariant in this spec maps to at least one assertion)
- Slice that introduced it: slice 2 (the merged-region decomposition that lifted the inline anonymous `MergedRegionContainer` instance in `Spreadsheet.java` lines 1075-1115 into a named class)
- Companion blocks: `MergedRegionCascade` (the post-merge/post-unmerge side-effect orchestrator, slice 43 pinned),
  `MergedRegionOperations` (the bulk add/remove driver),
  `MergedRegionSynchronizer` (the POI-state → client-state rebuilder),
  `MergedRegionUtil.findIncreasingSelection` (the primary consumer of
  this lookup — uses both methods to grow a user selection that overlaps
  partial merges)

## Contract status

Every invariant in this spec maps to at least one passing test in
`MergedRegionLookupTest.java`. The spec is the contract; the tests are the
enforcement. The merged-region trio (`MergedRegionLookup`,
`MergedRegionCascade`, `MergedRegionOperations`) now has:

| Block | Phase 2 | Phase 3 |
|---|---|---|
| MergedRegionLookup | ✓ slice 42 | ✓ slice 44 (this) |
| MergedRegionCascade | ✓ slice 43 | — |
| MergedRegionOperations | — | — |

Phase 3 for `MergedRegionCascade` and `MergedRegionOperations` is the
obvious next follow-up to close the trio's spec coverage.

## What the spec doesn't cover

- Performance characteristics. O(N) per query on a list typically ≤ few
  hundred items. Not relevant.
- Thread safety. The lookup itself has no mutable state; safe to call
  concurrently. The supplier and its list are caller's responsibility —
  the lookup makes no guarantees if a concurrent caller mutates the list
  during a query.
- Serializable behavior. The lookup is `Serializable`; the supplier
  field must serialize via its own contract. A lambda-supplier
  serializes as a `SerializedLambda` (Java 8+); a method reference to
  an instance field carries the enclosing instance.

## Human review

**Status: not yet reviewed.** Per pipeline §13.2, this draft needs a domain
expert to verify:

1. Is "first match wins" the right contract for overlapping merges, or
   should the lookup return the smallest/innermost match? Excel itself
   doesn't allow overlapping merges, but the data type permits them; the
   lookup's current behavior is implementation-defined. If callers expect
   a specific ordering (e.g. "smallest first"), pinning that in the
   contract may be more user-facing than the current "iteration order".
2. Should the lookup actively reject a null supplier in the constructor,
   or is the current "fail-on-first-call" semantic preferred? The journal
   notes the supplier is wired up from `Spreadsheet.mergedRegions` (now
   a `MirroredProperty.serialized`), so a null-supplier path in production
   shouldn't be reachable — but a sanity NPE in the constructor would
   catch a wiring bug earlier.
3. Is the inclusive-bounds convention for `getMergedRegion` consistent
   with how callers use it? The four-edge walk in
   `findIncreasingSelection` reads cells at every grid coordinate from
   the selection's top/right/bottom/left; inclusive bounds means a
   merge that touches the selection edge is considered "inside" and
   triggers selection growth. This matches Excel's behavior, but worth
   confirming against the original intent.
