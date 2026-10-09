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
  homeEndTarget,
  keyDirection
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

/**
 * Which way a key moves the selection. Where the step lands - over hidden
 * rows, onto merges, never off the sheet - is moveActiveCell's job, tested
 * in lit-keyboard-moves.
 */
describe('keyDirection', () => {
  it('moves down on Enter and up with shift', () => {
    expect(keyDirection('Enter', false)).to.equal('down');
    expect(keyDirection('Enter', true)).to.equal('up');
  });

  it('moves right on Tab and left with shift', () => {
    expect(keyDirection('Tab', false)).to.equal('right');
    expect(keyDirection('Tab', true)).to.equal('left');
  });

  it('follows an arrow key, which shift does not reverse', () => {
    expect(keyDirection('ArrowUp', false)).to.equal('up');
    expect(keyDirection('ArrowUp', true)).to.equal('up');
    expect(keyDirection('ArrowDown', false)).to.equal('down');
    expect(keyDirection('ArrowLeft', false)).to.equal('left');
    expect(keyDirection('ArrowRight', true)).to.equal('right');
  });

  it('gives no direction for any other key', () => {
    expect(keyDirection('a', false)).to.be.null;
    expect(keyDirection('Home', false)).to.be.null;
    expect(keyDirection('Escape', false)).to.be.null;
    expect(keyDirection(' ', false)).to.be.null;
  });
});

describe('homeEndTarget', () => {
  const activeCell = { col: 5, row: 5 };

  it('takes Home to the first column of the row', () => {
    expect(homeEndTarget('Home', false, activeCell, 10, 20)).to.deep.equal({ col: 1, row: 5 });
  });

  it('takes End to the last column of the row', () => {
    expect(homeEndTarget('End', false, activeCell, 10, 20)).to.deep.equal({ col: 10, row: 5 });
  });

  it('takes Home to A1 and End to the last cell with Ctrl or Cmd', () => {
    expect(homeEndTarget('Home', true, activeCell, 10, 20)).to.deep.equal({ col: 1, row: 1 });
    expect(homeEndTarget('End', true, activeCell, 10, 20)).to.deep.equal({ col: 10, row: 20 });
  });

  it('stays on the sheet when it has no rows or columns yet', () => {
    expect(homeEndTarget('End', true, activeCell, 0, 0)).to.deep.equal({ col: 1, row: 1 });
  });

  it('ignores every other key', () => {
    expect(homeEndTarget('ArrowDown', false, activeCell, 10, 20)).to.be.null;
    expect(homeEndTarget('PageDown', true, activeCell, 10, 20)).to.be.null;
    expect(homeEndTarget('a', false, activeCell, 10, 20)).to.be.null;
  });
});
