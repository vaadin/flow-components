/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { render } from 'lit';
import {
  buildPopupButtons,
  buildSheetOverlays
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/SheetOverlays.js';
import {
  type CellGridRenderState,
  emptyCellGridRenderState,
  type PositionedOverlay,
  type PositionedPopupButton
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';

/**
 * What the server puts on the sheet, on its own: pictures and components,
 * and the filter buttons, each in the pane of the cell it belongs to.
 */
describe('SheetOverlays', () => {
  let container: HTMLElement;

  beforeEach(() => {
    container = document.createElement('div');
    document.body.append(container);
  });

  afterEach(() => {
    container.remove();
  });

  /** A sheet frozen after column 1 and row 1. */
  function sheet(overrides: Partial<CellGridRenderState>): CellGridRenderState {
    return { ...emptyCellGridRenderState(), horizontalSplitPosition: 1, verticalSplitPosition: 1, ...overrides };
  }

  function overlay(id: string, col: number, row: number, type: PositionedOverlay['type'] = 'IMAGE'): PositionedOverlay {
    return { id, col, row, type, src: `${id}.png`, leftPx: 10, topPx: 20, widthPx: 30, heightPx: 40 };
  }

  function button(col: number, row: number, extra: Partial<PositionedPopupButton> = {}): PositionedPopupButton {
    return { col, row, leftPx: 70, topPx: 80, active: false, ...extra };
  }

  describe('pictures and components', () => {
    it("are each drawn in the pane of the cell they are anchored at", () => {
      const state = sheet({ overlays: [overlay('a', 3, 3), overlay('b', 1, 3), overlay('c', 3, 1), overlay('d', 1, 1)] });

      const ids = (quadrant: 'main' | 'top' | 'left' | 'corner') => {
        render(buildSheetOverlays(state, quadrant), container);
        return Array.from(container.querySelectorAll<HTMLElement>('.sheet-overlay'), (el) => el.dataset.overlayId);
      };

      expect([ids('main'), ids('left'), ids('top'), ids('corner')]).to.eql([['a'], ['b'], ['c'], ['d']]);
    });

    it('draw a picture where the server placed it, with its source and its anchor', () => {
      render(buildSheetOverlays(sheet({ overlays: [overlay('a', 3, 4)] }), 'main'), container);

      const picture = container.querySelector<HTMLImageElement>('img.sheet-overlay.sheet-overlay-image')!;
      expect(picture.getAttribute('src')).to.equal('a.png');
      expect([picture.dataset.col, picture.dataset.row]).to.eql(['3', '4']);
      expect(picture.style.cssText).to.equal('left: 10px; top: 20px; width: 30px; height: 40px;');
    });

    it("hold a component in a box of the overlay's size, by a slot of the overlay's own", () => {
      render(buildSheetOverlays(sheet({ overlays: [overlay('7', 3, 4, 'COMPONENT')] }), 'main'), container);

      const box = container.querySelector<HTMLElement>('div.sheet-overlay.sheet-overlay-component')!;
      expect(box.style.width).to.equal('30px');
      expect(box.querySelector('slot')!.name).to.equal('overlay-component-7');
    });

    it('keep their elements as the server sends them in another order', () => {
      render(buildSheetOverlays(sheet({ overlays: [overlay('a', 3, 3), overlay('b', 4, 4)] }), 'main'), container);
      const [a, b] = Array.from(container.querySelectorAll('img'));

      render(buildSheetOverlays(sheet({ overlays: [overlay('b', 4, 4), overlay('a', 3, 3)] }), 'main'), container);

      expect(Array.from(container.querySelectorAll('img'))).to.eql([b, a]);
    });
  });

  describe('the filter buttons', () => {
    function drawnButtons(buttons: PositionedPopupButton[], quadrant: 'main' | 'top' | 'left' | 'corner' = 'main') {
      render(buildPopupButtons(sheet({ popupButtons: buttons }), quadrant), container);
      return Array.from(container.querySelectorAll<HTMLElement>('.popup-button'));
    }

    it('are each drawn in the pane of their cell', () => {
      const buttons = [button(3, 3), button(3, 1), button(1, 3)];

      expect(drawnButtons(buttons, 'main').map((b) => b.dataset.row)).to.eql(['3']);
      expect(drawnButtons(buttons, 'top').map((b) => b.dataset.row)).to.eql(['1']);
      expect(drawnButtons(buttons, 'left').map((b) => b.dataset.col)).to.eql(['1']);
      expect(drawnButtons(buttons, 'corner')).to.eql([]);
    });

    it("are buttons placed at their cell's corner, carrying its height, and marked while their filter is in use", () => {
      const [plain, active] = drawnButtons([button(3, 3), button(4, 3, { active: true, cellHeightPx: 31 })]);

      expect([plain.className, active.className]).to.eql(['popup-button', 'popup-button active']);
      expect(plain.style.cssText).to.equal('left: 70px; top: 80px; --_cell-height: 20px;');
      expect(active.style.getPropertyValue('--_cell-height')).to.equal('31px');
      expect([plain.getAttribute('role'), plain.tabIndex, plain.getAttribute('aria-label')]).to.eql([
        'button',
        0,
        'Open pop-up'
      ]);
      expect(plain.getAttribute('part')).to.equal('popup-button');
    });

    it('say which is clicked, and keep the click from the cell under them', () => {
      const [b] = drawnButtons([button(3, 4)]);
      const pressed: unknown[] = [];
      let reachedCell = false;
      container.addEventListener('popup-button-click', (e) => pressed.push((e as CustomEvent).detail));
      container.addEventListener('click', () => (reachedCell = true));

      b.click();

      expect(pressed).to.eql([{ col: 3, row: 4, keyboard: false }]);
      expect(reachedCell).to.be.false;
    });

    it('are pressed from the keyboard with Enter or Space, taking those keys, and leave the others to the sheet', () => {
      const [b] = drawnButtons([button(3, 4)]);
      const pressed: unknown[] = [];
      const reached: string[] = [];
      container.addEventListener('popup-button-click', (e) => pressed.push((e as CustomEvent).detail));
      container.addEventListener('keydown', (e) => reached.push(e.key));

      const keys = ['Enter', ' ', 'a'].map((key) => {
        const e = new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true });
        b.dispatchEvent(e);
        return e.defaultPrevented;
      });

      expect(pressed).to.eql([
        { col: 3, row: 4, keyboard: true },
        { col: 3, row: 4, keyboard: true }
      ]);
      expect(keys).to.eql([true, true, false]);
      expect(reached).to.eql(['a']);
    });
  });
});
