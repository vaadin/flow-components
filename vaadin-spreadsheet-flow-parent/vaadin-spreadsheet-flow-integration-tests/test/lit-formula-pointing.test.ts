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
  canPoint,
  formulaReferences,
  isFormula,
  placeReference,
  REFERENCE_COLORS,
  referenceAt,
  referenceText
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/FormulaPointing.js';

/**
 * Writing a formula by pointing at cells - the GWT client's point mode and
 * reference colouring - as pure rules on the text and the caret.
 */
describe('FormulaPointing — what is a formula', () => {
  it('takes a value starting with = or + as a formula, as the GWT client did', () => {
    expect(isFormula('=A1')).to.be.true;
    expect(isFormula('+A1')).to.be.true;
    expect(isFormula('A1')).to.be.false;
    expect(isFormula('')).to.be.false;
  });
});

describe('FormulaPointing — the references in a formula', () => {
  const refs = (text: string, sheet = 'Sheet1') =>
    formulaReferences(text, sheet).map((r) => ({ text: r.text, start: r.start, end: r.end, range: r.range }));

  it('finds cells and ranges with where they are in the text', () => {
    expect(refs('=A1+SUM(B2:C3)')).to.eql([
      { text: 'A1', start: 1, end: 3, range: { c1: 1, c2: 1, r1: 1, r2: 1 } },
      { text: 'B2:C3', start: 8, end: 13, range: { c1: 2, c2: 3, r1: 2, r2: 3 } }
    ]);
  });

  it('takes absolute references, which the GWT client missed', () => {
    expect(refs('=$A$1+A$2+$B3').map((r) => r.text)).to.eql(['$A$1', 'A$2', '$B3']);
  });

  it('turns a range written backwards the right way round', () => {
    expect(refs('=C3:A1')[0].range).to.eql({ c1: 1, c2: 3, r1: 1, r2: 3 });
  });

  it('takes references to the sheet shown, and leaves those to other sheets', () => {
    expect(refs('=Sheet1!A1+Other!B2+sheet1!C3').map((r) => r.text)).to.eql(['Sheet1!A1', 'sheet1!C3']);
    expect(refs("='My Sheet'!A1+Sheet1!B2", 'My Sheet').map((r) => r.text)).to.eql(["'My Sheet'!A1"]);
  });

  it('leaves text in quotes alone', () => {
    expect(refs('="A1"&B2').map((r) => r.text)).to.eql(['B2']);
  });

  it('leaves function names and longer names alone, which the GWT client coloured', () => {
    expect(refs('=LOG10(A1)+ATAN2(B1,C1)+TAX2024A').map((r) => r.text)).to.eql(['A1', 'B1', 'C1']);
  });

  it('leaves an address past the last column or row alone', () => {
    expect(refs('=XFE1+A1048577+XFD1048576').map((r) => r.text)).to.eql(['XFD1048576']);
  });

  it('finds nothing in a value that is not a formula', () => {
    expect(refs('A1+B2')).to.eql([]);
  });

  it('gives the references their colours in the order they come, the same cells the same colour', () => {
    const colors = formulaReferences('=A1+B2+A1+$B$2', 'Sheet1').map((r) => r.color);
    expect(colors).to.eql([REFERENCE_COLORS[0], REFERENCE_COLORS[1], REFERENCE_COLORS[0], REFERENCE_COLORS[1]]);
  });

  it('goes round the sixteen colours', () => {
    const text = '=' + Array.from({ length: 17 }, (_, i) => `A${i + 1}`).join('+');
    const colors = formulaReferences(text, 'Sheet1').map((r) => r.color);
    expect(colors[16]).to.equal(REFERENCE_COLORS[0]);
    expect(new Set(colors.slice(0, 16)).size).to.equal(16);
  });
});

describe('FormulaPointing — when the arrows point', () => {
  it('points after an operator, a bracket or a separator', () => {
    for (const text of ['=', '=A1+', '=A1-', '=A1*', '=A1/', '=SUM(', '=SUM(A1,', '=SUM(A1:', '=A1&', '=A1>', '+']) {
      expect(canPoint(text, text.length), text).to.be.true;
    }
  });

  it('points after spaces that follow one', () => {
    expect(canPoint('=A1 + ', 6)).to.be.true;
  });

  it('does not point after a reference, a number or a name', () => {
    for (const text of ['=A1', '=1', '=SUM', '=SUM(A1)']) {
      expect(canPoint(text, text.length), text).to.be.false;
    }
  });

  it('does not point in a value that is not a formula', () => {
    expect(canPoint('A+', 2)).to.be.false;
  });

  it('goes by what is before the caret', () => {
    expect(canPoint('=SUM(A1)', 5)).to.be.true;
    expect(canPoint('=SUM(A1)', 7)).to.be.false;
  });
});

describe('FormulaPointing — the reference at the caret', () => {
  const refs = formulaReferences('=A1+B22', 'Sheet1');

  it('is the one the caret is in or right after', () => {
    expect(referenceAt(refs, 2)!.text).to.equal('A1');
    expect(referenceAt(refs, 3)!.text).to.equal('A1');
    expect(referenceAt(refs, 7)!.text).to.equal('B22');
  });

  it('is none elsewhere', () => {
    expect(referenceAt(refs, 0)).to.be.null;
  });
});

describe('FormulaPointing — putting a reference in', () => {
  it('writes a cell as A1 and a range from its top left', () => {
    expect(referenceText({ col: 3, row: 4 }, { col: 3, row: 4 })).to.equal('C4');
    expect(referenceText({ col: 3, row: 6 }, { col: 1, row: 4 })).to.equal('A4:C6');
  });

  it('inserts at the caret', () => {
    expect(placeReference('=SUM()', 5, null, 'B2')).to.eql({ text: '=SUM(B2)', start: 5, end: 7 });
  });

  it('replaces the span given', () => {
    expect(placeReference('=A1+C9', 6, { start: 4, end: 6 }, 'D10:E11')).to.eql({ text: '=A1+D10:E11', start: 4, end: 11 });
  });
});
