# Spreadsheet Public Styling API Audit

**Slice 172. Inputs**: `../docs/articles/components/spreadsheet/styling.adoc`, JavaDocs in `vaadin-spreadsheet-flow/src/main/java/`, `vaadin-spreadsheet-styles.js`, `SpreadsheetElement.java` (TestBench).

## Question this audit answers

After dropping the GWT bundle (slice 170), the Lit elements use new internal CSS class names. The IT suite fails because (a) the default stylesheet `vaadin-spreadsheet-styles.js` targets the GWT-era class names and (b) the TestBench `SpreadsheetElement` queries the same GWT-era classes via `By.className(...)`.

The user's directive: preserve **documented** API surface; everything undocumented is implementation detail and free to change. TestBench is a Vaadin product — its selectors are not user-facing.

## Public surface (documented — must preserve)

### CSS custom properties (from `styling.adoc`)
- `--vaadin-spreadsheet-border-color`
- `--vaadin-spreadsheet-cell-border-color`
- `--vaadin-spreadsheet-header-border-color`
- `--vaadin-spreadsheet-text-color`

### Part attributes (from `styling.adoc`)
- Formula bar: `formula-field`, `address-field`
- Cell editing: `cell-input`, `cell`, `selection-corner`, `cell-range`
- Headers: `row-header`, `column-header`, `header-selected`
- Sheet tabs: `tab`, `tab-selected`, `new-tab-button`
- Tab navigation: `scroll-tabs-button`, `scroll-tabs-to-start-button`, `scroll-tabs-to-end-button`, `scroll-tabs-backward-button`, `scroll-tabs-forward-button`, `scroll-tabs-button-disabled`
- Cell indicators: `comment-triangle`, `invalid-triangle`
- Interactive: `popup-button`

### Public Java constants (from `Spreadsheet.java` JavaDoc)
- `Spreadsheet.HIDE_FUNCTION_BAR_STYLE = "hidefunctionbar"` — applied to host via `setFunctionBarVisible(false)`
- `Spreadsheet.HIDE_TABSHEET_STYLE = "hidetabsheet"` — applied to host via `setSheetSelectionBarVisible(false)`

These two class names ARE public (used in user code through the constants). When the user adds either to the host element's class list, the spreadsheet must hide the matching bar.

## Internal (not documented — free to rename)

These appear ONLY in source code or TestBench, never in user-facing docs:

### Selection borders (GWT)
`.s-top`, `.s-bottom`, `.s-left`, `.s-right`, `.s-top.extend`, `.paintmode` — internal selection-overlay implementation. Lit equivalent: `.sheet-selection` div with positioned border children; the documented part is `cell-range`.

### Sheet tabs (GWT)
`.sheet-tabsheet`, `.sheet-tabsheet-container`, `.sheet-tabsheet-options`, `.sheet-tabsheet-infolabel`, `.add-new-tab`, `.scroll-tabs-beginning`, `.scroll-tabs-end`, `.scroll-tabs-left`, `.scroll-tabs-right` — internal tab implementation. Lit equivalent: `<vaadin-spreadsheet-internal-sheet-tabs>` with internal `.sheet-tab` cells. Documented parts: `tab`, `tab-selected`, `new-tab-button`, `scroll-tabs-button` (+ variants).

### Headers (GWT)
`.rh.row{N}`, `.ch.col{N}`, `.header-resize-dnd-first` — internal row/col header cells with index-suffixed classes. Lit equivalent: `.row-header-cell` / `.col-header-cell` with `data-row` / `data-col` attributes. Documented parts: `row-header`, `column-header`, `header-selected`.

### Pane containers (GWT)
`.bottom-right-pane`, `.top-left-pane`, `.top-right-pane`, `.bottom-left-pane`, `.sheet`, `.corner` — internal 4-quadrant pane layout for freeze. Lit equivalent: nested transform wrappers inside `<vaadin-spreadsheet-internal-cell-grid>`. No public part for the pane container itself.

### Other (GWT)
- `.merged-cell` — old merged-cell overlay. Lit uses positioned cells (different DOM, same visual). Part: none.
- `.sheet-image` — old image overlay class. Lit uses `.sheet-overlay-image`. Part: none documented.
- `.v-spreadsheet` — old GWT wrapper class. Lit relies on the `<vaadin-spreadsheet>` custom-element name.
- `.functionbar`, `.addressfield`, `.functionfield` — old formula bar. **The function bar is missing from Lit entirely** — see Implementation Gap below.
- `.v-contextmenu`, `.v-spreadsheet-popupbutton-overlay` — Vaadin 7-era overlays. Replaced by server-side `ContextMenu` component (slice 166 wired the event).

## Implementation gaps (need real work, not just renaming)

These are documented in the public docs (Vaadin docs page) but not yet implemented in Lit:

