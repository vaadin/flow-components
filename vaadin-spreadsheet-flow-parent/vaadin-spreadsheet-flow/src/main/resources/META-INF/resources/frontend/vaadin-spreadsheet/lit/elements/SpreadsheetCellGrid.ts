/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, type TemplateResult } from 'lit';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';

import { toKey } from '../algorithms/CellKeys.js';
import { isActiveCell, isCellSelected } from '../state/SelectionState.js';
import { type CellGridRenderState, emptyCellGridRenderState } from './types.js';

/**
 * Internal cell-grid Lit element. Renders absolutely-positioned cell
 * divs for the visible viewport portion of a spreadsheet.
 *
 * The element is driven entirely by its {@link renderState} property
 * — no internal state, no direct interaction with the connector.
 * The Spreadsheet connector builds a fresh {@code CellGridRenderState}
 * on every scroll / data update / selection change and assigns it
 * here. Lit's diff handles cell-element churn.
 *
 * This is slice 132's minimal render: cell positions + text content,
 * no styling, no merged regions, no selection overlays. Subsequent
 * slices fill in:
 * - Slice 135: per-cell CSS styling
 * - Slice 136: selection overlays
 * - Slice 137: merged-cell overlays + frozen-pane geometry
 *
 * CSS class names on rendered cells follow the wire-format
 * convention from {@link toKey}: each cell carries
 * {@code class="cell col<C> row<R>"}. This is byte-equivalent to
 * the existing TestBench module's selector expectations and to the
 * pre-existing DOM contract.
 *
 * Properties are declared via Lit's {@code static properties} API
 * rather than TS decorators because the IT module's test runner
 * (esbuild plugin without {@code experimentalDecorators}) doesn't
 * parse the legacy decorator syntax. The static-properties API is
 * the equivalent + portable.
 */
export class SpreadsheetCellGrid extends LitElement {
    static properties = {
        renderState: { attribute: false },
    };

    static styles = css`
        :host {
            display: block;
            position: relative;
            overflow: hidden;
        }

        .cell {
            position: absolute;
            box-sizing: border-box;
            overflow: hidden;
            white-space: nowrap;
            font-family: var(--vaadin-spreadsheet-cell-font, inherit);
            font-size: var(--vaadin-spreadsheet-cell-font-size, inherit);
        }

        /* Frozen-pane wrappers (slice 141). Each frozen quadrant lives
           in its own wrapper div that's transform-translated by the
           current scroll offset, so the cells inside stay visually
           pinned to their viewport edge as the scroll container
           scrolls. The transform property creates a containing block,
           so the cells' absolute positions remain relative to the
           wrapper's top-left — which is itself anchored at the
           cell-grid's origin. */
        .quadrant-wrapper {
            position: absolute;
            top: 0;
            left: 0;
        }
        .quadrant-wrapper-top { z-index: 2; }
        .quadrant-wrapper-left { z-index: 2; }
        .quadrant-wrapper-corner { z-index: 3; }

        /* Per-cell z-index for the unwrapped main quadrant. Frozen
           quadrants inherit z-index from their wrapper (each wrapper
           is its own stacking context via the transform property),
           so the cell-level rule below only takes effect for main
           cells. */
        .cell.quadrant-main { z-index: 1; }

        /* Cell editor input — absolutely positioned over the cell
           being edited, sized to match. z-index above all cells +
           overlays so the input + caret are always on top. Slice
           152. */
        .cell-editor {
            position: absolute;
            box-sizing: border-box;
            z-index: 10;
            border: 2px solid
                var(--vaadin-spreadsheet-editor-border, #1976d2);
            outline: none;
            font-family: var(--vaadin-spreadsheet-cell-font, inherit);
            font-size: var(--vaadin-spreadsheet-cell-font-size, inherit);
            padding: 0 2px;
        }
    `;

    /**
     * State driving the render. The connector assigns a fresh object
     * here; Lit's property change detection triggers re-render.
     * Defaults to an empty viewport so the element is safe to insert
     * + measure before any state has been pushed.
     *
     * Note: declared with {@code declare} (no initializer) so the
     * generated class field doesn't shadow Lit's reactive accessor.
     * Initialization happens in the constructor instead.
     */
    declare renderState: CellGridRenderState;

    constructor() {
        super();
        this.renderState = emptyCellGridRenderState();
    }

