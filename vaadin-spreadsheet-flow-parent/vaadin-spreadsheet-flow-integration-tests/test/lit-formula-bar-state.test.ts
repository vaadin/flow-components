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
  addressFieldText,
  formulaBarRenderState,
  NO_FORMULA_BAR_INPUTS
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/FormulaBarState.js';
import { selectSingleCell } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SelectionState.js';

/**
 * What the formula bar draws, worked out from what it is drawn from: the
 * address field's one rule, the formula field's text and the colours of a
 * formula being written.
 */
describe('Formula bar state', () => {
  const range = {
    activeCell: { col: 2, row: 2 },
    activeRange: { c1: 2, r1: 2, c2: 4, r2: 6 },
    auxRanges: []
  };

  describe('the address field', () => {
    it("names the active cell by its address while the server has given the selection no name", () => {
      expect(addressFieldText(selectSingleCell(28, 5), false, null)).to.equal('AB5');
    });

    it('shows the name the server gave the selection', () => {
      expect(addressFieldText(selectSingleCell(2, 2), false, 'Sales')).to.equal('Sales');
    });

    it('counts a range being grown out, whatever the server named it', () => {
      expect(addressFieldText(range, true, 'Sales')).to.equal('5R x 3C');
    });
  });

  describe('the whole bar', () => {
    it('shows the address, the text the formula field was given and the named ranges, with nothing coloured', () => {
      const state = formulaBarRenderState({
        bar: { ...NO_FORMULA_BAR_INPUTS, fieldText: '=A1', namedRanges: ['Sales'] },
        selection: selectSingleCell(3, 4),
        growing: false,
        draft: null,
        sheetName: 'Sheet1'
      });

      expect(state).to.eql({ address: 'C4', formula: '=A1', namedRanges: ['Sales'], highlights: null });
    });

    it('colours the references of a formula being written', () => {
      const state = formulaBarRenderState({
        bar: NO_FORMULA_BAR_INPUTS,
        selection: selectSingleCell(1, 1),
        growing: false,
        draft: { text: '=B2+C3:D4' },
        sheetName: 'Sheet1'
      });

      expect(state.highlights?.text).to.equal('=B2+C3:D4');
      expect(state.highlights?.references.map((ref) => [ref.start, ref.end])).to.eql([
        [1, 3],
        [4, 9]
      ]);
      expect(new Set(state.highlights?.references.map((ref) => ref.color)).size).to.equal(2);
    });
  });
});
