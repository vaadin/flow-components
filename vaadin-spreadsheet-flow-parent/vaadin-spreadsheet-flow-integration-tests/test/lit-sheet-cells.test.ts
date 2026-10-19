/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { SheetCells } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetCells.js';
import { SheetData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetData.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * The cells of a sheet, counting the changes to each row: a row's version
 * stays as it is until a cell of the row changes, whatever happens in
 * other rows.
 */
describe('SheetCells', () => {
  let cells: SheetCells;

  const cell = (col: number, row: number, value = 'x'): CellData => ({ col, row, value, cellStyle: 'cs0' });
  const put = (col: number, row: number, value = 'x'): void => {
    cells.set(`col${col} row${row}`, cell(col, row, value));
  };

  beforeEach(() => {
    cells = new SheetCells();
  });

  it('is a map of the cells by key, which a sheet starts out with empty', () => {
    put(2, 3, 'v');

    expect(cells.get('col2 row3')?.value).to.equal('v');
    expect(cells.size).to.equal(1);
    expect(cells.set('col1 row1', cell(1, 1)), 'set gives the map back, as a Map does').to.equal(cells);
    expect(new SheetData().cells).to.be.an.instanceOf(SheetCells).and.have.property('size', 0);
  });

  it('gives a row a new version when a cell of it is set, and leaves the other rows theirs', () => {
    put(1, 1);
    put(1, 2);
    const [first, second] = [cells.rowVersion(1), cells.rowVersion(2)];

    put(5, 2, 'again');

    expect(cells.rowVersion(1)).to.equal(first);
    expect(cells.rowVersion(2)).to.not.equal(second);
  });

  it('gives a row a new version each time, even when the same cell is set to the same value', () => {
    put(1, 1);
    const versions = new Set([cells.rowVersion(1)]);

    put(1, 1);
    versions.add(cells.rowVersion(1));
    put(1, 1);
    versions.add(cells.rowVersion(1));

    expect(versions.size).to.equal(3);
  });

  it('gives a row a new version when a cell of it is deleted, and none for a cell that is not there', () => {
    put(1, 4);
    const before = cells.rowVersion(4);

    expect(cells.delete('col2 row4')).to.be.false;
    expect(cells.rowVersion(4), 'nothing deleted').to.equal(before);
    expect(cells.delete('col1 row4')).to.be.true;
    expect(cells.rowVersion(4)).to.not.equal(before);
    expect(cells.has('col1 row4')).to.be.false;
  });

  it('gives every row a new version when cleared, rows no cell was ever set in too, and one again to a row set after', () => {
    put(1, 1);
    const set = cells.rowVersion(1);
    const never = cells.rowVersion(9);

    cells.clear();

    expect(cells.size).to.equal(0);
    const cleared = cells.rowVersion(1);
    expect(cleared).to.not.equal(set);
    expect(cells.rowVersion(9)).to.not.equal(never);
    expect(cells.rowVersion(9)).to.equal(cleared);
    put(1, 9);
    expect(cells.rowVersion(9)).to.not.equal(cleared);
    expect(cells.rowVersion(1), 'a row set in only before the clearing').to.equal(cleared);
  });

  it('reads the row of a key whatever the cell set under it says', () => {
    const before = cells.rowVersion(7);

    cells.set('col1 row7', cell(1, 1));

    expect(cells.rowVersion(7)).to.not.equal(before);
  });
});
