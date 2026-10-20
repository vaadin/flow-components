/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';

import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import {
    SpreadsheetRowHeader,
    emptyRowHeaderRenderState,
    type RowHeaderRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetRowHeader.js';

/**
 * Row header strip.
 *
 * Mirrors the column-header tests. Pins the wire-format
 * contract of the row-header element: one
 * {@code .row-header-cell .row<N>} per visible row with the row's
 * 1-based number inside.
 */

async function attachWithState(
    state: RowHeaderRenderState
): Promise<SpreadsheetRowHeader> {
    const el = document.createElement(
        'vaadin-spreadsheet-internal-row-header'
    ) as SpreadsheetRowHeader;
    el.renderState = state;
    document.body.appendChild(el);
    await el.updateComplete;
    return el;
}

function cleanup(el: HTMLElement) {
    el.remove();
}

function buildState(opts: {
    firstRowIndex: number;
    lastRowIndex: number;
    firstRowPosition: number;
    rowY: number[];
    rowHeights: number[];
    headerWidth: number;
    totalGridHeightPx?: number;
    selectedRows?: ReadonlySet<number>;
}): RowHeaderRenderState {
    const empty = emptyRowHeaderRenderState();
    return {
        viewport: {
            firstColumnIndex: 1,
            firstColumnPosition: 0,
            lastColumnIndex: 0,
            lastColumnPosition: 0,
            firstRowIndex: opts.firstRowIndex,
            firstRowPosition: opts.firstRowPosition,
            lastRowIndex: opts.lastRowIndex,
            lastRowPosition: 0,
        },
        rowY: opts.rowY,
        rowHeights: opts.rowHeights,
        headerWidth: opts.headerWidth,
        totalGridHeightPx: opts.totalGridHeightPx ?? empty.totalGridHeightPx,
        selectedRows: opts.selectedRows ?? empty.selectedRows,
    };
}

describe('emptyRowHeaderRenderState — helper for tests + connector initial state', () => {
    it('returns an empty viewport with zero headerWidth', () => {
        const state = emptyRowHeaderRenderState();

        expect(state.viewport.lastRowIndex).to.be.lessThan(
            state.viewport.firstRowIndex
        );
        expect(state.rowY).to.deep.equal([]);
        expect(state.rowHeights).to.deep.equal([]);
        expect(state.headerWidth).to.equal(0);
        expect(state.totalGridHeightPx).to.equal(0);
    });

    it('returns a fresh object on each call', () => {
        const a = emptyRowHeaderRenderState();
        const b = emptyRowHeaderRenderState();

        expect(a === b).to.be.false;
    });
});

describe('SpreadsheetRowHeader — element registration', () => {
    it('is a registered custom element', () => {
        expect(customElements.get('vaadin-spreadsheet-internal-row-header'))
            .to.equal(SpreadsheetRowHeader);
    });

    it('is constructable with default state', () => {
        const el = document.createElement(
            'vaadin-spreadsheet-internal-row-header'
        ) as SpreadsheetRowHeader;

        expect(el).to.be.instanceOf(SpreadsheetRowHeader);
        expect(el.renderState.headerWidth).to.equal(0);
        expect(el.renderState.rowY).to.deep.equal([]);
    });
});

describe('SpreadsheetRowHeader — render with header active', () => {
    it('renders nothing when headerWidth is 0', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 3,
                firstRowPosition: 0,
                rowY: [0, 20, 40],
                rowHeights: [20, 20, 20],
                headerWidth: 0,
            })
        );

        expect(el.shadowRoot!.querySelectorAll('.row-header-cell')).to.have.length(0);

        cleanup(el);
    });

    it('renders nothing when viewport is empty (lastRow < firstRow)', async () => {
        const el = await attachWithState({
            ...emptyRowHeaderRenderState(),
            headerWidth: 40,
        });

        expect(el.shadowRoot!.querySelectorAll('.row-header-cell')).to.have.length(0);

        cleanup(el);
    });

    it('renders one cell per visible row with .row<N> classes', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 3,
                firstRowPosition: 0,
                rowY: [0, 20, 40],
                rowHeights: [20, 20, 20],
                headerWidth: 40,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        expect(cells).to.have.length(3);
        expect(cells[0].classList.contains('row1')).to.be.true;
        expect(cells[1].classList.contains('row2')).to.be.true;
        expect(cells[2].classList.contains('row3')).to.be.true;
    });

    it('renders the row number as text content (1-based)', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 10,
                lastRowIndex: 12,
                firstRowPosition: 200,
                rowY: [200, 220, 240],
                rowHeights: [20, 20, 20],
                headerWidth: 40,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        expect(cells[0].textContent?.trim()).to.equal('10');
        expect(cells[1].textContent?.trim()).to.equal('11');
        expect(cells[2].textContent?.trim()).to.equal('12');
    });

    it('positions each header cell at rowY[i] with height = rowHeights[i]', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 3,
                firstRowPosition: 0,
                rowY: [0, 20, 60],
                rowHeights: [20, 40, 30],
                headerWidth: 40,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll(
            '.row-header-cell'
        ) as NodeListOf<HTMLElement>;
        expect(cells[0].style.top).to.equal('0px');
        expect(cells[0].style.height).to.equal('20px');
        expect(cells[1].style.top).to.equal('20px');
        expect(cells[1].style.height).to.equal('40px');
        expect(cells[2].style.top).to.equal('60px');
        expect(cells[2].style.height).to.equal('30px');
    });

    it('sets the width of each header cell to headerWidth and left to 0', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 1,
                firstRowPosition: 0,
                rowY: [0],
                rowHeights: [20],
                headerWidth: 40,
            })
        );

        const cell = el.shadowRoot!.querySelector(
            '.row-header-cell'
        ) as HTMLElement;
        expect(cell.style.width).to.equal('40px');
        expect(cell.style.left).to.equal('0px');
    });

    it('centres the number on the whole row, divider included', async () => {
        // The divider takes no room from the number, so the number sits
        // half a pixel lower than in a row whose divider is a border of its
        // own.
        for (const height of [20, 21]) {
            const el = await attachWithState(
                buildState({
                    firstRowIndex: 1,
                    lastRowIndex: 1,
                    firstRowPosition: 0,
                    rowY: [0],
                    rowHeights: [height],
                    headerWidth: 40,
                })
            );

            const cell = el.shadowRoot!.querySelector('.row-header-cell') as HTMLElement;
            const range = document.createRange();
            range.selectNodeContents(cell);
            const text = range.getBoundingClientRect();
            const middle = text.top + text.height / 2 - cell.getBoundingClientRect().top;
            expect(middle, `row of ${height}px`).to.be.closeTo(height / 2, 0.02);
            cleanup(el);
        }
    });

    it('skips rows whose height is 0 (hidden rows)', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 3,
                firstRowPosition: 0,
                rowY: [0, 20, 20],
                rowHeights: [20, 0, 20],
                headerWidth: 40,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        // Row 2 hidden → only 2 cells, for rows 1 and 3.
        expect(cells).to.have.length(2);
        expect(cells[0].classList.contains('row1')).to.be.true;
        expect(cells[1].classList.contains('row3')).to.be.true;
    });

    it('host has position:sticky CSS so the strip pins to the left of the scroll container', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 1,
                firstRowPosition: 0,
                rowY: [0],
                rowHeights: [20],
                headerWidth: 40,
            })
        );
        document.body.appendChild(el);

        const computed = getComputedStyle(el);
        expect(computed.position).to.equal('sticky');

        cleanup(el);
    });
});

