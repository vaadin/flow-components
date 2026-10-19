/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { ValueMeasurer } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/cell-grid/ValueMeasurer.js';
import {
  type CellGridRenderState,
  emptyCellGridRenderState
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/elements/types.js';
import { SheetCells } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetCells.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/** Stands in for the cell grid: measures in its shadow root, in a cell font of its own. */
class MeasuringHost extends HTMLElement {
  renderState: CellGridRenderState = emptyCellGridRenderState();
  theme: string | null = null;
  tooNarrowCells: ReadonlySet<string> = new Set();
  fittingSpills: ReadonlySet<string> = new Set();
  readonly measurer = new ValueMeasurer(this);

  constructor() {
    super();
    const fonts = new CSSStyleSheet();
    fonts.replaceSync('.cell { font: 10px monospace; padding: 0 2px; } .cs1 { font-size: 20px; }');
    this.attachShadow({ mode: 'open' }).adoptedStyleSheets = [fonts];
  }
}
customElements.define('test-value-measuring-host', MeasuringHost);

/**
 * Measuring values as the cells draw them, on its own: judging the cells the
 * server flagged, measuring each value once, and when the judging is done.
 */
describe('ValueMeasurer', () => {
  let host: MeasuringHost;
  /** How many probes went into the shadow root, by the layout they were added in. */
  let added: number[];
  let observer: MutationObserver;
  // The style tables a value is measured in, the same objects from one state to the next.
  const STYLES = { 0: 'color: black' };
  const NO_FORMATS = {};

  function cell(col: number, row: number, value: string, extra: Partial<CellData> = {}): CellData {
    return { col, row, value, cellStyle: 'cs0', locked: false, needsMeasure: false, isPercentage: false, ...extra };
  }

  /** Columns 1 to 3, as wide as given, and rows 1 and 2 of 20 pixels. */
  function sheet(cells: CellData[], colWidths: number[], overrides: Partial<CellGridRenderState> = {}): CellGridRenderState {
    const empty = emptyCellGridRenderState();
    return {
      ...empty,
      viewport: { ...empty.viewport, firstColumnIndex: 1, lastColumnIndex: 3, firstRowIndex: 1, lastRowIndex: 2 },
      colX: [0, colWidths[0], colWidths[0] + colWidths[1]],
      colWidths,
      rowY: [0, 20],
      rowHeights: [20, 20],
      cellData: new Map(cells.map((c) => [`col${c.col} row${c.row}`, c])),
      cellStyleToCSSStyle: STYLES,
      conditionalFormattingStyles: NO_FORMATS,
      ...overrides
    };
  }

  /** The probes added since asked last, by the layout they were added in. */
  function probesAdded(): number[] {
    for (const record of observer.takeRecords()) {
      if (record.addedNodes.length > 0) {
        added.push(record.addedNodes.length);
      }
    }
    const out = added;
    added = [];
    return out;
  }

  /** How wide a text is in the plain cell style. */
  const widthOf = (text: string): number => host.measurer.textWidths(['cs0'], text)[0];

  /** One update in which the state changed. */
  function update(): void {
    host.measurer.willUpdate(true);
    host.measurer.updated(true);
  }

  beforeEach(() => {
    host = new MeasuringHost();
    document.body.append(host);
    added = [];
    observer = new MutationObserver((records) => {
      for (const record of records) {
        if (record.addedNodes.length > 0) {
          added.push(record.addedNodes.length);
        }
      }
    });
    observer.observe(host.shadowRoot!, { childList: true });
  });

  afterEach(() => {
    observer.disconnect();
    host.remove();
  });

  describe('judging the flagged cells', () => {
    it('finds a flagged value wider than its column too narrow, and one as wide as its column not', () => {
      const w = widthOf('12345678');
      host.renderState = sheet(
        [cell(1, 1, '12345678', { needsMeasure: true }), cell(2, 1, '12345678', { needsMeasure: true })],
        [w - 1, w, 50]
      );

      update();

      expect([...host.tooNarrowCells]).to.eql(['col1 row1']);
    });

    it('finds a run-on value that fits its column after all, and leaves one wider running on', () => {
      const w = widthOf('12345678');
      host.renderState = sheet(
        [cell(1, 1, '12345678', { overflowWidth: 200 }), cell(2, 1, '12345678', { overflowWidth: 200 })],
        [w - 1, w, 50]
      );

      update();

      expect([...host.fittingSpills]).to.eql(['col2 row1']);
    });

    it('drops a drawn cell no longer flagged, and leaves a cell out of view as it was', () => {
      host.tooNarrowCells = new Set(['col1 row1', 'col9 row9']);
      host.fittingSpills = new Set(['col2 row1']);
      host.renderState = sheet([cell(1, 1, 'x'), cell(2, 1, 'y')], [50, 50, 50]);

      update();

      expect([...host.tooNarrowCells]).to.eql(['col9 row9']);
      expect([...host.fittingSpills]).to.eql([]);
    });

    it('finds nothing for a flagged cell in a column not drawn', () => {
      host.renderState = sheet([cell(1, 1, '12345678', { needsMeasure: true })], [0, 50, 50]);

      update();

      expect(host.tooNarrowCells.size).to.equal(0);
    });

    it('keeps the same sets when nothing changes, so the grid draws once', () => {
      host.renderState = sheet([cell(1, 1, '12345678', { needsMeasure: true })], [10, 50, 50]);
      update();
      const { tooNarrowCells, fittingSpills } = host;

      host.renderState = { ...host.renderState };
      update();

      expect(host.tooNarrowCells).to.equal(tooNarrowCells);
      expect(host.fittingSpills).to.equal(fittingSpills);
    });
  });

  describe('judging only the rows that changed', () => {
    /** The rows whose cells were looked up in the last judging: the rows judged. */
    let looked: Set<number>;
    let cells: SheetCells;

    /** A value too wide for the first column in rows 1 and 2, in a map that counts its changes. */
    function sheetCells(): SheetCells {
      const counted = new SheetCells();
      const get = counted.get.bind(counted);
      counted.get = (key: string) => {
        looked.add(Number(key.split('row')[1]));
        return get(key);
      };
      for (const c of [cell(1, 1, '12345678', { needsMeasure: true }), cell(1, 2, '12345678', { needsMeasure: true })]) {
        counted.set(`col${c.col} row${c.row}`, c);
      }
      return counted;
    }

    /** Judges the drawn cells, and gives the rows looked at, in order. */
    function judgedRows(): number[] {
      looked = new Set();
      update();
      return [...looked].sort((a, b) => a - b);
    }

    beforeEach(() => {
      looked = new Set();
      cells = sheetCells();
      host.renderState = sheet([], [10, 50, 50], { cellData: cells });
      expect(judgedRows(), 'the first judging').to.eql([1, 2]);
      expect([...host.tooNarrowCells]).to.eql(['col1 row1', 'col1 row2']);
    });

    it('judges no row again that nothing changed in', () => {
      const { tooNarrowCells } = host;
      host.renderState = { ...host.renderState };

      expect(judgedRows()).to.eql([]);
      expect(host.tooNarrowCells).to.equal(tooNarrowCells);
    });

    it('judges no row of a frozen pane again either, the same rows as the scrolling pane draws', () => {
      host.renderState = sheet([], [10, 50, 50], {
        cellData: cells,
        viewport: { ...host.renderState.viewport, firstColumnIndex: 2 },
        horizontalSplitPosition: 1,
        frozenColX: [0],
        frozenColWidths: [10]
      });
      expect(judgedRows(), 'the frozen column, and the columns after it').to.eql([1, 2]);

      host.renderState = { ...host.renderState };
      expect(judgedRows()).to.eql([]);
    });

    it('judges again only the row a cell was set in, and finds what it holds now', () => {
      cells.set('col1 row2', cell(1, 2, '1', { needsMeasure: true }));
      host.renderState = { ...host.renderState };

      expect(judgedRows()).to.eql([2]);
      expect([...host.tooNarrowCells]).to.eql(['col1 row1']);
    });

    it('judges every row again when a column is made wider, or the styles change', () => {
      host.renderState = sheet([], [200, 50, 50], { cellData: cells });
      expect(judgedRows()).to.eql([1, 2]);
      expect(host.tooNarrowCells.size, 'both fit now').to.equal(0);

      host.renderState = sheet([], [10, 50, 50], { cellData: cells });
      judgedRows();
      host.renderState = sheet([], [10, 50, 50], { cellData: cells, cellStyleToCSSStyle: { 0: 'color: red' } });
      expect(judgedRows(), 'new styles').to.eql([1, 2]);
    });

    it('judges every row again once either set was put in place of the one it left', () => {
      host.tooNarrowCells = new Set();
      expect(judgedRows()).to.eql([1, 2]);
      expect([...host.tooNarrowCells]).to.eql(['col1 row1', 'col1 row2']);

      host.fittingSpills = new Set(['col1 row1']);
      expect(judgedRows()).to.eql([1, 2]);
      expect(host.fittingSpills.size, 'not a run-on cell').to.equal(0);
    });

    it('judges every row again for another map, and every time for a map that does not count its changes', () => {
      host.renderState = { ...host.renderState, cellData: sheetCells() };
      expect(judgedRows()).to.eql([1, 2]);

      const plain = new Map([...cells]);
      const get = plain.get.bind(plain);
      plain.get = (key: string) => {
        looked.add(Number(key.split('row')[1]));
        return get(key);
      };
      host.renderState = { ...host.renderState, cellData: plain };
      expect(judgedRows()).to.eql([1, 2]);
      host.renderState = { ...host.renderState };
      expect(judgedRows()).to.eql([1, 2]);
    });

    it('takes a row scrolled out of view and back as it was judged, without looking at it again', () => {
      cells.set('col1 row3', cell(1, 3, '12345678', { needsMeasure: true }));
      const rows = (first: number): Partial<CellGridRenderState> => ({
        cellData: cells,
        viewport: { ...host.renderState.viewport, firstRowIndex: first, lastRowIndex: first + 1 },
        rowY: [20 * (first - 1), 20 * first]
      });

      host.renderState = sheet([], [10, 50, 50], rows(2));
      expect(judgedRows()).to.eql([3]);
      host.renderState = sheet([], [10, 50, 50], rows(1));
      expect(judgedRows()).to.eql([]);
      expect([...host.tooNarrowCells]).to.eql(['col1 row1', 'col1 row2', 'col1 row3']);
    });
  });

  describe('measuring', () => {
    it('measures each value once, all in one layout, and a value alike in another cell not again', () => {
      const flagged = [
        cell(1, 1, 'alike', { needsMeasure: true }),
        cell(2, 1, 'alike', { needsMeasure: true }),
        cell(3, 1, 'other', { overflowWidth: 99 })
      ];
      host.renderState = sheet(flagged, [10, 10, 10]);

      update();
      expect(probesAdded(), 'one layout, a probe for each value').to.eql([2]);

      update();
      expect(probesAdded(), 'the same cells').to.eql([]);
      host.renderState = sheet([cell(1, 1, 'alike', { needsMeasure: true })], [10, 10, 10]);
      update();
      expect(probesAdded(), 'the same value, in another cell').to.eql([]);
    });

    it('measures again once the style tables or the theme change', () => {
      const flagged = [cell(1, 1, 'value', { needsMeasure: true })];
      host.renderState = sheet(flagged, [10, 10, 10]);
      update();
      expect(probesAdded()).to.eql([1]);

      host.renderState = sheet(flagged, [10, 10, 10], { cellStyleToCSSStyle: { 0: 'color: red' } });
      update();
      expect(probesAdded(), 'new styles').to.eql([1]);
      host.theme = 'lumo';
      host.renderState = { ...host.renderState };
      update();
      expect(probesAdded(), 'another theme').to.eql([1]);
    });

    it('measures one value in one probe, kept for the next', () => {
      const short = host.measurer.measureValue('cs0', '12');
      const long = host.measurer.measureValue('cs0', '1234');
      const large = host.measurer.measureValue('cs1', '1234');

      expect(probesAdded()).to.eql([1]);
      expect(short < long && long < large, `${short} < ${long} < ${large}`).to.be.true;
    });

    it('measures a text in several styles in one layout, leaving nothing behind, and as nothing with nowhere to lay out', () => {
      const [plain, large] = host.measurer.textWidths(['cs0', 'cs1'], '5555');

      expect(probesAdded()).to.eql([2]);
      expect(host.shadowRoot!.querySelectorAll('span')).to.have.length(0);
      expect(large > plain && plain > 0).to.be.true;
      const unplaced = new ValueMeasurer({
        renderState: emptyCellGridRenderState(),
        theme: null,
        shadowRoot: null,
        tooNarrowCells: new Set(),
        fittingSpills: new Set()
      });
      expect(unplaced.textWidths(['cs0', 'cs1'], '5555')).to.eql([0, 0]);
    });
  });

  describe('when it judges', () => {
    const SENTINEL: ReadonlySet<string> = new Set(['untouched']);

    it('after drawing, the first time, before the styles are in the page', () => {
      host.renderState = sheet([cell(1, 1, '12345678', { needsMeasure: true })], [10, 50, 50]);

      host.measurer.willUpdate(true);
      expect(host.tooNarrowCells.size, 'not before drawing').to.equal(0);
      host.measurer.updated(true);

      expect([...host.tooNarrowCells]).to.eql(['col1 row1']);
    });

    it('before drawing, and not again after, while the styles drawn are the ones values are measured in', () => {
      host.renderState = sheet([], [10, 50, 50]);
      update();

      host.renderState = sheet([cell(2, 1, '12345678', { needsMeasure: true })], [10, 10, 50]);
      host.measurer.willUpdate(true);
      expect([...host.tooNarrowCells], 'before drawing').to.eql(['col2 row1']);
      host.tooNarrowCells = SENTINEL;
      host.measurer.updated(true);

      expect(host.tooNarrowCells, 'not again').to.equal(SENTINEL);
    });

    it('after drawing new styles', () => {
      host.renderState = sheet([], [10, 50, 50]);
      update();
      host.tooNarrowCells = SENTINEL;

      host.renderState = sheet([cell(2, 1, '12345678', { needsMeasure: true })], [10, 10, 50], {
        cellStyleToCSSStyle: { 0: 'color: red' }
      });
      host.measurer.willUpdate(true);
      expect(host.tooNarrowCells, 'not before drawing').to.equal(SENTINEL);
      host.measurer.updated(true);

      expect([...host.tooNarrowCells]).to.eql(['untouched', 'col2 row1']);
    });

    it('not at all in an update that changed no state', () => {
      host.renderState = sheet([cell(1, 1, '12345678', { needsMeasure: true })], [10, 50, 50]);

      host.measurer.willUpdate(false);
      host.measurer.updated(false);
      host.measurer.willUpdate(false);

      expect(host.tooNarrowCells.size).to.equal(0);
    });
  });
});
