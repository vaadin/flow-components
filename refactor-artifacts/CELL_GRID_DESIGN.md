# Tier 3 — Cell-grid Lit element design

> Drafted 2026-06-22 by slice 131 after surveying vaadin-grid-flow's
> architecture. This document specifies the design for the
> spreadsheet's cell-grid sub-element + the broader Lit-element
> structure under the clean-room rewrite direction (per
> `refactor-artifacts/QUIRKS_AUDIT.md` + `code-modernization` skill
> Mode A/B distinction).
> **Status:** design draft; implementation begins in slice 132.

## What this document decides

Phase 3.2's "cell rendering + virtual scrolling" work is the
critical path of the entire Tier 3 rewrite. Getting the
architecture right is leverage; getting it wrong means many
follow-up slices fix tangents. This doc captures the architectural
decisions BEFORE writing the first LitElement code.

Three concrete deliverables:

1. The **wrapper structure** that the cell-grid lives inside.
2. The **cell-grid element's own structure** (LitElement vs
   functional vs patched native).
3. The **state + render pipeline** — how server-pushed state
   reaches the rendered cell DOM.

## Architectural constraint that shapes everything

Grid follows a "no wrapper" pattern: the server's `@Tag("vaadin-grid")`
points at the native `<vaadin-grid>` web component (published as
`@vaadin/grid`), and `gridConnector.ts` PATCHES that web component
in place. There is no wrapper LitElement between the Java side and
the native element.

**Spreadsheet cannot follow this pattern.** There is no published
`@vaadin/spreadsheet` web component to patch. The wrapper Lit
element (`vaadin-spreadsheet.js`) IS the published element — it's
what `@Tag("vaadin-spreadsheet")` in `Spreadsheet.java` resolves
to. The wrapper is structurally required.

This is a real constraint, not laziness. Publishing `@vaadin/spreadsheet`
as a separate npm package would be a multi-quarter project + would
fragment the codebase. For Tier 3's clean-room rewrite, the
wrapper stays. We adopt grid's CONVENTIONS within that constraint.

## What we adopt from grid's pattern

### 1. The `$connector` naming convention

Grid stores its delegate at `element.$connector`. The spreadsheet
currently uses `element.api`. Rename to `$connector` to match
grid's convention.

The rename is mechanical (search-replace in `vaadin-spreadsheet.js`
and any code that reads from `this.api`). It signals "this is a
Vaadin Flow connector, same shape as grid's" to future readers and
to the team migrating between components.

**Impact**: ~30 callsites in `vaadin-spreadsheet.js`. Test impact: the
105-method API surface stays the same; only the property name on
the element changes.

### 2. State batched into a single applyState call

Grid's connector accepts batched property updates rather than
one-property-per-call. The spreadsheet currently has ~30 individual
`setX(value)` calls in `updated()`. Replace with:

```ts
// Today (in updated()):
if (changedProperties.has('rows')) this.api.setRows(this.rows);
if (changedProperties.has('cols')) this.api.setCols(this.cols);
// ... 28 more

// Tomorrow (grid-style):
this.$connector.applyState(this);
```

The connector's `applyState(host)` reads each property off the host
element + applies them in dependency order. This:
- Halves the diff at every state change.
- Lets the connector internally batch DOM writes.
- Makes the property→method mapping centralized in one place.

**Impact**: refactor the wrapper's `updated()` hook. Requires
defining the dependency-ordered application sequence in the
connector. ~200 LOC of new connector code; equivalent LOC removed
from the wrapper.

### 3. Closures over private state, not class fields

Grid's connector is a closure factory:

```ts
window.Vaadin.Flow.gridConnector.initLazy = (grid) => {
  if (grid.$connector) return;
  let selectedKeys = {};
  let requestDebouncer = null;
  // ... state lives in closures, not fields
  grid.$connector = {
    doSelection: function (...) { ... }, // closes over selectedKeys
    // ...
  };
};
```

The spreadsheet's current TS `Spreadsheet` class uses class fields:

```ts
private rows = 0;
private cols = 0;
// ...
setRows(n: number) { this.rows = n; }
```

**Decision**: keep the class structure for now. The closure pattern
works for grid's ~700 LOC; the spreadsheet's connector is ~3000+
LOC across 105 methods + state. Closures at that scale produce
deeply-nested code that's hard to debug + hard to test. The class
structure with private fields is the right tradeoff at this size.

**What we DO adopt**: the `initLazy(element)` lifecycle hook +
module-level singleton registration. See §4 below.

### 4. Module-level `initLazy` + `window.Vaadin.Flow` registration

Grid registers its connector at import time:

```ts
window.Vaadin.Flow.spreadsheetConnector = {
  initLazy: (element) => {
    if (element.$connector) return;
    element.$connector = new Spreadsheet(element);
  }
};
```

