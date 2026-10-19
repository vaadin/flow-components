/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { toKey } from '../algorithms/CellKeys.js';
import type { LoadRange, ViewportLoader } from '../state/ViewportLoader.js';
import type { SpreadsheetEvent } from '../types/rpc-client-to-server.js';
import type { CellData } from '../types/wire.js';
import { coerceArray } from '../wire/Coerce.js';

/**
 * Sample the cell-style width measurement uses: ten digits, as earlier
 * versions measured, so the ratios keep the same meaning server-side.
 */
const WIDTH_PROBE_TEXT = '5555555555';

/** The style index a cell's class list names - "cs5 r cf2" is 5 - or null. */
function styleIndexOf(cellStyle: string | undefined): number | null {
  const match = /(?:^|\s)cs(\d+)(?:\s|$)/.exec(cellStyle ?? '');
  return match ? Number(match[1]) : null;
}

/** What keeping the server in step with the view needs of the sheet. */
export interface ServerSyncHost {
  /** How many rows and columns the sheet has: none before the server says. */
  size(): { readonly rows: number; readonly cols: number };
  /** The cell styles the server defined, by style index. */
  cellStyles(): Readonly<Record<string, string>>;
  /** The cells the client has, by cell key. */
  cells(): Map<string, CellData>;
  /** The cells the grid draws, its drawing buffer included; null before anything is measured. */
  drawnRange(): LoadRange | null;
  /** How wide a text is under each cell style, as the grid lays it out: 0 where nothing is laid out. */
  textWidths(cellStyles: readonly string[], text: string): number[];
  /** Settles once the grid has painted what it was last handed. */
  painted(): Promise<unknown>;
  /** Asks for the sheet to be drawn again. */
  scheduleRender(): void;
  /** Sends an event to the server. */
  send(event: SpreadsheetEvent): void;
}

/**
 * Keeps the server in step with what the client shows: tells it the sheet is
 * drawn and where, asks it for the cells scrolled to, measures how wide a
 * character is in each cell style, and keeps the cells it sends.
 */
export class ServerSync {
  /** Whether the server has been told the sheet is drawn and where it is. */
  private announced = false;
  /** Whether the cell styles have changed since their widths were last measured. */
  private restyledSinceMeasured = false;
  /** The width measurement waiting for the grid to paint, which the viewport goes after. */
  private pendingMeasurement: Promise<void> | null = null;
  /** The style indexes last reported to the server with a width ratio. */
  private measured: ReadonlySet<number> = new Set();

  constructor(
    private readonly sheet: ServerSyncHost,
    private readonly loader: Pick<ViewportLoader, 'check' | 'reset'>
  ) {}

  /**
   * Brings the server up to date with a drawing: measures the cell styles
   * again if they changed, then tells the server where the sheet is.
   *
   * The viewport goes after the ratios, as in earlier versions: the server
   * builds the cells it sends against the ratios, and builds the ones in view
   * again when new ratios arrive - before, the first screen of every workbook
   * went out twice. Both still go in one request.
   */
  drawn(): void {
    if (this.restyledSinceMeasured && this.pendingMeasurement === null) {
      // Measured once the grid has painted the rules that were just handed to
      // it, not against the ones it is replacing. Styles that change while it
      // waits are measured again, after another drawing: their rules may not
      // be painted yet.
      this.restyledSinceMeasured = false;
      this.pendingMeasurement = this.sheet.painted().then(() => {
        this.pendingMeasurement = null;
        const restyledWhileWaiting = this.restyledSinceMeasured;
        this.reportWidthRatios();
        if (restyledWhileWaiting) {
          this.sheet.scheduleRender();
        }
      });
    }
    if (this.pendingMeasurement !== null) {
      void this.pendingMeasurement.then(() => this.announceViewport());
      return;
    }
    this.announceViewport();
  }

  /**
   * Asks the server to load the cells, comments, overlays and components of
   * a range. Its `onSheetScroll` loads what it has not sent before.
   */
  load(range: LoadRange): void {
    this.sheet.send({
      type: 'onSheetScroll',
      data: {
        firstRow: range.r1,
        firstColumn: range.c1,
        lastRow: range.r2,
        lastColumn: range.c2
      }
    });
  }

  /**
   * Keeps the cells the server sent, by cell key. All four quadrants share
   * the one map; the cell grid picks out what each pane draws.
   */
  store(cellData: CellData[]): void {
    // Anything but a list of cells is none: iterating text would walk it
    // character by character, filing one entry under
    // "colundefined rowundefined" and dropping every value on the sheet.
    const cells = this.sheet.cells();
    for (const cell of coerceArray<CellData>(cellData)) {
      cells.set(toKey(cell.col, cell.row), cell);
      // A style no ratio has been measured for yet: measure again after the
      // next drawing.
      const index = styleIndexOf(cell.cellStyle);
      if (index !== null && !this.measured.has(index)) {
        this.restyledSinceMeasured = true;
      }
    }
    this.sheet.scheduleRender();
  }

