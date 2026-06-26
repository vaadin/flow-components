/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, type TemplateResult } from 'lit';

import type { ViewportBounds } from '../algorithms/ViewportMath.js';

/**
 * Input state for the {@link SpreadsheetRowHeader} element. The
 * connector builds + pushes this alongside the cell-grid's render
 * state on every render cycle (slice 144).
 *
 * Mirror of {@link ColumnHeaderRenderState}: same shape, swapped
 * column axis → row axis.
 */
export interface RowHeaderRenderState {
    /** Currently visible rows — same shape as the cell-grid's viewport. */
    viewport: ViewportBounds;
    /**
     * Pixel y-offsets for visible rows, indexed by
     * {@code row - viewport.firstRowIndex} — copied through from
     * the cell-grid render state.
     */
    rowY: ReadonlyArray<number>;
    /** Pixel height of each visible row, same indexing as {@code rowY}. */
    rowHeights: ReadonlyArray<number>;
    /**
     * Width of the header strip in pixels. {@code 0} disables the
     * header (render emits an empty fragment). Excel's typical row-
     * header width is 30-40 px (wider for large row counts so 1M+
     * fits without truncation).
     */
    headerWidth: number;
    /**
     * Total grid pixel height — used to size the row-header strip's
     * intrinsic height so it occupies the same vertical scroll
     * extent as the cell-grid.
     */
    totalGridHeightPx: number;
    /**
     * Rows intersecting the active selection. Each cell whose row
     * is in this set gets the {@code header-selected} part in
     * addition to {@code row-header}, mirroring the documented
     * public styling API. Slice 173.
     */
    selectedRows: ReadonlySet<number>;
}

/** Helper: build an empty row-header render state. */
export function emptyRowHeaderRenderState(): RowHeaderRenderState {
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
        rowY: [],
        rowHeights: [],
        headerWidth: 0,
        totalGridHeightPx: 0,
        selectedRows: new Set(),
    };
}

/**
 * Internal row-header Lit element. Renders one
 * {@code .row-header-cell} per visible row with the row's 1-based
 * number inside.
 *
 * The element is sticky-positioned (via host CSS, {@code left:0})
 * so it stays pinned at the left edge of the scroll container as
 * the user scrolls right. Vertical scroll moves the strip
 * naturally with the cells, keeping row numbers aligned with
 * their rows.
 *
 * Same render-state-driven contract + same no-decorator pattern as
 * the cell-grid + column-header. Slice 144.
 */
export class SpreadsheetRowHeader extends LitElement {
    static properties = {
        renderState: { attribute: false },
    };

    static styles = css`
        :host {
            display: block;
            position: sticky;
            left: 0;
            z-index: 4;
            box-sizing: border-box;
            background: var(--vaadin-spreadsheet-header-bg, #f4f4f4);
            border-right: 1px solid
                var(--vaadin-spreadsheet-header-border, #c0c0c0);
            font-family: var(--vaadin-spreadsheet-header-font, inherit);
            font-size: var(--vaadin-spreadsheet-header-font-size, inherit);
        }

        .row-header-cell {
            position: absolute;
            box-sizing: border-box;
            overflow: hidden;
            text-align: center;
            border-bottom: 1px solid
                var(--vaadin-spreadsheet-header-border, #c0c0c0);
        }

        /* Resize handle on the bottom edge of each row header
           (slice 164). 5px tall, full-width, cursor:row-resize. */
        .row-resize-handle {
            position: absolute;
            left: 0;
            height: 5px;
            cursor: row-resize;
            z-index: 1;
        }
    `;

    declare renderState: RowHeaderRenderState;

    constructor() {
        super();
        this.renderState = emptyRowHeaderRenderState();
    }

