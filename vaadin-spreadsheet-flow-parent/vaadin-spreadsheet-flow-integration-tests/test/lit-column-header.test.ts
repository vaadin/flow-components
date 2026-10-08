/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';

import {
    SpreadsheetColumnHeader,
    emptyColumnHeaderRenderState,
    type ColumnHeaderRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetColumnHeader.js';

/**
 * Slice 143 — column header strip.
 *
 * Pins the wire-format contract of the column-header element: one
 * {@code .col-header-cell} per visible column with the column letter
 * (A, B, ..., Z, AA, AB, ...) and proper left + width inline styles.
 */

async function attachWithState(
    state: ColumnHeaderRenderState
): Promise<SpreadsheetColumnHeader> {
    const el = document.createElement(
        'vaadin-spreadsheet-internal-column-header'
    ) as SpreadsheetColumnHeader;
    el.renderState = state;
    document.body.appendChild(el);
    await el.updateComplete;
    return el;
}

function cleanup(el: HTMLElement) {
    el.remove();
}

function buildState(opts: {
    firstColumnIndex: number;
    lastColumnIndex: number;
    firstColumnPosition: number;
    colX: number[];
    colWidths: number[];
    headerHeight: number;
    totalGridWidthPx?: number;
    selectedColumns?: ReadonlySet<number>;
}): ColumnHeaderRenderState {
    const empty = emptyColumnHeaderRenderState();
    return {
        viewport: {
            firstColumnIndex: opts.firstColumnIndex,
            firstColumnPosition: opts.firstColumnPosition,
            lastColumnIndex: opts.lastColumnIndex,
            lastColumnPosition: 0,
            firstRowIndex: 1,
            firstRowPosition: 0,
            lastRowIndex: 0,
            lastRowPosition: 0,
        },
        colX: opts.colX,
        colWidths: opts.colWidths,
        headerHeight: opts.headerHeight,
        totalGridWidthPx: opts.totalGridWidthPx ?? empty.totalGridWidthPx,
        selectedColumns: opts.selectedColumns ?? empty.selectedColumns,
    };
}

describe('emptyColumnHeaderRenderState — helper for tests + connector initial state', () => {
    it('returns an empty viewport with zero headerHeight', () => {
        const state = emptyColumnHeaderRenderState();

        expect(state.viewport.lastColumnIndex).to.be.lessThan(
            state.viewport.firstColumnIndex
        );
        expect(state.colX).to.deep.equal([]);
        expect(state.colWidths).to.deep.equal([]);
        expect(state.headerHeight).to.equal(0);
        expect(state.totalGridWidthPx).to.equal(0);
    });

    it('returns a fresh object on each call', () => {
        const a = emptyColumnHeaderRenderState();
        const b = emptyColumnHeaderRenderState();

        expect(a).to.not.equal(b);
    });
});

describe('SpreadsheetColumnHeader — element registration', () => {
    it('is a registered custom element', () => {
        expect(customElements.get('vaadin-spreadsheet-internal-column-header'))
            .to.equal(SpreadsheetColumnHeader);
    });

    it('is constructable with default state', () => {
        const el = document.createElement(
            'vaadin-spreadsheet-internal-column-header'
        ) as SpreadsheetColumnHeader;

        expect(el).to.be.instanceOf(SpreadsheetColumnHeader);
        expect(el.renderState).to.deep.equal(emptyColumnHeaderRenderState());
    });
});

describe('SpreadsheetColumnHeader — render with header active', () => {
    it('renders nothing when headerHeight is 0', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 0,
            })
        );

        expect(el.shadowRoot!.querySelectorAll('.col-header-cell')).to.have.length(0);

        cleanup(el);
    });

    it('renders nothing when viewport is empty (lastCol < firstCol)', async () => {
        const el = await attachWithState({
            ...emptyColumnHeaderRenderState(),
            headerHeight: 24,
        });

        expect(el.shadowRoot!.querySelectorAll('.col-header-cell')).to.have.length(0);

        cleanup(el);
    });

    it('renders one cell per visible column with .col<N> classes', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        expect(cells).to.have.length(3);
        expect(cells[0].classList.contains('col1')).to.be.true;
        expect(cells[1].classList.contains('col2')).to.be.true;
        expect(cells[2].classList.contains('col3')).to.be.true;

        cleanup(el);
    });

    it('renders Excel column letters: A, B, C, ..., Z, AA, AB', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        expect(cells[0].textContent?.trim()).to.equal('A');
        expect(cells[1].textContent?.trim()).to.equal('B');
        expect(cells[2].textContent?.trim()).to.equal('C');

        cleanup(el);
    });

    it('renders letters past Z correctly (AA at col 27, AB at col 28)', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 27,
                lastColumnIndex: 28,
                firstColumnPosition: 1300,
                colX: [1300, 1350],
                colWidths: [50, 50],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        expect(cells[0].textContent?.trim()).to.equal('AA');
        expect(cells[1].textContent?.trim()).to.equal('AB');

        cleanup(el);
    });

    it('positions each header cell at colX[i] with width = colWidths[i]', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 60, 40],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll(
            '.col-header-cell'
        ) as NodeListOf<HTMLElement>;
        expect(cells[0].style.left).to.equal('0px');
        expect(cells[0].style.width).to.equal('50px');
        expect(cells[1].style.left).to.equal('50px');
        expect(cells[1].style.width).to.equal('60px');
        expect(cells[2].style.left).to.equal('100px');
        expect(cells[2].style.width).to.equal('40px');

        cleanup(el);
    });

    it('sizes each header cell height + line-height to the headerHeight', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 1,
                firstColumnPosition: 0,
                colX: [0],
                colWidths: [50],
                headerHeight: 30,
            })
        );

        const cell = el.shadowRoot!.querySelector(
            '.col-header-cell'
        ) as HTMLElement;
        expect(cell.style.height).to.equal('30px');
        // line-height = height so the letter is vertically centered.
        expect(cell.style.lineHeight).to.equal('30px');

        cleanup(el);
    });

    it('centres the letter on the whole column, divider included, as the GWT client did', async () => {
        // The GWT client's divider took no room from the letter, so the
        // letter sat half a pixel further right than in a column whose
        // divider is a border of its own.
        for (const width of [70, 71]) {
            const el = await attachWithState(
                buildState({
                    firstColumnIndex: 1,
                    lastColumnIndex: 1,
                    firstColumnPosition: 0,
                    colX: [0],
                    colWidths: [width],
                    headerHeight: 27,
                })
            );

            const cell = el.shadowRoot!.querySelector('.col-header-cell') as HTMLElement;
            const range = document.createRange();
            range.selectNodeContents(cell);
            const text = range.getBoundingClientRect();
            const middle = text.left + text.width / 2 - cell.getBoundingClientRect().left;
            expect(middle, `column of ${width}px`).to.be.closeTo(width / 2, 0.02);
            cleanup(el);
        }
    });

    it('skips columns whose width is 0 (hidden columns)', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 50],
                colWidths: [50, 0, 50],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        // Col 2 hidden → only 2 cells, for cols 1 and 3.
        expect(cells).to.have.length(2);
        expect(cells[0].classList.contains('col1')).to.be.true;
        expect(cells[1].classList.contains('col3')).to.be.true;

        cleanup(el);
    });

    it('host has position:sticky CSS so the strip pins to the top of the scroll container', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 1,
                firstColumnPosition: 0,
                colX: [0],
                colWidths: [50],
                headerHeight: 24,
            })
        );
        document.body.appendChild(el);

        const computed = getComputedStyle(el);
        // Verify the sticky positioning is applied from static styles.
        expect(computed.position).to.equal('sticky');

        cleanup(el);
    });
});