describe('SpreadsheetRowHeader — click delegation', () => {
    it('puts data-row on each rendered header cell', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 5,
                lastRowIndex: 7,
                firstRowPosition: 80,
                rowY: [80, 100, 120],
                rowHeights: [20, 20, 20],
                headerWidth: 40,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll(
            '.row-header-cell'
        ) as NodeListOf<HTMLElement>;
        expect(cells[0].dataset.row).to.equal('5');
        expect(cells[1].dataset.row).to.equal('6');
        expect(cells[2].dataset.row).to.equal('7');

        cleanup(el);
    });

    it('dispatches row-header-click on click with the row coord', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 3,
                firstRowPosition: 0,
                rowY: [0, 20, 40],
                rowHeights: [20, 20, 20],
                headerWidth: 40,
            })
        );

        let received: { row: number; shiftKey: boolean } | undefined;
        el.addEventListener('row-header-click', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            received = { row: detail.row, shiftKey: detail.shiftKey };
        });

        const cell = el.shadowRoot!.querySelector(
            '.row-header-cell.row2'
        ) as HTMLElement;
        cell.click();

        expect(received).to.deep.equal({ row: 2, shiftKey: false });
        cleanup(el);
    });
});

describe('SpreadsheetRowHeader — re-renders on renderState reassignment', () => {
    it('updates cells when renderState changes', async () => {
        const el = await attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 2,
                firstRowPosition: 0,
                rowY: [0, 20],
                rowHeights: [20, 20],
                headerWidth: 40,
            })
        );

        expect(el.shadowRoot!.querySelectorAll('.row-header-cell')).to.have.length(2);

        el.renderState = buildState({
            firstRowIndex: 100,
            lastRowIndex: 101,
            firstRowPosition: 1980,
            rowY: [1980, 2000],
            rowHeights: [20, 20],
            headerWidth: 40,
        });
        await el.updateComplete;

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        expect(cells).to.have.length(2);
        expect(cells[0].textContent?.trim()).to.equal('100');
        expect(cells[1].textContent?.trim()).to.equal('101');

        cleanup(el);
    });
});

