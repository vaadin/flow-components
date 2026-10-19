/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { render, type TemplateResult } from 'lit';
import {
  type QuadrantRenderOpts,
  quadrants
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/GridGeometry.js';
import {
  buildFormulaReferences,
  buildPaintOutline,
  buildSelectionOverlay,
  pieceBox,
  rangePiece
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/RangeOverlays.js';
import {
  type CellGridRenderState,
  emptyCellGridRenderState
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';

/**
 * The outlines drawn over the cells, on their own: the selection, the fill
 * handle's drag and the references of a formula, each a piece per pane.
 */
describe('RangeOverlays', () => {
  /**
   * Columns 2 to 5 of 50 pixels and rows 2 to 5 of 20 in view past one
   * frozen column and row, column 4 hidden.
   */
  function sheet(overrides: Partial<CellGridRenderState> = {}): CellGridRenderState {
    const empty = emptyCellGridRenderState();
    return {
      ...empty,
      viewport: { ...empty.viewport, firstColumnIndex: 2, lastColumnIndex: 5, firstRowIndex: 2, lastRowIndex: 5 },
      colX: [51, 101, 151, 151],
      colWidths: [50, 50, 0, 50],
      rowY: [21, 41, 61, 81],
      rowHeights: [20, 20, 20, 20],
      horizontalSplitPosition: 1,
      verticalSplitPosition: 1,
      frozenColX: [0],
      frozenColWidths: [50],
      frozenRowY: [0],
      frozenRowHeights: [20],
      ...overrides
    };
  }

  function pane(state: CellGridRenderState, name: QuadrantRenderOpts['quadrantName']): QuadrantRenderOpts {
    return quadrants(state).find((quadrant) => quadrant.quadrantName === name)!;
  }

  /** What a template draws, in a container of its own. */
  function drawn(template: TemplateResult | TemplateResult[] | string): HTMLElement {
    const container = document.createElement('div');
    render(template, container);
    return container;
  }

  describe('the piece of a range a pane shows', () => {
    it('is the whole range, every side its own, when the pane shows all of it', () => {
      expect(rangePiece(pane(sheet(), 'main'), { c1: 2, c2: 3, r1: 2, r2: 3 })).to.eql({
        left: 51,
        top: 21,
        right: 151,
        bottom: 61,
        hasTop: true,
        hasLeft: true,
        hasRight: true,
        hasBottom: true
      });
    });

    it('ends where the pane does, without the sides the range runs on past', () => {
      const piece = rangePiece(pane(sheet(), 'main'), { c1: 1, c2: 9, r1: 3, r2: 9 })!;

      expect([piece.left, piece.top, piece.right, piece.bottom]).to.eql([51, 41, 201, 101]);
      expect([piece.hasTop, piece.hasLeft, piece.hasRight, piece.hasBottom]).to.eql([true, false, false, false]);
    });

    it('is none for a range the pane does not show', () => {
      expect(rangePiece(pane(sheet(), 'main'), { c1: 7, c2: 8, r1: 2, r2: 3 })).to.equal(null);
      expect(rangePiece(pane(sheet(), 'corner'), { c1: 2, c2: 3, r1: 1, r2: 1 })).to.equal(null);
    });
  });

  describe('the box of a piece', () => {
    it("starts a pixel up and left on the range's own top and left sides, on the gridlines there", () => {
      const piece = rangePiece(pane(sheet(), 'main'), { c1: 2, c2: 3, r1: 2, r2: 3 })!;

      expect(pieceBox(piece)).to.equal(
        'left:50px;top:20px;width:101px;height:41px;position:absolute;pointer-events:none;'
      );
      expect(pieceBox({ ...piece, hasTop: false, hasLeft: false })).to.contain('left:51px;top:21px;width:100px;height:40px;');
    });

    it('is no size for a piece that ends before it starts', () => {
      const box = pieceBox({
        left: 30,
        top: 30,
        right: 10,
        bottom: 10,
        hasTop: false,
        hasLeft: false,
        hasRight: true,
        hasBottom: true
      });

      expect(box).to.contain('width:0px;height:0px;');
    });
  });

  describe('the selection', () => {
    it('is outlined on all four sides, the fill handle at its foot, carrying the active cell', () => {
      const state = sheet({
        selection: { activeCell: { col: 3, row: 3 }, activeRange: { c1: 2, c2: 3, r1: 2, r2: 3 }, auxRanges: [] }
      });

      const outline = drawn(buildSelectionOverlay(pane(state, 'main'))).querySelector<HTMLElement>('.sheet-selection')!;

      expect(outline.className).to.equal('sheet-selection');
      expect([outline.dataset.col, outline.dataset.row]).to.eql(['3', '3']);
      expect(outline.style.cssText).to.contain('left: 50px; top: 20px; width: 101px; height: 41px;');
      expect(Array.from(outline.children, (side) => side.className)).to.eql([
        's-top',
        's-left',
        's-right',
        's-bottom',
        'selection-corner'
      ]);
      expect(outline.querySelector('.selection-corner')!.getAttribute('part')).to.equal('selection-corner');
      expect(outline.hasAttribute('part'), 'no part of its own').to.be.false;
    });

    it('keeps the sides it runs on past, hidden, and the fill handle only where the range ends', () => {
      const state = sheet({
        selection: { activeCell: { col: 1, row: 3 }, activeRange: { c1: 1, c2: 3, r1: 3, r2: 9 }, auxRanges: [] }
      });

      const left = drawn(buildSelectionOverlay(pane(state, 'left'))).querySelector('.sheet-selection')!;
      const main = drawn(buildSelectionOverlay(pane(state, 'main'))).querySelector('.sheet-selection')!;

      expect(Array.from(left.children, (side) => side.className)).to.eql(['s-top', 's-left', 's-right cut', 's-bottom cut']);
      expect(Array.from(main.children, (side) => side.className)).to.eql(['s-top', 's-left cut', 's-right', 's-bottom cut']);
    });

    it('has no outline and no fill handle while several ranges are selected, only the active cell it carries', () => {
      const state = sheet({
        selection: {
          activeCell: { col: 2, row: 2 },
          activeRange: { c1: 2, c2: 2, r1: 2, r2: 2 },
          auxRanges: [{ c1: 5, c2: 5, r1: 5, r2: 5 }]
        }
      });

      const outline = drawn(buildSelectionOverlay(pane(state, 'main'))).querySelector<HTMLElement>('.sheet-selection')!;

      expect(outline.className).to.equal('sheet-selection multi-range');
      expect(outline.dataset.col).to.equal('2');
    });

    it('draws nothing but its place for a range all in hidden columns', () => {
      const state = sheet({
        selection: { activeCell: { col: 4, row: 2 }, activeRange: { c1: 4, c2: 4, r1: 2, r2: 3 }, auxRanges: [] }
      });

      const outline = drawn(buildSelectionOverlay(pane(state, 'main'))).querySelector<HTMLElement>('.sheet-selection')!;

      expect(outline.children).to.have.length(0);
      expect(outline.dataset.col).to.equal('4');
    });

    it('is not drawn in a pane that shows none of it', () => {
      expect(buildSelectionOverlay(pane(sheet(), 'main'))).to.equal('');
    });
  });

  describe('the selection used by touch', () => {
    const selection = { activeCell: { col: 2, row: 2 }, activeRange: { c1: 2, c2: 3, r1: 2, r2: 3 }, auxRanges: [] };

    function outlineOf(state: CellGridRenderState, name: QuadrantRenderOpts['quadrantName'] = 'main'): HTMLElement {
      return drawn(buildSelectionOverlay(pane(state, name))).querySelector<HTMLElement>('.sheet-selection')!;
    }

    const squares = (outline: HTMLElement) =>
      Array.from(outline.querySelectorAll('.fill-touch-square'), (square) => square.className);

    it('has the corner handle made for a finger', () => {
      const outline = outlineOf(sheet({ selection, touch: 'select' }));

      expect(outline.className).to.equal('sheet-selection touch');
      expect(outline.querySelector('.selection-corner')).to.exist;
      expect(squares(outline)).to.eql([]);
    });

    it('has a fill handle at the middle of each side once Fill is chosen, and no corner handle', () => {
      const outline = outlineOf(sheet({ selection, touch: 'fill' }));

      expect(outline.className).to.equal('sheet-selection touch fill');
      expect(outline.querySelector('.selection-corner')).to.equal(null);
      expect(squares(outline)).to.eql([
        'fill-touch-square top',
        'fill-touch-square left',
        'fill-touch-square right',
        'fill-touch-square bottom'
      ]);
    });

    it('has fill handles only on the sides of the range a pane shows', () => {
      const state = sheet({
        selection: { activeCell: { col: 1, row: 3 }, activeRange: { c1: 1, c2: 3, r1: 3, r2: 9 }, auxRanges: [] },
        touch: 'fill'
      });

      expect(squares(outlineOf(state, 'left'))).to.eql(['fill-touch-square top', 'fill-touch-square left']);
      expect(squares(outlineOf(state, 'main'))).to.eql(['fill-touch-square top', 'fill-touch-square right']);
    });

    it('has no handles for several ranges', () => {
      const outline = outlineOf(
        sheet({
          selection: { ...selection, auxRanges: [{ c1: 5, c2: 5, r1: 5, r2: 5 }] },
          touch: 'fill'
        })
      );

      expect(outline.className).to.equal('sheet-selection multi-range touch fill');
      expect(outline.querySelector('.selection-corner')).to.equal(null);
      expect(squares(outline)).to.eql([]);
    });
  });

  describe("the fill handle's drag", () => {
    it('is kept, hidden, in the scrolling pane alone while nothing is painted', () => {
      const hidden = drawn(buildPaintOutline(pane(sheet(), 'main'))).querySelector<HTMLElement>('.paintmode')!;

      expect(hidden.style.display).to.equal('none');
      expect(buildPaintOutline(pane(sheet(), 'top'))).to.equal('');
    });

    it('starts where its piece of the drag does', () => {
      const state = sheet({ paintRect: { c1: 2, c2: 3, r1: 3, r2: 9 } });

      const outline = drawn(buildPaintOutline(pane(state, 'main'))).querySelector<HTMLElement>('.paintmode')!;

      expect(outline.style.cssText).to.contain('left: 50px; top: 40px;');
      expect(outline.classList.contains('sheet-selection'), 'never found as the selection').to.be.false;
    });

    const sides = ['top', 'left', 'right', 'bottom'] as const;
    for (const [what, paintRect, cut] of [
      ['a drag inside the pane', { c1: 2, c2: 3, r1: 2, r2: 3 }, []],
      ['a drag from the frozen corner on past the view', { c1: 1, c2: 3, r1: 1, r2: 9 }, ['top', 'left', 'bottom']],
      ['a drag on past the right of the view', { c1: 2, c2: 9, r1: 2, r2: 3 }, ['right']]
    ] as const) {
      it(`is outlined only on the sides ${what} ends at`, () => {
        const outline = drawn(buildPaintOutline(pane(sheet({ paintRect }), 'main'))).querySelector<HTMLElement>(
          '.paintmode'
        )!;

        const widths = sides.map((side) => outline.style.getPropertyValue(`border-${side}-width`));
        expect(widths).to.eql(sides.map((side) => ((cut as readonly string[]).includes(side) ? '0px' : '')));
      });
    }

    it('is not drawn in a pane that shows none of it', () => {
      expect(buildPaintOutline(pane(sheet({ paintRect: { c1: 2, c2: 3, r1: 3, r2: 4 } }), 'top'))).to.equal('');
    });
  });

  describe("the formula's references", () => {
    it('tint each one in its colour, ring the one at the caret, and leave out those the pane does not show', () => {
      const state = sheet({
        formulaReferences: [
          { range: { c1: 2, c2: 2, r1: 2, r2: 2 }, color: '1, 2, 3', atCaret: false },
          { range: { c1: 3, c2: 3, r1: 3, r2: 4 }, color: '4, 5, 6', atCaret: true },
          { range: { c1: 1, c2: 1, r1: 1, r2: 1 }, color: '7, 8, 9', atCaret: true }
        ]
      });

      const refs = Array.from(
        drawn(buildFormulaReferences(pane(state, 'main'))).querySelectorAll<HTMLElement>('.formula-reference')
      );

      expect(refs.map((ref) => ref.style.background)).to.eql(['rgba(1, 2, 3, 0.25)', 'rgba(4, 5, 6, 0.25)']);
      expect(refs.map((ref) => ref.style.boxShadow)).to.eql(['', 'rgba(4, 5, 6, 0.75) 0px 0px 0px 2px inset']);
      expect(refs[1].style.cssText).to.contain('left: 101px; top: 41px; width: 50px; height: 40px;');
    });
  });
});