1. **Formula bar (function bar)** — `::part(formula-field)`, `::part(address-field)`. The Lit `vaadin-spreadsheet.js` wrapper doesn't render a formula bar at all. `setFunctionBarVisible(true)` is a no-op. `setSheetAddress` doesn't update any UI.
2. **Selection corner handle** — `::part(selection-corner)`. The GWT path renders a small square in the bottom-right of the active selection that the user drags to fill. Lit doesn't render this.
3. **Tab scroll buttons** — `::part(scroll-tabs-button)` family. Lit sheet tabs don't have scroll affordances (rare case for sheets that overflow horizontally).
4. **Comment-triangle / invalid-triangle parts** — slice 165 added `.comment-indicator` but didn't attach the documented `part="comment-triangle"`. Same for invalid-formula triangle (never implemented).
5. **Cell-input part** — slice 152 added the editor but didn't attach `part="cell-input"`.
6. **`header-selected` part** — the column/row header doesn't toggle a part variant when the column/row is selected.

## Plan (slices 172-178)

### Slice 172 — this audit doc

### Slice 173 — `part` attribute scaffolding on existing Lit elements
For each documented part above that has a Lit equivalent today: add the `part="..."` attribute to the rendered element. Mostly mechanical:
- `.cell` → `part="cell"`
- `.cell.cs{N}` for cells inside the active range → `part="cell cell-range"` (composite)
- `.col-header-cell` → `part="column-header"`; selected variant → `part="column-header header-selected"`
- `.row-header-cell` → `part="row-header"`; selected variant → `part="row-header header-selected"`
- `.sheet-tab` → `part="tab"`; active → `part="tab tab-selected"`
- `.popup-button` → `part="popup-button"`
- `.comment-indicator` → `part="comment-triangle"`
- `.cell-editor` input → `part="cell-input"`

Add `cell-range` and `selection-corner` parts to the selection-overlay rendering.

Add a few tests pinning that the parts appear on the right elements.

### Slice 174 — Honor `hidefunctionbar` + `hidetabsheet` host classes
The Lit Spreadsheet sub-elements need CSS that responds to those classes on the host. Until the function bar is implemented (slice 175), `hidefunctionbar` is a no-op visually but should not error. `hidetabsheet` should hide the sheet-tabs element when the host has that class.

### Slice 175 — Implement the formula bar
A new `<vaadin-spreadsheet-internal-formula-bar>` element rendered above the scroll container in the connector's setup. Two inputs: `part="address-field"` (read-only display of active cell address) + `part="formula-field"` (editable formula). Wire to `setSheetAddress` server push + `sheetAddressChanged` event dispatch.

### Slice 176 — Rewrite `vaadin-spreadsheet-styles.js`
Replace the 1832-line GWT-targeted stylesheet with a slim Lit-aligned version that:
- Defines the 4 CSS custom properties with default values (preserve)
- Hosts the `:host(.hidefunctionbar)` / `:host(.hidetabsheet)` rules so the constants keep working
- Otherwise relies on the internal stylesheet of each `<vaadin-spreadsheet-internal-*>` element

End-users who styled via `vaadin-spreadsheet::part(...)` keep their styling. Anyone who styled via internal class names (`.s-top`, etc.) — those are not part of the public contract per the docs and would break, but the docs explicitly direct them to use parts.

### Slice 177 — Update TestBench (`SpreadsheetElement` + `SheetCellElement`)
Update the Java code to query the new Lit DOM (using parts where the docs document them, internal classes where TestBench has historically used them). Keep the public TestBench Java API (`SpreadsheetElement.getCellAt`, etc.) verbatim — only the internal selector changes.

### Slice 178 — Re-run full IT suite
Iterate on remaining failures. Some tests test the formula bar (slice 175 coverage) or context menu (slice 166 + slice 177 coverage); some test selection borders that are now rendered differently. Expect ~5-10 iteration rounds on individual tests.

## Memory updates after the slices land

The pattern uncovered here is a reusable lesson:

- **CSS class names that appear in a default stylesheet are NOT automatically public API**. Public is what the docs + JavaDoc explicitly mention. For Vaadin Flow components specifically, the documented contract is `::part(...)` + CSS custom properties + Java constants — the stylesheet's internal selectors are implementation details.
- **TestBench selectors are NOT public API either**. TestBench is a Vaadin product; when the component's DOM changes, the TestBench element implementation updates alongside, preserving the Java method API.
- **Audit BEFORE flipping** any GWT-replacement: enumerate every selector in (a) the default stylesheet, (b) TestBench element queries, (c) JavaDoc, (d) user-facing docs. Classify each. Build the alias / replacement map. THEN flip.

This goes into a new memory entry to be referenced in future GWT-replacement work in other components.