    /**
     * Wire up click delegation on first render — the shadow root
     * doesn't exist until Lit has produced its first render. A
     * single listener at the shadow-root level finds the clicked
     * `.cell` ancestor and dispatches a {@code cell-click} custom
     * event with the cell's coordinate + modifier-key state. The
     * connector listens for this event to update its selection
     * state + push a fresh render. Slice 146.
     */
    firstUpdated(): void {
        this.shadowRoot!.addEventListener('click', this.handleShadowClick);
        // Double-click delegation for cell-editor activation
        // (slice 152). Same pattern as the single-click handler;
        // dispatches `cell-doubleclick` so the connector can open
        // its editor at the dbl-clicked cell.
        this.shadowRoot!.addEventListener('dblclick', this.handleShadowDoubleClick);
        // Mouse drag selection (slice 155). The cell-grid emits
        // `cell-mousedown` + `cell-mouseover`; the connector
        // tracks a dragging flag + extends the range as the
        // mouse moves over new cells. mouseup is bound at the
        // document level by the connector so a drag that ends
        // outside the spreadsheet still terminates cleanly.
        this.shadowRoot!.addEventListener('mousedown', this.handleShadowMouseDown);
        this.shadowRoot!.addEventListener('mouseover', this.handleShadowMouseOver);
    }

    /**
     * After every render, focus the editor input if one is now
     * active. The browser auto-focus attribute only works once on
     * insertion; this hook focuses on every transition into edit
     * mode so re-activating an editor at a new cell still places
     * the caret inside.
     */
    updated(changedProps: Map<string, unknown>): void {
        if (this.renderState.editor && changedProps.has('renderState')) {
            const input = this.shadowRoot!.querySelector(
                '.cell-editor'
            ) as HTMLInputElement | null;
            if (input && document.activeElement !== input) {
                input.focus();
                // Place caret at end so typing extends the existing
                // value (Excel convention).
                input.setSelectionRange(input.value.length, input.value.length);
            }
        }
    }

    /**
     * Mousedown handler — emits {@code cell-mousedown} so the
     * connector can mark the start of a drag-select. The
     * connector decides whether to consume the event vs let the
     * normal click handler run (Excel: mousedown immediately
     * selects, then drag extends, then mouseup commits the
     * range). Slice 155.
     */
    private readonly handleShadowMouseDown = (e: Event): void => {
        const target = e.target;
        if (!(target instanceof Element)) {
            return;
        }
        const cell = target.closest<HTMLElement>('.cell');
        if (!cell) {
            return;
        }
        const col = Number(cell.dataset.col);
        const row = Number(cell.dataset.row);
        if (!Number.isFinite(col) || !Number.isFinite(row) || col < 1 || row < 1) {
            return;
        }
        const ev = e as MouseEvent;
        this.dispatchEvent(new CustomEvent('cell-mousedown', {
            detail: {
                col, row,
                shiftKey: ev.shiftKey,
                ctrlKey: ev.ctrlKey,
                metaKey: ev.metaKey,
                altKey: ev.altKey,
            },
            bubbles: true,
            composed: true,
        }));
    };

    /**
     * Mouseover handler — emits {@code cell-mouseover} when the
     * mouse enters a new cell. Fires on every hover (not just
     * during drag); the connector filters by checking its
     * dragging flag. Slice 155.
     */
    private readonly handleShadowMouseOver = (e: Event): void => {
        const target = e.target;
        if (!(target instanceof Element)) {
            return;
        }
        const cell = target.closest<HTMLElement>('.cell');
        if (!cell) {
            return;
        }
        const col = Number(cell.dataset.col);
        const row = Number(cell.dataset.row);
        if (!Number.isFinite(col) || !Number.isFinite(row) || col < 1 || row < 1) {
            return;
        }
        this.dispatchEvent(new CustomEvent('cell-mouseover', {
            detail: { col, row },
            bubbles: true,
            composed: true,
        }));
    };

    /**
     * Double-click handler — opens the cell editor at the clicked
     * coordinate. Same shape as {@link handleShadowClick} but
     * dispatches {@code cell-doubleclick} (no modifier-key
     * metadata since the editor doesn't react to those). Slice
     * 152.
     */
    private readonly handleShadowDoubleClick = (e: Event): void => {
        const target = e.target;
        if (!(target instanceof Element)) {
            return;
        }
        const cell = target.closest<HTMLElement>('.cell');
        if (!cell) {
            return;
        }
        const col = Number(cell.dataset.col);
        const row = Number(cell.dataset.row);
        if (!Number.isFinite(col) || !Number.isFinite(row) || col < 1 || row < 1) {
            return;
        }
        this.dispatchEvent(new CustomEvent('cell-doubleclick', {
            detail: { col, row },
            bubbles: true,
            composed: true,
        }));
    };

