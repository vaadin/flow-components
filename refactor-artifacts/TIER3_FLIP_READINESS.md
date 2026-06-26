# Tier 3 Wrapper Flip — Readiness Assessment

**Last updated:** slice 168.

## What slice 162 did

The wrapper `vaadin-spreadsheet.js` now imports BOTH client implementations:

```js
import { Spreadsheet as GwtSpreadsheet } from './spreadsheet-export.js';
import { Spreadsheet as LitSpreadsheet } from './lit/Spreadsheet.js';
```

The `initLazy(element)` registration picks at instantiation time based on opt-in flags:

```js
const useLit = element.hasAttribute('use-lit')
    || window.VAADIN_SPREADSHEET_USE_LIT === true;
const SpreadsheetCtor = useLit ? LitSpreadsheet : GwtSpreadsheet;
element.$connector = new SpreadsheetCtor(element, element.renderRoot);
```

The TS class constructor now accepts the `(element, element.renderRoot)` calling convention — DOM goes into the shadow root, events fire on the host element. `resize()` + `relayout()` are implemented (they call `recomputeViewport()`).

**Default behavior is unchanged**: every existing app + IT uses the GWT bundle. Opt-in is per-instance via `use-lit` attribute or globally via `window.VAADIN_SPREADSHEET_USE_LIT`.

The wrapper-level feature flag exists ONLY through slice 168. Slice 169 removes the flag entirely — the Lit path becomes the only implementation. Slice 170 deletes `spreadsheet-export.js` + the `vaadin-spreadsheet-flow-client/` module.

## What works in the TS path

Slices 132-167 are functional through the wrapper:

| Surface | Status | Slice |
| --- | --- | --- |
| Cell rendering (4 quadrants + freeze) | ✅ | 132-141 |
| Column header (A/B/.../AA) | ✅ | 143 |
| Row header (1/2/3) | ✅ | 144 |
| Header corner | ✅ | 145 |
| Sheet tabs (bottom strip) | ✅ | 154 |
| Cell selection (click, shift-click) | ✅ | 146 |
| Header click → whole column/row select | ✅ | 148 |
| Keyboard nav (arrows, Tab, Enter, Home, End, Ctrl-Home/End, shift+arrow) | ✅ | 147 + 149 |
| Mouse drag → range selection | ✅ | 155 |
| Cell editor (double-click, F2, type-to-edit, Enter/Tab/Esc/blur, post-commit nav) | ✅ | 152 + 153 + 158 |
| Auto-scroll active cell into view | ✅ | 156 |
| ResizeObserver-driven initial render | ✅ | 142 |
| Server dispatch: cellSelected, cellRangeSelected, rowSelected, columnSelected, cellValueEdited, deleteSelectedCells, sheetSelected, onSheetScroll, onUndo, onRedo, popupButtonClick | ✅ | 150-159 |
| Sheet overlays (image rendering via setOverlays + setResources) | ✅ | 160 |
| Popup buttons (rendering + click dispatch) | ✅ | 161 |
| Copy / cut / paste keyboard shortcuts (TSV codec) | ✅ | 163 |
| Column / row resize via header drag (columnResized / rowsResized dispatch) | ✅ | 164 |
| Cell comments indicator (red corner triangle) | ✅ | 165 |
| Hyperlink cells (visual + linkCellClicked dispatch) | ✅ | 165 |
| Context menu (right-click → contextMenuOpenOnSelection / row+column variants) | ✅ | 166 |
| Column grouping outline (+/- buttons in gutter, groupingCollapsed dispatch) | ✅ | 167 |
| Component-overlay slot scaffolding (cell-grid emits `<slot name="overlay-${id}">`) | ⚠️ Scaffolding shipped, wrapper-side child move pending | 168 |

## What's still deferred (documented for the flip)

