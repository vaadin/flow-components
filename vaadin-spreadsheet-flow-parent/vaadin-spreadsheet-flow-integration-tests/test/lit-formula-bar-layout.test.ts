/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';

// Imported for its side effect too: it defines the element.
import '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetFormulaBar.js';
import type { SpreadsheetFormulaBar } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetFormulaBar.js';

/**
 * The formula bar, laid out as the GWT client laid it out.
 *
 * The Lit bar was 24px tall with an 80px address field, so every sheet
 * started its headers and cells six pixels higher than it used to, and a
 * long address or range name did not fit. The named-range picker was a
 * second, visible box beside the address instead of the arrow at its end.
 */
describe('SpreadsheetFormulaBar — laid out as the GWT client laid it out', () => {
    async function mount(namedRanges: string[] = []): Promise<SpreadsheetFormulaBar> {
        const el = document.createElement(
            'vaadin-spreadsheet-internal-formula-bar'
        ) as SpreadsheetFormulaBar;
        el.style.width = '600px';
        el.renderState = { address: 'A1', formula: '', namedRanges };
        document.body.appendChild(el);
        await el.updateComplete;
        return el;
    }

    function part(el: SpreadsheetFormulaBar, selector: string): HTMLElement | null {
        return el.shadowRoot!.querySelector<HTMLElement>(selector);
    }

    it('is 30px tall, its border included', async () => {
        const el = await mount();

        expect(el.getBoundingClientRect().height).to.equal(30);

        el.remove();
    });

    it('gives the address 176px, centred on its own ground', async () => {
        const el = await mount();

        const panel = part(el, '.address-panel')!;
        expect(panel.getBoundingClientRect().width).to.equal(176);
        expect(getComputedStyle(panel).backgroundColor).to.equal('rgb(250, 250, 250)');
        expect(getComputedStyle(part(el, '.address')!).textAlign).to.equal('center');
        // The formula field takes the rest, right after the panel.
        const formula = part(el, '.formula')!.getBoundingClientRect();
        expect(formula.left - el.getBoundingClientRect().left).to.equal(176);

        el.remove();
    });

    it('shows no picker until the workbook names a range', async () => {
        const el = await mount();

        expect(part(el, '.namedrangebox')).to.be.null;
        expect(part(el, '.namedrange-arrow')).to.be.null;

        el.remove();
    });

    it('puts the picker at the right end of the address field', async () => {
        const el = await mount(['Totals']);

        const panel = part(el, '.address-panel')!.getBoundingClientRect();
        const arrow = part(el, '.namedrange-arrow')!;
        const box = arrow.getBoundingClientRect();
        expect(arrow.textContent).to.equal('▼');
        expect(box.width).to.equal(15);
        // Inside the panel's right border.
        expect(box.right).to.equal(panel.right - 1);
        // The address keeps the whole panel; the arrow sits over its end.
        expect(part(el, '.address')!.getBoundingClientRect().width).to.equal(175);

        el.remove();
    });

    it('draws the arrow where the GWT client drew it, a little below the middle', async () => {
        // Whatever the font: a pixel in from the left of its 15px, on a
        // 33px line from the top of the bar. Centred in the bar instead,
        // it sat a pixel and a half higher and further right.
        const el = await mount(['Totals']);

        const arrow = part(el, '.namedrange-arrow')!;
        const range = document.createRange();
        range.selectNodeContents(arrow);
        const text = range.getBoundingClientRect();
        expect(text.left - arrow.getBoundingClientRect().left).to.equal(1);
        expect(text.top - el.getBoundingClientRect().top).to.equal(Math.floor((33 - text.height) / 2));

        el.remove();
    });

    it('opens the list from the arrow', async () => {
        // The select is the thing clicked; it is just not seen.
        const el = await mount(['Totals']);

        const select = part(el, '.namedrangebox')!;
        expect(getComputedStyle(select).opacity).to.equal('0');
        const box = part(el, '.namedrange-arrow')!.getBoundingClientRect();
        const hit = el.shadowRoot!.elementFromPoint(box.left + box.width / 2, box.top + box.height / 2);
        expect(hit).to.equal(select);

        el.remove();
    });
});

/**
 * The address in the browser's own font for text fields, as the GWT
 * client's was: its stylesheet gave the field a size but never a font, so
 * Chrome showed Arial there, beside a formula in Helvetica. The Lit bar
 * wrote both in Helvetica, the same size but different letters.
 */
