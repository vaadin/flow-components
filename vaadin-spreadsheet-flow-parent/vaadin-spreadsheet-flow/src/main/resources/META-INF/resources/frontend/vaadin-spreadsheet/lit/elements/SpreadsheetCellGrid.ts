/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, type TemplateResult } from 'lit';

import { toKey } from '../algorithms/CellKeys.js';
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
        const { viewport, cellData, colX, rowY, colWidths, rowHeights } =
            this.renderState;

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
                const style = `left:${left}px;top:${top}px;width:${width}px;height:${height}px;`;
                cells.push(html`
                    <div class="cell ${`col${col}`} ${`row${row}`}" style=${style}>
                        ${text}
                    </div>
                `);
            }
        }
        return html`${cells}`;
    }
}

customElements.define('vaadin-spreadsheet-internal-cell-grid', SpreadsheetCellGrid);

declare global {
    interface HTMLElementTagNameMap {
        'vaadin-spreadsheet-internal-cell-grid': SpreadsheetCellGrid;
    }
}
