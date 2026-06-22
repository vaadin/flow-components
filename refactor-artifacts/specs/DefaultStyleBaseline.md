# Block: DefaultStyleBaseline

> Phase 3 spec. Drafted from the source (`DefaultStyleBaseline.java`)
> and the characterization tests (`DefaultStyleBaselineTest.java`,
> slice 36). **Status:** initial draft, not yet reviewed by a human
> operator.

## Purpose

Cached default-font / default-alignment baseline captured from a
workbook's cell-style index 0. Held read-only after capture and
used as the comparison reference for
[`FontStyleComposer`](FontStyleComposer.md)'s "emit only if
different" optimization.

The capture method has a **double duty**:

1. Set the four baseline fields (font face, size, weight,
   color) from `style.getFont(...)`.
2. **Append the default font's CSS rules into a supplied
   StringBuilder as a side effect of the capture** — the
   rules are needed by the default style's CSS row, which is
   composed at workbook-style-reload time.

## Inputs

### `defaultFontStyle(CellStyle style, StringBuilder sb)`

- `style` — typically cell-style index 0 from the workbook.
- `sb` — the StringBuilder receiving the default style's CSS
  rules. SIDE-EFFECT output channel.
- Sets the four baseline fields and emits CSS into `sb`.

### `setDefaultTextAlign(HorizontalAlignment align)`

- Captured separately in
  [`SpreadsheetStyleFactory.reloadWorkbookStyles`](CellBorderStyleBuilder.md)
  — the alignment baseline doesn't travel with the font capture
  because alignment is per-cell-style, not per-font.

### Getter pairs

- `getDefaultFontFamily()`, `getDefaultFontSize()`,
  `getDefaultFontWeight()`, `getDefaultFontColor()`,
  `getDefaultTextAlign()`.

## Outputs

`defaultFontStyle` appends CSS into the StringBuilder; getter
methods return the captured values.

## Side effects

- Writes to the four internal baseline fields.
- Writes default font CSS into the caller's StringBuilder.

## Invariants

- **The capture is one-shot per workbook reload.** Called once
  by `SpreadsheetStyleFactory.reloadWorkbookStyles`; the
  baseline is then read by every other cell-style CSS
  composition. A regression that captured per-cell would slow
  the style pass by an order of magnitude.
- **The CSS-emission side effect is part of the contract.**
  Without it, the default style's CSS row would be empty. Pinned
  by slice 36 — the test asserts both the field captures AND
  the StringBuilder content after a capture call.
- **Alignment baseline is separate**. The
  `defaultTextAlign` field has its own setter
  (`setDefaultTextAlign`), not bundled into the
  `defaultFontStyle` capture method.

## Open questions

- The double-duty of `defaultFontStyle` (capture fields + emit
  CSS) is a code smell that pre-dates the slice-16 extraction.
  A future refactor could split into `captureFrom(style)` +
  `writeDefaultCss(sb)` for clarity; the test would need to
  follow but the contract is the same.
- The four baseline fields aren't `final` even though they're
  effectively immutable after capture. A future cleanup could
  make them `final` and use a constructor instead of the
  setter-style capture. Records aren't a fit (per slice 107's
  audit, because of the StringBuilder side-effect contract).