describe('SpreadsheetColumnHeader — click delegation (slice 148)', () => {
    it('puts data-col on each rendered header cell', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 24,
            })
        );

        const cells = el.shadowRoot!.querySelectorAll(
            '.col-header-cell'
        ) as NodeListOf<HTMLElement>;
        expect(cells[0].dataset.col).to.equal('1');
        expect(cells[1].dataset.col).to.equal('2');
        expect(cells[2].dataset.col).to.equal('3');

        cleanup(el);
    });

    it('dispatches column-header-click on click with the column coord', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 24,
            })
        );

        let received: { col: number; shiftKey: boolean } | undefined;
        el.addEventListener('column-header-click', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            received = { col: detail.col, shiftKey: detail.shiftKey };
        });

        const cell = el.shadowRoot!.querySelector(
            '.col-header-cell.col2'
        ) as HTMLElement;
        cell.click();

        expect(received).to.deep.equal({ col: 2, shiftKey: false });
        cleanup(el);
    });

    it('column-header-click detail includes the shiftKey modifier', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 1,
                firstColumnPosition: 0,
                colX: [0],
                colWidths: [50],
                headerHeight: 24,
            })
        );

        let shiftKey = false;
        el.addEventListener('column-header-click', (e: Event) => {
            shiftKey = (e as CustomEvent).detail.shiftKey;
        });

        const cell = el.shadowRoot!.querySelector(
            '.col-header-cell'
        ) as HTMLElement;
        cell.dispatchEvent(new MouseEvent('click', {
            bubbles: true, composed: true, shiftKey: true,
        }));

        expect(shiftKey).to.be.true;
        cleanup(el);
    });
});