describe('Row header — grouping outline', () => {
    function stateWithGroupings(opts: {
        max: number;
        inversed?: boolean;
        groupings: { uniqueIndex: number; level: number; startPx: number;
                     endPx: number; buttonPx: number; collapsed: boolean }[];
    }): RowHeaderRenderState {
        return {
            ...emptyRowHeaderRenderState(),
            viewport: {
                firstColumnIndex: 1, firstColumnPosition: 0,
                lastColumnIndex: 1, lastColumnPosition: 0,
                firstRowIndex: 1, firstRowPosition: 0,
                lastRowIndex: 4, lastRowPosition: 80,
            },
            rowY: [0, 20, 40, 60],
            rowHeights: [20, 20, 20, 20],
            // The row numbers' 50px, past the gutter: a 15px level per
            // depth and one more.
            headerWidth: 50 + (opts.max > 0 ? 1 + (opts.max + 1) * 15 : 0),
            totalGridHeightPx: 80,
            rowGroupings: opts.groupings,
            rowGroupingMax: opts.max,
            rowGroupingInversed: opts.inversed ?? false,
        };
    }

    const oneGroup = [{ uniqueIndex: 3, level: 1, startPx: 20, endPx: 60,
                        buttonPx: 4, collapsed: false }];

    it('draws a line and a toggle for each grouping', async () => {
        const el = await attachWithState(stateWithGroupings({ max: 1, groupings: oneGroup }));

        expect(el.shadowRoot!.querySelectorAll('.row-grouping-line')).to.have.length(1);
        expect(el.shadowRoot!.querySelectorAll('.row-grouping-toggle')).to.have.length(1);
        cleanup(el);
    });

    it('shows minus when expanded and plus when collapsed', async () => {
        const expanded = await attachWithState(stateWithGroupings({ max: 1, groupings: oneGroup }));
        expect(expanded.shadowRoot!.querySelector('.row-grouping-toggle')!.textContent!.trim())
            .to.equal('−');
        cleanup(expanded);

        const collapsed = await attachWithState(stateWithGroupings({
            max: 1,
            groupings: [{ ...oneGroup[0], collapsed: true }],
        }));
        expect(collapsed.shadowRoot!.querySelector('.row-grouping-toggle')!.textContent!.trim())
            .to.equal('+');
        cleanup(collapsed);
    });

    it('keeps a toggle grey whatever the sheet\'s border colour', async () => {
        // The column buttons keep their grey too: no button takes the
        // sheet's border colour.
        const el = await attachWithState(stateWithGroupings({ max: 1, groupings: oneGroup }));
        el.style.setProperty('--vaadin-spreadsheet-border-color', 'rgb(9, 8, 7)');

        const style = getComputedStyle(el.shadowRoot!.querySelector('.row-grouping-toggle')!);
        expect(style.backgroundColor).to.equal('rgb(199, 199, 199)');
        expect(style.boxShadow).to.equal('rgb(199, 199, 199) 0px 0px 0px 1px inset');
        cleanup(el);
    });

    it('carries the collapsed state as a data attribute', async () => {
        const el = await attachWithState(stateWithGroupings({ max: 1, groupings: oneGroup }));

        const toggle = el.shadowRoot!.querySelector('.row-grouping-toggle')!;
        expect(toggle.getAttribute('data-collapsed')).to.equal('false');
        expect(toggle.getAttribute('data-unique-index')).to.equal('3');
        cleanup(el);
    });

    it('renders no gutter when there are no grouping levels', async () => {
        const el = await attachWithState(stateWithGroupings({ max: 0, groupings: [] }));

        expect(el.shadowRoot!.querySelectorAll('.row-grouping-toggle')).to.have.length(0);
        cleanup(el);
    });

    it('offsets the row numbers past the gutter', async () => {
        // Two levels and the one more: 1 + 3 x 15px.
        const el = await attachWithState(stateWithGroupings({ max: 2, groupings: oneGroup }));

        const cell = el.shadowRoot!.querySelector<HTMLElement>('.row-header-cell')!;
        expect(cell.style.left).to.equal('46px');
        cleanup(el);
    });

    it('keeps the gutter on the left when inversed', async () => {
        // Inversed only moves a group's button to the row above it.
        const el = await attachWithState(stateWithGroupings({
            max: 1, inversed: true, groupings: oneGroup,
        }));

        const cell = el.shadowRoot!.querySelector<HTMLElement>('.row-header-cell')!;
        expect(cell.style.left).to.equal('31px');
        cleanup(el);
    });

    it('draws a bracket down the group and a round button at its summary row', async () => {
        const el = await attachWithState(stateWithGroupings({ max: 1, groupings: oneGroup }));

        const line = getComputedStyle(el.shadowRoot!.querySelector('.row-grouping-line')!);
        expect(`${line.borderLeftWidth} ${line.borderTopWidth}`).to.equal('2px 2px');
        expect(line.borderLeftColor).to.equal('rgb(199, 199, 199)');
        const button = el.shadowRoot!.querySelector<HTMLElement>('.row-grouping-toggle')!;
        const style = getComputedStyle(button);
        expect(`${style.width} ${style.height} ${style.borderRadius}`).to.equal('12px 12px 50%');
        expect(style.backgroundColor).to.equal('rgb(199, 199, 199)');
        expect(style.color).to.equal('rgb(255, 255, 255)');
        // Centred on the summary row's position.
        expect(button.style.top).to.equal(`${4 - 6}px`);
        // Its right edge 2px short of the end of the 10px tick that starts
        // at 6px: 6 + 10 - 2 - 12.
        expect(button.style.left).to.equal('2px');
        cleanup(el);
    });

    it('writes the sign on an 11px line at the top of the button', async () => {
        // Whatever the font: where a line that tall starts its text at the
        // button's top edge. Inside a border, the sign would sit a pixel
        // lower.
        const el = await attachWithState(stateWithGroupings({ max: 1, groupings: oneGroup }));

        const button = el.shadowRoot!.querySelector<HTMLElement>('.row-grouping-toggle')!;
        const range = document.createRange();
        range.selectNodeContents(button);
        const text = range.getBoundingClientRect();
        expect(text.top - button.getBoundingClientRect().top).to.equal(Math.floor((11 - text.height) / 2));
        cleanup(el);
    });

    it('turns the tick to the far end when inversed', async () => {
        const el = await attachWithState(stateWithGroupings({ max: 1, inversed: true, groupings: oneGroup }));

        const line = getComputedStyle(el.shadowRoot!.querySelector('.row-grouping-line')!);
        expect(`${line.borderTopStyle} ${line.borderBottomWidth}`).to.equal('none 2px');
        cleanup(el);
    });

    it('keeps only the button of a collapsed group', async () => {
        const el = await attachWithState(stateWithGroupings({
            max: 1,
            groupings: [{ ...oneGroup[0], collapsed: true }],
        }));

        expect(el.shadowRoot!.querySelector('.row-grouping-line')).to.be.null;
        expect(el.shadowRoot!.querySelector('.row-grouping-toggle')).to.not.be.null;
        cleanup(el);
    });

    it('divides the levels with dotted lines', async () => {
        const el = await attachWithState(stateWithGroupings({ max: 2, groupings: oneGroup }));

        const dividers = el.shadowRoot!.querySelectorAll<HTMLElement>('.row-grouping-separator');
        expect(Array.from(dividers).map((d) => d.style.left)).to.deep.equal(['15px', '30px']);
        expect(getComputedStyle(dividers[0]).borderRightStyle).to.equal('dotted');
        cleanup(el);
    });

    it('starts the dotted lines at the top of the gutters', async () => {
        // A dotted line's dots are laid from where it starts, so the lines
        // start at the top of the gutters: under the corner, 66px above row 1.
        const el = await attachWithState({ ...stateWithGroupings({ max: 1, groupings: oneGroup }), leadingPx: 66 });

        const divider = el.shadowRoot!.querySelector<HTMLElement>('.row-grouping-separator')!;
        // 66 + 80px rounded up to an odd length: along an even one the
        // browser doubles the first dot and shifts the rest.
        expect(`${divider.style.top} ${divider.style.height}`).to.equal('-66px 147px');
        cleanup(el);
    });

    it('draws no gutter beside numbers without groups', async () => {
        const el = await attachWithState(stateWithGroupings({ max: 0, groupings: [] }));

        expect(el.shadowRoot!.querySelector('.row-grouping-gutter')).to.be.null;
        cleanup(el);
    });

    it('leaves the gutter to the strip\'s own border without numbers', async () => {
        // Headings hidden: the header is the gutter alone, one level of 15px
        // and its border.
        const el = await attachWithState({
            ...stateWithGroupings({ max: 1, groupings: oneGroup }),
            headerWidth: 16,
            rowGroupingHeadings: false,
        });

        expect(el.shadowRoot!.querySelector('.row-grouping-gutter')).to.be.null;
        expect(el.shadowRoot!.querySelectorAll('.row-grouping-toggle')).to.have.length(1);
        cleanup(el);
    });

    it('borders the gutter off from the numbers beside it', async () => {
        const el = await attachWithState(stateWithGroupings({ max: 1, groupings: oneGroup }));

        const gutter = el.shadowRoot!.querySelector<HTMLElement>('.row-grouping-gutter')!;
        const style = getComputedStyle(gutter);
        // One level and one more: 1 + 2 x 15px, its border on the last.
        expect(gutter.getBoundingClientRect().width).to.equal(31);
        expect(`${style.borderRightWidth} ${style.borderRightStyle} ${style.borderRightColor}`).to.equal(
            '1px solid rgb(199, 199, 199)'
        );
        cleanup(el);
    });

    it('asks to collapse an expanded group when its toggle is clicked', async () => {
        const el = await attachWithState(stateWithGroupings({ max: 1, groupings: oneGroup }));
        const received: any[] = [];
        el.addEventListener('row-grouping-toggle', (e) =>
            received.push((e as CustomEvent).detail));

        el.shadowRoot!.querySelector<HTMLElement>('.row-grouping-toggle')!.click();

        expect(received).to.have.length(1);
        expect(received[0]).to.deep.equal({ uniqueIndex: 3, collapsed: true });
        cleanup(el);
    });

    it('asks to expand a collapsed group', async () => {
        const el = await attachWithState(stateWithGroupings({
            max: 1, groupings: [{ ...oneGroup[0], collapsed: true }],
        }));
        const received: any[] = [];
        el.addEventListener('row-grouping-toggle', (e) =>
            received.push((e as CustomEvent).detail));

        el.shadowRoot!.querySelector<HTMLElement>('.row-grouping-toggle')!.click();

        expect(received[0]).to.deep.equal({ uniqueIndex: 3, collapsed: false });
        cleanup(el);
    });

    it('does not also report a row-header click', async () => {
        const el = await attachWithState(stateWithGroupings({ max: 1, groupings: oneGroup }));
        const headerClicks: any[] = [];
        el.addEventListener('row-header-click', (e) => headerClicks.push(e));

        el.shadowRoot!.querySelector<HTMLElement>('.row-grouping-toggle')!.click();

        expect(headerClicks).to.have.length(0);
        cleanup(el);
    });
});