    private readonly handleShadowClick = (e: Event): void => {
        const ev = e as MouseEvent;
        const target = ev.target;
        // The event target may be the ShadowRoot when the listener
        // catches synthetic events that don't originate from a node
        // inside the tree. ShadowRoot doesn't implement `closest`,
        // so we narrow to Element first.
        if (!(target instanceof Element)) {
            return;
        }
        const cell = target.closest<HTMLElement>('.cell');
        if (!cell) {
            return;
        }
        const col = Number(cell.dataset.col);
        const row = Number(cell.dataset.row);
        if (!Number.isFinite(col) || !Number.isFinite(row) || col < 1 || row < 1) {
            return;
        }
        this.dispatchEvent(new CustomEvent('cell-click', {
            detail: {
                col,
                row,
                shiftKey: ev.shiftKey,
                ctrlKey: ev.ctrlKey,
                metaKey: ev.metaKey,
                altKey: ev.altKey,
            },
            bubbles: true,
            composed: true,
        }));
    };

    render(): TemplateResult {
        const {
            viewport,
            cellStyleToCSSStyle,
            mergedRegions,
            verticalSplitPosition,
            horizontalSplitPosition,
            scrollLeft,
            scrollTop,
        } = this.renderState;

        if (viewport.lastColumnIndex < viewport.firstColumnIndex
                || viewport.lastRowIndex < viewport.firstRowIndex) {
            // Empty viewport — render an empty host. (The connector
            // may push an empty state during initial measurement.)
            return html``;
        }

        // Coordinates covered by a merged region. The render loop
        // skips ALL of them — the merged-cell overlay div is the
        // sole rendering at the top-left.
        const coveredByMerge = computeCoveredCoords(mergedRegions);

        // Main (bottom-right) quadrant: scrolling rows + cols.
        // Emitted as direct children of the cell-grid so cells sit at
        // their virtual coordinates and scroll naturally with the
        // scroll container.
        const mainCells = renderQuadrant({
            state: this.renderState,
            coveredByMerge,
            quadrantName: 'main',
            colStart: viewport.firstColumnIndex,
            colEnd: viewport.lastColumnIndex,
            rowStart: viewport.firstRowIndex,
            rowEnd: viewport.lastRowIndex,
            getColLeft: (col) =>
                this.renderState.colX[col - viewport.firstColumnIndex] ?? 0,
            getColWidth: (col) =>
                this.renderState.colWidths[col - viewport.firstColumnIndex] ?? 0,
            getRowTop: (row) =>
                this.renderState.rowY[row - viewport.firstRowIndex] ?? 0,
            getRowHeight: (row) =>
                this.renderState.rowHeights[row - viewport.firstRowIndex] ?? 0,
        });

        // Frozen quadrants live inside transform-translated wrappers
        // so their cells visually stay pinned as the user scrolls
        // (slice 141). The wrapper transform counters the scroll
        // container's shift; cells inside use coordinates relative
        // to the wrapper, which is itself anchored at the cell-grid
        // origin. CSS z-index on the wrappers stacks corner > top +
        // left > main.

        let topQuadrant: TemplateResult | string = '';
        if (verticalSplitPosition > 0) {
            const topCells = renderQuadrant({
                state: this.renderState,
                coveredByMerge,
                quadrantName: 'top',
                colStart: viewport.firstColumnIndex,
                colEnd: viewport.lastColumnIndex,
                rowStart: 1,
                rowEnd: verticalSplitPosition,
                getColLeft: (col) =>
                    this.renderState.colX[col - viewport.firstColumnIndex] ?? 0,
                getColWidth: (col) =>
                    this.renderState.colWidths[col - viewport.firstColumnIndex] ?? 0,
                getRowTop: (row) => this.renderState.frozenRowY[row - 1] ?? 0,
                getRowHeight: (row) =>
                    this.renderState.frozenRowHeights[row - 1] ?? 0,
            });
            const transform = `transform:translateY(${scrollTop}px);`;
            topQuadrant = html`
                <div class="quadrant-wrapper quadrant-wrapper-top" style=${transform}>
                    ${topCells}
                </div>
            `;
        }

        let leftQuadrant: TemplateResult | string = '';
        if (horizontalSplitPosition > 0) {
            const leftCells = renderQuadrant({
                state: this.renderState,
                coveredByMerge,
                quadrantName: 'left',
                colStart: 1,
                colEnd: horizontalSplitPosition,
                rowStart: viewport.firstRowIndex,
                rowEnd: viewport.lastRowIndex,
                getColLeft: (col) => this.renderState.frozenColX[col - 1] ?? 0,
                getColWidth: (col) =>
                    this.renderState.frozenColWidths[col - 1] ?? 0,
                getRowTop: (row) =>
                    this.renderState.rowY[row - viewport.firstRowIndex] ?? 0,
                getRowHeight: (row) =>
                    this.renderState.rowHeights[row - viewport.firstRowIndex] ?? 0,
            });
            const transform = `transform:translateX(${scrollLeft}px);`;
            leftQuadrant = html`
                <div class="quadrant-wrapper quadrant-wrapper-left" style=${transform}>
                    ${leftCells}
                </div>
            `;
        }

        let cornerQuadrant: TemplateResult | string = '';
        if (verticalSplitPosition > 0 && horizontalSplitPosition > 0) {
            const cornerCells = renderQuadrant({
                state: this.renderState,
                coveredByMerge,
                quadrantName: 'corner',
                colStart: 1,
                colEnd: horizontalSplitPosition,
                rowStart: 1,
                rowEnd: verticalSplitPosition,
                getColLeft: (col) => this.renderState.frozenColX[col - 1] ?? 0,
                getColWidth: (col) =>
                    this.renderState.frozenColWidths[col - 1] ?? 0,
                getRowTop: (row) => this.renderState.frozenRowY[row - 1] ?? 0,
                getRowHeight: (row) =>
                    this.renderState.frozenRowHeights[row - 1] ?? 0,
            });
            const transform =
                `transform:translate(${scrollLeft}px, ${scrollTop}px);`;
            cornerQuadrant = html`
                <div class="quadrant-wrapper quadrant-wrapper-corner" style=${transform}>
                    ${cornerCells}
                </div>
            `;
        }

        const styleBlock = buildCellStyleBlock(cellStyleToCSSStyle);
        const mergedOverlays = buildMergedCellOverlays(this.renderState);
        const selectionOverlay = buildSelectionOverlay(this.renderState);
        const editor = buildCellEditorOverlay(this.renderState);
        return html`${styleBlock}${mainCells}${topQuadrant}${leftQuadrant}${cornerQuadrant}${mergedOverlays}${selectionOverlay}${editor}`;
    }
}