describe('SpreadsheetColumnHeader — re-renders on renderState reassignment', () => {
    it('updates cells when renderState changes', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 2,
                firstColumnPosition: 0,
                colX: [0, 50],
                colWidths: [50, 50],
                headerHeight: 24,
            })
        );

        expect(el.shadowRoot!.querySelectorAll('.col-header-cell')).to.have.length(2);

        el.renderState = buildState({
            firstColumnIndex: 5,
            lastColumnIndex: 6,
            firstColumnPosition: 200,
            colX: [200, 250],
            colWidths: [50, 50],
            headerHeight: 24,
        });
        await el.updateComplete;

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        expect(cells).to.have.length(2);
        expect(cells[0].classList.contains('col5')).to.be.true;
        expect(cells[1].classList.contains('col6')).to.be.true;
        expect(cells[0].textContent?.trim()).to.equal('E');
        expect(cells[1].textContent?.trim()).to.equal('F');

        cleanup(el);
    });
});

describe('SpreadsheetColumnHeader — the active cell\'s column', () => {
    async function attachWithSelection(
        selectedColumns: ReadonlySet<number>
    ): Promise<SpreadsheetColumnHeader> {
        return attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 3,
                firstColumnPosition: 0,
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 20,
                selectedColumns,
            })
        );
    }

    it('shows the GWT client\'s select-the-column pointer over a letter', async () => {
        // A downward arrow: clicking a letter selects its column.
        const el = await attachWithSelection(new Set());

        expect(getComputedStyle(el.shadowRoot!.querySelector('.col-header-cell')!).cursor).to.equal('s-resize');
        expect(getComputedStyle(el.shadowRoot!.querySelector('.col-resize-handle')!).cursor).to.equal('col-resize');

        cleanup(el);
    });

    it('marks only the selected column with the header-selected part', async () => {
        const el = await attachWithSelection(new Set([2]));

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        expect(cells[0].getAttribute('part')).to.equal('column-header');
        expect(cells[1].getAttribute('part')).to.equal(
            'column-header header-selected'
        );
        expect(cells[2].getAttribute('part')).to.equal('column-header');

        cleanup(el);
    });

    it('tints the selected column so the cursor stays findable', async () => {
        // The part alone does nothing visible. Before this the whole
        // header strip rendered one flat colour, and on a wide sheet
        // nothing said which column the cursor was in.
        const el = await attachWithSelection(new Set([2]));

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        const selected = getComputedStyle(cells[1]).backgroundColor;
        expect(selected).to.equal('rgb(230, 237, 244)');
        expect(getComputedStyle(cells[0]).backgroundColor).to.not.equal(
            selected
        );

        cleanup(el);
    });

    it('marks the selected column along the edge facing the cells', async () => {
        // The GWT client's two-pixel blue edge under the tint: a border, in
        // place of the letter's own grey one.
        const el = await attachWithSelection(new Set([2]));

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        const selected = getComputedStyle(cells[1]);
        expect(`${selected.borderBottomWidth} ${selected.borderBottomStyle} ${selected.borderBottomColor}`).to.equal(
            '2px solid rgb(99, 177, 255)'
        );
        // Nothing drawn over the edge: the one shadow is the divider, inside.
        expect(selected.boxShadow).to.equal('rgb(199, 199, 199) -1px 0px 0px 0px inset');
        const plain = getComputedStyle(cells[0]);
        expect(`${plain.borderBottomWidth} ${plain.borderBottomColor}`).to.equal('1px rgb(199, 199, 199)');

        cleanup(el);
    });

    it('draws a letter on the GWT client\'s gradient, down to a shade darker', async () => {
        const el = await attachWithSelection(new Set());

        const image = getComputedStyle(el.shadowRoot!.querySelector('.col-header-cell')!).backgroundImage;
        // Blended in sRGB, as the GWT client's two hex colours were: with the
        // mixed colour in it, the gradient would blend in Oklab, a level
        // lighter on some rows.
        expect(image).to.match(/^linear-gradient\(in srgb, rgb\(250, 250, 250\) 2%, .+ 98%\)$/);
        // #fafafa four parts in a hundred towards black is #f0f0f0.
        const probe = document.createElement('div');
        probe.style.color = image.replace(/^linear-gradient\(in srgb, rgb\(250, 250, 250\) 2%, (.+) 98%\)$/, '$1');
        document.body.appendChild(probe);
        expect(getComputedStyle(probe).color).to.match(/^(rgb\(240, 240, 240\)|color\(srgb 0\.94\d* 0\.94\d* 0\.94\d*\))$/);
        probe.remove();

        cleanup(el);
    });

    it('lets an application\'s part border replace the marker, as it did the GWT client\'s', async () => {
        const style = document.createElement('style');
        style.textContent =
            'vaadin-spreadsheet-internal-column-header::part(header-selected) { border-bottom: 2px solid rgb(0, 0, 255); }';
        document.head.appendChild(style);
        const el = await attachWithSelection(new Set([2]));

        const selected = getComputedStyle(el.shadowRoot!.querySelectorAll('.col-header-cell')[1]);
        expect(selected.borderBottomColor).to.equal('rgb(0, 0, 255)');
        // Nothing drawn over it: the one shadow is the divider, inside.
        expect(selected.boxShadow).to.equal('rgb(199, 199, 199) -1px 0px 0px 0px inset');

        style.remove();
        cleanup(el);
    });

    it('writes the letters in 13px on #fafafa, as the GWT client did', async () => {
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

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        expect(getComputedStyle(cells[1]).backgroundColor).to.equal(
            'rgb(1, 2, 3)'
        );

        cleanup(el);
    });

    it('leaves every column plain when nothing is selected', async () => {
        const el = await attachWithSelection(new Set());

        const cells = el.shadowRoot!.querySelectorAll('.col-header-cell');
        for (const cell of cells) {
            expect(cell.getAttribute('part')).to.equal('column-header');
            expect(getComputedStyle(cell).backgroundColor).to.not.equal(
                'rgb(230, 237, 244)'
            );
        }

        cleanup(el);
    });
});

