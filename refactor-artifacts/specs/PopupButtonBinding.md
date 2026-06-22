# Block: PopupButtonBinding

> Phase 3 spec. Drafted from the source (`PopupButtonBinding.java`)
> and the characterization tests (`PopupButtonBindingTest.java`, 9
> tests, slice 64). **Status:** initial draft, not yet reviewed by
> a human operator.

## Purpose

Wire a `PopupButton` to the host Spreadsheet's DOM and JS bridge.
Two responsibilities:

1. **Virtual-child attachment**. Appends the button's element as a
   Flow virtual child of the spreadsheet so it can be addressed
   from the client.
2. **JS bridge invocation**. Calls
   `getElement().callJsFunction("addPopupButton" / "removePopupButton",
   serializedState)` so the GWT widget knows about the button.

Tracks the set of currently-attached buttons in a `HashSet` so
`detachAll()` (called on sheet-change cleanup) can iterate and
detach all of them without scanning the registry.

Sibling helpers: [`PopupButtonRegistry`](PopupButtonRegistry.md),
[`PopupButtonRefresher`](PopupButtonRefresher.md),
[`PopupButtonRowShifter`](PopupButtonRowShifter.md).

## Inputs

### `attach(PopupButton button)`

- Adds the button to `attachedButtons`. ALWAYS calls
  `addPopupButton` JS function (even on idempotent re-attach —
  the GWT widget tolerates double-registration). Appends as a
  virtual child only if the button's element isn't already a
  child of the spreadsheet (parent-guard).

### `detach(PopupButton button)`

- Removes from `attachedButtons`. ALWAYS calls `removePopupButton`.
  Removes the virtual-child link only if it currently exists
  (parent-guard).

### `detachAll()`

- Empty-set short-circuit returns immediately. Otherwise iterates
  a snapshot of the attached buttons and detaches each, then
  clears the set.

## Outputs

All methods return `void`. Side effects on the element + JS
bridge + the internal tracking set.

## Side effects

- Per `attach`: JS function call `addPopupButton` with serialized
  state; possible virtual-child append.
- Per `detach`: JS function call `removePopupButton` with
  serialized state; possible virtual-child remove.
- `Serializer.serialize(button.getState())` is the wire bytes — the
  state is a `PopupButtonState` POJO whose JSON shape is pinned by
  `DtoWireFormatTest` (slice 106).

## Invariants

- **Both JS function calls fire unconditionally**. The
  parent-guard only controls the DOM-side virtual-child link, NOT
  the JS bridge. A regression that moved the JS call inside the
  parent guard would silently no-op when the button is being
  re-attached after a parent change.
- **Iteration is over a snapshot**. `detachAll` uses
  `new ArrayList<>(attachedButtons)` to avoid concurrent-
  modification on the set during the per-button `detach` call
  that itself removes from the set.
- **The attached-set is local state, NOT the registry**. Multiple
  buttons can be in the registry but not yet attached (e.g.
  buttons on a not-yet-loaded sheet). Conversely, every attached
  button is also in the registry. `detachAll` only touches the
  attached subset.

## Open questions

- The parent-guard `spreadsheet.getElement().equals(button
  .getElement().getParent())` — if the GWT widget ever
  reparents the button to a sub-element (e.g. a portal /
  modal), this guard would silently leak the attach. Not a
  current concern; flagged for future Lit-rewrite work.
