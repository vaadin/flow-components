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
  initialSheetModel,
  SHEET_PROPERTIES,
  type SheetPropertyName,
  withProperty,
  withValues
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetModel.js';
import { VaadinSpreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/vaadin-spreadsheet.js';

/**
 * The properties the server mirrors onto the element, as the client reads
 * them: what each is before the server sends it, the same read from JSON text
 * as from a parsed value, and a model replaced on each change that keeps the
 * values the change leaves alone.
 */
describe('Spreadsheet model of the server properties', () => {
  /** A value of each collection, as the server sends it parsed, and as read in. */
  const collections: Record<string, { sent: unknown; read: unknown }> = {
    rowH: { sent: { runs: [15, 1, 30, 2] }, read: [15, 30, 30] },
    colW: { sent: { runs: [50, 2, 80, 1] }, read: [50, 50, 80] },
    hiddenColumnIndexes: { sent: [2, 5], read: new Set([2, 5]) },
    hiddenRowIndexes: { sent: [3], read: new Set([3]) },
    lockedColumnIndexes: { sent: [1], read: new Set([1]) },
    lockedRowIndexes: { sent: [4], read: new Set([4]) },
    verticalScrollPositions: { sent: [0, 600], read: [0, 600] },
    horizontalScrollPositions: { sent: [10, 0], read: [10, 0] },
    cellStyleToCSSStyle: { sent: { 0: 'color:black;', 1: 'color:red;' }, read: { 0: 'color:black;', 1: 'color:red;' } },
    rowIndexToStyleIndex: { sent: { 3: 1 }, read: { 3: 1 } },
    columnIndexToStyleIndex: { sent: { 2: 4 }, read: { 2: 4 } },
    shiftedCellBorderStyles: { sent: ['.col2.row3::after{}'], read: ['.col2.row3::after{}'] },
    conditionalFormattingStyles: { sent: { 1: 'color:red;' }, read: { 1: 'color:red;' } },
    colGroupingData: {
      sent: [{ startIndex: 1, endIndex: 3, level: 1, collapsed: false }],
      read: [{ startIndex: 1, endIndex: 3, level: 1, collapsed: false }]
    },
    rowGroupingData: {
      sent: [{ startIndex: 2, endIndex: 4, level: 1, collapsed: true }],
      read: [{ startIndex: 2, endIndex: 4, level: 1, collapsed: true }]
    },
    mergedRegions: {
      sent: [{ id: 1, col1: 1, col2: 2, row1: 1, row2: 2 }],
      read: [{ id: 1, col1: 1, col2: 2, row1: 1, row2: 2 }]
    },
    cellKeysToEditorIdMap: { sent: { col2row3: '7' }, read: { col2row3: '7' } },
    componentIDtoCellKeysMap: { sent: { '8': 'col1row1' }, read: { '8': 'col1row1' } },
    cellComments: { sent: { col2row2: 'Note' }, read: { col2row2: 'Note' } },
    cellCommentAuthors: { sent: { col2row2: 'Ann' }, read: { col2row2: 'Ann' } },
    visibleCellComments: { sent: ['col2row2'], read: ['col2row2'] },
    invalidFormulaCells: { sent: ['col3row3'], read: new Set(['col3row3']) },
    overlays: {
      sent: { img: { id: 'img', type: 'IMAGE', col: 1, row: 1, width: 10, height: 10, dx: 0, dy: 0 } },
      read: { img: { id: 'img', type: 'IMAGE', col: 1, row: 1, width: 10, height: 10, dx: 0, dy: 0 } }
    },
    hyperlinksTooltips: { sent: { col1row1: 'https://vaadin.com' }, read: { col1row1: 'https://vaadin.com' } },
    resources: { sent: ['img'], read: ['img'] }
  };

  const plainValues: Record<string, unknown> = {
    rowBufferSize: 300,
    columnBufferSize: 150,
    rows: 1000,
    cols: 26,
    defRowH: 20,
    defColW: 80,
    colGroupingMax: 2,
    rowGroupingMax: 3,
    colGroupingInversed: true,
    rowGroupingInversed: true,
    sheetProtected: true,
    showCustomEditorOnFocus: true,
    hasComponentFactory: true,
    invalidFormulaErrorMessage: null,
    verticalSplitPosition: 2,
    horizontalSplitPosition: 1,
    displayGridlines: false,
    displayRowColHeadings: false
  };

  function write(name: string, value: unknown) {
    return withProperty(initialSheetModel(), name as SheetPropertyName, value as never);
  }

  it('has a sample of each property here', () => {
    expect([...Object.keys(collections), ...Object.keys(plainValues)].sort()).to.eql(
      Object.keys(SHEET_PROPERTIES).sort()
    );
  });

  it('starts as a sheet the server has sent nothing of', () => {
    expect(initialSheetModel()).to.eql({
      rowBufferSize: 200,
      columnBufferSize: 200,
      rows: 0,
      cols: 0,
      defRowH: 15,
      defColW: 64,
      rowH: [],
      colW: [],
      hiddenColumnIndexes: new Set(),
      hiddenRowIndexes: new Set(),
      lockedColumnIndexes: new Set(),
      lockedRowIndexes: new Set(),
      verticalScrollPositions: [],
      horizontalScrollPositions: [],
      cellStyleToCSSStyle: {},
      rowIndexToStyleIndex: {},
      columnIndexToStyleIndex: {},
      shiftedCellBorderStyles: [],
      conditionalFormattingStyles: {},
      colGroupingData: [],
      rowGroupingData: [],
      colGroupingMax: 0,
      rowGroupingMax: 0,
      colGroupingInversed: false,
      rowGroupingInversed: false,
      sheetProtected: false,
      mergedRegions: [],
      cellKeysToEditorIdMap: {},
      componentIDtoCellKeysMap: {},
      cellComments: {},
      cellCommentAuthors: {},
      visibleCellComments: [],
      invalidFormulaCells: new Set(),
      overlays: {},
      showCustomEditorOnFocus: false,
      hasComponentFactory: false,
      invalidFormulaErrorMessage: 'Invalid formula',
      verticalSplitPosition: 0,
      horizontalSplitPosition: 0,
      hyperlinksTooltips: {},
      resources: [],
      displayGridlines: true,
      displayRowColHeadings: true
    });
  });

  it('gives each sheet collections of its own', () => {
    const one = initialSheetModel();
    const other = initialSheetModel();

    for (const name of Object.keys(collections)) {
      expect(one[name as SheetPropertyName], name).to.not.equal(other[name as SheetPropertyName]);
    }
  });

  for (const [name, { sent, read }] of Object.entries(collections)) {
    it(`reads ${name} from the JSON the server sends`, () => {
      expect(write(name, sent)[name as SheetPropertyName]).to.eql(read);
    });

    it(`reads ${name} given as text as empty, not as its characters`, () => {
      const empty = initialSheetModel()[name as SheetPropertyName];

      expect(write(name, JSON.stringify(sent))[name as SheetPropertyName]).to.eql(empty);
    });

    it(`reads ${name} cleared as empty`, () => {
      const empty = initialSheetModel()[name as SheetPropertyName];

      expect(write(name, null)[name as SheetPropertyName]).to.eql(empty);
    });
  }

  it('takes a plain value as it comes', () => {
    for (const [name, value] of Object.entries(plainValues)) {
      expect(write(name, value)[name as SheetPropertyName], name).to.equal(value);
    }
  });

  it('replaces the model on a change, never changing it in place', () => {
    const before = initialSheetModel();

    const written = withProperty(before, 'rows', 10);
    const changed = withValues(written, { cols: 5 });

    expect(Object.isFrozen(before), 'frozen').to.be.true;
    expect([before.rows, written.rows, changed.rows]).to.eql([0, 10, 10]);
    expect([before.cols, written.cols, changed.cols]).to.eql([0, 0, 5]);
    expect(Object.isFrozen(written) && Object.isFrozen(changed), 'frozen after a change').to.be.true;
  });

  it('keeps the values a change leaves alone as they are', () => {
    const before = withProperty(initialSheetModel(), 'mergedRegions', [{ id: 1, col1: 1, col2: 2, row1: 1, row2: 2 }]);

    const after = withProperty(before, 'rows', 10);

    expect(after.mergedRegions).to.equal(before.mergedRegions);
  });

  it('holds each value the server sends as it is, not a copy', () => {
    const regions = [{ id: 1, col1: 1, col2: 2, row1: 1, row2: 2 }];

    expect(withProperty(initialSheetModel(), 'mergedRegions', regions).mergedRegions).to.equal(regions);
  });

  it('is written by the names the element declares its properties by', () => {
    const declared = Object.keys((VaadinSpreadsheet as unknown as { properties: object }).properties);

    expect(Object.keys(SHEET_PROPERTIES).filter((name) => !declared.includes(name))).to.eql([]);
  });

  it('has every property the element declares, but those the sheet takes apart', () => {
    const apart = [
      // Not passed on to the sheet.
      'theme',
      // Nothing to hold.
      'reload',
      'workbookChangeToggle',
      // Held by the parts that draw them.
      'sheetIndex',
      'sheetNames',
      'workbookProtected',
      'infoLabelValue',
      'namedRanges',
      'lockFormatColumns',
      'lockFormatRows',
      'hasActions'
    ];
    const declared = Object.keys((VaadinSpreadsheet as unknown as { properties: object }).properties);

    expect(declared.filter((name) => !(name in SHEET_PROPERTIES) && !apart.includes(name))).to.eql([]);
    expect(
      apart.filter((name) => !declared.includes(name)),
      'declared'
    ).to.eql([]);
  });
});