describe('SpreadsheetColumnHeader — the documented border colour', () => {
    it('draws its borders in --vaadin-spreadsheet-header-border-color', async () => {
        // The documented property used to be declared and read by nothing:
        // the headers took an undocumented name with a different default,
        // so setting the documented one did nothing and header borders came
        // out a different grey from the cell borders beside them.
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 2,
                firstColumnPosition: 0,
                colX: [0, 50],
                colWidths: [50, 50],
                headerHeight: 20,
            })
        );
        el.style.setProperty(
            '--vaadin-spreadsheet-header-border-color',
            'rgb(9, 8, 7)'
        );

        const cell = el.shadowRoot!.querySelector('.col-header-cell')!;
        expect(getComputedStyle(cell).boxShadow).to.equal('rgb(9, 8, 7) -1px 0px 0px 0px inset');
        expect(getComputedStyle(cell).borderBottomColor).to.equal('rgb(9, 8, 7)');

        cleanup(el);
    });

    it('defaults to the same grey the cell borders use', async () => {
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 2,
                firstColumnPosition: 0,
                colX: [0, 50],
                colWidths: [50, 50],
                headerHeight: 20,
            })
        );

        const cell = el.shadowRoot!.querySelector('.col-header-cell')!;
        expect(getComputedStyle(cell).boxShadow).to.equal('rgb(199, 199, 199) -1px 0px 0px 0px inset');

        cleanup(el);
    });

    it('draws the divider inside the letter, so the border along the cells runs under it, as the GWT client did', async () => {
        // As a border of its own, the divider met the active column's 2px
        // marker in a diagonal join, and the marker's last pixel came out
        // half grey.
        const el = await attachWithState(
            buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 2,
                firstColumnPosition: 0,
                colX: [0, 50],
                colWidths: [50, 50],
                headerHeight: 20,
                selectedColumns: new Set([1]),
            })
        );

        const style = getComputedStyle(el.shadowRoot!.querySelector('.col-header-cell.col1')!);
        expect(style.borderRightStyle).to.equal('none');
        expect(style.boxShadow).to.contain('inset');
        expect(style.borderBottomWidth).to.equal('2px');

        cleanup(el);
    });
});

