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
  computePositionedColGroupings,
  isCellInSelection,
  overlayComponentAppId,
  selectedColumnSet,
  selectedRowSet
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import { GROUPING_BUTTON_SIZE_PX } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetColumnHeader.js';
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

describe('computePositionedColGroupings', () => {
  const widths = [50, 60, 70, 80];
  const grouping = (over: Partial<any> = {}) => ({
    uniqueIndex: 1,
    level: 0,
    startIndex: 1,
    endIndex: 2,
    collapsed: false,
    ...over
  });

  it('starts the line at the first grouped column', () => {
    // Indices are 0-based on the wire, so startIndex 1 is the second
    // column and the line starts after the first one's 50px.
    const [g] = computePositionedColGroupings([grouping()], widths, false, 100);
    expect(g.startPx).to.equal(50);
  });

  it('ends the line after the last grouped column', () => {
    const [g] = computePositionedColGroupings([grouping()], widths, false, 100);
    expect(g.endPx).to.equal(50 + 60 + 70);
  });

  it('puts the button before the group by default', () => {
    const [g] = computePositionedColGroupings([grouping()], widths, false, 100);
    expect(g.buttonPx).to.equal(50 - GROUPING_BUTTON_SIZE_PX);
  });

  it('puts the button after the group when the outline is inversed', () => {
    // Excel can put the summary column on either side of its group.
    const [g] = computePositionedColGroupings([grouping()], widths, true, 100);
    expect(g.buttonPx).to.equal(50 + 60 + 70);
  });

  it('falls back to the default width past the measured columns', () => {
    const [g] = computePositionedColGroupings(
      [grouping({ startIndex: 5, endIndex: 5 })],
      widths,
      false,
      100
    );
    // 4 measured columns then one default-width column.
    expect(g.startPx).to.equal(50 + 60 + 70 + 80 + 100);
    expect(g.endPx).to.equal(g.startPx + 100);
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