| Feature | Status | Server-side impact | Notes |
| --- | --- | --- | --- |
| Row grouping outline | ❌ Not rendered | `groupingCollapsed { cols: false }` doesn't dispatch from the row gutter. Server-pushed `setRowGroupingData` is plumbed but the row-header doesn't render the gutter. | Mirror of slice 167's column variant — same `PositionedGrouping` shape + same click pattern. Rare in business spreadsheets. |
| Level header buttons (1/2/3 numbered group-collapse-by-level) | ❌ Not rendered | `levelHeaderClicked { cols, level }` doesn't dispatch. | UI-only follow-up; the wire format is already plumbed. |
| Custom-component overlay slotting (Vaadin components inside cells) | ⚠️ Partial | Components show as empty positioned boxes if the wrapper hasn't moved them into the cell-grid's light DOM. Most spreadsheets don't embed Vaadin components in cells — common case is empty. | Slice 168 added the `<slot name="overlay-${id}">` rendering in the cell-grid's shadow DOM. To actually project a component, the wrapper must reparent the Flow-pushed child element from the spreadsheet host into the cell-grid + assign the matching `slot` attribute. Tracked as a wrapper-side follow-up. |
| Cell-comment hover popup | ❌ Indicator only | Comment text + author isn't shown on hover. Server-pushed `cellComments` / `cellCommentAuthors` are stored but never rendered. | Cosmetic — power-users will see the indicator triangle but won't see the comment body. |
| `setVisibleCellComments` (always-visible comment popups) | ❌ Not handled | Server flag is plumbed but the connector doesn't keep popups open. | Cosmetic. |
| `updateCellComment` dispatch (typing a new comment) | ❌ Not handled | No UI for editing comments. | Requires significant UI work; defer to follow-up. |
| `contextMenuClosed` dispatch | ❌ Not handled | The server-side `ContextMenu` component doesn't get notified when its menu closes through the Lit path. | Server-side hook, not client-side — usually not load-bearing. |
| Cell value formatting (numbers, dates, currency) | ⚠️ Display value used as-is | The server sends pre-formatted `value` strings, so simple cases work; on-the-fly format changes won't reformat client-side. | Mode A wire contract: server is authoritative for formatting. |
| Visual styling fidelity (fonts, borders) | ⚠️ Best-effort | The TS path uses CSS variables + CSS classes that match the GWT-side conventions, but pixel-perfect match isn't guaranteed without visual-regression testing. The slice 122-123 golden screenshots are vs. the GWT path. | Acceptance: best-effort visual match per slice 129. |

## Path forward (slices 169-171)

### Slice 169 — flip default to Lit

Remove the `use-lit` feature-flag logic from `vaadin-spreadsheet.js`. The wrapper now always uses `LitSpreadsheet`. The `import { Spreadsheet as GwtSpreadsheet } from './spreadsheet-export.js'` line is deleted.

The GWT bundle (`spreadsheet-export.js`) and the `vaadin-spreadsheet-flow-client/` Maven module remain in the repo at this point — slice 170 deletes them.

### Slice 170 — delete GWT bundle + client module

- Delete `spreadsheet-export.js` (the 1.3MB / 25,965 LOC GWT artifact)
- Delete the `vaadin-spreadsheet-flow-client/` Maven module
- Remove the pom dependency from `vaadin-spreadsheet-flow` on the client module
- Clean up any remaining references (frontend resources, etc.)

### Slice 171 — port GWT client tests against the new Lit code

The GWT client (`vaadin-spreadsheet-flow-client/src/test/java/.../SpreadsheetJsApiTest.java`) has ~750 lines of behavior tests that pin the GWT wire surface. The Lit code has ~470 wtr tests pinning its own behavior. Port the wire-contract subset from the GWT tests to wtr to inherit decades of pinned behavior as a regression net before the GWT module disappears.

## Risk register

- **R1: Visual diff against golden screenshots** — the slice 122-123 baselines were captured against GWT. The TS path will produce visually similar but not identical screenshots. Acceptance criteria from slice 129 explicitly allow "best-effort visual match" (Mode A wire contract preserved, Mode B implementation clean-room). Set a tolerance in the diff IT.

- **R2: Event-shape mismatches** — slice 128's audit verified the symmetric event surface, but the dispatch SHAPE (detail field names) is wire contract. If the server's `SpreadsheetHandler` strict-matches field names that diverge from the TS dispatch, the server would silently drop events. Mitigation: add server-side IT that exercises each `SpreadsheetEvent` type. Slice 171 (porting GWT tests) materially de-risks this.

- **R3: ResizeObserver double-fire** — the wrapper has its own `spreadsheetResizeObserver` (calls `$connector.resize()`); the TS connector has its own ResizeObserver inside the scroll container. Both call `recomputeViewport()`. Idempotent — but emits double `onSheetScroll` events on resize. Acceptable; server dedupes.

- **R4: Untested setter no-ops** — many setters are still stubs in the TS class (e.g. `setCellCommentAuthors`). With the flip these are exercised by real apps for the first time. Stub no-ops are safe but features that the wrapper expects to use those setters' results may misbehave silently. Slice 171's GWT-test port will exercise each setter's dispatch shape.

- **R5: Custom-component slot mismatch** — the cell-grid emits `<slot name="overlay-${id}">` but the wrapper currently appends component children to the spreadsheet host's light DOM, not the cell-grid's. Until the wrapper-side re-parenting lands, COMPONENT overlays render as empty positioned boxes. The same was true before slice 168 (placeholder div); slice 168 just turned the placeholder into a `<slot>` so a future wrapper change is one-line. Acceptable for the flip; common spreadsheets don't use COMPONENT overlays.