interface QuadrantRenderOpts {
    state: CellGridRenderState;
    coveredByMerge: ReadonlySet<string>;
    quadrantName: 'main' | 'top' | 'left' | 'corner';
    colStart: number;
    colEnd: number;
    rowStart: number;
    rowEnd: number;
    getColLeft: (col: number) => number;
    getColWidth: (col: number) => number;
    getRowTop: (row: number) => number;
    getRowHeight: (row: number) => number;
}

/**
 * Render the cells of a single quadrant. Each cell carries a
 * {@code quadrant-<name>} class so CSS can apply z-index stacking
 * (corner > top, left > main). The per-cell position comes from the
 * caller's {@code getColLeft/getRowTop/getColWidth/getRowHeight}
 * closures — different quadrants use different position arrays
 * (frozen arrays vs main arrays).
 */
function renderQuadrant(opts: QuadrantRenderOpts): TemplateResult[] {
    const {
        state,
        coveredByMerge,
        quadrantName,
        colStart,
        colEnd,
        rowStart,
        rowEnd,
        getColLeft,
        getColWidth,
        getRowTop,
        getRowHeight,
    } = opts;
    const { cellData, selection } = state;

    const cells: TemplateResult[] = [];
    for (let row = rowStart; row <= rowEnd; row++) {
        const top = getRowTop(row);
        const height = getRowHeight(row);
        if (height === 0) {
            continue;
        }
        for (let col = colStart; col <= colEnd; col++) {
            const left = getColLeft(col);
            const width = getColWidth(col);
            if (width === 0) {
                continue;
            }
            const key = toKey(col, row);
            if (coveredByMerge.has(key)) {
                continue;
            }
            const data = cellData.get(key);
            const text = data?.value ?? '';
            const cellStyleClass = data?.cellStyle ?? '';
            const inSelection = isCellSelected(selection, col, row);
            const isActive = isActiveCell(selection, col, row);
            const cellRangeClass = inSelection && !isActive ? ' cell-range' : '';
            const classList = `cell col${col} row${row} quadrant-${quadrantName}`
                + (cellStyleClass ? ` ${cellStyleClass}` : '')
                + cellRangeClass;
            const style =
                `left:${left}px;top:${top}px;width:${width}px;height:${height}px;`;
            // data-col + data-row drive the click-delegation handler
            // in firstUpdated() (slice 146) — single source of truth
            // for the cell's coordinate when the event bubbles.
            cells.push(html`
                <div class=${classList} style=${style}
                     data-col=${col} data-row=${row}>
                    ${text}
                </div>
            `);
        }
    }
    return cells;
}

