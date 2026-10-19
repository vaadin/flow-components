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
import {
  type CellRect,
  computePaintRectangle,
  grownSelection,
  isNonEmpty,
  type PaintOutcome
} from '../algorithms/SelectionPaint.js';
import { addAuxRange, normalizedRange, selectSingleCell } from '../state/SelectionState.js';
import type { SheetStore } from '../state/SheetStore.js';
import type { SpreadsheetEvent } from '../types/rpc-client-to-server.js';
import type { FormulaPointer } from './FormulaPointer.js';
import type { SelectionReporter } from './SelectionReporter.js';

/** A fill drag that paints nothing. */
const NO_PAINT: PaintOutcome = { kind: 'none' };

/** A cell, by its column and row, 1-based. */
interface Cell {
  col: number;
  row: number;
}

/** The keys held with a press or a click. */
interface Keys {
  shiftKey: boolean;
  ctrlKey: boolean;
  metaKey: boolean;
}

/** What selecting with the pointer needs of the sheet. */
export interface PointerSelectionHost {
  /** How far the sheet is scrolled down, in pixels. */
  scrollTop(): number;
  /** How far the sheet is scrolled right, in pixels. */
  scrollLeft(): number;
  /** Ends a header drag on the release; whether one was under way. */
  finishHeaderResize(): boolean;
  /** The cell whose custom editor an event came out of, if any. */
  cellOfEditor(e: Event): Cell | null;
  /** Tells the server. */
  send(event: SpreadsheetEvent): void;
}

/**
 * Selecting with the pointer: a click on a cell, a press dragged out into a
 * range, Shift and Ctrl with them, a click on a column letter or a row
 * number, the fill handle dragged, and a press on a custom editor. A press
 * while a formula is written points at the cell instead; see
 * {@link FormulaPointer}.
 */
export class PointerSelection {
  constructor(
    private readonly store: SheetStore,
    private readonly report: SelectionReporter,
    private readonly pointing: FormulaPointer,
    private readonly host: PointerSelectionHost
  ) {}

  /**
   * Cell-click handler. Single click = move active cell + collapse
   * selection to that cell. Shift-click = extend the active range
   * to include the new anchor while keeping the existing active
   * cell as the range origin (Excel convention). Ctrl/Meta click
   * adds the cell to the selection as a range of its own, and
   * Ctrl/Meta+Shift extends the active range keeping the others.
   */
  click(detail: Cell & Keys): void {
    // The range the release of its press reported is not reported again.
    const reportedOnRelease = this.store.interaction.rangeReportedOnRelease;
    this.store.markRangeReportedOnRelease(false);
    if (this.pointing.clicked()) {
      // The press pointed at the cell for a formula; it selects nothing.
      return;
    }
    const { col, row, shiftKey, ctrlKey, metaKey } = detail;
    const { selection } = this.store;
    const ctrlOrMeta = ctrlKey || metaKey;
    if (ctrlOrMeta && shiftKey) {
      // Extend the active range, keeping the ranges already picked
      // out with earlier Ctrl-clicks.
      const anchor = selection.activeCell;
      const activeRange = this.store.rangeTo(anchor, col, row);
      this.store.select({ activeCell: anchor, activeRange, auxRanges: selection.auxRanges });
      this.report.rangeAdded(activeRange);
      return;
    }
    if (ctrlOrMeta) {
      // Non-coherent selection: the clicked cell joins the selection
      // as its own range and becomes active; the previous active
      // range is kept as an aux range.
      this.store.select(addAuxRange(selection, col, row, col, row));
      this.report.cellAdded(col, row);
      return;
    }
    if (shiftKey) {
      // Extend the active range from the existing active cell
      // (= the anchor) to the clicked coordinate. Ranges picked out
      // with earlier Ctrl-clicks stay: shift extends the last range,
      // it does not start the selection over.
      const anchor = selection.activeCell;
      this.store.select({
        activeCell: anchor,
        activeRange: this.store.rangeTo(anchor, col, row),
        auxRanges: selection.auxRanges
      });
      if (!reportedOnRelease) {
        this.report.range();
      }
      return;
    }
    this.store.selectCellOrMerge(col, row);
    this.report.selection();
    // Hyperlink cells additionally dispatch `linkCellClicked`
    // after the normal selection event. The
    // server's onLinkCellClicked handler opens the URL.
    if (this.store.model.hyperlinksTooltips[toKey(col, row)] !== undefined) {
      this.host.send({ type: 'linkCellClicked', data: { row, column: col } });
    }
  }

