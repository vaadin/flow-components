# Block: PopupButtonRowShifter

> Phase 3 spec. Drafted from the source (`PopupButtonRowShifter.java`)
> and the characterization tests (`PopupButtonRowShifterTest.java`, 9
> tests, slice 65). **Status:** initial draft, not yet reviewed by a
> human operator.

## Purpose

Remap pop-up-button cell references when rows are inserted, removed,
or shifted on the active sheet. Three actions per button:
**detach** (its row index is now stale), **classify** (was the row
removed, shifted up, or untouched), and **rebuild** (compute a new
cell reference and put it back in the registry under the new key).

The full registry's map is replaced atomically via
`registry.replaceAll(updated)` at the end of the shift.

Sibling helpers: [`PopupButtonRegistry`](PopupButtonRegistry.md),
[`PopupButtonBinding`](PopupButtonBinding.md),
[`PopupButtonRefresher`](PopupButtonRefresher.md).

## Inputs

### `shift(int first, int last, int n)`

- `first`, `last` — bounds of the affected row range (inclusive,
  in POI conventions).
- `n` — row offset to apply. Positive = rows inserted (buttons
  shift down); negative = rows removed (buttons either drop OR
  shift up).
- Empty-registry short-circuit returns immediately.

## Outputs

`void`. Side effects on the registry (via `replaceAll`), on the
binding (detaches every existing button), and on each button's
`setCellReference(...)`.

## Side effects

For each registered button:

1. Read its current `CellReference`.
2. `binding.detach(button)` — the DOM / JS state is now stale, so
   it's torn down.
3. Classify:
   - `rowWasRemoved` (n < 0 && row in [first, first-n)): drop the
     button (don't add to `updated`).
   - `numberOfRowsAboveWasChanged` (row >= first && row > last):
     shift the cell reference by `n` rows and add to `updated`.
   - Otherwise: keep the cell reference unchanged and add to
     `updated`.
4. After the loop: `registry.replaceAll(updated)` — atomic swap.

## Invariants

- **Detach happens BEFORE classification.** Even buttons that will
  end up at the same cell reference are detached and not re-
  attached here; the next `PopupButtonRefresher.refresh()` is
  expected to re-attach the visible ones. This is the
  "tear-down-then-rebuild" pattern.
- **`rowWasRemoved` is a half-open range**: `n < 0 && row >=
  first && row < first - n`. The inclusive lower bound + exclusive
  upper bound is load-bearing — slice 65's tests pin both
  boundaries.
- **`numberOfRowsAboveWasChanged` is `row >= first && row > last`**:
  the "above" of the affected range is what shifts. A button at
  `row == last` does NOT shift (it's part of the unchanged
  prefix). Counter-intuitive boundary; pinned by test.
- **The new CellReference is built with absolute flags `(true,
  true)`** — sheet-qualified, sheet-name-stable. Same convention
  as `PopupButtonRegistry`'s storage contract.
- **`replaceAll` is atomic**. The whole map swaps once at the end;
  no partial-update state is ever observable. A reader between
  `detach(button)` and the final `replaceAll` would see the OLD
  registry (the detach didn't remove it from the registry — only
  from the binding's attached set).

## Open questions

- The "detach-then-don't-reattach" pattern relies on
  `PopupButtonRefresher` running soon after. If a code path
  shifts rows but doesn't trigger a refresh, buttons that should
  be visible would silently not be. A test that pins the
  RowShiftOrchestrator's full sequence (shift → refresh) would
  formalize this dependency; slice 97's `RowShiftOrchestrator`
  spec captures the orchestration but doesn't cross-reference
  this shifter's expectation.
- `rowWasRemoved` and `numberOfRowsAboveWasChanged` are static
  predicates inside this class — they could be promoted to a
  small `RowShiftClassifier` value object if the same arithmetic
  ever appears outside this file.
