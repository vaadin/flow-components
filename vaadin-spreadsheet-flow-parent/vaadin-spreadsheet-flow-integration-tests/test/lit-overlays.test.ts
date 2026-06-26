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
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import type {
    OverlayInfo, PopupButtonState,
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/resources/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * Slice 160 — sheet overlay rendering.
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
        expect(result[0].heightPx).to.equal(60);
    });

    it('adds dx/dy offset to the anchor position', () => {
        const overlays: Record<string, OverlayInfo> = {
            img1: { type: 'IMAGE', col: 2, row: 2, width: 100, height: 60, dx: 7, dy: 11 },
        };
        const result = computePositionedOverlays(overlays, {}, uniformWidth, uniformHeight);
        expect(result[0].leftPx).to.equal(50 + 7);
        expect(result[0].topPx).to.equal(20 + 11);
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

describe('computePositionedPopupButtons (slice 161)', () => {
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
});

describe('Spreadsheet — setResources + setOverlays wiring (slice 160)', () => {
    function attachSheet(width: number, height: number): {
        host: HTMLElement;
        sheet: Spreadsheet;
        cleanup: () => void;
    } {
        const host = document.createElement('div');
        host.style.cssText = `width:${width}px;height:${height}px;`;
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

    it('setResources parses the JSON string into the resources map', () => {
        const { sheet, cleanup } = attachSheet(300, 200);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        sheet.setOverlays({
            img1: { type: 'IMAGE', col: 2, row: 2, width: 80, height: 40, dx: 0, dy: 0 },
        });
        sheet.setResources(sheet.getScrollContainer(), JSON.stringify({
            img1: '/img/photo.png',
        }));
        sheet.recomputeViewport();

        const state = sheet.getCellGrid().renderState;
        expect(state.overlays).to.have.length(1);
        expect(state.overlays[0].src).to.equal('/img/photo.png');
        cleanup();
    });

    it('setResources with malformed JSON keeps the resources map empty (no crash)', () => {
        const { sheet, cleanup } = attachSheet(300, 200);
        sheet.setResources(sheet.getScrollContainer(), 'not valid {{ json');
        // No throw + the connector still works.
        expect(sheet).to.be.instanceOf(Spreadsheet);
        cleanup();
    });

    it('cell-grid renders <img.sheet-overlay-image> for each IMAGE overlay', async () => {
        const { sheet, cleanup } = attachSheet(300, 200);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        sheet.setOverlays({
            img1: { type: 'IMAGE', col: 1, row: 1, width: 80, height: 40, dx: 0, dy: 0 },
        });
        sheet.setResources(sheet.getScrollContainer(), JSON.stringify({
            img1: '/img/photo.png',
        }));
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

    it('COMPONENT overlay contains a named <slot> for wrapper-side component injection (slice 168)', async () => {
        const { sheet, cleanup } = attachSheet(300, 200);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        sheet.setOverlays({
            comp42: { type: 'COMPONENT', col: 1, row: 1, width: 80, height: 80, dx: 0, dy: 0 },
        });
        sheet.recomputeViewport();
        await sheet.getCellGrid().updateComplete;

        const overlay = sheet.getCellGrid().shadowRoot!.querySelector(
            'div.sheet-overlay-component'
        ) as HTMLElement;
        expect(overlay).to.not.be.null;
        const slot = overlay.querySelector('slot');
        expect(slot).to.not.be.null;
        // Slot name encodes the overlay id so the wrapper can
        // assign the matching component via slot="overlay-comp42".
        expect(slot!.getAttribute('name')).to.equal('overlay-comp42');
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
        // + dx=5, dy=3 → leftPx=105, topPx=23.
        expect(overlay.style.left).to.equal('105px');
        expect(overlay.style.top).to.equal('23px');
        expect(overlay.style.width).to.equal('80px');
        expect(overlay.style.height).to.equal('40px');
        cleanup();
    });

    it('setPopupButtons + render: dispatches popupButtonClick on click (slice 161)', async () => {
        const { host, sheet, cleanup } = attachSheet(400, 300);
        sheet.setRows(10);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15, 15, 15, 15, 15, 15]);
        sheet.setPopupButtons([
            { col: 2, row: 3, active: false, headerHidden: false,
              popupHeight: null, popupWidth: null, sheet: 'Sheet1' },
        ]);
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

    it('active popup button gets .active class (slice 161)', async () => {
        const { sheet, cleanup } = attachSheet(400, 300);
        sheet.setRows(5);
        sheet.setCols(5);
        sheet.setColW([50, 50, 50, 50, 50]);
        sheet.setRowH([15, 15, 15, 15, 15]);
        sheet.setPopupButtons([
            { col: 1, row: 1, active: true, headerHidden: false,
              popupHeight: null, popupWidth: null, sheet: 'Sheet1' },
        ]);
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