  /**
   * Mouse drag — mousedown begin. Starts a drag.
   * Sets the selection to the single cell at the mousedown
   * coord (anchor) + opens the dragging flag so subsequent
   * `cell-mouseover` events extend the range. Doesn't dispatch
   * `cellSelected` here — the `cell-click` handler
   * (which fires on mouseup) takes care of that for a no-drag
   * click. A real drag ends with a `cellRangeSelected` event
   * on the {@link release}.
   */
  press(e: CustomEvent<Cell & Keys>): void {
    this.store.markRangeReportedOnRelease(false);
    const { col, row, shiftKey, ctrlKey, metaKey } = e.detail;
    // A formula is being written: the press points at the cell instead of
    // selecting it, and the keyboard stays where the formula is.
    const source = this.pointing.source();
    if (source !== null) {
      e.preventDefault();
      this.pointing.press(col, row, shiftKey, ctrlKey || metaKey, source);
      return;
    }
    if (ctrlKey || metaKey) {
      // Ctrl/Cmd-mousedown must leave the selection untouched. The
      // click that follows adds this cell as a further range, and it
      // needs the current active range to push aside — replacing the
      // selection here would wipe exactly what the Ctrl-click is meant
      // to keep. mousedown always precedes click for a real pointer.
      // Nor does it start a drag: a pointer that drifted into the next
      // cell before the button came up would replace every range picked
      // out with Ctrl with one rectangle.
      return;
    }
    this.store.changePress({ kind: 'select', crossed: false });
    if (shiftKey) {
      // Shift+mousedown: extend existing range to the
      // mousedown coord (treat the existing anchor as the
      // drag start). Standard Excel "shift-drag" behavior.
      // Aux ranges survive, as on a shift-click.
      const { selection } = this.store;
      const anchor = selection.activeCell;
      this.store.select({
        activeCell: anchor,
        activeRange: this.store.rangeTo(anchor, col, row),
        auxRanges: selection.auxRanges
      });
    } else {
      this.store.selectCellOrMerge(col, row);
    }
  }

  /**
   * Mouse drag — mouseover during drag. Extends
   * the range from the active cell (anchor) to the hovered
   * cell. Mouseover events that fire while NOT dragging are
   * silently ignored — the cell-grid emits on every hover.
   */
  over(col: number, row: number): void {
    if (this.pointing.dragOver(col, row)) {
      return;
    }
    const press = this.store.interaction.press;
    if (press?.kind === 'fill') {
      const outcome = computePaintRectangle(press.from, col, row);
      // By touch a fill only grows the selection.
      this.store.changePress({ ...press, outcome: press.byTouch && outcome.kind === 'decrease' ? NO_PAINT : outcome });
      return;
    }
    if (press?.kind !== 'select') {
      return;
    }
    if (!press.crossed) {
      this.store.changePress({ kind: 'select', crossed: true });
    }
    // Same as shift-arrow: keep activeCell as anchor, move
    // the extend corner.
    // The ranges picked out before stay: a plain press has already let
    // them go, and a Shift-press drags out the range it extends.
    const { selection } = this.store;
    const anchor = selection.activeCell;
    this.store.select({
      activeCell: anchor,
      activeRange: this.store.rangeTo(anchor, col, row),
      auxRanges: selection.auxRanges
    });
  }

  /**
   * Mouse drag — mouseup end. Closes the drag.
   * Dispatches a {@code cellRangeSelected} event when the
   * final range covers more than one cell (single-cell drag
   * is treated as a click, whose `cell-click` handler already
   * dispatched `cellSelected`).
   */
  release(): void {
    const press = this.store.interaction.press;
    if (press?.kind === 'fill') {
      this.finishPainting(press);
      return;
    }
    if (this.host.finishHeaderResize()) {
      return;
    }
    if (this.pointing.released()) {
      // The press that pointed is over; the click it ends in is to come.
      return;
    }
    if (press === null) {
      return;
    }
    this.store.changePress(null);
    if (press.kind === 'select' && this.store.selectsRange()) {
      // Multi-cell range — dispatch the server-bound
      // selection event so the server learns the final
      // dragged range: with the cell the drag started on, when it went
      // into other cells. One that stayed in its cell was a shift-click,
      // which grows the range from the selected cell.
      this.report.range(press.crossed);
      // The click that may follow the release reports nothing more.
      this.store.markRangeReportedOnRelease(true);
    }
    // Single-cell drag (mousedown + mouseup on the same cell
    // with no mouseover-extension) is handled by the
    // `cell-click` handler — no double dispatch.
  }

  /**
   * Column-header click handler. Selects the entire
   * column (rows 1..rows). The active cell becomes the
   * column's first row on screen. Shift-click extends from the existing anchor
   * column to the clicked column, selecting a whole-column
   * range.
   */
  columnHeaderClicked(detail: { col: number } & Keys): void {
    const { col, shiftKey, ctrlKey, metaKey } = detail;
    const { model, selection } = this.store;
    const rows = model.rows > 0 ? model.rows : 1;
    const row = this.firstRowOnScreen();
    if ((ctrlKey || metaKey) && !shiftKey) {
      // The whole column joins the selection as a further range.
      this.store.select(addAuxRange(selection, col, 1, col, rows, { col, row }));
      this.report.columnAdded(col, row);
      return;
    }
    if (shiftKey) {
      // Extend across columns, keeping ranges added with Ctrl.
      const anchor = selection.activeCell;
      this.store.select({
        activeCell: anchor,
        activeRange: normalizedRange(anchor.col, 1, col, rows),
        auxRanges: selection.auxRanges
      });
    } else {
      this.store.select({
        activeCell: { col, row },
        activeRange: normalizedRange(col, 1, col, rows),
        auxRanges: []
      });
    }
    this.report.selection();
  }

