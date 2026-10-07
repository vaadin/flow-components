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
