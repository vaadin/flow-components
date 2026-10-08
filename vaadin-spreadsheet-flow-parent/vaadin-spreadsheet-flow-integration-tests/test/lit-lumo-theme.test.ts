/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { resetMouse, sendMouse } from '@web/test-runner-commands';

import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import {
    spreadsheetOverlayStyles,
    spreadsheetStyles,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/vaadin-spreadsheet-styles.js';
import { SpreadsheetCellGrid } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetCellGrid.js';
import {
    SpreadsheetColumnHeader,
    emptyColumnHeaderRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetColumnHeader.js';
import {
    SpreadsheetRowHeader,
    emptyRowHeaderRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetRowHeader.js';
import { SpreadsheetHeaderCorner } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetHeaderCorner.js';
import { SpreadsheetFormulaBar } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetFormulaBar.js';
import {
    SpreadsheetSheetTabs,
    emptySheetTabsRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetSheetTabs.js';
import { SpreadsheetContextMenu } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetContextMenu.js';
import {
    type CellGridRenderState,
    emptyCellGridRenderState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * SpreadsheetTheme.LUMO.
 *
 * `Spreadsheet.setTheme(SpreadsheetTheme.LUMO)` sets `theme="lumo"` on the
 * host, and the GWT client drew the whole sheet from Lumo's tokens under it.
 * The Lit rewrite dropped all of that with the GWT stylesheet, so the call
 * did nothing. The tokens here are made up, each one distinct, so every
 * assertion says which token reached which surface.
 */
const TOKENS = [
    '--lumo-base-color: rgb(251, 252, 253)',
    '--lumo-body-text-color: rgb(11, 12, 13)',
    '--lumo-contrast-5pct: rgba(5, 5, 5, 0.05)',
    '--lumo-contrast-20pct: rgba(20, 20, 20, 0.2)',
    '--lumo-contrast-30pct: rgba(30, 30, 30, 0.3)',
    '--lumo-contrast-50pct: rgba(50, 50, 50, 0.5)',
    '--lumo-primary-color: rgb(0, 0, 200)',
    '--lumo-primary-color-10pct: rgba(0, 0, 200, 0.1)',
    '--lumo-primary-color-50pct: rgba(0, 0, 200, 0.5)',
    '--lumo-primary-text-color: rgb(0, 0, 150)',
    '--lumo-success-color: rgb(0, 150, 0)',
    '--lumo-success-color-10pct: rgba(0, 150, 0, 0.1)',
    '--lumo-warning-color: rgb(255, 160, 0)',
    '--lumo-error-color: rgb(200, 0, 0)',
    '--lumo-font-family: monospace',
    '--lumo-font-size-xs: 11px',
    '--lumo-font-size-s: 13px',
    '--lumo-font-size-m: 15px',
    '--lumo-font-size-xl: 22px',
    '--lumo-line-height-m: 1.5',
    '--lumo-space-xs: 3px',
    '--lumo-space-s: 7px',
    '--lumo-space-m: 15px',
    '--lumo-space-l: 23px',
    '--lumo-border-radius-s: 2px',
    '--lumo-border-radius-m: 6px',
    '--lumo-box-shadow-xs: 0 1px 2px rgba(1, 1, 1, 0.1)',
    '--lumo-box-shadow-s: 0 2px 4px rgba(2, 2, 2, 0.2)',
    '--lumo-box-shadow-m: 0 3px 6px rgba(3, 3, 3, 0.3)',
    '--lumo-clickable-cursor: grab',
    '--lumo-icons-angle-down: "v"',
    '--lumo-icons-cross: "x"',
].join(';');

const SHADOW_S = 'rgba(2, 2, 2, 0.2) 0px 2px 4px 0px';
const TINT_5 = 'linear-gradient(rgba(5, 5, 5, 0.05), rgba(5, 5, 5, 0.05))';
const CONTRAST_20 = 'rgba(20, 20, 20, 0.2)';
const HALF_CONTRAST = 'linear-gradient(rgba(50, 50, 50, 0.5), rgba(50, 50, 50, 0.5))';
const BASE = 'rgb(251, 252, 253)';
const BODY_TEXT = 'rgb(11, 12, 13)';

interface Themed {
    wrapper: HTMLElement;
    host: HTMLElement;
    root: ShadowRoot;
    cleanup: () => void;
}

/**
 * A host as the component makes one: the spreadsheet stylesheet in its
 * shadow root, the Lumo tokens on an ancestor as an application's theme
 * sets them.
 */
function themedHost(theme: string | null, wrapperStyle = ''): Themed {
    const wrapper = document.createElement('div');
    wrapper.style.cssText = `${TOKENS};${wrapperStyle}`;
    const host = document.createElement('div');
    host.style.cssText = 'width:800px;height:300px;display:flex;flex-direction:column;';
    if (theme !== null) {
        host.setAttribute('theme', theme);
    }
    wrapper.appendChild(host);
    document.body.appendChild(wrapper);
    const root = host.attachShadow({ mode: 'open' });
    root.adoptedStyleSheets = [spreadsheetStyles.styleSheet!];
    return { wrapper, host, root, cleanup: () => wrapper.remove() };
}

/** An internal element inside a themed host, carrying the theme itself. */
async function inHost<T extends HTMLElement & { updateComplete: Promise<boolean> }>(
    tag: string,
    theme: string | null,
    configure: (el: T) => void,
    wrapperStyle = ''
): Promise<{ el: T; cleanup: () => void }> {
    const themed = themedHost(theme, wrapperStyle);
    const el = document.createElement(tag) as T;
    if (theme !== null) {
        el.setAttribute('theme', theme);
    }
    configure(el);
    themed.root.appendChild(el);
    await el.updateComplete;
    return { el, cleanup: themed.cleanup };
}

function cellData(col: number, row: number, cellStyle = 'cs0'): CellData {
    return { row, col, value: 'x', cellStyle, locked: false, needsMeasure: false, isPercentage: false };
}

const DEFAULT_STYLE =
    '--default-font-family:Calibri;font-family:var(--default-font-family);' +
    '--default-font-size:11pt;font-size:var(--default-font-size);';

/** Three columns by three rows, with a few cells styled the workbook's way. */
function gridState(extra: Partial<CellGridRenderState> = {}): CellGridRenderState {
    return {
        ...emptyCellGridRenderState(),
        viewport: {
            firstColumnIndex: 1,
            firstColumnPosition: 0,
            lastColumnIndex: 3,
            lastColumnPosition: 150,
            firstRowIndex: 1,
            firstRowPosition: 0,
            lastRowIndex: 3,
            lastRowPosition: 60,
        },
        colX: [0, 50, 100],
        rowY: [0, 20, 40],
        colWidths: [50, 50, 50],
        rowHeights: [20, 20, 20],
        cellData: new Map<string, CellData>([
            ['col1 row1', cellData(1, 1)],
            ['col2 row1', cellData(2, 1, 'cs5')],
            ['col3 row1', cellData(3, 1, 'cs6')],
        ]),
        cellStyleToCSSStyle: {
            0: DEFAULT_STYLE,
            // A cell in a font of its own, the way the server writes one.
            5: 'font-family: var(--default-font-family);--default-font-family:Arial;',
            6: 'font-size:20pt;',
        },
        ...extra,
    };
}

async function grid(theme: string | null, extra: Partial<CellGridRenderState> = {}, wrapperStyle = '') {
    return inHost<SpreadsheetCellGrid>(
        'vaadin-spreadsheet-internal-cell-grid',
        theme,
        (el) => {
            el.renderState = gridState(extra);
        },
        wrapperStyle
    );
}

function inGrid(el: HTMLElement, selector: string): HTMLElement {
    return el.shadowRoot!.querySelector<HTMLElement>(selector)!;
}

describe('Lumo theme — the host passes the theme to its parts', () => {
    function mount(theme: string | null): { themed: Themed; sheet: Spreadsheet } {
        const themed = themedHost(theme);
        const sheet = new Spreadsheet(themed.host, themed.root);
        return { themed, sheet };
    }

    function parts(sheet: Spreadsheet): HTMLElement[] {
        return [
            sheet.getFormulaBar(),
            sheet.getContextMenu(),
            sheet.getHeaderCorner(),
            sheet.getColumnHeader(),
            sheet.getRowHeader(),
            sheet.getCellGrid(),
            sheet.getSheetTabs(),
        ];
    }

    it('copies theme="lumo" onto every internal element', () => {
        const { themed, sheet } = mount('lumo');

        for (const part of parts(sheet)) {
            expect(part.getAttribute('theme'), part.tagName).to.equal('lumo');
        }

        sheet.dispose();
        themed.cleanup();
    });

    it('follows the host when the theme changes after it is made', async () => {
        const { themed, sheet } = mount(null);
        for (const part of parts(sheet)) {
            expect(part.hasAttribute('theme'), part.tagName).to.be.false;
        }

        themed.host.setAttribute('theme', 'lumo');
        await Promise.resolve();
        for (const part of parts(sheet)) {
            expect(part.getAttribute('theme'), part.tagName).to.equal('lumo');
        }

        themed.host.removeAttribute('theme');
        await Promise.resolve();
        for (const part of parts(sheet)) {
            expect(part.hasAttribute('theme'), part.tagName).to.be.false;
        }

        sheet.dispose();
        themed.cleanup();
    });

    it('draws the host on Lumo\'s ground, framed in its contrast, in the page\'s font', () => {
        // The GWT client set Lumo's font on the sheet's parts, not on the
        // host: its notes, menu and pop-ups were drawn in the page's font.
        const { themed, sheet } = mount('lumo');

        const style = getComputedStyle(themed.host);
        expect(style.backgroundColor).to.equal(BASE);
        expect(style.fontFamily).to.equal(getComputedStyle(themed.wrapper).fontFamily);
        expect(style.fontSize).to.equal(getComputedStyle(themed.wrapper).fontSize);
        expect(style.color).to.equal(BODY_TEXT);
        expect(getComputedStyle(sheet.getRoot()).borderTopColor).to.equal(CONTRAST_20);

        sheet.dispose();
        themed.cleanup();
    });

    it('keeps the GWT client\'s own look without the theme', () => {
        const { themed, sheet } = mount(null);

        const style = getComputedStyle(themed.host);
        expect(style.backgroundColor).to.equal('rgb(255, 255, 255)');
        expect(style.color).to.equal('rgb(71, 71, 71)');
        expect(getComputedStyle(sheet.getRoot()).borderTopColor).to.equal('rgb(199, 199, 199)');

        sheet.dispose();
        themed.cleanup();
    });
});

describe('Lumo theme — documented properties set on an ancestor', () => {
    // The host declared the four properties the GWT client documented, so
    // a value set anywhere above the element - an application theme's html
    // rule - never reached the sheet.
    for (const theme of [null, 'lumo']) {
        it(`reach the sheet ${theme ? 'under Lumo' : 'without a theme'}`, async () => {
            const { el, cleanup } = await grid(
                theme,
                {},
                '--vaadin-spreadsheet-cell-border-color: rgb(1, 2, 3); --vaadin-spreadsheet-text-color: rgb(4, 5, 6)'
            );

            expect(getComputedStyle(inGrid(el, '.cell.col1.row1')).borderRightColor).to.equal('rgb(1, 2, 3)');
            expect(getComputedStyle(el.getRootNode() instanceof ShadowRoot ? (el.getRootNode() as ShadowRoot).host : el).color).to.equal(
                'rgb(4, 5, 6)'
            );

            cleanup();
        });
    }

    it('win over the Lumo default when set on the element', async () => {
        const themed = themedHost('lumo');
        themed.host.style.setProperty('--vaadin-spreadsheet-border-color', 'rgb(7, 8, 9)');
        const sheet = new Spreadsheet(themed.host, themed.root);

        expect(getComputedStyle(sheet.getRoot()).borderTopColor).to.equal('rgb(7, 8, 9)');

        sheet.dispose();
        themed.cleanup();
    });
});

describe('Lumo theme — cells', () => {
    it('puts every cell in the Lumo font, the workbook\'s own fonts too', async () => {
        // As the GWT client did: its Lumo rule overrode the font variable
        // every style sets, so a cell styled in Arial showed in Lumo's font.
        const { el, cleanup } = await grid('lumo');

        expect(getComputedStyle(inGrid(el, '.cell.col1.row1')).fontFamily).to.equal('monospace');
        expect(getComputedStyle(inGrid(el, '.cell.col2.row1')).fontFamily).to.equal('monospace');

        cleanup();
    });

    it('sizes a cell at Lumo\'s small size unless its style sets a size', async () => {
        const { el, cleanup } = await grid('lumo');

        expect(getComputedStyle(inGrid(el, '.cell.col1.row1')).fontSize).to.equal('13px');
        expect(getComputedStyle(inGrid(el, '.cell.col3.row1')).fontSize).to.equal('26.6667px');

        cleanup();
    });

    it('keeps the workbook\'s fonts without the theme', async () => {
        const { el, cleanup } = await grid(null);

        expect(getComputedStyle(inGrid(el, '.cell.col1.row1')).fontFamily).to.equal('Calibri');
        expect(getComputedStyle(inGrid(el, '.cell.col2.row1')).fontFamily).to.equal('Arial');
        expect(getComputedStyle(inGrid(el, '.cell.col1.row1')).fontSize).to.equal('14.6667px');

        cleanup();
    });

    it('pads, grounds and borders a cell from Lumo\'s tokens', async () => {
        const { el, cleanup } = await grid('lumo');

        const style = getComputedStyle(inGrid(el, '.cell.col1.row2'));
        expect(style.paddingLeft).to.equal('3px');
        expect(style.paddingRight).to.equal('3px');
        expect(style.backgroundColor).to.equal(BASE);
        expect(style.borderRightColor).to.equal(CONTRAST_20);
        expect(style.borderBottomColor).to.equal(CONTRAST_20);

        cleanup();
    });

    it('pads a cell showing a custom editor by Lumo\'s smallest space all round', async () => {
        const { el, cleanup } = await grid('lumo', { cellSlots: { 'col1 row2': 'custom-editor-77' } });

        expect(getComputedStyle(inGrid(el, '.cell.col1.row2')).padding).to.equal('3px');

        cleanup();
    });

    it('draws the comment and invalid-formula markers in Lumo\'s warning and error colours', async () => {
        const { el, cleanup } = await grid('lumo', {
            commentCells: new Set(['col1 row2']),
            invalidFormulaCells: new Set(['col2 row2']),
        });

        expect(getComputedStyle(inGrid(el, '.comment-indicator')).backgroundColor).to.equal('rgb(255, 160, 0)');
        expect(getComputedStyle(inGrid(el, '.invalidformula-indicator')).backgroundColor).to.equal('rgb(200, 0, 0)');

        cleanup();
    });
});

describe('Lumo theme — the selection', () => {
    const SELECTION = {
        selection: {
            activeCell: { col: 1, row: 1 },
            activeRange: { c1: 1, c2: 2, r1: 1, r2: 2 },
            auxRanges: [],
        },
    } as Partial<CellGridRenderState>;

    it('outlines it in the primary colour, with a success-coloured handle on a base ring', async () => {
        const { el, cleanup } = await grid('lumo', SELECTION);

        expect(getComputedStyle(inGrid(el, '.sheet-selection .s-top')).backgroundColor).to.equal('rgb(0, 0, 200)');
        const corner = getComputedStyle(inGrid(el, '.selection-corner'));
        expect(corner.backgroundColor).to.equal('rgb(0, 150, 0)');
        expect(corner.outlineColor).to.equal(BASE);

        cleanup();
    });

    it('tints the range in the primary tint', async () => {
        const { el, cleanup } = await grid('lumo', SELECTION);

        expect(getComputedStyle(inGrid(el, '.cell.col2.row1')).boxShadow).to.contain('rgba(0, 0, 200, 0.1)');

        cleanup();
    });

    it('draws the fill drag on Lumo\'s success tint', async () => {
        const { el, cleanup } = await grid('lumo', { ...SELECTION, paintRect: { c1: 1, c2: 2, r1: 1, r2: 3 } });

        const paint = getComputedStyle(inGrid(el, '.paintmode'));
        expect(paint.backgroundColor).to.equal('rgba(0, 150, 0, 0.1)');
        expect(paint.borderTopColor).to.equal('rgb(0, 150, 0)');

        cleanup();
    });
});

describe('Lumo theme — the cell editor', () => {
    const EDITING = { editor: { col: 1, row: 1, initialValue: 'x' } } as Partial<CellGridRenderState>;

    it('drops the border for a ring in the editor colour, lifted off the sheet', async () => {
        const { el, cleanup } = await grid('lumo', EDITING);

        const style = getComputedStyle(inGrid(el, '.cell-editor'));
        expect(style.borderTopWidth).to.equal('0px');
        expect(`${style.outlineWidth} ${style.outlineStyle} ${style.outlineColor}`).to.equal('1px solid rgb(0, 0, 200)');
        expect(style.boxShadow).to.equal(SHADOW_S);
        expect(style.marginTop).to.equal('-1px');
        expect(style.marginLeft).to.equal('-1px');

        cleanup();
    });

    it('types in the Lumo font, as the cell under it shows', async () => {
        const { el, cleanup } = await grid('lumo', EDITING);

        expect(getComputedStyle(inGrid(el, '.cell-editor')).fontFamily).to.equal('monospace');

        cleanup();
    });

    // A red cell, the way the server writes one.
    const RED_CELL = {
        editor: { col: 2, row: 1, initialValue: 'x' },
        cellStyleToCSSStyle: { 0: DEFAULT_STYLE, 5: 'color: rgb(255, 0, 0);' },
    } as Partial<CellGridRenderState>;

    it('types in the sheet\'s text colour, whatever colour the cell is', async () => {
        const { el, cleanup } = await grid('lumo', RED_CELL);

        expect(getComputedStyle(inGrid(el, '.cell.col2.row1')).color).to.equal('rgb(255, 0, 0)');
        expect(getComputedStyle(inGrid(el, '.cell-editor')).color).to.equal(BODY_TEXT);

        cleanup();
    });

    it('types in the text colour an application sets', async () => {
        const { el, cleanup } = await grid('lumo', RED_CELL, '--vaadin-spreadsheet-text-color: rgb(1, 2, 3)');

        expect(getComputedStyle(inGrid(el, '.cell-editor')).color).to.equal('rgb(1, 2, 3)');

        cleanup();
    });

    it('types in the cell\'s own colour without the theme', async () => {
        const { el, cleanup } = await grid(null, RED_CELL);

        expect(getComputedStyle(inGrid(el, '.cell-editor')).color).to.equal('rgb(255, 0, 0)');

        cleanup();
    });

    it('types in the cell\'s own font without the theme', async () => {
        const { el, cleanup } = await grid(null, EDITING);

        const style = getComputedStyle(inGrid(el, '.cell-editor'));
        expect(style.fontFamily).to.equal('Calibri');
        expect(style.boxShadow).to.equal('rgba(0, 0, 0, 0.75) 0px 0px 10px 0px');

        cleanup();
    });
});

describe('Lumo theme — the filter button', () => {
    const BUTTON = {
        popupButtons: [{ col: 2, row: 1, leftPx: 100, topPx: 20, cellHeightPx: 20, active: false }],
    } as Partial<CellGridRenderState>;

    it('is a square as tall as its cell, tinted, with Lumo\'s chevron', async () => {
        const { el, cleanup } = await grid('lumo', BUTTON);

        const button = inGrid(el, '.popup-button');
        const box = button.getBoundingClientRect();
        expect([box.width, box.height]).to.deep.equal([21, 21]);
        const style = getComputedStyle(button);
        expect(style.borderTopColor).to.equal(CONTRAST_20);
        expect(style.backgroundImage).to.equal(TINT_5);
        expect(style.borderTopLeftRadius).to.equal('2px');
        expect(style.cursor).to.equal('grab');
        const arrow = getComputedStyle(button, '::after');
        expect(arrow.content).to.equal('"v"');
        expect(arrow.fontFamily).to.equal('lumo-icons');
        expect(arrow.fontSize).to.equal('11px');

        cleanup();
    });

    it('stays the GWT client\'s small box with its arrow without the theme', async () => {
        const { el, cleanup } = await grid(null, BUTTON);

        const button = inGrid(el, '.popup-button');
        const box = button.getBoundingClientRect();
        expect([box.width, box.height]).to.deep.equal([13, 13]);
        expect(getComputedStyle(button, '::after').content).to.equal('"▼"');

        cleanup();
    });

    it('marks an active filter with Lumo\'s inset shadow', async () => {
        const { el, cleanup } = await grid('lumo', {
            popupButtons: [{ col: 2, row: 1, leftPx: 100, topPx: 20, cellHeightPx: 20, active: true }],
        });

        expect(getComputedStyle(inGrid(el, '.popup-button')).boxShadow).to.equal('rgba(1, 1, 1, 0.1) 0px 1px 2px 0px inset');

        cleanup();
    });
});

describe('Lumo theme — comments', () => {
    it('shows a comment on Lumo\'s card', async () => {
        const { el, cleanup } = await grid('lumo', {
            commentCells: new Set(['col1 row2']),
            commentText: { 'col1 row2': 'A note' },
            commentAuthors: { 'col1 row2': 'Ann' },
            visibleCommentKeys: ['col1 row2'],
        });

        const card = getComputedStyle(inGrid(el, '.v-spreadsheet-comment-overlay'));
        expect(card.paddingTop).to.equal('7px');
        expect(card.borderTopLeftRadius).to.equal('6px');
        expect(card.color).to.equal(BODY_TEXT);
        expect(card.backgroundColor).to.equal(BASE);
        expect(card.borderTopColor).to.equal(CONTRAST_20);
        expect(card.boxShadow).to.equal('rgba(3, 3, 3, 0.3) 0px 3px 6px 0px');
        const author = getComputedStyle(inGrid(el, '.comment-overlay-author'));
        expect(author.paddingBottom).to.equal('7px');
        expect(author.fontSize).to.equal('11px');

        cleanup();
    });
});

describe('Lumo theme — the line along a freeze', () => {
    it('is drawn in Lumo\'s half contrast, solid over its ground', async () => {
        const { el, cleanup } = await grid('lumo', {
            viewport: {
                firstColumnIndex: 3,
                firstColumnPosition: 100,
                lastColumnIndex: 4,
                lastColumnPosition: 200,
                firstRowIndex: 3,
                firstRowPosition: 40,
                lastRowIndex: 4,
                lastRowPosition: 80,
            },
            colX: [100, 150],
            rowY: [40, 60],
            colWidths: [50, 50],
            rowHeights: [20, 20],
            verticalSplitPosition: 2,
            frozenRowY: [0, 20],
            frozenRowHeights: [20, 20],
            totalGridWidthPx: 1000,
            totalGridHeightPx: 500,
        });

        // Lumo's line colour laid over the sheet's ground, solid, as the GWT
        // client's pane border lay over it: see-through, it darkened where
        // its pieces overlapped and over the cells scrolling under it.
        const line = getComputedStyle(inGrid(el, '.freeze-line'));
        expect(line.backgroundImage).to.equal(HALF_CONTRAST);
        expect(line.backgroundColor).to.equal(BASE);

        cleanup();
    });

    it('is drawn solid in the headers too', async () => {
        // Columns and rows 2 and 3 in view, past one frozen of each.
        const viewport = {
            firstColumnIndex: 2,
            firstColumnPosition: 50,
            lastColumnIndex: 3,
            lastColumnPosition: 150,
            firstRowIndex: 2,
            firstRowPosition: 20,
            lastRowIndex: 3,
            lastRowPosition: 60,
        };
        const letters = await inHost<SpreadsheetColumnHeader>('vaadin-spreadsheet-internal-column-header', 'lumo', (el) => {
            el.renderState = {
                ...emptyColumnHeaderRenderState(),
                viewport,
                colX: [50, 100],
                colWidths: [50, 50],
                headerHeight: 27,
                horizontalSplitPosition: 1,
                frozenColX: [0],
                frozenColWidths: [50],
                frozenRows: true,
            };
        });
        const numbers = await inHost<SpreadsheetRowHeader>('vaadin-spreadsheet-internal-row-header', 'lumo', (el) => {
            el.renderState = {
                ...emptyRowHeaderRenderState(),
                viewport,
                rowY: [20, 40],
                rowHeights: [20, 20],
                headerWidth: 50,
                verticalSplitPosition: 1,
                frozenRowY: [0],
                frozenRowHeights: [20],
                frozenColumns: true,
            };
        });
        // Rows frozen alone, scrolled right: the piece of their line that
        // slid left under the numbers.
        const slid = await inHost<SpreadsheetRowHeader>('vaadin-spreadsheet-internal-row-header', 'lumo', (el) => {
            el.renderState = {
                ...emptyRowHeaderRenderState(),
                viewport,
                rowY: [20, 40],
                rowHeights: [20, 20],
                headerWidth: 50,
                verticalSplitPosition: 1,
                frozenRowY: [0],
                frozenRowHeights: [20],
                scrollLeft: 20,
            };
        });

        // Three hosts left on the page would push the ones after them out of
        // the window.
        try {
            for (const line of [
                letters.el.shadowRoot!.querySelector('.frozen-header-strip > .freeze-line')!,
                numbers.el.shadowRoot!.querySelector('.frozen-header-strip > .freeze-line')!,
                slid.el.shadowRoot!.querySelector('.freeze-gap.slid')!,
            ]) {
                const style = getComputedStyle(line);
                expect(style.backgroundImage).to.equal(HALF_CONTRAST);
                expect(style.backgroundColor).to.equal(BASE);
            }
        } finally {
            letters.cleanup();
            numbers.cleanup();
            slid.cleanup();
        }
    });
});

describe('Lumo theme — the headers', () => {
    async function columns(theme: string | null) {
        return inHost<SpreadsheetColumnHeader>('vaadin-spreadsheet-internal-column-header', theme, (el) => {
            el.renderState = {
                ...emptyColumnHeaderRenderState(),
                viewport: {
                    firstColumnIndex: 1,
                    firstColumnPosition: 0,
                    lastColumnIndex: 3,
                    lastColumnPosition: 150,
                    firstRowIndex: 1,
                    firstRowPosition: 0,
                    lastRowIndex: 1,
                    lastRowPosition: 20,
                },
                colX: [0, 50, 100],
                colWidths: [50, 50, 50],
                headerHeight: 27,
                totalGridWidthPx: 150,
                selectedColumns: new Set([2]),
                colGroupings: [{ level: 1, startIndex: 1, endIndex: 2, collapsed: false } as any],
                colGroupingMax: 1,
            };
        });
    }

    async function rows(theme: string | null) {
        return inHost<SpreadsheetRowHeader>('vaadin-spreadsheet-internal-row-header', theme, (el) => {
            el.renderState = {
                ...emptyRowHeaderRenderState(),
                viewport: {
                    firstColumnIndex: 1,
                    firstColumnPosition: 0,
                    lastColumnIndex: 1,
                    lastColumnPosition: 50,
                    firstRowIndex: 1,
                    firstRowPosition: 0,
                    lastRowIndex: 3,
                    lastRowPosition: 60,
                },
                rowY: [0, 20, 40],
                rowHeights: [20, 20, 20],
                headerWidth: 50,
                totalGridHeightPx: 60,
                selectedRows: new Set([2]),
                rowGroupings: [{ level: 1, startIndex: 1, endIndex: 2, collapsed: false } as any],
                rowGroupingMax: 1,
            };
        });
    }

    it('tints the column strip over Lumo\'s ground, in its font', async () => {
        const { el, cleanup } = await columns('lumo');

        const style = getComputedStyle(el);
        expect(style.backgroundColor).to.equal(BASE);
        expect(style.backgroundImage).to.equal(TINT_5);
        expect(style.fontFamily).to.equal('monospace');
        expect(style.fontSize).to.equal('13px');
        expect(style.borderBottomColor).to.equal(CONTRAST_20);

        cleanup();
    });

    it('marks the active cell\'s column in the primary tint', async () => {
        const { el, cleanup } = await columns('lumo');

        const selected = getComputedStyle(el.shadowRoot!.querySelector('.col-header-cell.col2')!);
        expect(selected.backgroundColor).to.equal(BASE);
        expect(selected.backgroundImage).to.equal('linear-gradient(rgba(0, 0, 200, 0.1), rgba(0, 0, 200, 0.1))');
        expect(`${selected.borderBottomWidth} ${selected.borderBottomColor}`).to.equal('2px rgba(0, 0, 200, 0.5)');

        cleanup();
    });

    it('gives each column letter the strip\'s tint over the Lumo ground, as the GWT client did', async () => {
        // Opaque, so a frozen column's letter covers the one scrolled under it.
        const { el, cleanup } = await columns('lumo');

        const letter = getComputedStyle(el.shadowRoot!.querySelector('.col-header-cell.col1')!);
        expect(letter.backgroundColor).to.equal(BASE);
        expect(letter.backgroundImage).to.equal(TINT_5);

        cleanup();
    });

    it('gives each row number the strip\'s tint over the Lumo ground, as the GWT client did', async () => {
        const { el, cleanup } = await rows('lumo');

        const number = getComputedStyle(el.shadowRoot!.querySelector('.row-header-cell.row1')!);
        expect(number.backgroundColor).to.equal(BASE);
        expect(number.backgroundImage).to.equal(TINT_5);

        cleanup();
    });

    it('keeps the GWT client\'s marker without the theme', async () => {
        const { el, cleanup } = await columns(null);

        const selected = getComputedStyle(el.shadowRoot!.querySelector('.col-header-cell.col2')!);
        expect(selected.backgroundColor).to.equal('rgb(230, 237, 244)');
        expect(`${selected.borderBottomWidth} ${selected.borderBottomColor}`).to.equal('2px rgb(99, 177, 255)');

        cleanup();
    });

    it('draws the column outline toggle on Lumo\'s stronger tint', async () => {
        const { el, cleanup } = await columns('lumo');

        const toggle = getComputedStyle(el.shadowRoot!.querySelector('.col-grouping-toggle')!);
        expect(toggle.backgroundColor).to.equal(BASE);
        expect(toggle.backgroundImage).to.equal('linear-gradient(rgba(30, 30, 30, 0.3), rgba(30, 30, 30, 0.3))');
        // No ring: the one drawn inside the button is transparent.
        expect(toggle.boxShadow).to.equal('rgba(0, 0, 0, 0) 0px 0px 0px 1px inset');
        expect(toggle.fontSize).to.equal('13px');

        cleanup();
    });

    it('does the same for the row strip', async () => {
        const { el, cleanup } = await rows('lumo');

        const style = getComputedStyle(el);
        expect(style.backgroundImage).to.equal(TINT_5);
        expect(style.borderRightColor).to.equal(CONTRAST_20);
        const selected = getComputedStyle(el.shadowRoot!.querySelector('.row-header-cell.row2')!);
        expect(selected.backgroundImage).to.equal('linear-gradient(rgba(0, 0, 200, 0.1), rgba(0, 0, 200, 0.1))');
        expect(`${selected.borderRightWidth} ${selected.borderRightColor}`).to.equal('2px rgba(0, 0, 200, 0.5)');
        expect(getComputedStyle(el.shadowRoot!.querySelector('.row-grouping-toggle')!).fontSize).to.equal('11px');

        cleanup();
    });

    it('tints the corner and writes its level numbers in Lumo\'s text', async () => {
        const { el, cleanup } = await inHost<SpreadsheetHeaderCorner>(
            'vaadin-spreadsheet-internal-header-corner',
            'lumo',
            (corner) => {
                corner.renderState = { headerWidth: 50, headerHeight: 27, colGroupingMax: 1, rowGroupingMax: 1 };
            }
        );

        expect(getComputedStyle(el).backgroundImage).to.equal(TINT_5);
        const level = getComputedStyle(el.shadowRoot!.querySelector('.level-header')!);
        expect(level.color).to.equal(BODY_TEXT);
        expect(level.fontSize).to.equal('13px');
        expect(level.cursor).to.equal('grab');

        cleanup();
    });
});

describe('Lumo theme — the formula bar', () => {
    async function bar() {
        return inHost<SpreadsheetFormulaBar>('vaadin-spreadsheet-internal-formula-bar', 'lumo', (el) => {
            el.renderState = { address: 'A1', formula: '', namedRanges: ['Totals'] };
        });
    }

    it('sits on Lumo\'s ground in its font, ruled in its contrast', async () => {
        const { el, cleanup } = await bar();

        const style = getComputedStyle(el);
        expect(style.backgroundColor).to.equal(BASE);
        expect(style.borderBottomColor).to.equal(CONTRAST_20);
        expect(style.fontFamily).to.equal('monospace');
        expect(style.fontSize).to.equal('13px');

        cleanup();
    });

    it('indents both fields and tints the address panel', async () => {
        const { el, cleanup } = await bar();

        for (const input of Array.from(el.shadowRoot!.querySelectorAll('input'))) {
            expect(getComputedStyle(input).paddingLeft).to.equal('7px');
        }
        const panel = getComputedStyle(el.shadowRoot!.querySelector('.address-panel')!);
        expect(panel.backgroundColor).to.equal(BASE);
        expect(panel.backgroundImage).to.equal(TINT_5);
        expect(getComputedStyle(el.shadowRoot!.querySelector('.namedrange-arrow')!).fontSize).to.equal('11px');

        cleanup();
    });
});

describe('Lumo theme — the sheet tabs', () => {
    async function tabs() {
        return inHost<SpreadsheetSheetTabs>('vaadin-spreadsheet-internal-sheet-tabs', 'lumo', (el) => {
            el.style.width = '800px';
            el.renderState = {
                ...emptySheetTabsRenderState(),
                sheetNames: ['One', 'Two'],
                activeIndex: 1,
                infoLabel: 'Sum: 3',
            };
        });
    }

    it('draws the strip on Lumo\'s ground under its contrast', async () => {
        const { el, cleanup } = await tabs();

        const style = getComputedStyle(el);
        expect(style.backgroundColor).to.equal(BASE);
        expect(style.borderTopColor).to.equal(CONTRAST_20);
        expect(style.height).to.equal('28px');

        cleanup();
    });

    it('draws a tab tinted, in regular weight and Lumo\'s spacing', async () => {
        const { el, cleanup } = await tabs();

        const tab = getComputedStyle(el.shadowRoot!.querySelector('.sheet-tab:not(.active)')!);
        expect(tab.fontWeight).to.equal('400');
        expect(tab.fontFamily).to.equal('monospace');
        expect(tab.color).to.equal(BODY_TEXT);
        expect(tab.backgroundImage).to.equal(TINT_5);
        expect(tab.marginLeft).to.equal('7px');
        expect(tab.paddingTop).to.equal('3px');
        expect(tab.borderBottomLeftRadius).to.equal('6px');
        expect(tab.borderLeftColor).to.equal(CONTRAST_20);
        expect(tab.cursor).to.equal('grab');

        cleanup();
    });

    it('lifts the active tab off the strip in the primary text colour', async () => {
        const { el, cleanup } = await tabs();

        const active = getComputedStyle(el.shadowRoot!.querySelector('.sheet-tab.active')!);
        expect(active.backgroundImage).to.equal('none');
        expect(active.backgroundColor).to.equal(BASE);
        expect(active.color).to.equal('rgb(0, 0, 150)');
        expect(active.boxShadow).to.equal('rgba(1, 1, 1, 0.1) 0px 1px 2px 0px');

        cleanup();
    });

    it('colours the buttons in the primary text colour, the idle ones in its contrast', async () => {
        const { el, cleanup } = await tabs();

        expect(getComputedStyle(el.shadowRoot!.querySelector('.add-new-tab')!).color).to.equal('rgb(0, 0, 150)');
        expect(getComputedStyle(el.shadowRoot!.querySelector('.scroll-tabs-beginning')!).color).to.equal(CONTRAST_20);

        cleanup();
    });

    it('shows the status label on Lumo\'s tint, in its small size', async () => {
        const { el, cleanup } = await tabs();

        const label = getComputedStyle(el.shadowRoot!.querySelector('.sheet-tabsheet-infolabel')!);
        expect(label.backgroundColor).to.equal('rgba(5, 5, 5, 0.05)');
        expect(label.fontSize).to.equal('13px');
        expect(label.paddingLeft).to.equal('3px');

        cleanup();
    });
});

describe('Lumo theme — the context menu', () => {
    // A Lumo page's own font size, which the menu is drawn in.
    async function menu() {
        return inHost<SpreadsheetContextMenu>(
            'vaadin-spreadsheet-internal-context-menu',
            'lumo',
            (el) => {
                el.renderState = {
                    open: true,
                    x: 10,
                    y: 10,
                    actions: [{ caption: 'Cut', key: '1', type: 0, iconNodeId: 0 }],
                };
            },
            'font-size: 15px'
        );
    }

    it('is Lumo\'s card', async () => {
        const { el, cleanup } = await menu();

        const card = getComputedStyle(el.shadowRoot!.querySelector('.menu')!);
        expect(card.backgroundColor).to.equal(BASE);
        expect(card.color).to.equal(BODY_TEXT);
        expect(card.paddingTop).to.equal('3px');
        expect(card.borderTopLeftRadius).to.equal('2px');
        // Lumo's shadow and no edge, as the GWT client's Lumo menu had: the
        // documented edge colour is a hairline after the shadow, clear here.
        expect(card.boxShadow).to.equal(`${SHADOW_S}, rgba(0, 0, 0, 0) 0px 0px 0px 1px`);
        expect(card.borderTopWidth).to.equal('0px');

        cleanup();
    });

    it('darkens the item being pressed in Lumo\'s primary colour, as the GWT client did', async () => {
        const { el, cleanup } = await menu();
        const item = el.shadowRoot!.querySelector<HTMLElement>('.item')!;
        const box = item.getBoundingClientRect();

        await sendMouse({ type: 'move', position: [Math.round(box.left + 10), Math.round(box.top + box.height / 2)] });
        await sendMouse({ type: 'down' });
        try {
            const tint = getComputedStyle(item, '::before');
            expect(`${tint.backgroundColor} ${tint.opacity}`).to.equal('rgb(0, 0, 200) 0.15');
        } finally {
            await sendMouse({ type: 'up' });
            await resetMouse();
            cleanup();
        }
    });

    it('spaces its items as Lumo does and tints the focused one', async () => {
        const { el, cleanup } = await menu();

        const item = el.shadowRoot!.querySelector<HTMLElement>('.item')!;
        const style = getComputedStyle(item);
        expect(`${style.paddingLeft} ${style.paddingRight}`).to.equal('15px 23px');
        // One and a half of Lumo's line height, at the page's 15px.
        expect(style.lineHeight).to.equal('33.75px');
        item.focus();
        const focused = getComputedStyle(item);
        expect(focused.backgroundColor).to.equal('rgba(0, 0, 200, 0.1)');
        expect(focused.color).to.equal(BODY_TEXT);

        cleanup();
    });
});

describe('Lumo theme — the popup', () => {
    const APP = 'LUMO-APP';
    const originalVaadin = (window as any).Vaadin;

    afterEach(() => {
        (window as any).Vaadin = originalVaadin;
    });

    function open(theme: string | null): { themed: Themed; sheet: Spreadsheet } {
        const themed = themedHost(theme);
        const sheet = new Spreadsheet(themed.host, themed.root);
        sheet.setCols(5);
        sheet.setRows(5);
        sheet.setColW(new Array(5).fill(50));
        sheet.setRowH(new Array(5).fill(15));
        sheet.recomputeViewport();
        const content = document.createElement('div');
        content.textContent = 'filter';
        (window as any).Vaadin = { Flow: { clients: { [APP]: { getByNodeId: () => content } } } };
        sheet.onPopupButtonOpen(1, 2, '7', APP);
        return { themed, sheet };
    }

    it('is Lumo\'s card, its close box Lumo\'s cross at the right under a rule', () => {
        const { themed, sheet } = open('lumo');

        const overlay = sheet.getPopupOverlay();
        const card = getComputedStyle(overlay);
        expect(card.paddingTop).to.equal('3px');
        expect(card.borderTopLeftRadius).to.equal('6px');
        expect(card.color).to.equal(BODY_TEXT);
        expect(card.backgroundColor).to.equal(BASE);
        expect(card.borderTopColor).to.equal(CONTRAST_20);
        expect(card.boxShadow).to.equal(SHADOW_S);
        const header = getComputedStyle(overlay.querySelector('.popup-header')!);
        expect(header.borderBottomColor).to.equal(CONTRAST_20);
        expect(header.marginBottom).to.equal('3px');
        const close = overlay.querySelector<HTMLElement>('.popup-close')!;
        expect(close.getBoundingClientRect().right).to.equal(
            overlay.querySelector('.popup-header')!.getBoundingClientRect().right
        );
        const closeStyle = getComputedStyle(close);
        expect(closeStyle.color).to.equal('rgba(50, 50, 50, 0.5)');
        expect(closeStyle.fontSize).to.equal('22px');
        const glyph = getComputedStyle(close, '::before');
        expect(glyph.content).to.equal('"x"');
        expect(glyph.fontFamily).to.equal('lumo-icons');

        sheet.dispose();
        themed.cleanup();
    });

    it('captions the header at the left in bold, as the GWT client\'s Lumo popup did', () => {
        const { themed, sheet } = open('lumo');

        const caption = getComputedStyle(sheet.getPopupOverlay().querySelector('.header-caption')!);
        expect(`${caption.marginLeft} ${caption.fontWeight} ${caption.textAlign}`).to.equal('3px 700 left');
        expect(caption.color).to.equal(BODY_TEXT);

        sheet.dispose();
        themed.cleanup();
    });

    it('keeps the GWT client\'s card and close box without the theme', () => {
        const { themed, sheet } = open(null);

        const overlay = sheet.getPopupOverlay();
        const card = getComputedStyle(overlay);
        expect(`${card.paddingTop} ${card.borderTopLeftRadius} ${card.color}`).to.equal('4px 4px rgb(71, 71, 71)');
        const close = overlay.querySelector<HTMLElement>('.popup-close')!;
        expect(getComputedStyle(close).left).to.equal('0px');
        expect(getComputedStyle(close, '::before').content).to.equal('"×"');

        sheet.dispose();
        themed.cleanup();
    });
});

describe('Lumo theme — the filter popup\'s content', () => {
    // The content is the application's components in the host's light DOM,
    // so its layout comes from the document-level overlay styles.
    function filterContent(theme: string | null): { list: HTMLElement; content: HTMLElement; cleanup: () => void } {
        const style = document.createElement('style');
        style.textContent = spreadsheetOverlayStyles.cssText;
        document.head.appendChild(style);
        const wrapper = document.createElement('div');
        wrapper.style.cssText = TOKENS;
        const host = document.createElement('vaadin-spreadsheet');
        if (theme !== null) {
            host.setAttribute('theme', theme);
        }
        const content = document.createElement('div');
        content.className = 'spreadsheet-filter-table-content';
        const list = document.createElement('div');
        list.className = 'spreadsheet-item-filter-layout';
        content.appendChild(list);
        host.appendChild(content);
        wrapper.appendChild(host);
        document.body.appendChild(wrapper);
        return {
            list,
            content,
            cleanup: () => {
                wrapper.remove();
                style.remove();
            },
        };
    }

    it('lays the values out in a scrolling column, as the GWT client did', () => {
        const { list, content, cleanup } = filterContent(null);

        const listStyle = getComputedStyle(list);
        expect(`${listStyle.display} ${listStyle.flexDirection} ${listStyle.rowGap}`).to.equal('flex column 4px');
        expect(listStyle.maxHeight).to.equal('275px');
        expect(listStyle.overflowY).to.equal('auto');
        const contentStyle = getComputedStyle(content);
        expect(`${contentStyle.display} ${contentStyle.flexDirection} ${contentStyle.rowGap}`).to.equal('flex column 4px');

        cleanup();
    });

    it('spaces them by Lumo\'s spacing under its contrast rule', () => {
        const { list, content, cleanup } = filterContent('lumo');

        const listStyle = getComputedStyle(list);
        expect(listStyle.rowGap).to.equal('3px');
        expect(listStyle.borderBottomColor).to.equal(CONTRAST_20);
        expect(listStyle.borderBottomWidth).to.equal('1px');
        expect(getComputedStyle(content).rowGap).to.equal('3px');

        cleanup();
    });
});