/**
 * Build the set of cell-keys that are INSIDE a merged region. The
 * per-cell render loop skips ALL of them — the merged-cell overlay
 * div is the sole rendering at the top-left, and the other
 * positions are covered (not rendered separately).
 *
 * Returned set holds keys in the {@link toKey} format
 * ({@code "col<C> row<R>"}) for O(1) lookup during the per-cell
 * render loop.
 */
function computeCoveredCoords(
    mergedRegions: ReadonlyArray<{
        col1: number;
        col2: number;
        row1: number;
        row2: number;
    }>
): Set<string> {
    const covered = new Set<string>();
    for (const region of mergedRegions) {
        for (let row = region.row1; row <= region.row2; row++) {
            for (let col = region.col1; col <= region.col2; col++) {
                covered.add(toKey(col, row));
            }
        }
    }
    return covered;
}

/**
 * Build merged-cell overlay divs for every {@link MergedRegion}
 * whose top-left coordinate falls inside the visible viewport. Each
 * overlay is a single div positioned at the merge's top-left cell,
 * sized to span the full region (sum of column widths × sum of row
 * heights).
 *
 * The CSS class list includes {@code cell col<C1> row<R1> merged-cell}
 * — the {@code merged-cell} class is the TestBench-side contract
 * (see {@code AbstractSpreadsheetIT.mergedCell:567-575}).
 *
 * Skips merges whose top-left is outside the visible viewport — the
 * connector / scroll handler is responsible for keeping the viewport
 * aligned. Per-cell styling, selection state, and cellData lookup
 * use the top-left cell's data; the merged overlay is treated as a
 * single rendered cell.
 */
function buildMergedCellOverlays(
    state: CellGridRenderState
): TemplateResult[] {
    const {
        viewport,
        cellData,
        colX,
        rowY,
        colWidths,
        rowHeights,
        cellStyleToCSSStyle,
        mergedRegions,
        selection,
    } = state;
    void cellStyleToCSSStyle; // styling slice 135 emits rules globally; we just attach the class

    const overlays: TemplateResult[] = [];
    for (const region of mergedRegions) {
        // Only render overlays whose top-left is in the viewport.
        if (region.col1 < viewport.firstColumnIndex
                || region.col1 > viewport.lastColumnIndex
                || region.row1 < viewport.firstRowIndex
                || region.row1 > viewport.lastRowIndex) {
            continue;
        }

        const colStartIdx = region.col1 - viewport.firstColumnIndex;
        const rowStartIdx = region.row1 - viewport.firstRowIndex;
        const left = colX[colStartIdx] ?? 0;
        const top = rowY[rowStartIdx] ?? 0;

        // Sum widths from col1 to min(col2, lastColumnIndex).
        let width = 0;
        const colEnd = Math.min(region.col2, viewport.lastColumnIndex);
        for (let col = region.col1; col <= colEnd; col++) {
            width += colWidths[col - viewport.firstColumnIndex] ?? 0;
        }
        // Sum heights from row1 to min(row2, lastRowIndex).
        let height = 0;
        const rowEnd = Math.min(region.row2, viewport.lastRowIndex);
        for (let row = region.row1; row <= rowEnd; row++) {
            height += rowHeights[row - viewport.firstRowIndex] ?? 0;
        }

        const key = toKey(region.col1, region.row1);
        const data = cellData.get(key);
        const text = data?.value ?? '';
        const cellStyleClass = data?.cellStyle ?? '';

        // Selection: a merged cell carries cell-range if its top-left
        // is in the selected range (excluding active cell). Matches
        // single-cell selection behavior.
        const inSelection = isCellSelected(selection, region.col1, region.row1);
        const isActive = isActiveCell(selection, region.col1, region.row1);
        const cellRangeClass = inSelection && !isActive ? ' cell-range' : '';

        const classList =
            `cell col${region.col1} row${region.row1} merged-cell`
            + (cellStyleClass ? ` ${cellStyleClass}` : '')
            + cellRangeClass;
        const style =
            `left:${left}px;top:${top}px;width:${width}px;height:${height}px;`;

        overlays.push(html`
            <div class=${classList} style=${style}>
                ${text}
            </div>
        `);
    }
    return overlays;
}

