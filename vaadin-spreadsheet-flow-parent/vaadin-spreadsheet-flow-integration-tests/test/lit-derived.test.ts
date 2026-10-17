/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { Derived } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/Derived.js';
import {
  initialSheetModel,
  type SheetModel,
  withValues
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetModel.js';
import {
  type SelectionState,
  selectSingleCell
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SelectionState.js';
import type { PopupButtonState } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * What the sheet draws that is worked out from the model and the selection:
 * kept, the same value, while all it is worked out from stays the same, as it
 * does through a scroll, and worked out again when any of it changes.
 */

const MODEL = withValues(initialSheetModel(), {
  rows: 10,
  cols: 10,
  colW: [50, 60, 70],
  rowH: [15, 30, 15],
  horizontalSplitPosition: 2,
  verticalSplitPosition: 2,
  colGroupingData: [{ uniqueIndex: 1, level: 1, startIndex: 1, endIndex: 2, collapsed: false }],
  rowGroupingData: [{ uniqueIndex: 2, level: 1, startIndex: 1, endIndex: 2, collapsed: false }],
  cellComments: { 'col1 row1': 'A note' },
  hyperlinksTooltips: { 'col2 row2': 'https://vaadin.com' },
  componentIDtoCellKeysMap: { '7': 'col2 row2' },
  cellKeysToEditorIdMap: { 'col3 row3': '8' }
});

const BUTTONS: PopupButtonState[] = [{ col: 2, row: 3, active: false } as PopupButtonState];

const DRAFT = { text: '=A1+B2', caret: 3 };

interface Inputs {
  model: SheetModel;
  selection: SelectionState;
  activeCellKey: string | null;
  draft: { text: string; caret: number } | null;
  sheetName: string;
  buttons: readonly PopupButtonState[];
}

const INPUTS: Inputs = {
  model: MODEL,
  selection: selectSingleCell(2, 2),
  activeCellKey: 'col2 row2',
  draft: DRAFT,
  sheetName: 'Sheet1',
  buttons: BUTTONS
};

/** The model with values changed. */
function changed(values: Partial<SheetModel>): Partial<Inputs> {
  return { model: withValues(MODEL, values) };
}

const VALUES: Array<{
  name: string;
  of: (derived: Derived, i: Inputs) => unknown;
  from: Record<string, Partial<Inputs>>;
}> = [
  {
    name: 'the frozen panes',
    of: (d, i) => d.frozenPanes(i.model),
    from: {
      'the frozen columns': changed({ horizontalSplitPosition: 3 }),
      'the frozen rows': changed({ verticalSplitPosition: 3 }),
      'the row heights': changed({ rowH: [20, 30, 15] }),
      'the column widths': changed({ colW: [55, 60, 70] }),
      'the hidden rows': changed({ hiddenRowIndexes: new Set([1]) }),
      'the hidden columns': changed({ hiddenColumnIndexes: new Set([1]) }),
      'the default row height': changed({ defRowH: 20 }),
      'the default column width': changed({ defColW: 80 })
    }
  },
  {
    name: 'the cell slots',
    of: (d, i) => d.cellSlots(i.model, i.activeCellKey),
    from: {
      'the components in cells': changed({ componentIDtoCellKeysMap: { '7': 'col4 row4' } }),
      'the editors in cells': changed({ cellKeysToEditorIdMap: { 'col5 row5': '8' } }),
      'showing editors on focus': changed({ showCustomEditorOnFocus: true }),
      'the selected cell': { activeCellKey: 'col3 row3' }
    }
  },
  {
    name: 'the cells with a note',
    of: (d, i) => d.commentCells(i.model),
    from: { 'the notes': changed({ cellComments: { 'col4 row4': 'Another' } }) }
  },
  {
    name: 'the cells with a link',
    of: (d, i) => d.hyperlinkCells(i.model),
    from: { 'the links': changed({ hyperlinksTooltips: { 'col4 row4': 'https://vaadin.com/docs' } }) }
  },
  {
    name: 'the selected columns',
    of: (d, i) => d.selectedColumns(i.selection),
    from: { 'the selection': { selection: selectSingleCell(3, 2) } }
  },
  {
    name: 'the selected rows',
    of: (d, i) => d.selectedRows(i.selection),
    from: { 'the selection': { selection: selectSingleCell(2, 3) } }
  },
  {
    name: 'the column outline',
    of: (d, i) => d.columnGroupings(i.model),
    from: {
      'the column groups': changed({
        colGroupingData: [{ uniqueIndex: 1, level: 1, startIndex: 2, endIndex: 3, collapsed: false }]
      }),
      'the column widths': changed({ colW: [55, 60, 70] }),
      'the summary column side': changed({ colGroupingInversed: true }),
      'the default column width': changed({ defColW: 80 }),
      'the row outline showing': changed({ rowGroupingMax: 1 })
    }
  },
  {
    name: 'the row outline',
    of: (d, i) => d.rowGroupings(i.model),
    from: {
      'the row groups': changed({
        rowGroupingData: [{ uniqueIndex: 2, level: 1, startIndex: 2, endIndex: 3, collapsed: false }]
      }),
      'the row heights': changed({ rowH: [20, 30, 15] }),
      'the summary row side': changed({ rowGroupingInversed: true }),
      'the default row height': changed({ defRowH: 20 }),
      'the column outline showing': changed({ colGroupingMax: 1 })
    }
  },
  {
    name: 'the references of the formula being written',
    of: (d, i) => d.formulaReferences(i.draft, i.sheetName),
    from: {
      'the formula': { draft: { text: '=A1+C3', caret: 3 } },
      'the sheet shown': { sheetName: 'Sheet2' }
    }
  },
  {
    name: 'the popup buttons',
    of: (d, i) => d.popupButtons(i.buttons, i.model),
    from: {
      'the buttons': { buttons: [...BUTTONS] },
      'the frozen columns': changed({ horizontalSplitPosition: 3 }),
      'the frozen rows': changed({ verticalSplitPosition: 3 }),
      'the row heights': changed({ rowH: [20, 30, 15] }),
      'the column widths': changed({ colW: [55, 60, 70] }),
      'the hidden rows': changed({ hiddenRowIndexes: new Set([1]) }),
      'the hidden columns': changed({ hiddenColumnIndexes: new Set([1]) }),
      'the default row height': changed({ defRowH: 20 }),
      'the default column width': changed({ defColW: 80 })
    }
  }
];

describe('Spreadsheet — what it draws from the model and the selection', () => {
  for (const { name, of, from } of VALUES) {
    describe(name, () => {
      it('is kept while all it comes from stays the same', () => {
        const derived = new Derived();
        const first = of(derived, INPUTS);

        // A model replaced by a change to something else.
        const second = of(derived, { ...INPUTS, model: withValues(MODEL, { rowBufferSize: 300 }) });

        expect(second).to.equal(first);
      });

      for (const [what, change] of Object.entries(from)) {
        it(`is worked out again when ${what} change`, () => {
          const derived = new Derived();
          const first = of(derived, INPUTS);

          expect(of(derived, { ...INPUTS, ...change })).to.not.equal(first);
        });
      }
    });
  }

  it('places the frozen columns and rows from the top left corner', () => {
    expect(new Derived().frozenPanes(MODEL)).to.eql({
      colX: [0, 50],
      colWidths: [50, 60],
      width: 110,
      // 15 pt is 20 px, 30 pt 40 px.
      rowY: [0, 20],
      rowHeights: [20, 40],
      height: 60
    });
  });

  it('draws a hidden frozen column and row as nothing', () => {
    const model = withValues(MODEL, { hiddenColumnIndexes: new Set([1]), hiddenRowIndexes: new Set([2]) });

    const frozen = new Derived().frozenPanes(model);

    expect([frozen.colWidths, frozen.width, frozen.rowHeights, frozen.height]).to.eql([[0, 60], 60, [20, 0], 20]);
  });

  it('colours no references while no formula is being written', () => {
    expect(new Derived().formulaReferences(null, 'Sheet1')).to.eql([]);
  });

  it('marks the reference the caret is in', () => {
    const refs = new Derived().formulaReferences(DRAFT, 'Sheet1');

    expect(refs.map((ref) => ref.atCaret)).to.eql([true, false]);
  });

  it('slots each cell with its component and its editor', () => {
    const { slots } = new Derived().cellSlots(MODEL, null);

    expect(Object.keys(slots).sort()).to.eql(['col2 row2', 'col3 row3']);
  });
});