  /**
   * Row-header click handler. Mirror of the column
   * variant — selects an entire row (cols 1..cols).
   */
  rowHeaderClicked(detail: { row: number } & Keys): void {
    const { row, shiftKey, ctrlKey, metaKey } = detail;
    const { model, selection } = this.store;
    const cols = model.cols > 0 ? model.cols : 1;
    const col = this.firstColumnOnScreen();
    if ((ctrlKey || metaKey) && !shiftKey) {
      // The whole row joins the selection as a further range.
      this.store.select(addAuxRange(selection, 1, row, cols, row, { col, row }));
      this.report.rowAdded(row, col);
      return;
    }
    if (shiftKey) {
      // Extend across rows, keeping ranges added with Ctrl.
      const anchor = selection.activeCell;
      this.store.select({
        activeCell: anchor,
        activeRange: normalizedRange(1, anchor.row, cols, row),
        auxRanges: selection.auxRanges
      });
    } else {
      this.store.select({
        activeCell: { col, row },
        activeRange: normalizedRange(1, row, cols, row),
        auxRanges: []
      });
    }
    this.report.selection();
  }

  /**
   * Mousedown on the fill handle, or a finger on a fill handle. The drag that
   * follows paints rather than selects, so the current range is remembered
   * as the one being grown or shrunk - by touch, only grown.
   */
  fillStarted(byTouch = false): void {
    const { c1, c2, r1, r2 } = this.store.selection.activeRange;
    this.store.changePress({ kind: 'fill', from: { c1, c2, r1, r2 }, outcome: NO_PAINT, byTouch });
  }

  /**
   * A finger on the corner handle of the selection. The drag that follows
   * stretches the selection from its active cell, as a drag with Shift held
   * does, and the release reports it.
   */
  stretchStarted(): void {
    this.store.changePress({ kind: 'select', crossed: false });
  }

  /**
   * Clicking a custom editor selects the cell it belongs to.
   *
   * An editor is usually wider than its cell, so a click aimed at the cell
   * often lands on the editor instead — and the cell-grid's own click
   * delegation cannot see it, because the editor is projected in rather
   * than a descendant of the cell. Without this, selecting such a cell by
   * mouse is impossible, and the server never learns the editor is showing.
   *
   * Bound to pointerdown rather than focus on purpose: the server focuses
   * an editor itself when it displays one, and reporting that back as a
   * selection would move the selection to whatever the server was last
   * talking about, in a loop.
   */
  editorPressed(e: Event): void {
    const cell = this.host.cellOfEditor(e);
    if (cell === null) {
      return;
    }
    const active = this.store.selection.activeCell;
    if (active.col === cell.col && active.row === cell.row) {
      return;
    }
    this.store.select(selectSingleCell(cell.col, cell.row));
    this.report.selection();
  }

  /**
   * The row a click on a column letter makes active: the first one whose top
   * is on screen, as in GWT, or row 1 when rows are frozen. Row 1 of a sheet
   * scrolled far down is off screen, and the keys went there.
   */
  private firstRowOnScreen(): number {
    const { model } = this.store;
    if (model.verticalSplitPosition > 0 || model.rows <= 0) {
      return 1;
    }
    let row = this.store.derived.sheetSizes(model).rows.reach(1, this.host.scrollTop());
    while (row < model.rows && model.hiddenRowIndexes.has(row)) {
      row++;
    }
    return row;
  }

  /** The column a click on a row number makes active; see {@link firstRowOnScreen}. */
  private firstColumnOnScreen(): number {
    const { model } = this.store;
    if (model.horizontalSplitPosition > 0 || model.cols <= 0) {
      return 1;
    }
    let col = this.store.derived.sheetSizes(model).columns.reach(1, this.host.scrollLeft());
    while (col < model.cols && model.hiddenColumnIndexes.has(col)) {
      col++;
    }
    return col;
  }

  /**
   * Release of a fill-handle drag: tell the server what to copy or clear.
   *
   * The two directions are different operations, and the server takes them
   * through different events — growing reports the whole new rectangle,
   * shrinking reports only the corner the selection retreats to.
   */
  private finishPainting(painting: { from: CellRect; outcome: PaintOutcome }): void {
    this.store.changePress(null);
    if (painting.outcome.kind === 'none') {
      return;
    }

    if (painting.outcome.kind === 'decrease') {
      const { r1, c1 } = painting.outcome.painted;
      this.host.send({ type: 'selectionDecreasePainted', data: { row: r1, col: c1 } });
      return;
    }

    const grown = grownSelection(painting.from, painting.outcome.painted);
    if (!isNonEmpty(grown)) {
      return;
    }
    this.host.send({
      type: 'selectionIncreasePainted',
      data: { r1: grown.r1, c1: grown.c1, r2: grown.r2, c2: grown.c2 }
    });
  }
}