/**
 * Build the {@code .sheet-selection} overlay div positioned over the
 * active range. The div contains 4 child elements
 * ({@code .s-top}, {@code .s-left}, {@code .s-right}, {@code .s-bottom})
 * which TestBench's {@code SpreadsheetElement.findSelectionOutline}
 * locates by class name.
 *
 * Returns an empty string when the active range doesn't overlap the
 * visible viewport (selection scrolled off-screen) — TestBench's
 * waitUntil lookup will then poll until the user scrolls back.
 *
 * Wire contract: class names match the GWT-side
 * {@code SelectionWidget.java:116-126}. The overlay is positioned
 * absolutely within the cell-grid's shadow DOM. CSS styling for the
 * border-line children is deferred to the consumer (theme + a future
 * slice) — slice 137's scope is the structural DOM that TestBench
 * locates.
 */
function buildSelectionOverlay(
    state: CellGridRenderState
): TemplateResult | string {
    const { viewport, selection, colX, rowY, colWidths, rowHeights } = state;
    const range = selection.activeRange;

    // Clamp the range to the visible viewport.
    const leftCol = Math.max(range.c1, viewport.firstColumnIndex);
    const rightCol = Math.min(range.c2, viewport.lastColumnIndex);
    const topRow = Math.max(range.r1, viewport.firstRowIndex);
    const bottomRow = Math.min(range.r2, viewport.lastRowIndex);

    if (leftCol > rightCol || topRow > bottomRow) {
        // Active range outside the visible viewport.
        return '';
    }

    const leftIndex = leftCol - viewport.firstColumnIndex;
    const rightIndex = rightCol - viewport.firstColumnIndex;
    const topIndex = topRow - viewport.firstRowIndex;
    const bottomIndex = bottomRow - viewport.firstRowIndex;

    const left = colX[leftIndex] ?? 0;
    const top = rowY[topIndex] ?? 0;
    const right = (colX[rightIndex] ?? 0) + (colWidths[rightIndex] ?? 0);
    const bottom = (rowY[bottomIndex] ?? 0) + (rowHeights[bottomIndex] ?? 0);

    const width = Math.max(0, right - left);
    const height = Math.max(0, bottom - top);
    const overlayStyle =
        `left:${left}px;top:${top}px;width:${width}px;height:${height}px;`
        + 'position:absolute;pointer-events:none;';

    return html`
        <div class="sheet-selection" style=${overlayStyle}>
            <div class="s-top"></div>
            <div class="s-left"></div>
            <div class="s-right"></div>
            <div class="s-bottom"></div>
        </div>
    `;
}

/**
 * Build a {@code <style>} TemplateResult containing one rule per
 * entry in {@code cellStyleToCSSStyle}. Each rule has the form
 * {@code .cell.cs<N> { <body> }}. The body is taken verbatim from
 * the map (the server side composes the CSS declarations; we just
 * place them inside braces).
 *
 * Returns an empty fragment when the map is empty.
 */
function buildCellStyleBlock(
    cellStyleToCSSStyle: Readonly<Record<number, string>>
): TemplateResult | string {
    const entries = Object.entries(cellStyleToCSSStyle);
    if (entries.length === 0) {
        return '';
    }
    // Compose the rules as plain text + use unsafeHTML — Lit's
    // template literal doesn't substitute into <style> children
    // safely otherwise. The content is server-controlled CSS, so
    // this is the same trust boundary the wrapper already exposes
    // (the GWT path emitted these rules via document.createElement
    // ('style') + appendChild).
    const rules = entries
        .map(([index, body]) => `.cell.cs${index} { ${body} }`)
        .join('\n');
    return html`<style>${unsafeHTML(rules)}</style>`;
}

