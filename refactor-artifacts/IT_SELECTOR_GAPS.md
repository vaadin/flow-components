# IT selectors the Lit DOM cannot satisfy

The integration tests hardcode 30 distinct CSS selectors from the GWT client.
`AbstractSpreadsheetIT.translateSelector` maps the ones where the feature exists
under different markup. This file records the rest: selectors that fail because
the **feature is missing**, not because the name changed.

Fix drift by extending `translateSelector`, never by adding legacy class aliases
to the Lit DOM — those names are not public API (see
`PUBLIC_STYLING_API_AUDIT.md`).

## Mapped (slice 177 + slice 186)

| IT selector | Lit equivalent |
| --- | --- |
| `.ch.col<N>` | `.col-header-cell.col<N>` |
| `.rh.row<N>` | `.row-header-cell.row<N>` |
| `.sheet-tabsheet-tab.selected-tab` | `.sheet-tab.active` |
| `.addressfield`, `input.addressfield` | `[part~="address-field"]` |
| `.functionfield` | `[part~="formula-field"]` |
| `input[id='cellinput']` | `[part~="cell-input"]` |
| `.s-corner` | `.selection-corner` |
| `.popupbutton` | `.popup-button` |
| `.cell-comment-triangle` | `[part~="comment-triangle"]` |
| `.col-group-pane .grouping.plus` | `.col-grouping-toggle[data-collapsed="true"]` |
| `.col-group-pane .grouping.minus` | `.col-grouping-toggle[data-collapsed="false"]` |

## Not mappable — the feature is missing

| IT selector | Why | What it needs |
| --- | --- | --- |
| `.row-group-pane .grouping.plus` / `.minus` | Row grouping outline was never implemented. Slice 167 did columns only — `SpreadsheetRowHeader.ts` contains no grouping code at all. | Port the column-grouping outline to the row header. |
| `.sheet-image.col<N>.row<N>` | Overlays render from pixel geometry only. `PositionedOverlay` carries `id`, `src` and a pixel box — no cell coordinates, so an overlay cannot be addressed by the cell it is anchored to. | Plumb the anchor cell through `PositionedOverlay` and emit `data-col` / `data-row`, matching the convention cells already use. Then map to `.sheet-overlay-image[data-col="N"][data-row="N"]`. |
| `top-left-pane`, `top-right-pane`, `bottom-left-pane` | The Lit client uses a different frozen-pane architecture — CSS Grid plus transform-based frozen wrappers, with no per-quadrant pane element to name. | Decide whether the quadrant concept should be addressable at all. If tests only need "is this cell in the frozen region", assert on the cell instead. |
| `namedrangebox` | The formula bar has an address field and a formula field only. The named-range dropdown was not carried over. | Add a named-range selector to `SpreadsheetFormulaBar`. |
| `.cell-invalidformula-triangle` | Nothing is emitted for invalid-formula cells. `setInvalidFormulaCells` is stored but never rendered. | Render an indicator, mirroring `comment-triangle`. |
| `vaadin-context-menu-overlay`, `.popupContent`, `.v-contextmenu` | `showActions()` is a no-op, so the context menu never opens. 13 IT failures. | Implement `showActions` against Vaadin's context menu. |
| `.header-resize-dnd-second` | Resize handles exist (`.col-resize-handle`, `.row-resize-handle`) but GWT's "-second" naming encodes which side of which header the handle belongs to. The positional semantics are not obviously equivalent. | Confirm the intended semantics against the GWT source before mapping, otherwise the tests will pass against the wrong handle. |
