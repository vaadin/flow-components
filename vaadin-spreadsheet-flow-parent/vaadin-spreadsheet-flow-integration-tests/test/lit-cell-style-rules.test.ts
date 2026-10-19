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
  buildCellStyleBlock,
  cellStyleRules,
  editorCellStyle,
  type StyleTables,
  styleRuleInputs
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/CellStyleRules.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * A workbook's styles as CSS, on their own: the rules that style the cells,
 * in the order they cascade, and the same styles resolved for the editor
 * over one cell.
 */
describe('CellStyleRules', () => {
  const NO_STYLES: StyleTables = {
    cellStyleToCSSStyle: {},
    conditionalFormattingStyles: {},
    shiftedCellBorderStyles: []
  };
  const LINK_RULE = '.cell.link-cell { color: var(--vaadin-spreadsheet-link-fg, var(--default-color)); }';

  function tables(overrides: Partial<StyleTables>): StyleTables {
    return { ...NO_STYLES, ...overrides };
  }

  describe('the rules for the cells', () => {
    it('are the colour of links alone for a workbook with no styles', () => {
      expect(cellStyleRules(NO_STYLES)).to.eql([LINK_RULE]);
    });

    it('turn the gridlines off first, for a sheet without them', () => {
      expect(cellStyleRules(tables({ displayGridlines: false }))[0]).to.equal(
        '.cell.cell { border-right: 0; border-bottom: 0; }'
      );
      expect(cellStyleRules(tables({ displayGridlines: true }))).to.eql([LINK_RULE]);
    });

    it("lay the default style under every cell, its font through the documented properties, and links in its colour", () => {
      const rules = cellStyleRules(tables({ cellStyleToCSSStyle: { 0: 'color: #111; font-family: Calibri; color: #222' } }));

      expect(rules).to.eql([
        '.cell.cell { color: #111; font-family: Calibri; color: #222 ' +
          'font-family: var(--vaadin-spreadsheet-cell-font, var(--default-font-family)); ' +
          'font-size: var(--vaadin-spreadsheet-cell-font-size, var(--default-font-size)); }',
        '.cell.link-cell { color: var(--vaadin-spreadsheet-link-fg, #222); }'
      ]);
    });

    it('style the cells of a style, and the unstyled cells of the rows and columns formatted with it', () => {
      const rules = cellStyleRules(
        tables({
          cellStyleToCSSStyle: { 0: 'color: black', 3: 'font-weight: bold' },
          rowIndexToStyleIndex: { 4: 3, 9: 3 },
          columnIndexToStyleIndex: { 2: 3 }
        })
      );

      expect(rules.slice(2)).to.eql(['.cell.cs3, .cell.row4.cs0, .cell.row9.cs0, .cell.col2.cs0 { font-weight: bold }']);
    });

    it('list at most a thousand selectors in any one rule', () => {
      const rows: Record<number, number> = {};
      for (let row = 1; row <= 1500; row++) {
        rows[row] = 3;
      }

      const rules = cellStyleRules(tables({ cellStyleToCSSStyle: { 3: 'color: red' }, rowIndexToStyleIndex: rows }));

      const [first, second] = rules.slice(1).map((rule) => rule.split(', ').length);
      expect([first, second]).to.eql([1000, 501]);
      expect(rules.slice(1).every((rule) => rule.endsWith(' { color: red }'))).to.be.true;
    });

    it('put the conditional formats after the styles, the later rule last', () => {
      const rules = cellStyleRules(
        tables({ cellStyleToCSSStyle: { 1: 'color: red' }, conditionalFormattingStyles: { 10: 'color: blue', 2: 'color: green' } })
      );

      expect(rules.slice(1)).to.eql([
        '.cell.cs1 { color: red }',
        '.cell.cf2 { color: green }',
        '.cell.cf10 { color: blue }'
      ]);
    });

    it('scope the borders shifted onto a neighbour to cells, last, and drop a rule with no block', () => {
      const rules = cellStyleRules(
        tables({
          conditionalFormattingStyles: { 1: 'color: blue' },
          shiftedCellBorderStyles: ['.col3.row4, .col3.row5 { border-bottom: 1px solid red; }', '.col1.row1']
        })
      );

      expect(rules.slice(1)).to.eql([
        '.cell.cf1 { color: blue }',
        '.cell.col3.row4, .cell.col3.row5 { border-bottom: 1px solid red; }'
      ]);
    });

    it('go into one style element, as text', () => {
      const container = document.createElement('div');

      render(buildCellStyleBlock(tables({ cellStyleToCSSStyle: { 1: 'font-family: "</style><b>"' } })), container);

      const style = container.querySelector('style')!;
      expect(container.querySelectorAll('style, b')).to.have.length(1);
      expect(style.textContent).to.contain('.cell.cs1 { font-family: "</style><b>" }');
    });

    it('are written from the style tables alone', () => {
      const state = tables({ displayGridlines: false, rowIndexToStyleIndex: {}, columnIndexToStyleIndex: {} });

      expect(styleRuleInputs(state)).to.eql([
        state.cellStyleToCSSStyle,
        false,
        state.rowIndexToStyleIndex,
        state.columnIndexToStyleIndex,
        state.conditionalFormattingStyles,
        state.shiftedCellBorderStyles
      ]);
    });
  });

  describe("the editor's style", () => {
    function cellStyled(cellStyle: string): Map<string, CellData> {
      return new Map([
        [
          'col3 row2',
          { col: 3, row: 2, value: '', cellStyle, locked: false, needsMeasure: false, isPercentage: false }
        ]
      ]);
    }
    const STYLES = {
      0: 'font-family: Calibri; font-size: 11pt; color: #000; border-bottom: 1px solid',
      3: 'background-color: yellow; padding-left: 4px; font-weight: bold',
      4: 'font-style: italic',
      7: 'text-decoration: underline'
    };

    it("wears the default's colour but not its font, then the cell's own text and fill, never its box", () => {
      const style = editorCellStyle(
        { cellData: cellStyled('cs3'), cellStyleToCSSStyle: STYLES, conditionalFormattingStyles: {} },
        3,
        2
      );

      expect(style).to.equal('color: #000;background-color: yellow;font-weight: bold;');
    });

    it("wears the conditional formats the cell matches, the later last, then a number's right alignment", () => {
      const style = editorCellStyle(
        {
          cellData: cellStyled('cs3 cf10 r cf2'),
          cellStyleToCSSStyle: { 3: 'color: red' },
          conditionalFormattingStyles: { 2: 'color: green', 10: 'color: blue; border: 0' }
        },
        3,
        2
      );

      expect(style).to.equal('color: red;color: green;color: blue;text-align: right;');
    });

    it("wears the styles of its row and column, in the order of their rules, for a cell with no style of its own", () => {
      const state = {
        cellData: cellStyled('cs0'),
        cellStyleToCSSStyle: STYLES,
        conditionalFormattingStyles: {},
        rowIndexToStyleIndex: { 2: 7 },
        columnIndexToStyleIndex: { 3: 4 }
      };

      expect(editorCellStyle(state, 3, 2)).to.equal('color: #000;font-style: italic;text-decoration: underline;');
      expect(editorCellStyle({ ...state, cellData: cellStyled('cs3') }, 3, 2), 'a style of its own').to.not.contain(
        'italic'
      );
    });

    it('wears the default style for a cell the server sent nothing for, and passes on the font properties', () => {
      const style = editorCellStyle(
        {
          cellData: new Map(),
          cellStyleToCSSStyle: { 0: 'color: #333; --default-font-family: Arial; --default-font-size: 15px' },
          conditionalFormattingStyles: {}
        },
        5,
        5
      );

      expect(style).to.equal('color: #333;--default-font-family: Arial;--default-font-size: 15px;');
    });
  });
});