The wrapper's `connectedCallback()` calls
`window.Vaadin.Flow.spreadsheetConnector.initLazy(this)` instead of
`new Spreadsheet(this)` directly. This:
- Allows the connector module to be loaded independently of the
  wrapper module (lazy / on-demand).
- Matches the inspect-the-page Vaadin debugging convention: any
  `<vaadin-X>` element with an associated connector has it at
  `element.$connector`, queryable from devtools.

**Impact**: ~20 LOC added to `lit/Spreadsheet.ts` (the registration
block at module top); ~5 LOC changed in the wrapper.

### 5. Patching the rendering pipeline, not building one

Grid PATCHES the native vaadin-grid's `__updateRow`, `__applySorters`,
etc. Each patch wraps the original method, calls the connector's
hook, then delegates back. This is grid's elegance — it doesn't
own the render loop, it intercepts events on the render loop the
native element already runs.

The spreadsheet doesn't have a native render loop to patch. The
wrapper's `<div id="container"></div>` is the rendering target.
The connector (the TS Spreadsheet class) owns the entire render
loop end-to-end.

**Decision**: the connector OWNS the render loop. There's no native
element to patch. We compose the render from:
- The cell-grid sub-element (this doc's main subject)
- The formula bar sub-element (Phase 3.3)
- The sheet-tabs sub-element (Phase 3.5)
- The overlays sub-element (Phase 3.4)

Each sub-element is a separate LitElement. The connector creates +
positions them inside the wrapper's container. State flows from
connector → sub-elements via Lit property bindings.

## Cell-grid element structure (the main subject)

### Element class

```ts
// lit/elements/SpreadsheetCellGrid.ts
@customElement('vaadin-spreadsheet-cell-grid')
export class SpreadsheetCellGrid extends LitElement {
    @property({ type: Number }) scrollLeft = 0;
    @property({ type: Number }) scrollTop = 0;
    @property({ type: Number }) viewportWidth = 0;
    @property({ type: Number }) viewportHeight = 0;
    @property({ attribute: false }) renderState!: CellGridRenderState;

    render(): TemplateResult { /* ... */ }
}
```

This is an INTERNAL element — not registered on the global custom-
element registry under `vaadin-spreadsheet-cell-grid` directly. It's
created via `document.createElement` by the connector + appended
inside the wrapper's container. The `@customElement` decorator
registers it (required by Lit) but the name has a
`vaadin-spreadsheet-internal-` prefix to make the "don't reach for
this directly" intent clear.

### State input shape

The connector pushes a single `CellGridRenderState` object on each
update — no individual property setters:

```ts
interface CellGridRenderState {
    /** Visible row range + pixel positions, from computeViewportBounds. */
    viewport: ViewportBounds;
    /** Cell data the connector knows about, keyed by `toKey(col, row)`. */
    cellData: Map<string, CellData>;
    /** Cell-style index → CSS string. */
    cellStyleToCSSStyle: Record<number, string>;
    /** Merged regions overlapping the visible viewport. */
    mergedRegions: MergedRegion[];
    /** Current selection state (active cell + ranges). */
    selection: SelectionState;
    /** Pixel widths per visible column index. */
    colWidths: number[];
    /** Pixel heights per visible row index. */
    rowHeights: number[];
}
```

The connector mutates its internal state freely; on each
`requestUpdate` boundary it builds a fresh `CellGridRenderState`
and assigns it to `cellGrid.renderState`. Lit's `@property` change
detection then triggers re-render.

This is grid-style "batched state application" applied within the
spreadsheet's internal element tree.

### Render output

The cell-grid renders an absolutely-positioned grid of cells. For
the visible-range portion of the viewport (per `computeViewportBounds`):

```html
<div class="cell-grid" style="position: relative; width: <total>px; height: <total>px;">
  <div class="cell col1 row1"
       style="position: absolute; left: 0px; top: 0px; width: 50px; height: 20px;">
    A1
  </div>
  <div class="cell col2 row1" style="...">B1</div>
  <!-- ... visible cells ... -->
</div>
```

CSS class names (`col1`, `row1`, `cell`) are byte-equivalent to the
GWT side (Mode A — wire contract; the TestBench module hardcodes
them). The structure is otherwise clean — absolute positioning,
explicit pixel coords, no nested table.

### Cell-recycling pattern

Grid's pattern: render rows × visible-count, recycle on scroll. The
cell-grid follows the same:

- On scroll → connector calls `computeViewportBounds` with the
  new scroll position → builds new `CellGridRenderState` →
  assigns to `cellGrid.renderState`.
- Lit's diff sees changed `cellData` Map → re-renders the visible
  cells.
- Off-screen cells are NOT removed eagerly; Lit's diff handles the
  cell-element churn.

This is a clean-room implementation of virtual scrolling — same
behavior contract as the GWT side (visible-range computation +
buffer rendering) but using Lit's diff instead of manual
DOM operations.

### Why a sub-element, not the wrapper rendering cells directly

Three reasons:
- **Separation of concerns**: the wrapper handles the spreadsheet's
  top-level chrome (formula bar, sheet tabs, toolbar). Cell rendering
  is its own subsystem with different perf characteristics.
- **Testability**: a separate Lit element can be unit-tested with
  fixed `renderState` inputs, without instantiating the full
  Spreadsheet class.
- **Reusability**: future "spreadsheet preview" components could
  embed the cell-grid without dragging in the full wrapper.

## Naming + module conventions

Per slice 130's clean-room direction:

- **Files in `lit/elements/`**: `SpreadsheetCellGrid.ts`,
  `SpreadsheetFormulaBar.ts`, `SpreadsheetSheetTabs.ts`. PascalCase
  matching grid's `gridConnector.ts` convention (camelCase for
  connector, PascalCase for class). Each file exports exactly one
  LitElement class.
