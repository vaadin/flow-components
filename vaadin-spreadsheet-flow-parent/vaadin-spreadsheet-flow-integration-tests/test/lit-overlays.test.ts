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
    Spreadsheet,
    computePositionedOverlays,
    computePositionedPopupButtons,
    freezeGap,
    pastFreeze,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import type {
    OverlayInfo, PopupButtonState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';
import { SpreadsheetCellGrid } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetCellGrid.js';
import { emptyCellGridRenderState } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';

/**
 * Sheet overlay rendering.
 *
 * Pure tests for `computePositionedOverlays` (the position math
 * helper) + integration tests for setOverlays / setResources
 * wiring through the connector.
 */

describe('computePositionedOverlays — anchor positioning', () => {
    const uniformWidth = (_c: number) => 50;
    const uniformHeight = (_r: number) => 20;

    it('returns empty array when overlay map is empty', () => {
        const result = computePositionedOverlays({}, {}, uniformWidth, uniformHeight);
        expect(result).to.deep.equal([]);
    });

    it('positions an overlay at the anchor cell pixel position', () => {
        const overlays: Record<string, OverlayInfo> = {
            img1: { type: 'IMAGE', col: 3, row: 2, width: 100, height: 60, dx: 0, dy: 0 },
        };
        const resources = { img1: '/img/photo.png' };
        const result = computePositionedOverlays(overlays, resources, uniformWidth, uniformHeight);

        // col 3 → cumulative sum of widths 1..2 = 50+50 = 100.
        // row 2 → cumulative sum of heights 1..1 = 20.
        expect(result).to.have.length(1);
        expect(result[0].leftPx).to.equal(100);
        expect(result[0].topPx).to.equal(20);
        expect(result[0].widthPx).to.equal(100);
        // 60pt at 96 ppi = 80px. Height arrives in points; width does not.
        expect(result[0].heightPx).to.equal(80);
    });

    it('adds dx/dy offset to the anchor position', () => {
        const overlays: Record<string, OverlayInfo> = {
            img1: { type: 'IMAGE', col: 2, row: 2, width: 100, height: 60, dx: 7, dy: 11 },
        };
        const result = computePositionedOverlays(overlays, {}, uniformWidth, uniformHeight);
        // dx is pixels, dy is points — 11pt at 96 ppi is 14.67px.
        expect(result[0].leftPx).to.equal(50 + 7);
        expect(result[0].topPx).to.be.closeTo(20 + 44 / 3, 1e-9);
    });

    it('keeps the fractions of a point offset and height, as the GWT client did', () => {
        // A logo 11.0796pt down its cell and 17.9278pt tall. The GWT
        // client set both in pt and let the browser place the edges; cut
        // down to whole pixels, the logo sat a pixel high and short.
        const overlays: Record<string, OverlayInfo> = {
            logo: { type: 'IMAGE', col: 1, row: 1, width: 94.0997, height: 17.9278, dx: 0, dy: 11.0796 },
        };
        const result = computePositionedOverlays(overlays, {}, uniformWidth, uniformHeight);

        expect(result[0].topPx).to.be.closeTo(14.7728, 1e-4);
        expect(result[0].heightPx).to.be.closeTo(23.9037, 1e-4);
    });

    it('reads the vertical measurements as points and the horizontal as pixels', () => {
        // POI hands out anything vertical in points, the way row heights
        // arrive. Applying `height` as pixels renders every overlay a
        // quarter too short: a square icon comes out 20x15.
        const overlays: Record<string, OverlayInfo> = {
            icon: { type: 'IMAGE', col: 1, row: 1, width: 20, height: 15, dx: 0, dy: 0 },
        };
        const result = computePositionedOverlays(overlays, {}, uniformWidth, uniformHeight);

        expect(result[0].widthPx).to.equal(20);
        expect(result[0].heightPx).to.equal(20);
    });

    it('places a picture past the freeze line\'s own pixel when its cell is past a split', () => {
        // The GWT client put a picture in the pane of its anchor cell; the
        // scrolling pane started past the frozen panes' borders.
        const overlays: Record<string, OverlayInfo> = {
            frozen: { type: 'IMAGE', col: 2, row: 2, width: 20, height: 15, dx: 0, dy: 0 },
            past: { type: 'IMAGE', col: 3, row: 3, width: 20, height: 15, dx: 0, dy: 0 },
        };
        const [frozen, past] = computePositionedOverlays(
            overlays, {}, uniformWidth, uniformHeight, 96, { cols: 2, rows: 2 }
        );
        expect(`${frozen.leftPx} ${frozen.topPx}`).to.equal('50 20');
        expect(`${past.leftPx} ${past.topPx}`).to.equal(`${100 + 1} ${40 + 1}`);
    });

    it('honours a measured ppi over the 96 fallback', () => {
        const overlays: Record<string, OverlayInfo> = {
            icon: { type: 'IMAGE', col: 1, row: 1, width: 20, height: 15, dx: 0, dy: 30 },
        };
        const result = computePositionedOverlays(
            overlays, {}, uniformWidth, uniformHeight, 144
        );

        // 15pt at 144 ppi = 30px; 30pt = 60px.
        expect(result[0].heightPx).to.equal(30);
        expect(result[0].topPx).to.equal(60);
    });

    it('resolves IMAGE src from the resources map', () => {
        const overlays: Record<string, OverlayInfo> = {
            chart1: { type: 'IMAGE', col: 1, row: 1, width: 50, height: 50, dx: 0, dy: 0 },
        };
        const resources = { chart1: 'https://example.com/chart.png' };
        const result = computePositionedOverlays(overlays, resources, uniformWidth, uniformHeight);
        expect(result[0].src).to.equal('https://example.com/chart.png');
        expect(result[0].type).to.equal('IMAGE');
    });

    it('IMAGE src is undefined when the resources map lacks the id', () => {
        const overlays: Record<string, OverlayInfo> = {
            mystery: { type: 'IMAGE', col: 1, row: 1, width: 50, height: 50, dx: 0, dy: 0 },
        };
        const result = computePositionedOverlays(overlays, {}, uniformWidth, uniformHeight);
        expect(result[0].src).to.be.undefined;
    });

    it('COMPONENT overlays have no src regardless of resources map', () => {
        const overlays: Record<string, OverlayInfo> = {
            c1: { type: 'COMPONENT', col: 1, row: 1, width: 50, height: 50, dx: 0, dy: 0 },
        };
        const resources = { c1: 'should-not-be-used.png' };
        const result = computePositionedOverlays(overlays, resources, uniformWidth, uniformHeight);
        expect(result[0].src).to.be.undefined;
        expect(result[0].type).to.equal('COMPONENT');
    });

    it('returns the overlay id in the result', () => {
        const overlays: Record<string, OverlayInfo> = {
            'overlay-42': { type: 'IMAGE', col: 1, row: 1, width: 50, height: 50, dx: 0, dy: 0 },
        };
        const result = computePositionedOverlays(overlays, {}, uniformWidth, uniformHeight);
        expect(result[0].id).to.equal('overlay-42');
    });

    it('handles multiple overlays in stable order', () => {
        const overlays: Record<string, OverlayInfo> = {
            a: { type: 'IMAGE', col: 1, row: 1, width: 50, height: 50, dx: 0, dy: 0 },
            b: { type: 'IMAGE', col: 5, row: 5, width: 30, height: 30, dx: 0, dy: 0 },
        };
        const result = computePositionedOverlays(overlays, {}, uniformWidth, uniformHeight);
        expect(result).to.have.length(2);
        expect(result.map((o) => o.id).sort()).to.deep.equal(['a', 'b']);
    });

    it('respects variable column widths via the colWidth callback', () => {
        const variableWidth = (c: number) => c === 1 ? 100 : 50;
        const overlays: Record<string, OverlayInfo> = {
            img1: { type: 'IMAGE', col: 3, row: 1, width: 50, height: 50, dx: 0, dy: 0 },
        };
        const result = computePositionedOverlays(overlays, {}, variableWidth, uniformHeight);
        // col 3 → widths 1..2 sum = 100 + 50 = 150.
        expect(result[0].leftPx).to.equal(150);
    });
});

describe('computePositionedPopupButtons', () => {
    const uniformWidth = (_c: number) => 50;
    const uniformHeight = (_r: number) => 20;

    it('returns empty array when input is empty', () => {
        const result = computePositionedPopupButtons([], uniformWidth, uniformHeight);
        expect(result).to.deep.equal([]);
    });

    it('positions a button at the anchor cell\'s bottom-right corner', () => {
        const states: PopupButtonState[] = [
            { col: 2, row: 3, active: false, headerHidden: false,
              popupHeight: null, popupWidth: null, sheet: 'X' },
        ];
        const result = computePositionedPopupButtons(states, uniformWidth, uniformHeight);
        // col 2 → widths 1..2 sum = 100. row 3 → heights 1..3 sum = 60.
        expect(result[0].leftPx).to.equal(100);
        expect(result[0].topPx).to.equal(60);
        expect(result[0].col).to.equal(2);
        expect(result[0].row).to.equal(3);
        expect(result[0].active).to.be.false;
    });

    it('preserves active flag', () => {
        const states: PopupButtonState[] = [
            { col: 1, row: 1, active: true, headerHidden: false,
              popupHeight: null, popupWidth: null, sheet: 'X' },
        ];
        const result = computePositionedPopupButtons(states, uniformWidth, uniformHeight);
        expect(result[0].active).to.be.true;
    });

    it('moves a button past the freeze line\'s own pixel with its cell, and no other', () => {
        // Columns A-B and rows 1-2 frozen: a cell past both splits starts a
        // pixel further on each way; a frozen one stays put.
        const at = (col: number, row: number): PopupButtonState => ({
            col, row, active: false, headerHidden: false, popupHeight: null, popupWidth: null, sheet: 'X'
        });
        const [frozen, past] = computePositionedPopupButtons(
            [at(2, 2), at(3, 3)], uniformWidth, uniformHeight, { cols: 2, rows: 2 }
        );
        expect(`${frozen.leftPx} ${frozen.topPx}`).to.equal('100 40');
        expect(`${past.leftPx} ${past.topPx}`).to.equal(`${150 + 1} ${60 + 1}`);
    });
});

describe('the freeze line\'s own pixel', () => {
    it('is a pixel when any column or row is frozen, and none otherwise', () => {
        expect([0, 1, 5].map(freezeGap)).to.eql([0, 1, 1]);
    });

    it('comes before the columns or rows past the frozen ones only', () => {
        expect([1, 2, 3, 4].map((i) => pastFreeze(i, 2))).to.eql([0, 0, 1, 1]);
        expect(pastFreeze(4, 0), 'nothing frozen').to.equal(0);
    });
});

describe('Spreadsheet — component overlays', () => {
    /**
     * Charts and other server-side components are attached as Flow virtual
     * children: Flow creates the element and leaves it out of the document.
     * The connector has to parent it and point it at the slot the cell-grid
     * renders over the sheet.
     */
    const originalVaadin = (window as any).Vaadin;

    function attachSheetWithFlow(components: Record<string, HTMLElement>): {
        host: HTMLElement;
        sheet: Spreadsheet;
        cleanup: () => void;
    } {
        const host = document.createElement('div');
        host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
        (host as any).appId = 'ROOT';
        document.body.appendChild(host);
        // The wrapper renders into its own shadow root, and that boundary
        // is exactly what the forwarding slots exist to cross. A plain div
        // host would let a broken chain pass.
        const renderTarget = host.attachShadow({ mode: 'open' });
        (window as any).Vaadin = {
            Flow: {
                clients: {
                    ROOT: { getByNodeId: (n: number) => components[String(n)] ?? null },
                },
            },
        };
        const sheet = new Spreadsheet(host, renderTarget);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        return {
            host,
            sheet,
            cleanup: () => {
                sheet.dispose();
                host.remove();
                (window as any).Vaadin = originalVaadin;
            },
        };
    }

    function componentOverlay(col: number, row: number): OverlayInfo {
        return { type: 'COMPONENT', col, row, width: 120, height: 60, dx: 0, dy: 0 };
    }

    it('parents the component to the host and slots it', async () => {
        const chart = document.createElement('div');
        chart.id = 'the-chart';
        const { host, sheet, cleanup } = attachSheetWithFlow({ '31': chart });
        sheet.setOverlays({ '31': componentOverlay(2, 2) });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        expect(chart.parentElement).to.equal(host);
        expect(chart.getAttribute('slot')).to.equal('overlay-component-31');
        cleanup();
    });

    it('renders a matching slot inside the positioned wrapper', async () => {
        const chart = document.createElement('div');
        const { host, sheet, cleanup } = attachSheetWithFlow({ '31': chart });
        sheet.setOverlays({ '31': componentOverlay(2, 2) });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const wrapper = sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>(
            '.sheet-overlay-component'
        )!;
        expect(wrapper).to.not.be.null;
        const slot = wrapper.querySelector('slot')!;
        expect(slot.getAttribute('name')).to.equal('overlay-component-31');
        // The projection is what puts the component on the sheet. flatten
        // follows the forwarding slot that carries it across the second
        // shadow boundary. Compare identity, never deep-equality: chai
        // walks a DOM node's cyclic references and never returns.
        const assigned = slot.assignedElements({ flatten: true });
        expect(assigned).to.have.length(1);
        expect(assigned[0]).to.equal(chart);
        cleanup();
    });

    it('positions the wrapper at the anchor cell', async () => {
        const chart = document.createElement('div');
        const { host, sheet, cleanup } = attachSheetWithFlow({ '31': chart });
        sheet.setOverlays({ '31': componentOverlay(3, 2) });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const wrapper = sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>(
            '.sheet-overlay-component'
        )!;
        // col 3 → 50+50 = 100px across; row 2 → one 15pt row = 20px down.
        expect(wrapper.style.left).to.equal('100px');
        expect(wrapper.style.top).to.equal('20px');
        cleanup();
    });

    it('is exactly the size the workbook gives it', async () => {
        // The wrapper is the picture, nothing more. The GWT client drew
        // the workbook's offset as padding, so its box also covered the
        // corner of the anchor cell and measured wider and taller than
        // the picture by that offset - which is what made a minimized
        // chart, whose size is nothing at all, still measure 28x16.
        const chart = document.createElement('div');
        const { sheet, cleanup } = attachSheetWithFlow({ '31': chart });
        sheet.setOverlays({
            '31': { type: 'COMPONENT', col: 3, row: 2, width: 120, height: 45, dx: 8, dy: 12 },
        });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const wrapper = sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>(
            '.sheet-overlay-component'
        )!;
        expect(wrapper.style.width).to.equal('120px');
        // 45pt at 96 ppi is 60px.
        expect(wrapper.style.height).to.equal('60px');
        expect(wrapper.getBoundingClientRect().width).to.equal(120);
        expect(wrapper.getBoundingClientRect().height).to.equal(60);
        cleanup();
    });

    it('starts the workbook offset in from the anchor cell', async () => {
        const chart = document.createElement('div');
        const { sheet, cleanup } = attachSheetWithFlow({ '31': chart });
        sheet.setOverlays({
            '31': { type: 'COMPONENT', col: 3, row: 2, width: 120, height: 45, dx: 8, dy: 12 },
        });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const wrapper = sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>(
            '.sheet-overlay-component'
        )!;
        // col 3 starts 100px across, plus 8px of offset; row 2 starts
        // 20px down, plus 12pt = 16px.
        expect(wrapper.style.left).to.equal('108px');
        expect(wrapper.style.top).to.equal('36px');
        cleanup();
    });

    it('takes no space at all when the chart is minimized', async () => {
        // Minimizing gives the space back: the server sends a size of
        // nothing, and nothing is what the wrapper takes.
        const chart = document.createElement('div');
        const { sheet, cleanup } = attachSheetWithFlow({ '31': chart });
        sheet.setOverlays({
            '31': { type: 'COMPONENT', col: 3, row: 2, width: 0, height: 0, dx: 8, dy: 12 },
        });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const wrapper = sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>(
            '.sheet-overlay-component'
        )!;
        expect(wrapper.getBoundingClientRect().width).to.equal(0);
        expect(wrapper.getBoundingClientRect().height).to.equal(0);
        cleanup();
    });

    it('takes the component back out when the overlay goes away', async () => {
        const chart = document.createElement('div');
        const { host, sheet, cleanup } = attachSheetWithFlow({ '31': chart });
        sheet.setOverlays({ '31': componentOverlay(2, 2) });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        sheet.setOverlays({});
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        expect(chart.parentElement).to.be.null;
        expect(chart.hasAttribute('slot')).to.be.false;
        cleanup();
    });

    it('does not re-parent the component when the sheet repaints', async () => {
        // Re-parenting a live Vaadin component destroys and rebuilds it, and
        // the sheet repaints on every scroll tick.
        const chart = document.createElement('div');
        const { host, sheet, cleanup } = attachSheetWithFlow({ '31': chart });
        sheet.setOverlays({ '31': componentOverlay(2, 2) });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        let moved = false;
        new MutationObserver(() => {
            moved = true;
        }).observe(sheet.getCellGrid(), { childList: true });

        sheet.recomputeViewport();
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        expect(moved).to.be.false;
        expect(chart.parentElement).to.equal(host);
        cleanup();
    });

    it('still renders the wrapper when Flow has no element yet', async () => {
        // The geometry and the component arrive in separate server
        // responses, so the first paint can legitimately find nothing.
        const { host, sheet, cleanup } = attachSheetWithFlow({});
        sheet.setOverlays({ '31': componentOverlay(2, 2) });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const wrapper = sheet.getCellGrid().shadowRoot!.querySelector(
            '.sheet-overlay-component'
        );
        expect(wrapper).to.not.be.null;
        expect(
          wrapper!.querySelector('slot')!.assignedElements({ flatten: true })
        ).to.have.length(0);
        cleanup();
    });

    it('picks the component up on a later repaint', async () => {
        const components: Record<string, HTMLElement> = {};
        const { host, sheet, cleanup } = attachSheetWithFlow(components);
        sheet.setOverlays({ '31': componentOverlay(2, 2) });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const chart = document.createElement('div');
        components['31'] = chart;
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        expect(chart.parentElement).to.equal(host);
        cleanup();
    });

    it('releases the component when the sheet is disposed', async () => {
        const chart = document.createElement('div');
        const { sheet, host, cleanup } = attachSheetWithFlow({ '31': chart });
        sheet.setOverlays({ '31': componentOverlay(2, 2) });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        sheet.dispose();

        expect(chart.parentElement).to.be.null;
        // dispose() already ran; finish the teardown by hand rather than
        // calling cleanup(), which would dispose a second time.
        host.remove();
        (window as any).Vaadin = originalVaadin;
    });
});

describe('Spreadsheet — setResources + setOverlays wiring', () => {
    function attachSheet(width: number, height: number): {
        host: HTMLElement;
        sheet: Spreadsheet;
        cleanup: () => void;
    } {
        const host = document.createElement('div');
        host.style.cssText = `width:${width}px;height:${height}px;display:flex;flex-direction:column;`;
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        return {
            host,
            sheet,
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    it('resolves each resource URL from its host attribute', () => {
        // The server sends the resource KEYS and puts each URL on the host as
        // a `resource-<key>` attribute, because the URLs are Flow stream
        // resources minted per element rather than plain strings.
        const { host, sheet, cleanup } = attachSheet(300, 200);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        sheet.setOverlays({
            img1: { type: 'IMAGE', col: 2, row: 2, width: 80, height: 40, dx: 0, dy: 0 },
        });
        host.setAttribute('resource-img1', '/img/photo.png');
        sheet.setResources(host, ['img1']);
        sheet.recomputeViewport();

        const state = sheet.getCellGrid().renderState;
        expect(state.overlays).to.have.length(1);
        expect(state.overlays[0].src).to.equal('/img/photo.png');
        cleanup();
    });

    it('accepts the JSON-serialized key list the server sends', () => {
        const { host, sheet, cleanup } = attachSheet(300, 200);
        host.setAttribute('resource-img1', '/img/photo.png');
        sheet.setResources(host, JSON.stringify(['img1']) as any);

        expect(sheet.getResources().img1).to.equal('/img/photo.png');
        cleanup();
    });

    it('skips a key with no attribute rather than inventing a URL', () => {
        const { host, sheet, cleanup } = attachSheet(300, 200);
        host.setAttribute('resource-known', '/img/known.png');
        sheet.setResources(host, ['known', 'missing']);

        expect(sheet.getResources()).to.deep.equal({ known: '/img/known.png' });
        cleanup();
    });

    it('keeps the same <img> element when the overlay map is reordered', async () => {
        // The server rebuilds its overlay map as a HashMap, so removing and
        // re-adding one entry — what scrolling an overlay out of view and
        // back does — can change the iteration order of all of them. Without
        // a key, Lit rewrites each img's src in place and every image
        // re-downloads: a visible flicker on an ordinary scroll.
        const { host, sheet, cleanup } = attachSheet(400, 300);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        host.setAttribute('resource-a', '/img/a.png');
        host.setAttribute('resource-b', '/img/b.png');
        sheet.setResources(host, ['a', 'b']);
        sheet.setOverlays({
            a: { type: 'IMAGE', col: 1, row: 1, width: 40, height: 20, dx: 0, dy: 0 },
            b: { type: 'IMAGE', col: 3, row: 1, width: 40, height: 20, dx: 0, dy: 0 },
        });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const find = (id: string) =>
            sheet.getCellGrid().shadowRoot!.querySelector(
                `.sheet-overlay-image[data-overlay-id="${id}"]`
            );
        const firstA = find('a');
        const firstB = find('b');
        expect(firstA).to.not.be.null;
        expect(firstB).to.not.be.null;

        // Same overlays, opposite insertion order.
        sheet.setOverlays({
            b: { type: 'IMAGE', col: 3, row: 1, width: 40, height: 20, dx: 0, dy: 0 },
            a: { type: 'IMAGE', col: 1, row: 1, width: 40, height: 20, dx: 0, dy: 0 },
        });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        expect(find('a')).to.equal(firstA);
        expect(find('b')).to.equal(firstB);
        cleanup();
    });

    it('follows the attribute when the server re-mints a resource', async () => {
        // Scrolling an overlay out of view unregisters its stream resource;
        // scrolling back mints a fresh one under the same id. Only the
        // attribute changes — the key list is identical, so there is no
        // property push. A URL cached when the keys arrived would point at a
        // registration the server has already dropped and the image 404s.
        const { host, sheet, cleanup } = attachSheet(300, 200);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        sheet.setOverlays({
            img1: { type: 'IMAGE', col: 1, row: 1, width: 80, height: 40, dx: 0, dy: 0 },
        });
        host.setAttribute('resource-img1', '/dynamic/resource/0/first/');
        sheet.setResources(host, ['img1']);
        sheet.recomputeViewport();
        expect(sheet.getCellGrid().renderState.overlays[0].src)
            .to.equal('/dynamic/resource/0/first/');

        host.setAttribute('resource-img1', '/dynamic/resource/0/second/');
        sheet.recomputeViewport();

        expect(sheet.getResources().img1).to.equal('/dynamic/resource/0/second/');
        expect(sheet.getCellGrid().renderState.overlays[0].src)
            .to.equal('/dynamic/resource/0/second/');
        cleanup();
    });

    it('drops a resource whose attribute the server removed', () => {
        const { host, sheet, cleanup } = attachSheet(300, 200);
        host.setAttribute('resource-img1', '/img/photo.png');
        sheet.setResources(host, ['img1']);
        expect(sheet.getResources().img1).to.equal('/img/photo.png');

        host.removeAttribute('resource-img1');

        expect(sheet.getResources()).to.deep.equal({});
        cleanup();
    });

    it('cell-grid renders <img.sheet-overlay-image> for each IMAGE overlay', async () => {
        const { host, sheet, cleanup } = attachSheet(300, 200);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        sheet.setOverlays({
            img1: { type: 'IMAGE', col: 1, row: 1, width: 80, height: 40, dx: 0, dy: 0 },
        });
        host.setAttribute('resource-img1', '/img/photo.png');
        sheet.setResources(host, ['img1']);
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const overlay = sheet.getCellGrid().shadowRoot!.querySelector(
            'img.sheet-overlay-image'
        ) as HTMLImageElement;
        expect(overlay).to.not.be.null;
        // src is the URL from the resources map.
        expect(overlay.src).to.contain('/img/photo.png');
        // data-overlay-id is the overlay key.
        expect(overlay.dataset.overlayId).to.equal('img1');
        cleanup();
    });

    it('cell-grid renders <div.sheet-overlay-component> for COMPONENT overlays', async () => {
        const { sheet, cleanup } = attachSheet(300, 200);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        sheet.setOverlays({
            c1: { type: 'COMPONENT', col: 1, row: 1, width: 100, height: 100, dx: 0, dy: 0 },
        });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const overlay = sheet.getCellGrid().shadowRoot!.querySelector(
            'div.sheet-overlay-component'
        ) as HTMLElement;
        expect(overlay).to.not.be.null;
        expect(overlay.dataset.overlayId).to.equal('c1');
        // Not an img (no src logic).
        expect(overlay.tagName).to.equal('DIV');
        cleanup();
    });

    it('overlay element is positioned at the computed pixel coordinates', async () => {
        const { sheet, cleanup } = attachSheet(400, 200);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        sheet.setOverlays({
            img1: { type: 'IMAGE', col: 3, row: 2, width: 80, height: 40, dx: 5, dy: 3 },
        });
        sheet.setResources(sheet.getScrollContainer(), JSON.stringify({
            img1: '/x.png',
        }));
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const overlay = sheet.getCellGrid().shadowRoot!.querySelector(
            '.sheet-overlay-image'
        ) as HTMLElement;
        // col 3 = sum of widths 1..2 = 100. row 2 = height of row 1 = 20.
        // dx=5 is already pixels; dy=3 and height=40 are points, so at 96
        // ppi they become 4px and 53.33px - the fraction kept, as the GWT
        // client's pt kept it.
        expect(overlay.style.left).to.equal('105px');
        expect(overlay.style.top).to.equal('24px');
        expect(overlay.style.width).to.equal('80px');
        expect(parseFloat(overlay.style.height)).to.be.closeTo(160 / 3, 1e-3);
        cleanup();
    });

    it('a popup button tells the server it was clicked', async () => {
        const { host, sheet, cleanup } = attachSheet(400, 300);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        sheet.addPopupButton({
            col: 2, row: 3, active: false, headerHidden: false,
            popupHeight: null, popupWidth: null, sheet: 'Sheet1',
        });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const button = sheet.getCellGrid().shadowRoot!.querySelector(
            '.popup-button'
        ) as HTMLElement;
        expect(button).to.not.be.null;
        expect(button.dataset.col).to.equal('2');
        expect(button.dataset.row).to.equal('3');

        let received: { type: string; data: Record<string, unknown> } | undefined;
        host.addEventListener('spreadsheet-event', (e: Event) => {
            const detail = (e as CustomEvent).detail;
            if (detail.type === 'popupButtonClick') {
                received = detail;
            }
        });

        button.click();

        expect(received).to.exist;
        expect(received!.data).to.deep.equal({ row: 3, column: 2 });
        cleanup();
    });

    it('active popup button gets .active class', async () => {
        const { sheet, cleanup } = attachSheet(400, 300);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.addPopupButton({
            col: 1, row: 1, active: true, headerHidden: false,
            popupHeight: null, popupWidth: null, sheet: 'Sheet1',
        });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const button = sheet.getCellGrid().shadowRoot!.querySelector(
            '.popup-button'
        ) as HTMLElement;
        expect(button.classList.contains('active')).to.be.true;
        cleanup();
    });

    it('no overlays renders nothing', async () => {
        const { sheet, cleanup } = attachSheet(300, 200);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        expect(sheet.getCellGrid().shadowRoot!.querySelectorAll(
            '.sheet-overlay'
        )).to.have.length(0);
        cleanup();
    });
});

describe('Overlays — addressable by their anchor cell', () => {
    const uniformWidth = (_c: number) => 50;
    const uniformHeight = (_r: number) => 20;

    it('carries the anchor cell through positioning', () => {
        // Pixel geometry alone does not say which cell an overlay belongs
        // to, so nothing could address an overlay by its anchor.
        const overlays: Record<string, OverlayInfo> = {
            img1: { type: 'IMAGE', col: 3, row: 2, width: 100, height: 60, dx: 0, dy: 0 },
        };
        const result = computePositionedOverlays(overlays, {}, uniformWidth, uniformHeight);

        expect(result[0].col).to.equal(3);
        expect(result[0].row).to.equal(2);
    });

    it('renders the anchor as data attributes on an image overlay', async () => {
        const el = document.createElement(
            'vaadin-spreadsheet-internal-cell-grid'
        ) as SpreadsheetCellGrid;
        el.renderState = {
            ...emptyCellGridRenderState(),
            // render() bails out on an empty viewport, so give it one.
            viewport: {
                firstColumnIndex: 1, firstColumnPosition: 0,
                lastColumnIndex: 6, lastColumnPosition: 300,
                firstRowIndex: 1, firstRowPosition: 0,
                lastRowIndex: 6, lastRowPosition: 120,
            },
            colX: [0, 50, 100, 150, 200, 250],
            rowY: [0, 20, 40, 60, 80, 100],
            colWidths: [50, 50, 50, 50, 50, 50],
            rowHeights: [20, 20, 20, 20, 20, 20],
            overlays: [
                { id: 'i1', type: 'IMAGE', src: '/a.png', col: 3, row: 2,
                  leftPx: 100, topPx: 20, widthPx: 40, heightPx: 30 },
            ],
        };
        document.body.appendChild(el);
        await el.updateComplete;

        const img = el.shadowRoot!.querySelector('.sheet-overlay-image')!;
        expect(img.getAttribute('data-col')).to.equal('3');
        expect(img.getAttribute('data-row')).to.equal('2');
        el.remove();
    });

    it('renders the anchor on a component overlay too', async () => {
        const el = document.createElement(
            'vaadin-spreadsheet-internal-cell-grid'
        ) as SpreadsheetCellGrid;
        el.renderState = {
            ...emptyCellGridRenderState(),
            // render() bails out on an empty viewport, so give it one.
            viewport: {
                firstColumnIndex: 1, firstColumnPosition: 0,
                lastColumnIndex: 6, lastColumnPosition: 300,
                firstRowIndex: 1, firstRowPosition: 0,
                lastRowIndex: 6, lastRowPosition: 120,
            },
            colX: [0, 50, 100, 150, 200, 250],
            rowY: [0, 20, 40, 60, 80, 100],
            colWidths: [50, 50, 50, 50, 50, 50],
            rowHeights: [20, 20, 20, 20, 20, 20],
            overlays: [
                { id: 'c1', type: 'COMPONENT', src: undefined, col: 5, row: 4,
                  leftPx: 200, topPx: 60, widthPx: 40, heightPx: 30 },
            ],
        };
        document.body.appendChild(el);
        await el.updateComplete;

        const placeholder = el.shadowRoot!.querySelector('.sheet-overlay-component')!;
        expect(placeholder.getAttribute('data-col')).to.equal('5');
        expect(placeholder.getAttribute('data-row')).to.equal('4');
        el.remove();
    });
});

describe('Spreadsheet — popup (filter) buttons', () => {
    /**
     * A popup button is a real component on the server, so each one is
     * attached and detached over its own RPC call rather than arriving
     * as a list property. The connector keeps them in the list the
     * renderer already positions.
     */
    function attachSheet(): { sheet: Spreadsheet; cleanup: () => void } {
        const host = document.createElement('div');
        host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
        document.body.appendChild(host);
        const sheet = new Spreadsheet(host);
        sheet.setRows(20);
        sheet.setCols(10);
        sheet.setColW(new Array(10).fill(50));
        sheet.setRowH(new Array(20).fill(15));
        return {
            sheet,
            cleanup: () => {
                sheet.dispose();
                host.remove();
            },
        };
    }

    const button = (col: number, row: number, over: Record<string, unknown> = {}) => ({
        col, row, active: false, headerHidden: false,
        popupHeight: null, popupWidth: null, sheet: 'Sheet1', ...over,
    });

    async function buttons(sheet: Spreadsheet): Promise<HTMLElement[]> {
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;
        return [
            ...sheet.getCellGrid().shadowRoot!.querySelectorAll<HTMLElement>(
                '[part~="popup-button"]'
            ),
        ];
    }

    it('draws the button as the GWT client did: a white box with a grey arrow', async () => {
        const { sheet, cleanup } = attachSheet();
        sheet.addPopupButton(button(2, 3) as any);

        const [b] = await buttons(sheet);
        const style = getComputedStyle(b);
        expect(`${style.width} ${style.height}`).to.equal('13px 13px');
        expect(style.backgroundColor).to.equal('rgb(255, 255, 255)');
        expect(style.color).to.equal('rgb(128, 128, 128)');
        expect(`${style.borderTopWidth} ${style.borderTopStyle} ${style.borderTopColor}`).to.equal(
            '1px solid rgb(211, 211, 211)'
        );
        expect(style.borderTopLeftRadius).to.equal('2px');
        cleanup();
    });

    it('tucks it into the corner of its cell, inside the borders', async () => {
        const { sheet, cleanup } = attachSheet();
        sheet.addPopupButton(button(2, 3) as any);

        const [b] = await buttons(sheet);
        const cell = sheet.getCellGrid().shadowRoot!.querySelector('.cell.col2.row3')!.getBoundingClientRect();
        const box = b.getBoundingClientRect();
        expect(box.right).to.equal(cell.right - 2);
        expect(box.bottom).to.equal(cell.bottom - 1);
        cleanup();
    });

    it('marks a filter in use with a blue line along its top', async () => {
        const { sheet, cleanup } = attachSheet();
        sheet.addPopupButton(button(2, 3, { active: true }) as any);

        const [b] = await buttons(sheet);
        expect(getComputedStyle(b).boxShadow).to.equal('rgb(0, 171, 238) 0px 2px 0px 0px inset');
        cleanup();
    });

    it('renders a button that was added', async () => {
        const { sheet, cleanup } = attachSheet();
        sheet.addPopupButton(button(2, 3) as any);

        expect(await buttons(sheet)).to.have.length(1);
        cleanup();
    });

    it('accepts the JSON-serialized state the server sends', async () => {
        const { sheet, cleanup } = attachSheet();
        sheet.addPopupButton(JSON.stringify(button(2, 3)) as any);

        expect(await buttons(sheet)).to.have.length(1);
        cleanup();
    });

    it('renders one per anchored cell', async () => {
        const { sheet, cleanup } = attachSheet();
        sheet.addPopupButton(button(2, 3) as any);
        sheet.addPopupButton(button(4, 3) as any);
        sheet.addPopupButton(button(6, 3) as any);

        expect(await buttons(sheet)).to.have.length(3);
        cleanup();
    });

    it('replaces rather than duplicates when a cell is re-added', async () => {
        // The server re-attaches a button when its state changes.
        const { sheet, cleanup } = attachSheet();
        sheet.addPopupButton(button(2, 3) as any);
        sheet.addPopupButton(button(2, 3, { active: true }) as any);

        expect(await buttons(sheet)).to.have.length(1);
        cleanup();
    });

    it('removes the button for a cell', async () => {
        const { sheet, cleanup } = attachSheet();
        sheet.addPopupButton(button(2, 3) as any);
        sheet.addPopupButton(button(4, 3) as any);

        sheet.removePopupButton(button(2, 3) as any);

        const rendered = await buttons(sheet);
        expect(rendered).to.have.length(1);
        expect(rendered[0].dataset.col).to.equal('4');
        cleanup();
    });

    it('identifies the button to remove by its cell, not the rest of its state', async () => {
        const { sheet, cleanup } = attachSheet();
        sheet.addPopupButton(button(2, 3, { active: true }) as any);

        sheet.removePopupButton(button(2, 3, { active: false }) as any);

        expect(await buttons(sheet)).to.have.length(0);
        cleanup();
    });

    it('ignores a malformed state rather than rendering nonsense', async () => {
        const { sheet, cleanup } = attachSheet();
        sheet.addPopupButton('' as any);
        sheet.addPopupButton('{oops' as any);

        expect(await buttons(sheet)).to.have.length(0);
        cleanup();
    });
});
