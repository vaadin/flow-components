# Block: PopupButtonRefresher

> Phase 3 spec. Drafted from the source (`PopupButtonRefresher.java`)
> and the characterization tests (`PopupButtonRefresherTest.java`, 6
> tests, slice 63). **Status:** initial draft, not yet reviewed by
> a human operator.

## Purpose

Re-run the visibility check on every popup button registered for
the active sheet and attach / detach each based on whether its
anchored cell is currently visible. Called after scroll, sheet
change, or any operation that may have shifted which cells are
rendered.

Pure dispatcher: reads from `PopupButtonRegistry`, asks the
spreadsheet "is this cell visible right now?", and forwards
attach / detach decisions to
[`PopupButtonBinding`](PopupButtonBinding.md).

Sibling helpers: [`PopupButtonRegistry`](PopupButtonRegistry.md),
[`PopupButtonBinding`](PopupButtonBinding.md),
[`PopupButtonRowShifter`](PopupButtonRowShifter.md).

## Inputs

### `refresh()`

- No arguments. Iterates `registry.values()`:
  1. **Sheet filter**: `if (!activeSheetName.equals(button.getCellReference().getSheetName())) continue;`
     — buttons on inactive sheets are skipped silently.
  2. **Visibility check**: `spreadsheet.isCellVisible(row, column)` (1-based row/column).
  3. **Dispatch**: visible → `binding.attach(button)`; not visible
     → `binding.detach(button)`.

## Outputs

`void`. Side effects on the binding's attached-set + the JS
bridge + the DOM virtual-child links.

## Side effects

For each button on the active sheet, exactly one of
`binding.attach(...)` or `binding.detach(...)` fires. The binding
itself handles the no-op case when the desired state already
holds (it always issues the JS bridge call but skips the
virtual-child mutation when the parent matches).

## Invariants

- **Sheet-name comparison gates the iteration**. Buttons on
  inactive sheets are NEVER attached / detached by this refresher
  — they stay in their previous state. This is load-bearing
  because `PopupButtonBinding` is per-spreadsheet, not
  per-sheet: detaching a button while still on its home sheet
  would lose its DOM presence even though the user is on a
  different tab.
- **1-based row/column for `isCellVisible`**. The binding uses
  0-based references throughout; the refresher adjusts by `+1`
  before calling `isCellVisible`. A regression that dropped the
  `+1` would silently misalign by one cell.

## Open questions

- The refresher visits EVERY button on every call — `O(N)` per
  refresh. For sheets with hundreds of popup buttons, a future
  perf pass might track the previous-visible set and only
  process the diff.