/**
 * Build the cell-editor overlay {@code <input>} when {@code state
 * .editor} is non-null (slice 152). The input is sized to match
 * the cell's pixel box using the appropriate quadrant arrays:
 *
 *  - col within frozen cols: use {@code frozenColX} / {@code
 *    frozenColWidths}
 *  - col within viewport: use {@code colX} / {@code colWidths}
 *  - row within frozen rows: use {@code frozenRowY} / {@code
 *    frozenRowHeights}
 *  - row within viewport: use {@code rowY} / {@code rowHeights}
 *
 * When the editor's coordinate falls outside both the viewport
 * and the frozen ranges, returns empty (defensive — the
 * connector should never push an editor at an off-screen cell;
 * this keeps the render safe regardless).
 *
 * On keydown:
 *   Enter / Tab → dispatch {@code cell-editor-commit} with the
 *     current input value; the connector dispatches
 *     `cellValueEdited` (slice 153) + clears editor state.
 *   Escape       → dispatch {@code cell-editor-cancel};
 *                  connector clears editor state without
 *                  dispatching cellValueEdited.
 *   Blur          → same as commit (Excel convention; slice 153).
 *
 * The detail event payload is {@code { col, row, value }} so the
 * connector can route the value to the correct cell without
 * re-querying its state.
 */
function buildCellEditorOverlay(
    state: CellGridRenderState
): TemplateResult | string {
    const editor = state.editor;
    if (!editor) {
        return '';
    }
    const { col, row, initialValue } = editor;
    const { viewport, colX, colWidths, rowY, rowHeights } = state;
    const { frozenColX, frozenColWidths, frozenRowY, frozenRowHeights } = state;

    let left = 0;
    let width = 0;
    if (col >= viewport.firstColumnIndex && col <= viewport.lastColumnIndex) {
        const i = col - viewport.firstColumnIndex;
        left = colX[i] ?? 0;
        width = colWidths[i] ?? 0;
    } else if (col >= 1 && col <= frozenColX.length) {
        left = frozenColX[col - 1] ?? 0;
        width = frozenColWidths[col - 1] ?? 0;
    } else {
        return '';
    }

    let top = 0;
    let height = 0;
    if (row >= viewport.firstRowIndex && row <= viewport.lastRowIndex) {
        const i = row - viewport.firstRowIndex;
        top = rowY[i] ?? 0;
        height = rowHeights[i] ?? 0;
    } else if (row >= 1 && row <= frozenRowY.length) {
        top = frozenRowY[row - 1] ?? 0;
        height = frozenRowHeights[row - 1] ?? 0;
    } else {
        return '';
    }

    if (width === 0 || height === 0) {
        return '';
    }

    const style =
        `left:${left}px;top:${top}px;width:${width}px;height:${height}px;`;

    const dispatchEditorEvent = (
        target: EventTarget,
        type: 'cell-editor-commit' | 'cell-editor-cancel',
        value: string
    ): void => {
        target.dispatchEvent(new CustomEvent(type, {
            detail: { col, row, value },
            bubbles: true,
            composed: true,
        }));
    };

    const onKeyDown = (e: KeyboardEvent): void => {
        const input = e.currentTarget as HTMLInputElement;
        if (e.key === 'Enter' || e.key === 'Tab') {
            e.preventDefault();
            dispatchEditorEvent(input, 'cell-editor-commit', input.value);
        } else if (e.key === 'Escape') {
            e.preventDefault();
            dispatchEditorEvent(input, 'cell-editor-cancel', input.value);
        }
        // Other keys (typing) are NOT stopped so the input receives
        // them. Arrow keys etc. move the caret inside the input
        // rather than navigating the spreadsheet — Excel convention.
        e.stopPropagation();
    };

    const onBlur = (e: FocusEvent): void => {
        const input = e.currentTarget as HTMLInputElement;
        dispatchEditorEvent(input, 'cell-editor-commit', input.value);
    };

    return html`
        <input class="cell-editor"
               .value=${initialValue}
               style=${style}
               @keydown=${onKeyDown}
               @blur=${onBlur} />
    `;
}

customElements.define('vaadin-spreadsheet-internal-cell-grid', SpreadsheetCellGrid);

declare global {
    interface HTMLElementTagNameMap {
        'vaadin-spreadsheet-internal-cell-grid': SpreadsheetCellGrid;
    }
}