- **Custom-element names**: `vaadin-spreadsheet-internal-cell-grid`
  / `-formula-bar` / `-sheet-tabs`. The `-internal-` infix marks
  them as not-user-facing. Lit requires kebab-case with one hyphen
  minimum, so this is the lowest-friction convention.
- **Comments**: cite the BEHAVIOR contract (Excel/POI/clipboard
  semantics) + the test file. Do NOT cite GWT-side file paths as
  "ported from..." (per slice 130's QUIRKS_AUDIT cleanup).
- **State-input types**: PascalCase suffix `State` (e.g.
  `CellGridRenderState`) or `Input` (e.g. `ViewportInput`). One
  file per type.

## Build + test infrastructure

The cell-grid element + sibling sub-elements live in
`lit/elements/`. Test files in
`vaadin-spreadsheet-flow-integration-tests/test/` named
`lit-cell-grid.test.ts`, etc. — same naming as existing slice 118-130
tests. wtr already picks them up via the `test/lit-*.test.ts` glob.

Visual-regression IT (slices 122-123) is the safety net for the
rendering changes. Phase 3.6 acceptance gate: run the diff IT after
swapping the wrapper's `api` from the GWT blob to the TS
Spreadsheet — every fixture should match its baseline within the
0.01 tolerance.

## Implementation order (slices 132+)

1. **Slice 132**: Scaffold `lit/elements/SpreadsheetCellGrid.ts` —
   the LitElement class + the `CellGridRenderState` interface +
   minimal render that produces an absolutely-positioned grid
   with cell content but no styling. ~150 LOC + ~10 tests.
2. **Slice 133**: Connector → cell-grid state pipeline. Rename
   `this.api` → `this.$connector` in the wrapper. Add the
   `initLazy` + `window.Vaadin.Flow.spreadsheetConnector` module-
   level registration. ~80 LOC.
3. **Slice 134**: Wire `computeViewportBounds` into the connector
   + push `CellGridRenderState` to the cell-grid on scroll.
4. **Slice 135**: Cell styling — apply `cellStyleToCSSStyle`
   mapping to rendered cells.
5. **Slice 136**: Selection rendering — apply `selection.activeCell`
   + `selection.activeRange` as CSS class overlays on visible cells.
6. **Slice 137+**: Merged-cell overlays, frozen-pane geometry,
   formula bar (Phase 3.3).

Each slice ships a focused diff with its own visual-regression
check.

## What this enables for GWT elimination

After slices 132-137, the cell-grid + the cell rendering path is
TS-only. The GWT blob still runs (it's the `this.api` target for
now), but the new path EXISTS as a sibling. Phase 3.6's cutover —
the single import-line change in `vaadin-spreadsheet.js` — flips
which path is active. Phase 3.7's cleanup removes the GWT path.

The visual-regression IT verifies that the new path produces the
same rendered output as the GWT path before the cutover ships.

## Risks + open questions

- **Lit's render perf for very large grids**: 100K-row fixtures
  (the existing `100_000_rows.xlsx` test) are the perf-baseline.
  Lit's diff is fast but not free. If profiling shows the diff
  itself is the bottleneck (e.g. >10ms per scroll event), we
  consider direct DOM manipulation for the cell-grid only. The
  ViewportMath buffer + viewport-bounds approach already limits
  rendering to ~100 cells; Lit should handle that easily.
- **Z-index + frozen-pane stacking**: 4 quadrants (top-left,
  top-right, bottom-left, bottom-right) each render their own
  cell-grid sub-tree, with z-index layering to keep frozen rows /
  columns above scrolling content. Pattern TBD in slice 137.
- **The `vaadin-spreadsheet-internal-*` naming**: alternative is
  `vaadin-spreadsheet-cell-grid` (no `internal` infix), matching
  what users would write IF they could compose at this level. We
  pick `internal` to signal "don't use directly" — adjust if the
  team prefers the cleaner name.
- **Custom-element registry collision**: if a future API publishes
  `vaadin-spreadsheet-cell-grid` as a user-facing element, our
  internal naming has to dodge it. The `internal` infix keeps that
  option open.
