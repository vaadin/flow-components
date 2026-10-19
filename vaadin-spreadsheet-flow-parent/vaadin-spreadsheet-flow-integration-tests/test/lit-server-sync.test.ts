/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { toKey } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/CellKeys.js';
import { ServerSync } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/ServerSync.js';
import type { LoadRange } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/ViewportLoader.js';
import type { SpreadsheetEvent } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/rpc-client-to-server.js';
import type { CellData } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/wire.js';

/**
 * Keeping the server in step with the view, on its own: the handshake, the
 * loads, the cells the server sends, and the widths of the cell styles.
 */
describe('ServerSync', () => {
  let sent: SpreadsheetEvent[];
  let loader: { checks: number; resets: number; check(): void; reset(): void };
  let size: { rows: number; cols: number };
  let styles: Record<string, string>;
  let cells: Map<string, CellData>;
  let drawnRange: LoadRange | null;
  let widths: Record<string, number>;
  let probeTexts: string[];
  let paints: number;
  let renders: number;
  let sync: ServerSync;

  const types = (): string[] => sent.map((event) => event.type);
  const ratioReports = (): SpreadsheetEvent[] => sent.filter((event) => event.type === 'setCellStyleWidthRatios');
  /** Lets the grid's paint, and what waits for it, run. */
  const painted = (): Promise<void> => new Promise((resolve) => setTimeout(resolve));

  function cell(col: number, row: number, cellStyle = 'cs0'): CellData {
    return { col, row, value: `${col}:${row}`, cellStyle };
  }

  beforeEach(() => {
    sent = [];
    loader = {
      checks: 0,
      resets: 0,
      check() {
        this.checks++;
      },
      reset() {
        this.resets++;
      }
    };
    size = { rows: 100, cols: 20 };
    styles = {};
    cells = new Map();
    drawnRange = { r1: 1, c1: 1, r2: 30, c2: 10 };
    widths = {};
    probeTexts = [];
    paints = 0;
    renders = 0;
    sync = new ServerSync(
      {
        size: () => size,
        cellStyles: () => styles,
        cells: () => cells,
        drawnRange: () => drawnRange,
        textWidths: (cellStyles, text) => {
          probeTexts.push(text);
          return cellStyles.map((cellStyle) => widths[cellStyle] ?? 0);
        },
        painted: () => {
          paints++;
          return Promise.resolve();
        },
        scheduleRender: () => renders++,
        send: (event) => sent.push(event)
      },
      loader
    );
  });

  describe('the handshake', () => {
    it('introduces the sheet once, when it has a size and is drawn, and has the loader check on every drawing', () => {
      sync.drawn();
      sync.drawn();

      expect(types()).to.eql(['onConnectorInit']);
      expect(loader.checks).to.equal(2);
    });

    it('says nothing before the sheet has a size', () => {
      size = { rows: 0, cols: 0 };

      sync.drawn();

      expect(sent).to.eql([]);
      expect(loader.checks).to.equal(0);
    });

    it('says nothing before anything is drawn', () => {
      drawnRange = null;

      sync.drawn();

      expect(sent).to.eql([]);
      expect(loader.checks).to.equal(0);
    });

    it('introduces the sheet again after a reset, which resets the loader too', () => {
      sync.drawn();

      sync.reset();
      sync.drawn();

      expect(types()).to.eql(['onConnectorInit', 'onConnectorInit']);
      expect(loader.resets).to.equal(1);
    });
  });

  it('asks the server for a range', () => {
    sync.load({ r1: 3, c1: 2, r2: 40, c2: 12 });

    expect(sent).to.eql([
      { type: 'onSheetScroll', data: { firstRow: 3, firstColumn: 2, lastRow: 40, lastColumn: 12 } }
    ]);
  });

  describe('the cells the server sends', () => {
    it('keeps them by cell key and has the sheet drawn', () => {
      sync.store([cell(2, 3), cell(4, 5)]);

      expect(cells.get(toKey(2, 3))?.value).to.equal('2:3');
      expect(cells.get(toKey(4, 5))?.value).to.equal('4:5');
      expect(renders).to.equal(1);
    });

    it('keeps nothing of what is not a list', () => {
      sync.store('[{"col":1,"row":1}]' as unknown as CellData[]);

      expect(cells.size).to.equal(0);
    });

    it('replaces every cell there is', () => {
      sync.store([cell(1, 1)]);

      sync.replace([cell(2, 2)]);

      expect([...cells.keys()]).to.eql([toKey(2, 2)]);
    });
  });

  describe('the widths of the cell styles', () => {
    it('measures every style the server defined, as ten digits, before introducing the sheet', async () => {
      styles = { '0': 'font-size:11pt', '3': 'font-size:14pt' };
      widths = { cs0: 70, cs3: 90 };

      sync.restyled();
      sync.drawn();
      expect(sent, 'before the grid has painted').to.eql([]);
      await painted();

      expect(types()).to.eql(['setCellStyleWidthRatios', 'onConnectorInit']);
      expect(sent[0]).to.eql({ type: 'setCellStyleWidthRatios', data: { styleIndexes: [0, 3], ratios: [7, 9] } });
      expect(probeTexts).to.eql(['5555555555']);
    });

    it('also measures a style a cell wears that the server defined no rule for', async () => {
      styles = { '0': '' };
      widths = { cs0: 70, cs7: 80 };
      sync.store([cell(1, 1, 'cs7 r')]);

      sync.drawn();
      await painted();

      expect(ratioReports()).to.eql([
        { type: 'setCellStyleWidthRatios', data: { styleIndexes: [0, 7], ratios: [7, 8] } }
      ]);
    });

    it('says nothing of widths when there is no style, and still introduces the sheet', async () => {
      sync.restyled();
      sync.drawn();
      await painted();

      expect(types()).to.eql(['onConnectorInit']);
    });

    it('measures again later when nothing was laid out', async () => {
      styles = { '0': '' };
      sync.restyled();
      sync.drawn();
      await painted();
      expect(ratioReports(), 'nothing laid out').to.eql([]);

      widths = { cs0: 70 };
      sync.drawn();
      await painted();

      expect(ratioReports()).to.have.length(1);
    });

    it('measures once for drawings that come before the grid has painted', async () => {
      styles = { '0': '' };
      widths = { cs0: 70 };

      sync.restyled();
      sync.drawn();
      sync.drawn();
      sync.drawn();
      await painted();

      expect(paints).to.equal(1);
      expect(ratioReports()).to.have.length(1);
    });

    it('does not measure again for a drawing that changed no style', async () => {
      styles = { '0': '' };
      widths = { cs0: 70 };
      sync.restyled();
      sync.drawn();
      await painted();
      sent.length = 0;

      sync.drawn();
      await painted();

      expect(sent).to.eql([]);
      expect(paints).to.equal(1);
    });

    it('measures again for a cell that brings a style not measured yet, and not for one measured', async () => {
      styles = { '0': '', '3': '' };
      widths = { cs0: 70, cs3: 90, cs5: 100 };
      sync.restyled();
      sync.drawn();
      await painted();
      sent.length = 0;

      sync.store([cell(1, 1, 'cs3')]);
      sync.drawn();
      await painted();
      expect(ratioReports(), 'a style measured').to.eql([]);

      sync.store([cell(2, 2, 'cs5')]);
      sync.drawn();
      await painted();
      expect(ratioReports()).to.eql([
        { type: 'setCellStyleWidthRatios', data: { styleIndexes: [0, 3, 5], ratios: [7, 9, 10] } }
      ]);
    });

    it('measures every style again for a workbook loaded anew, its styles unchanged, and has it drawn', async () => {
      styles = { '0': '' };
      widths = { cs0: 70 };
      sync.restyled();
      sync.drawn();
      await painted();
      sent.length = 0;

      sync.workbookLoaded();
      sync.drawn();
      await painted();

      expect(renders).to.equal(1);
      expect(ratioReports()).to.have.length(1);
    });

    it('measures again, after another drawing, for a workbook loaded while a measurement waited for the paint', async () => {
      styles = { '0': '' };
      widths = { cs0: 70 };
      sync.restyled();
      sync.drawn();
      sync.workbookLoaded();
      // The drawing the load asked for, before the grid has painted
      sync.drawn();
      await painted();
      expect(ratioReports(), 'measured once while waiting').to.have.length(1);
      expect(renders, 'the load, and another drawing to measure again').to.equal(2);

      sync.drawn();
      await painted();

      expect(ratioReports()).to.have.length(2);
    });

    it('measures again for styles the server sent while a measurement waited for the paint', async () => {
      styles = { '0': '' };
      widths = { cs0: 70, cs1: 80 };
      sync.restyled();
      sync.drawn();
      styles = { '0': '', '1': '' };
      sync.restyled();
      await painted();

      sync.drawn();
      await painted();

      expect(ratioReports()).to.have.length(2);
      expect(ratioReports()[1].data).to.eql({ styleIndexes: [0, 1], ratios: [7, 8] });
    });

    it('asks for no drawing after a measurement that found nothing laid out', async () => {
      styles = { '0': '' };
      sync.restyled();
      sync.drawn();
      await painted();

      expect(ratioReports()).to.eql([]);
      expect(renders, 'measured again at the next drawing, not in a loop').to.equal(0);
    });
  });
});
