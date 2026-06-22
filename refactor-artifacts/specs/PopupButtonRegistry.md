# Block: PopupButtonRegistry

> Phase 3 spec. Drafted from the source (`PopupButtonRegistry.java`)
> and the characterization tests (`PopupButtonRegistryTest.java`, 10
> tests, slice 49). **Status:** initial draft, not yet reviewed by a
> human operator.

## Purpose

The cell-to-button storage layer of the popup-button family. Pure
storage — no DOM attachment, no JS bridge, no event firing. Owns a
single `Map<CellReference, PopupButton>` keyed by **absolute**
(sheet-qualified) cell references so the entries survive
sheet-name changes.

Sibling helpers: [`PopupButtonBinding`](PopupButtonBinding.md) (DOM
attach / detach + JS bridge), [`PopupButtonRefresher`](PopupButtonRefresher.md)
(re-check visibility post-scroll / sheet-change),
[`PopupButtonRowShifter`](PopupButtonRowShifter.md) (re-key the
registry after row-shift).

## Inputs

### `get(CellReference absoluteCellReference)`

- Absolute cell reference (sheet-qualified). Returns the registered
  button or `null`.

### `put(CellReference absoluteCellReference, PopupButton button)`

- Stores the button under the key. Overwrites silently.

### `remove(CellReference absoluteCellReference)`

- Removes and returns the previous entry, or `null` if absent.

### `values()`

- Returns the live collection — NOT a defensive copy. Callers that
  iterate while mutating must copy first.

### `replaceAll(Map<CellReference, PopupButton> updated)`

- Atomically replaces the entire backing map. Used by
  `PopupButtonRowShifter` for en-masse row remapping.

### `isEmpty()` / `clear()`

- Standard helpers.

## Outputs

`get` returns `PopupButton` or `null`. `remove` same. `values()`
returns a `Collection<PopupButton>`. Other methods return `void` or
`boolean`.

## Side effects

None outside the internal map mutation. No DOM, no events, no JS.

## Invariants

- **Cell references stored here are ABSOLUTE.** Callers must
  convert relative references via
  `SpreadsheetUtil.relativeToAbsolute` before calling. A regression
  that stored a relative reference would silently lose the entry
  on the next sheet rename.
- **`values()` returns a LIVE collection, not a copy.** This is
  the "live-reference-with-comment" pattern named in slice 67
  (TableRegistry has the same shape). Caller-side iteration during
  mutation is unsafe; the tests for `PopupButtonRowShifter` and
  `PopupButtonRefresher` both wrap the iteration in
  `new ArrayList<>(registry.values())`.
- **`replaceAll` swaps the underlying map reference.** Not just
  `clear + putAll` — the new map becomes the field. This is
  load-bearing for the row-shifter's atomic replacement contract.

## Open questions

- The live-collection contract is unenforceable by Java's type
  system — a reader can call `registry.values().add(...)` and
  observe the registry mutate. The risk is documentary; a future
  refactor could return `Collections.unmodifiableCollection(...)`
  if the row-shifter's mutation-during-iteration pattern can be
  reworked.
