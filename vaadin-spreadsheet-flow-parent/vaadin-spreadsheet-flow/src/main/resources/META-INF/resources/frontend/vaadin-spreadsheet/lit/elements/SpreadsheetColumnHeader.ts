/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, type TemplateResult } from 'lit';

import { columnIndexToLetters } from '../algorithms/CellRef.js';
import type { ViewportBounds } from '../algorithms/ViewportMath.js';

/**
 * Input state for the {@link SpreadsheetColumnHeader} element.
 * The Spreadsheet connector builds a fresh instance alongside the
 * cell-grid's {@code CellGridRenderState} and pushes it to the
 * header on every render cycle (slice 143).
 *
 * Coordinates use the same conventions as the cell-grid: 1-based
 * column indices, pixel offsets from the (virtual) grid origin.
 */
export interface ColumnHeaderRenderState {
    /** Currently visible columns — same shape as the cell-grid's viewport. */
    viewport: ViewportBounds;
    /**
     * Pixel x-offsets for visible columns, indexed by
     * {@code col - viewport.firstColumnIndex} — copied through
     * from the cell-grid render state.
     */
    colX: ReadonlyArray<number>;
    /** Pixel width of each visible column, same indexing as {@code colX}. */
    colWidths: ReadonlyArray<number>;
    /**
     * Height of the header strip in pixels. {@code 0} disables the
     * header (the connector still pushes a state, but render() emits
     * an empty fragment). Excel's default header is around 18-22 px;
     * the connector picks the value via {@code setHeaderRowHeight}.
     */
    headerHeight: number;
    /**
     * Total grid pixel width — used to size the header's intrinsic
     * width so it contributes to the scroll container's scroll
     * extent the same way the cell-grid does.
     */
    totalGridWidthPx: number;
}

/** Helper: build an empty header render state for tests + initial render. */
export function emptyColumnHeaderRenderState(): ColumnHeaderRenderState {
    return {
        viewport: {
            firstColumnIndex: 1,
            firstColumnPosition: 0,
            lastColumnIndex: 0,
            lastColumnPosition: 0,
            firstRowIndex: 1,
            firstRowPosition: 0,
            lastRowIndex: 0,
            lastRowPosition: 0,
        },
        colX: [],
        colWidths: [],
        headerHeight: 0,
        totalGridWidthPx: 0,
    };
}

/**
 * Internal column-header Lit element. Renders one
 * {@code .col-header-cell} per visible column with the column's
 * letter ({@code A, B, ..., Z, AA, AB, ...}) inside.
 *
 * The element is sticky-positioned (via host CSS) so it stays
 * pinned at the top of the scroll container as the user scrolls
 * down. Horizontal scroll moves the header naturally with the
 * cells, keeping column letters aligned with their columns.
 *
 * The connector creates an instance, appends it to the scroll
 * container ABOVE the cell-grid, and pushes a fresh render state
 * on every {@code applyRenderState} call.
 *
 * Properties are declared via Lit's {@code static properties} API
 * (no decorators) for the same esbuild-compatibility reason as
 * {@link SpreadsheetCellGrid} (slice 132).
 */
export class SpreadsheetColumnHeader extends LitElement {
    static properties = {
        renderState: { attribute: false },
    };

    static styles = css`
        :host {
            display: block;
            position: sticky;
            top: 0;
            z-index: 5;
            box-sizing: border-box;
            background: var(--vaadin-spreadsheet-header-bg, #f4f4f4);
            border-bottom: 1px solid
                var(--vaadin-spreadsheet-header-border, #c0c0c0);
            font-family: var(--vaadin-spreadsheet-header-font, inherit);
            font-size: var(--vaadin-spreadsheet-header-font-size, inherit);
        }

        .col-header-cell {
            position: absolute;
            box-sizing: border-box;
            overflow: hidden;
            text-align: center;
            border-right: 1px solid
                var(--vaadin-spreadsheet-header-border, #c0c0c0);
        }
    `;

    declare renderState: ColumnHeaderRenderState;

    constructor() {
        super();
        this.renderState = emptyColumnHeaderRenderState();
    }

    /**
     * Wire shadow-root click delegation on first render — same
     * pattern as {@link SpreadsheetCellGrid}. Each click that
     * lands on a {@code .col-header-cell} dispatches a
     * {@code column-header-click} custom event with the column
     * coordinate + modifier-key state. Slice 148.
     */
    firstUpdated(): void {
        this.shadowRoot!.addEventListener('click', this.handleShadowClick);
    }

    private readonly handleShadowClick = (e: Event): void => {
        const ev = e as MouseEvent;
        const target = ev.target;
        if (!(target instanceof Element)) {
            return;
        }
        const cell = target.closest<HTMLElement>('.col-header-cell');
        if (!cell) {
            return;
        }
        const col = Number(cell.dataset.col);
        if (!Number.isFinite(col) || col < 1) {
            return;
        }
        this.dispatchEvent(new CustomEvent('column-header-click', {
            detail: {
                col,
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
        const { viewport, colX, colWidths, headerHeight } = this.renderState;

        if (headerHeight === 0
                || viewport.lastColumnIndex < viewport.firstColumnIndex) {
            return html``;
        }

        const cells: TemplateResult[] = [];
        for (let col = viewport.firstColumnIndex; col <= viewport.lastColumnIndex; col++) {
            const left = colX[col - viewport.firstColumnIndex] ?? 0;
            const width = colWidths[col - viewport.firstColumnIndex] ?? 0;
            if (width === 0) {
                continue;
            }
            const letter = columnIndexToLetters(col);
            const style =
                `left:${left}px;top:0;width:${width}px;height:${headerHeight}px;`
                + `line-height:${headerHeight}px;`;
            // data-col drives the click delegation handler in
            // firstUpdated() (slice 148) — same pattern as the
            // cell-grid (slice 146).
            cells.push(html`
                <div class="col-header-cell col${col}" style=${style}
                     data-col=${col}>
                    ${letter}
                </div>
            `);
        }
        return html`${cells}`;
    }
}

customElements.define(
    'vaadin-spreadsheet-internal-column-header',
    SpreadsheetColumnHeader
);

declare global {
    interface HTMLElementTagNameMap {
        'vaadin-spreadsheet-internal-column-header': SpreadsheetColumnHeader;
    }
}
