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
  type QuadrantRenderOpts,
  quadrants
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/GridGeometry.js';
import {
  buildMergedCellOverlays,
  cellMarkers,
  coveredByMergeCoords,
  renderQuadrant,
  selectionClassFor
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/QuadrantCells.js';
import {
  type CellGridRenderState,
  emptyCellGridRenderState
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * The cells of one pane, on their own: a plain cell and a merged one, how
 * each shows the selection and its corner markers, and the cells a merge
 * covers.
 */
describe('QuadrantCells', () => {
  const region = (col1: number, row1: number, col2: number, row2: number) => ({ id: 0, col1, row1, col2, row2 });

  function cell(col: number, row: number, extra: Partial<CellData> = {}): [string, CellData] {
    return [
      `col${col} row${row}`,
      { col, row, value: `v${col}.${row}`, cellStyle: 'cs0', locked: false, needsMeasure: false, isPercentage: false, ...extra }
    ];
  }

  /** Columns 1 to 4 of 50 pixels, the third hidden, and rows 1 to 3 of 20, nothing frozen. */
  function sheet(overrides: Partial<CellGridRenderState> = {}): CellGridRenderState {
    const empty = emptyCellGridRenderState();
    return {
      ...empty,
      viewport: { ...empty.viewport, firstColumnIndex: 1, lastColumnIndex: 4, firstRowIndex: 1, lastRowIndex: 3 },
      colX: [0, 50, 100, 100],
      colWidths: [50, 50, 0, 50],
      rowY: [0, 20, 40],
      rowHeights: [20, 20, 20],
      ...overrides
    };
  }

  function main(state: CellGridRenderState): QuadrantRenderOpts {
    return quadrants(state)[0];
  }

  function drawn(template: unknown): HTMLElement {
    const container = document.createElement('div');
    render(template, container);
    return container;
  }

  const inDrawn = (container: HTMLElement, col: number, row: number): HTMLElement =>
    container.querySelector<HTMLElement>(`.cell.col${col}.row${row}`)!;

  describe('the cells a merge covers', () => {
    it('are only the part of a merge inside the quadrant', () => {
      // A merge as tall as the sheet cost a million coordinates on every
      // render before it was clipped.
      const covered = coveredByMergeCoords([region(1, 1, 1, 1048576)], 1, 3, 10, 12);

      expect([...covered].sort()).to.eql(['col1 row10', 'col1 row11', 'col1 row12']);
    });

    it('are none for a merge outside the quadrant', () => {
      expect(coveredByMergeCoords([region(5, 5, 6, 6)], 1, 3, 1, 3).size).to.equal(0);
    });

    it('are where a merge and the quadrant overlap', () => {
      const covered = coveredByMergeCoords([region(2, 2, 9, 9)], 1, 3, 1, 3);

      expect([...covered].sort()).to.eql(['col2 row2', 'col2 row3', 'col3 row2', 'col3 row3']);
    });
  });

  describe('how a cell shows the selection', () => {
    const SELECTION = { activeCell: { col: 2, row: 2 }, activeRange: { c1: 2, c2: 3, r1: 2, r2: 3 }, auxRanges: [] };

    it('tints the cells of the range other than the active one, which the cell-range part names too', () => {
      expect(selectionClassFor(SELECTION, 3, 3)).to.eql({ classes: ' cell-range', part: 'cell cell-range' });
    });

    it('marks neither the active cell of one range nor a cell outside it', () => {
      expect(selectionClassFor(SELECTION, 2, 2)).to.eql({ classes: '', part: 'cell' });
      expect(selectionClassFor(SELECTION, 5, 5)).to.eql({ classes: '', part: 'cell' });
    });

    it('rings the active cell of a selection of several ranges, and tints the cells of the others', () => {
      const several = { ...SELECTION, auxRanges: [{ c1: 5, c2: 5, r1: 5, r2: 5 }] };

      expect(selectionClassFor(several, 2, 2)).to.eql({ classes: ' selected-cell-highlight', part: 'cell' });
      expect(selectionClassFor(several, 5, 5)).to.eql({ classes: ' cell-range', part: 'cell cell-range' });
    });
  });

  describe("a cell's corner markers", () => {
    it('are none for a cell with neither a comment nor a bad formula', () => {
      expect(cellMarkers(sheet(), 'col1 row1', 1, 1)).to.eql({ invalid: '', comment: '' });
    });

    it('mark a bad formula, and a comment with the cell it marks, each only where there is one', () => {
      const state = sheet({ invalidFormulaCells: new Set(['col2 row3']), commentCells: new Set(['col4 row1']) });
      const bad = cellMarkers(state, 'col2 row3', 2, 3);
      const commented = cellMarkers(state, 'col4 row1', 4, 1);

      const invalid = drawn(bad.invalid).firstElementChild as HTMLElement;
      const comment = drawn(commented.comment).firstElementChild as HTMLElement;
      expect([invalid.className, invalid.getAttribute('part')]).to.eql(['invalidformula-indicator', 'invalid-triangle']);
      expect([comment.className, comment.getAttribute('part'), comment.dataset.col, comment.dataset.row]).to.eql([
        'comment-indicator',
        'comment-triangle',
        '4',
        '1'
      ]);
      expect([bad.comment, commented.invalid]).to.eql(['', '']);
    });

    it('are none of either for a sheet that lists no such cells', () => {
      const state = { ...sheet(), invalidFormulaCells: undefined, commentCells: undefined };

      expect(cellMarkers(state, 'col1 row1', 1, 1)).to.eql({ invalid: '', comment: '' });
    });
  });

  describe('the plain cells', () => {
    it('are drawn a row of cells at a time, each where its column and row are, by its coordinates', () => {
      const container = drawn(renderQuadrant(main(sheet({ cellData: new Map([cell(2, 3)]) }))));

      expect(container.querySelectorAll('.cell-row')).to.have.length(3);
      expect(container.querySelector('.cell-row')!.children).to.have.length(4);
      const c = inDrawn(container, 2, 3);
      expect(c.className).to.equal('cell col2 row3 quadrant-main cs0');
      expect([c.dataset.col, c.dataset.row, c.getAttribute('part'), c.textContent]).to.eql(['2', '3', 'cell', 'v2.3']);
      expect(c.style.cssText).to.equal('left: 50px; top: 40px; width: 50px; height: 20px;');
    });

    it('collapse in a hidden column, and wear the colour a number format gives the value', () => {
      const container = drawn(renderQuadrant(main(sheet({ cellData: new Map([cell(1, 1, { textColor: 'ff0000' })]) }))));

      expect(inDrawn(container, 3, 1).style.display).to.equal('none');
      expect(inDrawn(container, 1, 1).style.color).to.equal('rgb(255, 0, 0)');
      expect(inDrawn(container, 2, 1).className, 'the default style for a cell sent nothing').to.contain(' cs0');
    });

    it('show hashes for a value too wide for its column, once measured so', () => {
      const flagged = cell(1, 1, { needsMeasure: true });

      const unmeasured = drawn(renderQuadrant(main(sheet({ cellData: new Map([flagged]) }))));
      const measured = drawn(
        renderQuadrant(main(sheet({ cellData: new Map([flagged]), tooNarrowCells: new Set(['col1 row1']) })))
      );

      expect(inDrawn(unmeasured, 1, 1).textContent).to.equal('v1.1');
      expect(inDrawn(measured, 1, 1).textContent).to.equal('###');
    });

    it('run text on into the cells beside them, from the left edge for a right-aligned value', () => {
      const container = drawn(
        renderQuadrant(
          main(
            sheet({
              cellData: new Map([
                cell(1, 1, { overflowWidth: 120 }),
                cell(1, 2, { overflowWidth: 120, cellStyle: 'cs0 r' }),
                cell(1, 3, { overflowWidth: 50 })
              ])
            })
          )
        )
      );

      const runsOn = inDrawn(container, 1, 1);
      expect(runsOn.classList.contains('overflowing')).to.be.true;
      expect(runsOn.querySelector<HTMLElement>('.cell-spill')!.style.width).to.equal('120px');
      const right = inDrawn(container, 1, 2).querySelector<HTMLElement>('.cell-spill.runs-right')!;
      expect(right.style.width).to.equal('118px');
      const cut = inDrawn(container, 1, 3);
      expect([cut.classList.contains('cut-short'), cut.classList.contains('overflowing')]).to.eql([true, false]);
    });

    it('keep text that fits after all, and text under a merge, to themselves', () => {
      const container = drawn(
        renderQuadrant(
          main(
            sheet({
              cellData: new Map([cell(1, 1, { overflowWidth: 120 }), cell(2, 2, { overflowWidth: 120 })]),
              fittingSpills: new Set(['col1 row1']),
              mergedRegions: [region(2, 2, 2, 3)]
            })
          )
        )
      );

      expect(inDrawn(container, 1, 1).querySelector('.cell-spill')).to.equal(null);
      expect(inDrawn(container, 2, 2).querySelector('.cell-spill')).to.equal(null);
    });

    it("show the component a cell holds, marking a custom editor's cell, and leave a merge's slot to the merge", () => {
      const container = drawn(
        renderQuadrant(
          main(
            sheet({
              cellSlots: { 'col1 row1': 'custom-component-4', 'col2 row1': 'custom-editor-5', 'col4 row2': 'custom-component-6' },
              mergedRegions: [region(4, 2, 4, 3)]
            })
          )
        )
      );

      expect(inDrawn(container, 1, 1).querySelector('slot')!.name).to.equal('custom-component-4');
      expect(inDrawn(container, 1, 1).classList.contains('custom-editor-cell')).to.be.false;
      expect(inDrawn(container, 2, 1).classList.contains('custom-editor-cell')).to.be.true;
      expect(inDrawn(container, 4, 2).querySelector('slot')).to.equal(null);
    });

    it('mark links, the selection, and their corners, leaving the markers of a covered cell to its merge', () => {
      const container = drawn(
        renderQuadrant(
          main(
            sheet({
              hyperlinkCells: new Set(['col1 row1']),
              commentCells: new Set(['col1 row2', 'col4 row2']),
              mergedRegions: [region(4, 2, 4, 3)],
              selection: { activeCell: { col: 1, row: 1 }, activeRange: { c1: 1, c2: 2, r1: 1, r2: 1 }, auxRanges: [] }
            })
          )
        )
      );

      expect(inDrawn(container, 1, 1).className).to.contain(' link-cell');
      expect(inDrawn(container, 2, 1).getAttribute('part')).to.equal('cell cell-range');
      expect(inDrawn(container, 2, 1).className).to.contain(' cell-range');
      expect(inDrawn(container, 1, 2).querySelector('.comment-indicator')).to.not.equal(null);
      expect(inDrawn(container, 4, 2).querySelector('.comment-indicator')).to.equal(null);
    });

    it('draw a cell with no markers as its text and the one marker of its binding, a thousand cells being drawn', () => {
      const container = drawn(renderQuadrant(main(sheet({ cellData: new Map([cell(1, 1)]) }))));

      const nodes = Array.from(inDrawn(container, 1, 1).childNodes, (node) => node.nodeType);
      expect(nodes).to.eql([Node.COMMENT_NODE, Node.TEXT_NODE]);
    });
  });

  describe('the merged cells', () => {
    it("are drawn over the cells they span, as far as the pane shows them, with their first cell's value", () => {
      const state = sheet({ cellData: new Map([cell(1, 2, { cellStyle: 'cs4' })]), mergedRegions: [region(1, 2, 2, 9)] });

      const merged = drawn(buildMergedCellOverlays(main(state))).querySelector<HTMLElement>('.merged-cell')!;

      expect(merged.className).to.equal('cell col1 row2 merged-cell cs4');
      expect(merged.style.cssText).to.equal('left: 0px; top: 20px; width: 100px; height: 40px; z-index: 2;');
      expect(merged.textContent!.trim()).to.equal('v1.2');
      expect([merged.dataset.col, merged.dataset.row, merged.getAttribute('part')]).to.eql(['1', '2', 'cell']);
    });

    it('are drawn at their box, in the pane of their first cell alone, while any of them is in view', () => {
      const state = sheet({
        horizontalSplitPosition: 1,
        frozenColX: [0],
        frozenColWidths: [50],
        mergedRegions: [region(1, 1, 3, 1), region(2, 5, 3, 6)],
        mergedRegionBoxes: [
          { left: 0, top: 0, width: 150, height: 20 },
          { left: 50, top: 80, width: 50, height: 40 }
        ]
      });
      const [scrolling, frozen] = quadrants(state);

      const inScrolling = drawn(buildMergedCellOverlays(scrolling)).querySelectorAll('.merged-cell');
      const inFrozen = drawn(buildMergedCellOverlays(frozen)).querySelectorAll<HTMLElement>('.merged-cell');

      expect(inScrolling, 'starts in the frozen pane; the other is out of view').to.have.length(0);
      expect(inFrozen).to.have.length(1);
      expect(inFrozen[0].style.width).to.equal('150px');
    });

    it("show their first cell's place in the selection, its markers and the component it holds", () => {
      const state = sheet({
        mergedRegions: [region(2, 2, 3, 3)],
        cellSlots: { 'col2 row2': 'custom-component-1' },
        invalidFormulaCells: new Set(['col2 row2']),
        commentCells: new Set(['col2 row2']),
        selection: { activeCell: { col: 1, row: 1 }, activeRange: { c1: 1, c2: 2, r1: 1, r2: 2 }, auxRanges: [] }
      });

      const merged = drawn(buildMergedCellOverlays(main(state))).querySelector<HTMLElement>('.merged-cell')!;

      expect(merged.className).to.contain(' cell-range');
      expect(merged.getAttribute('part')).to.equal('cell cell-range');
      expect(Array.from(merged.children, (child) => child.localName + '.' + child.className)).to.eql([
        'div.invalidformula-indicator',
        'div.comment-indicator',
        'slot.'
      ]);
    });

    it('are left out of a pane that does not show their first cell', () => {
      const state = sheet({ mergedRegions: [region(6, 1, 7, 2)] });

      expect(buildMergedCellOverlays(main(state))).to.eql([]);
    });
  });
});