  /**
   * The cells to have, in place of every cell there is: the server worked
   * them all out again and sent those shown, and any other kept here would
   * be out of date. Those are sent again when they are shown.
   */
  replace(cellData: CellData[]): void {
    this.sheet.cells().clear();
    this.store(cellData);
  }

  /**
   * The server sent new cell styles. It sizes General-format numbers against
   * how wide a digit is in each style, and only the client can measure that:
   * measured after the next drawing.
   */
  restyled(): void {
    this.restyledSinceMeasured = true;
  }

  /**
   * A workbook was loaded, the same one again included: every style is
   * measured and sent again. The server keeps the ratios with the workbook,
   * and the same styles bring no change of the style map that would say so.
   */
  workbookLoaded(): void {
    this.restyledSinceMeasured = true;
    this.sheet.scheduleRender();
  }

  /**
   * The server threw away what it knew of the sheet - a sheet switch, a new
   * file, a reset - and what it was told of where the sheet is with it. The
   * next drawing introduces the sheet again, and loads again what is shown,
   * even the same range.
   */
  reset(): void {
    this.announced = false;
    this.loader.reset();
  }

  /**
   * Complete the startup handshake, once the viewport is known.
   *
   * The server only loads overlays, hyperlinks, comments, tables, popup
   * buttons and custom editors from `loadCells`, and `loadCells` runs off
   * `onSheetScroll`. Nothing else calls it. Until the client reports a
   * viewport, none of that content exists — which looks like a sheet that
   * renders its cells and nothing else.
   *
   * `onConnectorInit` first, so the server marks the next scroll as a full
   * reload rather than diffing against viewport bounds it has never seen.
   *
   * The handshake runs once: {@link reset} clears the latch whenever the
   * server discards its sheet state, and the next completed viewport
   * introduces itself again.
   *
   * Loading itself is checked again on every drawing, not only on
   * scrolling: plenty of things resize the sheet without a scroll - a tab
   * revealing it, a split pane opening, the window resizing. A sheet that
   * came up 0 wide inside a hidden tab would otherwise keep the viewport it
   * measured while hidden, and the cells and popup buttons outside it would
   * never load. The {@link ViewportLoader} asks only for what is not loaded
   * yet.
   */
  private announceViewport(): void {
    const { rows, cols } = this.sheet.size();
    if (rows <= 0 || cols <= 0) {
      // The sheet's size has not arrived yet. Announcing now would report a
      // viewport that means nothing and the server would load nothing.
      return;
    }
    if (this.sheet.drawnRange() === null) {
      // Nothing measured yet; try again after the next drawing.
      return;
    }
    if (!this.announced) {
      this.announced = true;
      this.sheet.send({ type: 'onConnectorInit', data: {} });
    }
    this.loader.check();
  }

  /**
   * Tell the server how wide one character is in each cell style.
   *
   * A cell under the General format shows as many digits as fit and falls
   * back to scientific notation when they do not, and the server decides
   * that: it knows the column width in pixels but not the font, so it needs
   * a pixels-per-character ratio per style index. Without one it assumes
   * everything fits and no number is ever shortened.
   *
   * Measured as ten digits in a probe carrying the style's class, divided
   * by ten, as earlier versions measured it, so the server's existing
   * arithmetic keeps its meaning.
   */
  private reportWidthRatios(): void {
    // Every style a cell wears, not only those the style map holds: a style
    // set on a cell straight through POI never reaches the map, and without
    // a ratio the server judges every value in it as fitting. Unstyled, it
    // measures as the default does.
    const styleIndexes = [
      ...new Set([...Object.keys(this.sheet.cellStyles()).map(Number), ...this.styleIndexesInUse()])
    ]
      .filter((index) => Number.isFinite(index))
      .sort((a, b) => a - b);
    if (styleIndexes.length === 0) {
      return;
    }
    const ratios = this.sheet
      .textWidths(
        styleIndexes.map((index) => `cs${index}`),
        WIDTH_PROBE_TEXT
      )
      .map((width) => width / WIDTH_PROBE_TEXT.length);

    if (ratios.every((ratio) => ratio === 0)) {
      // Nothing was laid out — the sheet is hidden, or the styles have not
      // been applied yet. A zero ratio would tell the server every value
      // fits in no space at all, so say nothing and measure again later.
      this.restyledSinceMeasured = true;
      return;
    }

    this.measured = new Set(styleIndexes);
    this.sheet.send({
      type: 'setCellStyleWidthRatios',
      data: { styleIndexes, ratios }
    });
  }

  /** The style indexes the cells loaded so far wear, from their cs<N> class. */
  private styleIndexesInUse(): Set<number> {
    const indexes = new Set<number>();
    for (const cell of this.sheet.cells().values()) {
      const index = styleIndexOf(cell.cellStyle);
      if (index !== null) {
        indexes.add(index);
      }
    }
    return indexes;
  }
}