    /**
     * Shadow-root click delegation — same pattern as the column
     * header (slice 148). Dispatches {@code row-header-click}
     * with the row coordinate + modifier-key state.
     */
    firstUpdated(): void {
        this.shadowRoot!.addEventListener('click', this.handleShadowClick);
        // Slice 164: row-resize drag mousedown.
        this.shadowRoot!.addEventListener(
            'mousedown',
            this.handleShadowMouseDown as EventListener
        );
        // Slice 166: right-click on a row header dispatches
        // `row-header-contextmenu`. preventDefault suppresses the
        // browser's native menu so only the server-supplied
        // ContextMenu is shown.
        this.shadowRoot!.addEventListener(
            'contextmenu',
            this.handleShadowContextMenu
        );
    }

    private readonly handleShadowContextMenu = (e: Event): void => {
        const target = e.target;
        if (!(target instanceof Element)) {
            return;
        }
        const cell = target.closest<HTMLElement>('.row-header-cell');
        if (!cell) {
            return;
        }
        const row = Number(cell.dataset.row);
        if (!Number.isFinite(row) || row < 1) {
            return;
        }
        e.preventDefault();
        this.dispatchEvent(new CustomEvent('row-header-contextmenu', {
            detail: { row },
            bubbles: true,
            composed: true,
        }));
    };

    private readonly handleShadowMouseDown = (e: Event): void => {
        const target = e.target;
        if (!(target instanceof Element)) {
            return;
        }
        const handle = target.closest<HTMLElement>('.row-resize-handle');
        if (!handle) {
            return;
        }
        const row = Number(handle.dataset.row);
        if (!Number.isFinite(row) || row < 1) {
            return;
        }
        e.preventDefault();
        const ev = e as MouseEvent;
        this.dispatchEvent(new CustomEvent('row-resize-start', {
            detail: { row, clientY: ev.clientY },
            bubbles: true,
            composed: true,
        }));
    };

    private readonly handleShadowClick = (e: Event): void => {
        const ev = e as MouseEvent;
        const target = ev.target;
        if (!(target instanceof Element)) {
            return;
        }
        const cell = target.closest<HTMLElement>('.row-header-cell');
        if (!cell) {
            return;
        }
        const row = Number(cell.dataset.row);
        if (!Number.isFinite(row) || row < 1) {
            return;
        }
        this.dispatchEvent(new CustomEvent('row-header-click', {
            detail: {
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
            viewport, rowY, rowHeights, headerWidth,
            selectedRows = new Set<number>(),
        } = this.renderState;

        if (headerWidth === 0
                || viewport.lastRowIndex < viewport.firstRowIndex) {
            return html``;
        }

        const cells: TemplateResult[] = [];
        const handles: TemplateResult[] = [];
        for (let row = viewport.firstRowIndex; row <= viewport.lastRowIndex; row++) {
            const top = rowY[row - viewport.firstRowIndex] ?? 0;
            const height = rowHeights[row - viewport.firstRowIndex] ?? 0;
            if (height === 0) {
                continue;
            }
            const style =
                `left:0;top:${top}px;width:${headerWidth}px;height:${height}px;`
                + `line-height:${height}px;`;
            // Slice 173: `part="row-header"` (+ `header-selected`
            // when the row intersects the active selection) mirrors
            // the documented public styling API.
            const isSelected = selectedRows.has(row);
            const cellPart = isSelected
                ? 'row-header header-selected'
                : 'row-header';
            // data-row drives the click-delegation handler in
            // firstUpdated() (slice 148).
            cells.push(html`
                <div class="row-header-cell row${row}" part=${cellPart}
                     style=${style} data-row=${row}>
                    ${row}
                </div>
            `);
            // Resize handle on the row's bottom edge (slice 164).
            const handleStyle =
                `top:${top + height - 3}px;width:${headerWidth}px;`;
            handles.push(html`
                <div class="row-resize-handle" style=${handleStyle}
                     data-row=${row}></div>
            `);
        }
        return html`${cells}${handles}`;
    }
}

customElements.define(
    'vaadin-spreadsheet-internal-row-header',
    SpreadsheetRowHeader
);

declare global {
    interface HTMLElementTagNameMap {
        'vaadin-spreadsheet-internal-row-header': SpreadsheetRowHeader;
    }
}
