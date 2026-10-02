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
  commitStep,
  computeMergedRegionBoxes,
  computePositionedColGroupings,
  isCellInSelection,
  overlayComponentAppId,
  selectedColumnSet,
  selectedRowSet
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import {
  cellComponentSlotName,
  cellEditorSlotName,
  overlayComponentSlotName
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/VirtualChildren.js';
import {
  addAuxRange,
  selectRange,
  selectSingleCell
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SelectionState.js';

/**
 * Pure helpers the connector leans on, pinned directly.
 *
 * Each of these was reachable only through a rendered sheet, which makes
 * an off-by-one in the geometry or a missed aux range read as "the
 * screen looks slightly wrong" rather than as a failing assertion.
 */

describe('selectedColumnSet / selectedRowSet', () => {
  it('covers every column of the active range', () => {
    const set = selectedColumnSet(selectRange(2, 1, 4, 1));
    expect([...set].sort((a, b) => a - b)).to.deep.equal([2, 3, 4]);
  });

  it('covers every row of the active range', () => {
    const set = selectedRowSet(selectRange(1, 3, 1, 6));
    expect([...set].sort((a, b) => a - b)).to.deep.equal([3, 4, 5, 6]);
  });

  it('handles a range dragged right to left', () => {
    // A selection dragged backwards stores its corners unordered.
    const set = selectedColumnSet(selectRange(5, 1, 2, 1));
    expect([...set].sort((a, b) => a - b)).to.deep.equal([2, 3, 4, 5]);
  });

  it('holds a single cell as one column and one row', () => {
    expect([...selectedColumnSet(selectSingleCell(3, 7))]).to.deep.equal([3]);
    expect([...selectedRowSet(selectSingleCell(3, 7))]).to.deep.equal([7]);
  });

  it('ignores aux ranges — only the active range is headed', () => {
    // A Ctrl-click makes the clicked cell the active range and demotes
    // the previous one to an aux range. Header highlighting follows the
    // active one; the extras are drawn on the cells themselves.
    const selection = addAuxRange(selectSingleCell(1, 1), 9, 9, 9, 9);
    expect(selection.auxRanges).to.have.length(1);
    expect([...selectedColumnSet(selection)]).to.deep.equal([9]);
  });
});

describe('isCellInSelection', () => {
  it('finds a cell inside the active range', () => {
    expect(isCellInSelection(3, 3, selectRange(2, 2, 4, 4))).to.be.true;
  });

  it('rejects a cell outside every range', () => {
    expect(isCellInSelection(9, 9, selectRange(2, 2, 4, 4))).to.be.false;
  });

  it('counts the edges of the range as inside', () => {
    const selection = selectRange(2, 2, 4, 4);
    expect(isCellInSelection(2, 2, selection)).to.be.true;
    expect(isCellInSelection(4, 4, selection)).to.be.true;
  });

  it('finds a cell in an aux range', () => {
    // Right-clicking inside any part of a Ctrl-click selection keeps it;
    // only a click outside all of them moves the selection.
    const selection = addAuxRange(selectSingleCell(1, 1), 7, 7, 8, 8);
    expect(isCellInSelection(8, 7, selection)).to.be.true;
  });

  it('handles a range stored with its corners reversed', () => {
    expect(isCellInSelection(3, 3, selectRange(4, 4, 2, 2))).to.be.true;
  });
});

/**
 * Where a group's bracket and button go, as the GWT client laid them out.
 *
 * The button sits over the group's summary column - after the group by
 * default, before it when the outline is inversed - and the bracket runs to
 * it. The Lit client had the button on the wrong side: before the group
 * by default.
 */
describe('computePositionedColGroupings', () => {
  // Columns A-D; the group is B:C, at 50..180.
  const widths = [50, 60, 70, 80];
  const grouping = (over: Partial<any> = {}) => ({
    uniqueIndex: 1,
    level: 0,
    startIndex: 1,
    endIndex: 2,
    collapsed: false,
    ...over
  });

  it('starts the bracket a few pixels into the first grouped column', () => {
    // Indices are 0-based on the wire, so startIndex 1 is the second
    // column, which starts after the first one's 50px.
    const [g] = computePositionedColGroupings([grouping()], widths, false, 100, 5);
    expect(g.startPx).to.equal(50 + 5);
  });

  it('runs the bracket to the middle of the summary column after the group', () => {
    const [g] = computePositionedColGroupings([grouping()], widths, false, 100, 5);
    expect(g.endPx).to.equal(50 + 60 + 70 + 80 / 2);
  });

  it('puts the button over the summary column after the group by default', () => {
    // Excel's default: summary columns to the right of their detail.
    const [g] = computePositionedColGroupings([grouping()], widths, false, 100, 5);
    expect(g.buttonPx).to.equal(50 + 60 + 70 + 80 / 2);
  });

  it('puts the button over the summary column before the group when inversed', () => {
    // The bracket then runs from that button to the group's end.
    const [g] = computePositionedColGroupings([grouping()], widths, true, 100, 5);
    expect(g.buttonPx).to.equal(50 / 2);
    expect(g.startPx).to.equal(50 / 2);
    expect(g.endPx).to.equal(50 + 60 + 70);
  });

  it('starts an inversed group in the first column at the sheet\'s edge', () => {
    const [g] = computePositionedColGroupings([grouping({ startIndex: 0, endIndex: 1 })], widths, true, 100, 5);
    expect(g.buttonPx).to.equal(0);
  });

  it('falls back to the default width past the measured columns', () => {
    const [g] = computePositionedColGroupings(
      [grouping({ startIndex: 5, endIndex: 5 })],
      widths,
      false,
      100
    );
    // 4 measured columns then one default-width column, and the summary
    // column after it is default width too.
    expect(g.startPx).to.equal(50 + 60 + 70 + 80 + 100);
    expect(g.endPx).to.equal(g.startPx + 100 + 100 / 2);
  });

  it('carries level and collapsed through untouched', () => {
    const [g] = computePositionedColGroupings(
      [grouping({ level: 2, collapsed: true, uniqueIndex: 9 })],
      widths,
      false,
      100
    );
    expect(g.level).to.equal(2);
    expect(g.collapsed).to.be.true;
    expect(g.uniqueIndex).to.equal(9);
  });

  it('positions each of several groupings', () => {
    const positions = computePositionedColGroupings(
      [grouping({ uniqueIndex: 1, startIndex: 0, endIndex: 0 }),
       grouping({ uniqueIndex: 2, startIndex: 2, endIndex: 3 })],
      widths,
      false,
      100
    );
    expect(positions.map((p) => p.startPx)).to.deep.equal([0, 110]);
  });

  it('returns nothing when the sheet has no groupings', () => {
    expect(computePositionedColGroupings([], widths, false, 100)).to.deep.equal([]);
  });
});

describe('slot names — the contract between the connector and the grid', () => {
  it('names each kind of placement apart', () => {
    // A cell can hold both a component and an editor, and a chart
    // overlay is keyed by the same kind of node id. Sharing a name
    // between any two would cross the projections.
    const names = [
      overlayComponentSlotName('7'),
      cellComponentSlotName('7'),
      cellEditorSlotName('7')
    ];
    expect(new Set(names).size).to.equal(3);
  });

  it('keeps the names the GWT client used', () => {
    // Not internal: an application can style or query these.
    expect(overlayComponentSlotName('7')).to.equal('overlay-component-7');
    expect(cellComponentSlotName('7')).to.equal('custom-component-7');
    expect(cellEditorSlotName('7')).to.equal('custom-editor-7');
  });
});

describe('overlayComponentAppId', () => {
  it('reads the appId the server put on the host', () => {
    const host = document.createElement('div');
    (host as any).appId = 'ROOT-1';
    expect(overlayComponentAppId(host)).to.equal('ROOT-1');
  });

  it('reads an absent appId as empty rather than "undefined"', () => {
    // The string "undefined" would be looked up as a real application
    // name and quietly find nothing.
    expect(overlayComponentAppId(document.createElement('div'))).to.equal('');
  });
});

/**
 * Where the selection lands when an edit is committed with a key.
 *
 * Extracted from the commit handler so the direction table can be read at
 * a glance: Enter down, Tab right, shift reverses each, an arrow the way
 * it points, and nothing at all for a blur.
 */
describe('commitStep — direction of the post-commit move', () => {
    it('moves down on Enter and up with shift', () => {
        expect(commitStep('Enter', false)).to.eql({ dCol: 0, dRow: 1 });
        expect(commitStep('Enter', true)).to.eql({ dCol: 0, dRow: -1 });
    });

    it('moves right on Tab and left with shift', () => {
        expect(commitStep('Tab', false)).to.eql({ dCol: 1, dRow: 0 });
        expect(commitStep('Tab', true)).to.eql({ dCol: -1, dRow: 0 });
    });

    it('follows an arrow key, which shift does not reverse', () => {
        expect(commitStep('ArrowUp', false)).to.eql({ dCol: 0, dRow: -1 });
        expect(commitStep('ArrowUp', true)).to.eql({ dCol: 0, dRow: -1 });
        expect(commitStep('ArrowDown', false)).to.eql({ dCol: 0, dRow: 1 });
        expect(commitStep('ArrowLeft', false)).to.eql({ dCol: -1, dRow: 0 });
        expect(commitStep('ArrowRight', false)).to.eql({ dCol: 1, dRow: 0 });
    });

    it('stays put for a commit with no key — a blur', () => {
        expect(commitStep(undefined, false)).to.be.null;
        expect(commitStep('a', false)).to.be.null;
    });
});

/**
 * Where merged regions are drawn.
 *
 * Every region gets a box, wherever its top-left cell is: a merge drawn only
 * while its first cell was on screen vanished as soon as that row scrolled
 * away, and one drawn only as far as its own quadrant cut off where it ran
 * past a freeze.
 */
describe('computeMergedRegionBoxes', () => {
  // 50px columns and 20px rows throughout.
  const col = (): number => 50;
  const row = (): number => 20;
  const none = { rows: 0, cols: 0 };
  const unscrolled = { left: 0, top: 0 };

  it('places a region by the sheet\'s own coordinates', () => {
    const [box] = computeMergedRegionBoxes(
      [{ id: 0, col1: 3, col2: 4, row1: 2, row2: 4 }],
      none,
      unscrolled,
      col,
      row
    );

    expect(box).to.deep.equal({ left: 100, top: 20, width: 100, height: 60 });
  });

  it('keeps the whole of a region however far the sheet scrolls', () => {
    // Without a freeze the region scrolls with the sheet: nothing pinned,
    // nothing to shorten.
    const [box] = computeMergedRegionBoxes(
      [{ id: 0, col1: 1, col2: 2, row1: 1, row2: 5 }],
      none,
      { left: 30, top: 50 },
      col,
      row
    );

    expect(box).to.deep.equal({ left: 0, top: 0, width: 100, height: 100 });
  });

  it('pins a region that runs past the frozen rows, shortened by the scroll', () => {
    // Rows 1-2 frozen; the region runs from row 2 to row 4, 40px past the
    // split. Scrolled 15px, 25px of that part is still out from under the
    // frozen rows.
    const regions = [{ id: 0, col1: 3, col2: 4, row1: 2, row2: 4 }];
    const frozen = { rows: 2, cols: 0 };

    const [still] = computeMergedRegionBoxes(regions, frozen, unscrolled, col, row);
    const [scrolled] = computeMergedRegionBoxes(regions, frozen, { left: 0, top: 15 }, col, row);
    const [gone] = computeMergedRegionBoxes(regions, frozen, { left: 0, top: 500 }, col, row);

    expect(still.height).to.equal(60);
    expect(scrolled.height).to.equal(20 + 25);
    expect(gone.height, 'never shorter than its frozen part').to.equal(20);
  });

  it('pins a region that runs past both splits on both axes', () => {
    const [box] = computeMergedRegionBoxes(
      [{ id: 0, col1: 1, col2: 3, row1: 1, row2: 3 }],
      { rows: 1, cols: 1 },
      { left: 60, top: 10 },
      col,
      row
    );

    expect(box).to.deep.equal({ left: 0, top: 0, width: 50 + 40, height: 20 + 30 });
  });

  it('does not shorten a region that starts past the split', () => {
    const [box] = computeMergedRegionBoxes(
      [{ id: 0, col1: 3, col2: 4, row1: 5, row2: 8 }],
      { rows: 2, cols: 2 },
      { left: 70, top: 30 },
      col,
      row
    );

    expect(box).to.deep.equal({ left: 100, top: 80, width: 100, height: 80 });
  });

  it('gives hidden rows and columns no size', () => {
    const [box] = computeMergedRegionBoxes(
      [{ id: 0, col1: 1, col2: 3, row1: 1, row2: 3 }],
      none,
      unscrolled,
      (c) => (c === 2 ? 0 : 50),
      (r) => (r === 2 ? 0 : 20)
    );

    expect(box).to.deep.equal({ left: 0, top: 0, width: 100, height: 40 });
  });
});
