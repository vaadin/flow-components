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

    render(): TemplateResult {
        const {
            viewport,
            cellData,
            colX,
            rowY,
            colWidths,
            rowHeights,
            cellStyleToCSSStyle,
            selection,
        } = this.renderState;

        if (viewport.lastColumnIndex < viewport.firstColumnIndex
                || viewport.lastRowIndex < viewport.firstRowIndex) {
            // Empty viewport — render an empty host. (The connector
            // may push an empty state during initial measurement.)
            return html``;
        }

        const cells: TemplateResult[] = [];
        for (let row = viewport.firstRowIndex; row <= viewport.lastRowIndex; row++) {
            const rowOffset = row - viewport.firstRowIndex;
            const top = rowY[rowOffset] ?? 0;
            const height = rowHeights[rowOffset] ?? 0;
            if (height === 0) {
                // Hidden row — skip rendering its cells.
                continue;
            }
            for (let col = viewport.firstColumnIndex; col <= viewport.lastColumnIndex; col++) {
                const colOffset = col - viewport.firstColumnIndex;
                const left = colX[colOffset] ?? 0;
                const width = colWidths[colOffset] ?? 0;
                if (width === 0) {
                    // Hidden column — skip.
                    continue;
                }
                const key = toKey(col, row);
                const data = cellData.get(key);
                const text = data?.value ?? '';
                const cellStyleClass = data?.cellStyle ?? '';
                // Selection: cells in the active range (excluding the
                // active cell) + cells in any aux range get the
                // `cell-range` class. The active cell does NOT get
                // `cell-range` — it's outlined by a separate overlay
                // (slice 137). This matches the GWT convention pinned by
                // SpreadsheetElement.isCellActiveWithinSelection.
                const inSelection = isCellSelected(selection, col, row);
                const isActive = isActiveCell(selection, col, row);
                const cellRangeClass = inSelection && !isActive ? ' cell-range' : '';
                const classList = `cell col${col} row${row}`
                    + (cellStyleClass ? ` ${cellStyleClass}` : '')
                    + cellRangeClass;
                const style = `left:${left}px;top:${top}px;width:${width}px;height:${height}px;`;
                cells.push(html`
                    <div class=${classList} style=${style}>
                        ${text}
                    </div>
                `);
            }
        }

        const styleBlock = buildCellStyleBlock(cellStyleToCSSStyle);
        const selectionOverlay = buildSelectionOverlay(this.renderState);
        return html`${styleBlock}${cells}${selectionOverlay}`;
    }
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

customElements.define('vaadin-spreadsheet-internal-cell-grid', SpreadsheetCellGrid);

declare global {
    interface HTMLElementTagNameMap {
        'vaadin-spreadsheet-internal-cell-grid': SpreadsheetCellGrid;
    }
}