describe('SpreadsheetFormulaBar — the address in the browser\'s font for text fields', () => {
    /** The font the browser gives a text field no stylesheet has touched. */
    function browserFieldFont(): string {
        const host = document.createElement('div');
        document.body.appendChild(host);
        const input = document.createElement('input');
        host.attachShadow({ mode: 'open' }).appendChild(input);
        const family = getComputedStyle(input).fontFamily;
        host.remove();
        return family;
    }

    async function mount(wrapperStyle = ''): Promise<{ el: SpreadsheetFormulaBar; cleanup: () => void }> {
        const wrapper = document.createElement('div');
        wrapper.style.cssText = wrapperStyle;
        const el = document.createElement('vaadin-spreadsheet-internal-formula-bar') as SpreadsheetFormulaBar;
        el.renderState = { address: 'A1', formula: '=A1', namedRanges: [] };
        wrapper.appendChild(el);
        document.body.appendChild(wrapper);
        await el.updateComplete;
        return { el, cleanup: () => wrapper.remove() };
    }

    function font(el: SpreadsheetFormulaBar, selector: string): string {
        const style = getComputedStyle(el.shadowRoot!.querySelector(selector)!);
        return `${style.fontFamily} ${style.fontSize}`;
    }

    it('writes the address in the browser\'s font, at the bar\'s size', async () => {
        const { el, cleanup } = await mount();
        try {
            expect(font(el, '.address')).to.equal(`${browserFieldFont()} 14px`);
        } finally {
            cleanup();
        }
    });

    it('writes the formula in Helvetica', async () => {
        const { el, cleanup } = await mount();
        try {
            expect(font(el, '.formula')).to.equal('Helvetica 14px');
        } finally {
            cleanup();
        }
    });

    it('writes both in a font set for the bar', async () => {
        const { el, cleanup } = await mount('--vaadin-spreadsheet-formulabar-font: Courier');
        try {
            expect(font(el, '.address')).to.equal('Courier 14px');
            expect(font(el, '.formula')).to.equal('Courier 14px');
        } finally {
            cleanup();
        }
    });
});

/**
 * The bar's lines in the documented colours, as the GWT client drew them:
 * the line under the bar in the header border colour, the divider beside the
 * address in the sheet's border colour. The Lit bar drew both in a grey of
 * its own, so an application's border colours left them behind.
 */
describe('SpreadsheetFormulaBar — its lines in the documented border colours', () => {
    async function mount(wrapperStyle: string): Promise<{ el: SpreadsheetFormulaBar; cleanup: () => void }> {
        const wrapper = document.createElement('div');
        wrapper.style.cssText = wrapperStyle;
        const el = document.createElement('vaadin-spreadsheet-internal-formula-bar') as SpreadsheetFormulaBar;
        el.renderState = { address: 'A1', formula: '', namedRanges: [] };
        wrapper.appendChild(el);
        document.body.appendChild(wrapper);
        await el.updateComplete;
        return { el, cleanup: () => wrapper.remove() };
    }

    const COLOURS = '--vaadin-spreadsheet-header-border-color: rgb(9, 8, 7); --vaadin-spreadsheet-border-color: rgb(1, 2, 3);';

    function lines(el: SpreadsheetFormulaBar): string {
        const divider = getComputedStyle(el.shadowRoot!.querySelector('.address-panel')!).borderRightColor;
        return `${getComputedStyle(el).borderBottomColor} | ${divider}`;
    }

    it('rules the bar off in the header border colour and divides the address off in the sheet\'s', async () => {
        const { el, cleanup } = await mount(COLOURS);
        try {
            expect(lines(el)).to.equal('rgb(9, 8, 7) | rgb(1, 2, 3)');
        } finally {
            cleanup();
        }
    });

    it('keeps the GWT client\'s grey for both by default', async () => {
        const { el, cleanup } = await mount('');
        try {
            expect(lines(el)).to.equal('rgb(199, 199, 199) | rgb(199, 199, 199)');
        } finally {
            cleanup();
        }
    });

    it('draws both in the bar\'s own border colour when one is set', async () => {
        const { el, cleanup } = await mount(`${COLOURS} --vaadin-spreadsheet-formulabar-border: rgb(4, 5, 6);`);
        try {
            expect(lines(el)).to.equal('rgb(4, 5, 6) | rgb(4, 5, 6)');
        } finally {
            cleanup();
        }
    });
});