describe('Row header — grouping reaches the server', () => {
    it('forwards a row toggle as a row grouping, not a column one', () => {
        const host = document.createElement('div');
        host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        const received: any[] = [];
        host.addEventListener('spreadsheet-event', (e) =>
            received.push((e as CustomEvent).detail));

        sheet.getRowHeader().dispatchEvent(
            new CustomEvent('row-grouping-toggle', {
                detail: { uniqueIndex: 5, collapsed: true },
                bubbles: true,
                composed: true,
            })
        );

        const grouping = received.filter((e) => e.type === 'groupingCollapsed');
        expect(grouping).to.have.length(1);
        // cols:false is what distinguishes a row outline from a column one.
        expect(grouping[0].data).to.deep.equal({
            cols: false, colIndex: 5, collapsed: true,
        });

        sheet.dispose();
        host.remove();
    });
});

describe('SpreadsheetRowHeader — the active cell\'s row', () => {
    async function attachWithSelection(
        selectedRows: ReadonlySet<number>
    ): Promise<SpreadsheetRowHeader> {
        return attachWithState(
            buildState({
                firstRowIndex: 1,
                lastRowIndex: 3,
                firstRowPosition: 0,
                rowY: [0, 20, 40],
                rowHeights: [20, 20, 20],
                headerWidth: 40,
                selectedRows,
            })
        );
    }

    it('shows a select-the-row pointer over a number', async () => {
        // An arrow pointing into the row: clicking a number selects it.
        const el = await attachWithSelection(new Set());

        expect(getComputedStyle(el.shadowRoot!.querySelector('.row-header-cell')!).cursor).to.equal('e-resize');
        expect(getComputedStyle(el.shadowRoot!.querySelector('.row-resize-handle')!).cursor).to.equal('row-resize');

        cleanup(el);
    });

    it('draws the divider inside the number, so the border along the cells runs past it', async () => {
        // As a border of its own, the divider met the active row's 2px
        // marker in a diagonal join, and the marker's last pixel came out
        // half grey.
        const el = await attachWithSelection(new Set([2]));

        const style = getComputedStyle(el.shadowRoot!.querySelector('.row-header-cell.row2')!);
        expect(style.borderBottomStyle).to.equal('none');
        expect(style.boxShadow).to.equal('rgb(199, 199, 199) 0px -1px 0px 0px inset');
        expect(style.borderRightWidth).to.equal('2px');

        cleanup(el);
    });

    it('marks only the selected row with the header-selected part', async () => {
        const el = await attachWithSelection(new Set([2]));

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        expect(cells[0].getAttribute('part')).to.equal('row-header');
        expect(cells[1].getAttribute('part')).to.equal(
            'row-header header-selected'
        );
        expect(cells[2].getAttribute('part')).to.equal('row-header');

        cleanup(el);
    });

    it('tints the selected row so the cursor stays findable', async () => {
        // The part alone does nothing visible. Before this the whole
        // header strip rendered one flat colour, and on a tall sheet
        // nothing said which row the cursor was in.
        const el = await attachWithSelection(new Set([2]));

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        const selected = getComputedStyle(cells[1]).backgroundColor;
        expect(selected).to.equal('rgb(230, 237, 244)');
        expect(getComputedStyle(cells[0]).backgroundColor).to.not.equal(
            selected
        );

        cleanup(el);
    });

    it('marks the selected row along the edge facing the cells', async () => {
        // A two-pixel blue edge beside the tint: a border, in place of the
        // number's own grey one.
        const el = await attachWithSelection(new Set([2]));

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        const selected = getComputedStyle(cells[1]);
        expect(`${selected.borderRightWidth} ${selected.borderRightStyle} ${selected.borderRightColor}`).to.equal(
            '2px solid rgb(99, 177, 255)'
        );
        // Nothing drawn over the edge: the one shadow is the divider, inside.
        expect(selected.boxShadow).to.equal('rgb(199, 199, 199) 0px -1px 0px 0px inset');
        const plain = getComputedStyle(cells[0]);
        expect(`${plain.borderRightWidth} ${plain.borderRightColor}`).to.equal('1px rgb(199, 199, 199)');

        cleanup(el);
    });

    it('writes the numbers in 13px on #fafafa', async () => {
        const el = await attachWithSelection(new Set());

        const style = getComputedStyle(el);
        expect(style.fontSize).to.equal('13px');
        expect(style.backgroundColor).to.equal('rgb(250, 250, 250)');

        cleanup(el);
    });

    it('takes the tint from --vaadin-spreadsheet-header-selected-bg', async () => {
        const el = await attachWithSelection(new Set([2]));
        el.style.setProperty(
            '--vaadin-spreadsheet-header-selected-bg',
            'rgb(1, 2, 3)'
        );

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        expect(getComputedStyle(cells[1]).backgroundColor).to.equal(
            'rgb(1, 2, 3)'
        );

        cleanup(el);
    });

    it('leaves every row plain when nothing is selected', async () => {
        const el = await attachWithSelection(new Set());

        const cells = el.shadowRoot!.querySelectorAll('.row-header-cell');
        for (const cell of cells) {
            expect(cell.getAttribute('part')).to.equal('row-header');
            expect(getComputedStyle(cell).backgroundColor).to.not.equal(
                'rgb(230, 237, 244)'
            );
        }

        cleanup(el);
    });
});
