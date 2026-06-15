# Block: SheetOverlayStorage

> Eleventh Phase 3 spec. First spec for the slice-4 overlay family.
> Pairs with slice 51's tests (`SheetOverlayStorageTest`, 12 tests, every
> invariant pinned). Captures a **dead-state observation** worth a
> follow-up cleanup (see "Non-obvious quirks").
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Pure storage class for the active sheet's overlays — image/picture
wrappers (POI's `Picture` / `XSSFShape` family) and the Vaadin Flow
`Component` instances that are currently attached to render them.

Owns two parallel collections with **different lifecycles**:

- `overlays` — `HashSet<SheetOverlayWrapper>`, lazy-initialized via
  `initialize()`. Before `initialize`, the field is `null` and any
  write/read except `isInitialized` and `isEmpty` throws NPE. After
  `initialize`, the set is a fresh empty `HashSet`. Subsequent
  `initialize` calls replace it with a new empty set (discarding any
  prior contents).
- `overlayComponents` — `HashSet<Component>`, eagerly initialized at
  construction. Has only `addComponent` / `removeComponent` — no
  getter, no iteration, no clear. The set is **write-only from outside
  this class** (see "Non-obvious quirks").

No POI access, no DOM attachment, no visibility logic — those live on
`SheetOverlayInfoBuilder`, `SheetOverlayVisibility`, and
`SheetOverlayManager`.

## Inputs

### Constructor

- No arguments. Fields default to: `overlays = null`,
  `overlayComponents = new HashSet<>()`.

### `initialize()`

- No arguments. Sets `overlays = new HashSet<>()`. Idempotent in the
  sense that calling it again replaces the set with a fresh empty one
  (prior overlays are discarded).

### `add(SheetOverlayWrapper overlay)`

- `overlay` — required, non-null. Added to the `overlays` set.
  Requires `initialize` to have been called.

### `addComponent(Component component)` / `removeComponent(Component component)`

- `component` — required, non-null. Add/remove on the
  `overlayComponents` set. Does NOT require `initialize`.

## Outputs

- `isInitialized()` → `true` iff `initialize` has been called.
- `isEmpty()` → `true` if `overlays == null` OR `overlays.isEmpty()`.
  The pre-initialize state is conceptually empty.
- `all()` → the `overlays` set directly (no defensive copy). Returns
  `null` pre-initialize.
- `add` / `clearOverlays` / `addComponent` / `removeComponent` →
  `void`.

## Side effects

None outside the two backing sets. Pure storage.

## Invariants

- **Pre-initialize `isEmpty()` returns `true`, `isInitialized()`
  returns `false`.** The two queries report different perspectives on
  the same state: "conceptually empty" vs "backing set not yet
  allocated".
- **`all()` returns the live set reference, not a copy.** Mutating the
  returned collection mutates the storage's backing set. Pinned
  implicitly by the existing tests; callers are expected to treat the
  reference as read-only or use the storage's own `add` /
  `clearOverlays` methods.
- **`initialize` is destructive.** Calling it on an already-initialized
  storage drops the prior overlays. Used by Spreadsheet's workbook
  reload path.
- **`clearOverlays` preserves the initialized state.** It empties the
  set in place; `isInitialized()` stays `true` after a clear. Matches
  the semantic "the workbook is loaded but has no overlays."
- **`overlayComponents` and `overlays` have independent lifecycles.**
  Components can be added/removed before `initialize` is ever called;
  overlays cannot.

## Edge cases

- **Adding the same overlay twice** → HashSet semantic; second add is
  a no-op.
- **Adding the same component twice** → HashSet semantic; second add
  is a no-op.
- **`clearOverlays` on an empty initialized set** → no-op.
- **Removing a component that was never added** → no-op (HashSet.remove).
- **`all()` iterated while modifying** — undefined; throws
  `ConcurrentModificationException` per HashSet's fail-fast iterator.
  Same caveat as any HashSet.

## Error modes

- **`add` pre-initialize** → NPE on `overlays.add(...)`. Not swallowed.
- **`all()` iterated pre-initialize** → NPE on null dereference. The
  method itself doesn't throw — it returns null cleanly — but
  iterating the null reference does.
- **`clearOverlays` pre-initialize** → NPE on `overlays.clear()`. Not
  swallowed.
- **Constructor never throws.** Both fields are initialized to
  well-defined states (`null` and `new HashSet<>()` respectively).

## Non-obvious quirks

- **`overlayComponents` is write-only from outside this class.** The
  set has no getter, no iteration helper, no clear method. Searching
  `src/main` reveals only `SheetOverlayManager` calls `addComponent`
  (line 102, when an overlay attaches) and `removeComponent` (line
  140, when one detaches). Nothing reads the set back. From the
  storage's API alone, the set is dead state — written but never
  observed. **Pinned in slice 51's
  `overlayComponents_isWriteOnly` test as a future-cleanup marker**;
  either the field needs a getter so callers can iterate the attached
  components (current Spreadsheet implementation may want to detach
  them all on workbook reload), or the field should be removed
  entirely. Plausible historical reason: a previous Spreadsheet version
  used the set to drive component detachment on reload, but the
  detach path moved elsewhere and the field stayed orphaned.
- **The two collections' asymmetric lazy-init.** `overlays` is null
  pre-initialize because Spreadsheet's pre-workbook-load state has
  no sheet (and thus no overlays). `overlayComponents` is non-null
  because components can be tracked independently of which workbook
  is loaded. Different lifecycles, deliberate by design.
- **`all()` returning the live reference is a performance choice.**
  The storage is the source of truth; callers that need a stable
  snapshot must copy themselves. The decomposition didn't introduce a
  defensive copy because the historical god-class code path also
  exposed the live field directly.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/SheetOverlayStorage.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/SheetOverlayStorageTest.java` (12 tests; every invariant in this spec maps to at least one assertion)
- Slice that introduced it: slice 4 (the sheet-overlays decomposition)
- Companion blocks: `SheetOverlayVisibility` (z-order + visibility
  rules), `SheetOverlayInfoBuilder` (POI-DTO translation),
  `SheetOverlayManager` (lifecycle orchestrator — the primary caller
  of this storage), `SheetOverlayWrapper` (the value type)

## Contract status

Every invariant in this spec maps to at least one passing test in
`SheetOverlayStorageTest.java`. The slice-4 overlay family now has one
member fully Phase 2 + Phase 3 covered; the other three
(`SheetOverlayVisibility`, `SheetOverlayInfoBuilder`,
`SheetOverlayManager`) are still IT-only.

## What the spec doesn't cover

- Performance characteristics. O(1) per operation (HashSet);
  not relevant for typical overlay counts.
- Thread safety. Stateful (two mutable collections); not thread-safe.
  Relies on Vaadin Flow's request-thread serialization for safe access.
- Serializable behavior. The block is `Serializable`; both fields
  are serializable types. The `overlays` field starts null and is
  re-initialized on the receiving side via the calling Spreadsheet's
  workbook-load path.

## Human review

**Status: not yet reviewed.** Per pipeline §13.2, this draft needs a domain
expert to verify:

1. **Is the dead-state `overlayComponents` set actually dead?** Worth
   confirming nothing reads it via reflection or in a non-source
   location (e.g. generated code, dynamic JS bridge). If genuinely
   dead, the field + the `addComponent` / `removeComponent` methods
   should be removed. If not, this spec should be updated to document
   the consumer.
2. **Should `add` / `clearOverlays` lazy-init `overlays`?** The
   current NPE pre-initialize is load-bearing in the sense that it
   surfaces usage errors immediately, but it's also brittle. A
   lazy-init `if (overlays == null) overlays = new HashSet<>();` at
   the top of `add` would be friendlier without changing observable
   semantics for normal callers. Decide based on whether the explicit
   initialize-on-workbook-load is a contract worth enforcing.
3. **Should `all()` return an unmodifiable view or a copy instead of
   the live reference?** Today callers can mutate the storage by
   mutating the returned collection — a footgun. The performance
   cost of `Collections.unmodifiableSet(overlays)` is negligible.