/**
 * The column outline, drawn as the GWT client drew it.
 *
 * The Lit gutter drew a hairline and a square "-" box before each group,
 * in 16px levels with no room for the level that expands everything. The
 * GWT client drew a bracket over the group - a line with a tick where it
 * starts - and a round grey button over its summary column, in 18px levels
 * divided by dotted lines.
 */
describe('SpreadsheetColumnHeader — the outline gutter', () => {
    function withGroups(groups: Array<{ level: number; collapsed?: boolean }>, inversed = false): ColumnHeaderRenderState {
        return {
            ...buildState({
                firstColumnIndex: 1,
                lastColumnIndex: 4,
                firstColumnPosition: 0,
                colX: [0, 50, 100, 150],
                colWidths: [50, 50, 50, 50],
                headerHeight: 27,
                totalGridWidthPx: 400,
            }),
            colGroupingMax: Math.max(...groups.map((g) => g.level)),
            colGroupingInversed: inversed,
            colGroupings: groups.map((g, i) => ({
                uniqueIndex: i + 1,
                level: g.level,
                startPx: 55,
                endPx: 175,
                buttonPx: 175,
                collapsed: g.collapsed ?? false,
            })),
        };
    }

    it('puts the letters under a gutter of 18px levels and one more', async () => {
        const el = await attachWithState(withGroups([{ level: 1 }, { level: 2 }]));

        const letter = el.shadowRoot!.querySelector<HTMLElement>('.col-header-cell')!;
        expect(letter.style.top).to.equal(`${3 + 3 * 18}px`);
        cleanup(el);
    });

    it('draws a bracket over the group, its tick where the group starts', async () => {
        const el = await attachWithState(withGroups([{ level: 1 }]));

        const line = el.shadowRoot!.querySelector<HTMLElement>('.col-grouping-line')!;
        const style = getComputedStyle(line);
        expect(`${style.borderTopWidth} ${style.borderLeftWidth} ${style.borderRightStyle}`).to.equal('2px 2px none');
        expect(style.borderTopColor).to.equal('rgb(199, 199, 199)');
        // Level 1, 9px down the gutter; the tick's border comes on top of
        // the width.
        expect(`${line.style.left} ${line.style.top} ${line.style.width}`).to.equal('55px 9px 120px');
        expect(line.getBoundingClientRect().width).to.equal(122);
        cleanup(el);
    });

    it('turns the tick to the far end when inversed', async () => {
        const el = await attachWithState(withGroups([{ level: 1 }], true));

        const style = getComputedStyle(el.shadowRoot!.querySelector('.col-grouping-line')!);
        expect(`${style.borderLeftStyle} ${style.borderRightWidth}`).to.equal('none 2px');
        cleanup(el);
    });

    it('draws a round grey button centred on the line at the summary column', async () => {
        const el = await attachWithState(withGroups([{ level: 2 }]));

        const button = el.shadowRoot!.querySelector<HTMLElement>('.col-grouping-toggle')!;
        const style = getComputedStyle(button);
        expect(`${style.width} ${style.height} ${style.borderRadius}`).to.equal('12px 12px 50%');
        expect(style.backgroundColor).to.equal('rgb(199, 199, 199)');
        expect(style.color).to.equal('rgb(255, 255, 255)');
        // Level 2's line is at 9 + 18; the button's centre sits on it.
        expect(`${button.style.left} ${button.style.top}`).to.equal(`${175 - 6}px ${9 + 18 + 1 - 6}px`);
        cleanup(el);
    });

    it('keeps only the button of a collapsed group', async () => {
        const el = await attachWithState(withGroups([{ level: 1, collapsed: true }]));

        expect(el.shadowRoot!.querySelector('.col-grouping-line')).to.be.null;
        expect(el.shadowRoot!.querySelector('.col-grouping-toggle')!.textContent!.trim()).to.equal('+');
        cleanup(el);
    });

    it('divides the levels with dotted lines', async () => {
        const el = await attachWithState(withGroups([{ level: 1 }, { level: 2 }]));

        const dividers = el.shadowRoot!.querySelectorAll<HTMLElement>('.col-grouping-separator');
        expect(Array.from(dividers).map((d) => d.style.top)).to.deep.equal(['18px', '36px']);
        expect(getComputedStyle(dividers[0]).borderBottomStyle).to.equal('dotted');
        cleanup(el);
    });

    it('starts the dotted lines at the sheet\'s edge, where the GWT client\'s did', async () => {
        // A dotted line's dots are laid from where it starts; started under
        // the corner, 81px left of column A, they fall on the GWT client's
        // pixels instead of between them.
        const el = await attachWithState({ ...withGroups([{ level: 1 }]), leadingPx: 81 });

        const divider = el.shadowRoot!.querySelector<HTMLElement>('.col-grouping-separator')!;
        expect(`${divider.style.left} ${divider.style.width}`).to.equal('-81px 481px');
        cleanup(el);
    });

    it('writes the sign on an 11px line at the top of the button, as the GWT client did', async () => {
        // Whatever the font: where a line that tall starts its text at the
        // button's top edge. Inside a border, the Lit client's sign sat a
        // pixel lower.
        const el = await attachWithState(withGroups([{ level: 1 }]));

        const button = el.shadowRoot!.querySelector<HTMLElement>('.col-grouping-toggle')!;
        const range = document.createRange();
        range.selectNodeContents(button);
        const text = range.getBoundingClientRect();
        expect(text.top - button.getBoundingClientRect().top).to.equal(Math.floor((11 - text.height) / 2));
        expect(getComputedStyle(button).boxShadow).to.contain('rgb(199, 199, 199)');
        cleanup(el);
    });

    it('gives the dotted lines an odd length, so the dots alternate from the start', async () => {
        // Along an even length the browser doubles the first dot and shifts
        // the rest a pixel off the GWT client's.
        const el = await attachWithState({ ...withGroups([{ level: 1 }]), leadingPx: 80 });

        const divider = el.shadowRoot!.querySelector<HTMLElement>('.col-grouping-separator')!;
        expect(divider.style.width).to.equal('481px');
        cleanup(el);
    });

    it('borders the gutter off from the letters below it, as the GWT client did', async () => {
        const el = await attachWithState(withGroups([{ level: 1 }]));

        const gutter = el.shadowRoot!.querySelector<HTMLElement>('.col-grouping-gutter')!;
        const style = getComputedStyle(gutter);
        // One level and one more: 3 + 2 x 18px, its border on the last.
        expect(gutter.getBoundingClientRect().height).to.equal(39);
        expect(`${style.borderBottomWidth} ${style.borderBottomStyle} ${style.borderBottomColor}`).to.equal(
            '1px solid rgb(199, 199, 199)'
        );
        cleanup(el);
    });

    it('leaves the gutter to the strip\'s own border without letters', async () => {
        const el = await attachWithState({ ...withGroups([{ level: 1 }]), headerHeight: 0 });

        expect(el.shadowRoot!.querySelector('.col-grouping-gutter')).to.be.null;
        cleanup(el);
    });
});
