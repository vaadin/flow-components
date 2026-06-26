# Tier 3 Wrapper Flip — Readiness Assessment

**Last updated:** slice 162.

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

## What works in the TS path

All slices 132-161 are functional through the wrapper:

| Surface | Status |
| --- | --- |
| Cell rendering (4 quadrants + freeze) | ✅ |
| Column header (A/B/.../AA) | ✅ |
| Row header (1/2/3) | ✅ |
| Header corner | ✅ |
| Sheet tabs (bottom strip) | ✅ |
| Cell selection (click, shift-click) | ✅ |
| Header click → whole column/row select | ✅ |
| Keyboard nav (arrows, Tab, Enter, Home, End, Ctrl-Home/End, shift+arrow) | ✅ |
| Mouse drag → range selection | ✅ |
| Cell editor (double-click, F2, type-to-edit, Enter/Tab/Esc/blur, post-commit nav) | ✅ |
| Auto-scroll active cell into view | ✅ |
| ResizeObserver-driven initial render | ✅ |
| Server dispatch: cellSelected, cellRangeSelected, rowSelected, columnSelected, cellValueEdited, deleteSelectedCells, sheetSelected, onSheetScroll, onUndo, onRedo, popupButtonClick | ✅ |
| Sheet overlays (image rendering via setOverlays + setResources) | ✅ |
| Popup buttons (rendering + click dispatch) | ✅ |

## What's degraded if you flip now

The following work in the GWT bundle but are degraded (or missing) in the TS path:

| Feature | Status | Server-side impact |
| --- | --- | --- |
| Cell comments (yellow triangle + popup) | ❌ Not rendered | Server still pushes comment data; just no visual indicator. |
| Hyperlinks (clickable link cells) | ❌ Not interactive | Cells render with their text; `linkCellClicked` doesn't dispatch on click. |
| Context menu (right-click on cells/headers) | ❌ Not handled | `contextMenuOpenOnSelection` / row/column variants don't dispatch. |
| Copy / paste / cut (Ctrl+C/V/X) | ❌ Not handled | Browser default applies; `onPaste`/`clearSelectedCellsOnCut` don't dispatch. |
| Column / row resize via header drag | ❌ Not interactive | `columnResized` / `rowsResized` don't dispatch. |
| Grouping outline (+/- buttons on sides) | ❌ Not rendered | `groupingCollapsed` doesn't dispatch. |
| Custom-component overlay slotting | ⚠️ Placeholder rendered, but Flow component injection (slot) isn't wired | Components show as empty boxes. |
| Cell value formatting (numbers, dates, currency) | ⚠️ Display value used as-is | The server sends pre-formatted `value` strings, so simple cases work; on-the-fly format changes won't reformat client-side. |
| Visual styling fidelity (fonts, borders) | ⚠️ Best-effort | The TS path uses CSS variables + CSS classes that match the GWT-side conventions, but pixel-perfect match isn't guaranteed without visual-regression testing. The slice 122-123 golden screenshots are vs. the GWT path. |

## Recommended next steps

### Path A (incremental flip)

1. Slice 163+: implement deferred features one-by-one (cell comments, hyperlinks, context menu, copy/paste, resize, grouping).
2. After each, run the IT suite with `window.VAADIN_SPREADSHEET_USE_LIT = true` injected via setup script.
3. Once all degraded features are addressed, flip the default in `initLazy` (or delete the GWT branch).

Estimated: 8-12 slices.

### Path B (flip with documented degradations)

1. Update the IT suite to set `VAADIN_SPREADSHEET_USE_LIT = true` by default.
2. Run the suite; document failures + screenshot diffs.
3. For each broken test, decide: fix the TS path, accept the change, or revert to GWT default for that test.
4. Ship "Tier 3 Phase 1" as the MVP, ship remaining features as Tier 3 Phase 2.

Estimated: 2-3 slices to assess + ship the partial flip + a doc of remaining work.

### Path C (full feature parity then flip)

Wait until cell comments, hyperlinks, context menu, copy/paste, column/row resize, grouping, and custom-component slotting are all implemented before flipping any default.

Estimated: 15-20 slices.

## Recommended

**Path B** for fastest real-world feedback. The TS client is functional enough for ~80% of use cases; the missing 20% (comments, links, context menu, copy/paste, resize, grouping, custom components) is well-known and bounded. Flipping forces actual user-flow validation, which catches issues that no amount of synthetic testing would.

## Risk register

- **R1: Visual diff against golden screenshots** — the slice 122-123 baselines were captured against GWT. The TS path will produce visually similar but not identical screenshots. Acceptance criteria from slice 129 explicitly allow "best-effort visual match" (Mode A wire contract preserved, Mode B implementation clean-room). Set a tolerance in the diff IT.

- **R2: Event-shape mismatches** — slice 128's audit verified the symmetric event surface, but the dispatch SHAPE (detail field names) is wire contract. If the server's `SpreadsheetHandler` strict-matches field names that diverge from the TS dispatch, the server would silently drop events. Mitigation: add server-side IT that exercises each `SpreadsheetEvent` type.

- **R3: ResizeObserver double-fire** — the wrapper has its own `spreadsheetResizeObserver` (calls `$connector.resize()`); the TS connector has its own ResizeObserver inside the scroll container. Both call `recomputeViewport()`. Idempotent — but emits double `onSheetScroll` events on resize. Acceptable; server dedupes.

- **R4: Untested setter no-ops** — many setters are still stubs in the TS class (e.g. `setCellCommentAuthors`). With opt-in flip these are exercised by real apps for the first time. Stub no-ops are safe but features that the wrapper expects to use those setters' results may misbehave silently.

## Concrete next-slice candidates

After this slice (162), the most-impactful sequence is:

- **Slice 163** — feature flag for IT: a Maven property that sets `VAADIN_SPREADSHEET_USE_LIT=true` before TestBench starts, so the existing IT suite can be re-run against the TS path. Document which existing tests fail.
- **Slice 164** — cell-comments overlay (yellow triangle + popup on hover).
- **Slice 165** — hyperlink cells + `linkCellClicked` dispatch.
- **Slice 166** — copy/paste/cut keyboard shortcuts.
- **Slice 167** — column/row resize via header drag.
- **Slice 168** — flip default (remove GWT branch).
- **Slice 169** — delete `spreadsheet-export.js` + the GWT build pipeline; ship as a single replaced module.

That sequence ends with the GWT bundle deleted: Tier 3 done.
