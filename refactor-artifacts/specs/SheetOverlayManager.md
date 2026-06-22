# Block: SheetOverlayManager

> Phase 3 spec. Drafted from the source (`SheetOverlayManager.java`)
> and the characterization tests (`SheetOverlayManagerTest.java`, 14
> tests, slice 55). **Status:** initial draft, not yet reviewed by a
> human operator.

## Purpose

Orchestrate loading + refreshing the active sheet's overlays
(images, charts, embedded components placed on cells) using three
specialized siblings:

- `SheetOverlayStorage` — the wrapper + attached-component
  collection. The lifecycle owner.
- `SheetOverlayVisibility` — "is this overlay inside the viewport?"
  decision logic. Pure function over the overlay's anchor.
- `SheetOverlayInfoBuilder` — turn a wrapper into the client-side
  `OverlayInfo` payload (the wire DTO consumed by the GWT widget).

Itself stateful for ONE thing: `reloadFromPoi` (a deferred-flush
boolean set externally after row / column resizing, consumed and
cleared on the next `loadOrUpdate`).

## Inputs

### `add(SheetOverlayWrapper overlay)`

- `overlay` — pre-constructed wrapper carrying the anchor + payload.
  Delegates to `storage.add(overlay)`. No-op if storage rejects (it
  doesn't, in practice).

### `markReloadFromPoi()`

- No arguments. Sets the deferred-flush flag. Called by any code
  that mutates POI in a way that affects overlay positioning
  (typically row/column resizing).

### `loadOrUpdate()`

- No arguments. The main entry point — called by the
  active-sheet-reload flow. Multi-branch:
  1. If storage is empty: load from POI via the static factory.
  2. If still empty after that: early return.
  3. If `reloadFromPoi` flag is set: `clear()` + reload from POI,
     then unset the flag.
  4. For each overlay in storage: dispatch to `addOverlayData` if
     visible, or `removeOverlayData` if previously-visible-now-not.
- Crucially, "previously visible" is read from
  `overlay.isVisible()` — the wrapper carries the prior-frame
  visibility state, which `loadOrUpdate` reads and updates.

### `clear()`

- Removes all overlay data + clears storage. Called both during
  `reloadFromPoi` flush and during sheet-change cleanup.

### `hasOverlays()`

- Predicate. Read by external orchestrators (e.g.,
  `RowShiftOrchestrator` consults it to decide whether to
  `markReloadFromPoi`).

## Outputs

- `hasOverlays()` returns `boolean`.
- All other public methods return `void`. Side effects are
  observable on storage state, on each `SheetOverlayWrapper`'s
  visibility flag, and on the Spreadsheet's resource map.

## Side effects

### `loadOrUpdate` (per call)

1. Possibly: `SpreadsheetFactory.loadSheetOverlays(spreadsheet)`
   (static factory) — populates storage from POI.
2. Possibly: `clear()` + reload (`reloadFromPoi` branch).
3. Per visible overlay: `spreadsheet.registerCustomComponent(...)`,
   `storage.addComponent(...)`, and `spreadsheet.setResource(...)`
   (only if a resource handler exists and isn't yet registered).
4. Per went-out-of-visibility overlay:
   `spreadsheet.unregisterCustomComponent(...)`,
   `storage.removeComponent(...)`, and `spreadsheet.removeResource(...)`.
5. `overlay.setVisible(...)` — updates the wrapper's prior-state
   flag.

## Invariants

- **`reloadFromPoi` is a single-flush flag, not a repeated trigger.**
  Set externally; cleared inside `loadOrUpdate`'s branch 3 after
  the reload runs. Repeated `markReloadFromPoi` calls between
  `loadOrUpdate`s coalesce to one reload (idempotent on the
  set side).
- **Empty storage + non-empty POI path is mandatory** — without
  the branch-1 lazy load, freshly-loaded sheets would have no
  overlays until the first POI mutation. The "kludge" comment in
  the source (lines 59-61) names this: it's a workaround for
  freeze-row / freeze-column interactions that drop overlays.
- **Visibility transitions are observable on `overlay.isVisible()`**
  — the wrapper is the source of truth for "did this overlay's
  visibility change since the last `loadOrUpdate`". This is why
  `loadOrUpdate` reads `overlay.isVisible()` for the OLD value
  before writing the NEW one.
- **Resource registration is idempotent.** The
  `!spreadsheet.getResources().containsKey(overlay.getId())` guard
  prevents double-registering — important because the same
  overlay may be visible across multiple `loadOrUpdate` rounds.
- **`OverlayChangeListener` callback re-triggers `loadOrUpdate`**.
  Slice 55's audit-fix-pass added a test pinning this:
  registered via storage; when fired (e.g. when the wrapped POI
  drawing's anchor changes), it invokes
  `loadOrUpdate()` on the owning manager. Without this callback,
  overlays whose anchor moves mid-session would not re-render.

## Open questions

- The `loadOrUpdate` "kludge" comment names a freeze-row/column
  bug that hasn't been root-caused. The current fix is "reload
  from POI on any empty-storage call" — but if POI's overlay
  collection is itself wrong post-freeze, the reload would
  reinstall the same wrong data. A future investigation would
  trace whether POI's drawing state is consistent across freeze
  operations.
- The `addOverlayData` / `removeOverlayData` pair is asymmetric —
  add registers a resource if missing, but remove unregisters
  unconditionally. If two overlays share a resource (rare but
  possible), the second remove would no-op gracefully but the
  first would unregister a still-needed resource. Worth a test
  in the audit-fix pile.
