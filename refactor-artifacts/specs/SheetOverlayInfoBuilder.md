# Block: SheetOverlayInfoBuilder

> Phase 3 spec. Drafted from the source
> (`SheetOverlayInfoBuilder.java`) and the characterization tests
> (`SheetOverlayInfoBuilderTest.java`, 9 tests, slice 54).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Build the client-side `OverlayInfo` DTO from a
`SheetOverlayWrapper`. Two branches:

1. **`DONT_MOVE_AND_RESIZE` XSSF anchor** — absolute pixel /
   EMU coordinates (Excel "absolute" overlay positioning).
2. **Regular cell-anchored** — skip leading hidden rows and
   columns, compute width and height from the active sheet's
   dimensions.

Sibling helpers:
[`SheetOverlayStorage`](SheetOverlayStorage.md),
[`SheetOverlayVisibility`](SheetOverlayVisibility.md),
[`SheetOverlayManager`](SheetOverlayManager.md).

## Inputs

### `build(SheetOverlayWrapper overlayWrapper)`

- The wrapper carries the POI anchor + overlay type.
- Reads the active sheet for hidden-row / hidden-column checks
  + column-width / row-height resolution.

## Outputs

`OverlayInfo` — the DTO consumed by the GWT client. Wire shape
pinned by [`DtoWireFormatTest`](../../vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/DtoWireFormatTest.java).

## Side effects

None. Pure read-and-transform.

## Invariants

- **`DONT_MOVE_AND_RESIZE` branch uses col=1, row=1 sentinel**
  with absolute `dx`, `dy`, `width`, `height` in pixels / points.
  The client reads these as "ignore col/row, position by
  absolute coordinates." Pinned by slice 54.
- **EMU conversion uses Units.EMU_PER_PIXEL for X / width,
  Units.EMU_PER_POINT for Y / height.** XLSX stores Y / height in
  English Metric Units (EMU) where the per-point conversion
  differs from per-pixel. A regression that used per-pixel for Y
  would shift every overlay vertically by ~16%.
- **Hidden-row / hidden-column skip is iterative**. While the
  anchor's row / col is hidden, increment. The overlay
  effectively "snaps" to the next visible row / col. Pinned by
  the slice 54 hidden-skip test.
- **Width / height are in pixels for X-axis, points for
  Y-axis**. Matches POI's storage convention; the wire-format
  uses these units verbatim (no conversion at the boundary).

## Open questions

- The `DONT_MOVE_AND_RESIZE` branch only handles XSSF. HSSF's
  equivalent ("absolute" overlay positioning) isn't currently
  routed through this branch — would silently fall through to
  the cell-anchored path and misposition. No real-world HSSF
  files trigger this in the current test suite; a future
  hardening could detect HSSF DONT_MOVE_AND_RESIZE explicitly.
- The `(Long)` cast on `xssfAnchor.getPosition().getX()` is
  unnecessary (POI returns `long`). A future cleanup could
  drop it.
