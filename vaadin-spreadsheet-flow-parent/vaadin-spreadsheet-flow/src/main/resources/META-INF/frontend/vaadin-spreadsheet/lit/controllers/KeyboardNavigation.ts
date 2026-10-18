/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import {
  homeEndTarget,
  keyDirection,
  moveActiveCell,
  moveRangeEdge,
  selectCellAt,
  type SingleCellSelection,
  typesCharacter
} from '../algorithms/KeyboardMoves.js';
import { inSelectionWalkDirection, walkInsideSelection } from '../algorithms/SelectionWalk.js';
import { normalizedRange } from '../state/SelectionState.js';
import type { SheetStore } from '../state/SheetStore.js';
import type { SpreadsheetEvent } from '../types/rpc-client-to-server.js';
import type { EditSession } from './EditSession.js';
import type { SelectionReporter } from './SelectionReporter.js';

/** A cell, by its column and row, 1-based. */
interface Cell {
  col: number;
  row: number;
}

/** A key pressed on the sheet, with what is worked out of it once for all the commands. */
interface KeyPress {
  readonly e: KeyboardEvent;
  readonly key: string;
  readonly ctrlOrMeta: boolean;
  /**
   * Whether the key that holds a shortcut - select all, undo, redo - is
   * down: Cmd on a Mac, where Ctrl+A and Ctrl+Z do nothing, and Ctrl or Cmd
   * elsewhere.
   */
  readonly shortcut: boolean;
  /** Whether the key types a character; see {@link typesCharacter}. */
  readonly types: boolean;
}

/** What the keyboard on the sheet needs of the sheet. */
export interface KeyboardNavigationHost {
  /** Whether an event was raised inside a component the sheet hosts: a custom editor, an in-cell component, an overlay. */
  raisedInside(e: Event): boolean;
  /** Whether an event was raised inside a custom cell editor. */
  raisedInEditor(e: Event): boolean;
  /** Gives the keyboard to the custom editor of a cell, if it has one; whether it did. */
  focusEditorOf(col: number, row: number): boolean;
  /** Whether Escape, then Tab, takes the key, leaving the sheet; see {@code KeyboardExit}. */
  exits(e: KeyboardEvent): boolean;
  /** Gives the keyboard to the sheet. */
  focusSheet(): void;
  /** Whether the sheet runs on a Mac, where Cmd holds the shortcuts. */
  isMac(): boolean;
  /** Brings a cell into view. */
  scrollIntoView(cell: Cell): void;
  /** Tells the server. */
  send(event: SpreadsheetEvent): void;
}

/**
 * Whether a key pressed in a custom editor takes the keyboard back to the
 * sheet. Enter does, unless the editor used it: for a new line in a text
 * area or in rich text, or to pick from a list it has open.
 */
function leavesEditor(e: KeyboardEvent): boolean {
  if (e.key === 'Escape' || e.key === 'Tab') {
    return true;
  }
  if (e.key !== 'Enter' || e.defaultPrevented) {
    return false;
  }
  const target = e.composedPath()[0];
  return !(target instanceof HTMLTextAreaElement) && !(target instanceof HTMLElement && target.isContentEditable);
}

/** Whether a key goes to a button, which the key itself is pressing. */
function pressesButton(e: KeyboardEvent): boolean {
  const target = e.composedPath()[0];
  return target instanceof HTMLElement && (target.localName === 'button' || target.getAttribute('role') === 'button');
}

/**
 * The keyboard on the sheet. Arrow keys, Tab and Enter move the active cell
 * over hidden rows and columns and out of merges; Shift with an arrow
 * grows the range from the active cell, which stays where it is, as in
 * Excel. Home and End go along the row, or with Ctrl to the sheet's first
 * and last cell; typing starts an edit; the shortcuts select all, undo and
 * redo. Escape, then Tab, leaves the sheet; see {@code KeyboardExit}.
 */
export class KeyboardNavigation {
  /**
   * What a key does, in the order it is asked: the first command that takes
   * a key handles it. The order matters - a custom editor takes the keys
   * that start an edit before the sheet does, and the shortcuts come before
   * typing a character.
   */
  private readonly commands: ReadonlyArray<(press: KeyPress) => boolean> = [
    (p) => this.editThroughCustomEditor(p),
    (p) => this.selectAll(p),
    (p) => this.undo(p),
    (p) => this.redo(p),
    (p) => this.openEditor(p),
    (p) => this.typeOverCell(p),
    (p) => this.clearCells(p),
    (p) => this.walkInsideRange(p),
    (p) => this.moveOrGrow(p),
    (p) => this.homeOrEnd(p)
  ];

  constructor(
    private readonly store: SheetStore,
    private readonly report: SelectionReporter,
    private readonly edit: EditSession,
    private readonly host: KeyboardNavigationHost
  ) {}

  /** A key pressed on the sheet. */
  keyDown(e: KeyboardEvent): void {
    // Enter and Space on a button of the sheet's own - an outline button, a
    // popup's close box - press that button, as on any button, rather than
    // editing the active cell.
    if ((e.key === 'Enter' || e.key === ' ') && pressesButton(e)) {
      return;
    }
    if (this.host.raisedInside(e)) {
      // The key belongs to the component the user is typing into. Acting
      // on it as a sheet shortcut moves the selection out from under a
      // custom editor mid-edit, which takes the editor off the sheet.
      //
      // Three keys are the way back out of a custom editor, so they are the
      // sheet's after all: Escape returns the focus, Tab returns it and then
      // moves on to the next cell, and Enter returns it once the editor has
      // had it, leaving the cell selected, as in GWT.
      if (!this.host.raisedInEditor(e) || !leavesEditor(e)) {
        return;
      }
      this.host.focusSheet();
      if (e.key !== 'Tab') {
        return;
      }
      e.preventDefault();
    } else if (this.host.exits(e)) {
      return;
    }
    const isMac = this.host.isMac();
    const ctrlOrMeta = e.ctrlKey || e.metaKey;
    const press: KeyPress = {
      e,
      key: e.key,
      ctrlOrMeta,
      shortcut: isMac ? e.metaKey : ctrlOrMeta,
      types: typesCharacter(e, isMac)
    };
    for (const command of this.commands) {
      if (command(press)) {
        return;
      }
    }
  }

  /**
   * A key let go on the page. Shift let go: the selection is made, so the
   * address field goes back to naming the cell the user started from.
   */
  keyUp(e: KeyboardEvent): void {
    if (e.key !== 'Shift' || !this.store.interaction.extending) {
      return;
    }
    this.store.extendSelection(false);
  }

  /**
   * Move the selection to one cell - or merge - from the keyboard. The cell
   * is scrolled into view before the render, and named in the address field
   * as the server hears of it.
   */
  moveTo(next: SingleCellSelection): void {
    this.store.extendSelection(false);
    this.store.pick(next);
    this.host.scrollIntoView(next.activeCell);
  }

  /**
   * Put the active cell somewhere else inside the range that is already
   * selected, keeping the range itself.
   *
   * The server hears that only the active cell moved; see
   * {@link SelectionReporter.activeCellMoved}.
   */
  moveInsideSelection(next: Cell): void {
    const { selection } = this.store;
    this.store.select({
      activeCell: next,
      activeRange: selection.activeRange,
      auxRanges: selection.auxRanges
    });
    this.host.scrollIntoView(next);
    this.report.activeCellMoved(next);
  }

  /**
   * A cell showing a custom editor is edited through that editor, so
   * every key that would otherwise start editing hands it the focus
   * instead: F2, Enter, and any printable character.
   *
   * Deliberately without preventDefault. The character that opened the
   * editor should land in it, and the browser delivers a keypress to
   * whatever holds the focus once the handlers have run — which by then
   * is the editor.
   */
  private editThroughCustomEditor(p: KeyPress): boolean {
    const startsEditing =
      !this.store.editor && (((p.key === 'F2' || p.key === 'Enter') && !p.ctrlOrMeta && !p.e.altKey) || p.types);
    const { activeCell } = this.store.selection;
    return startsEditing && this.host.focusEditorOf(activeCell.col, activeCell.row);
  }

  /** Ctrl+A / Cmd+A — select the entire sheet. */
  private selectAll(p: KeyPress): boolean {
    if (!p.shortcut || (p.key !== 'a' && p.key !== 'A')) {
      return false;
    }
    if (!this.store.hasCells()) {
      return true;
    }
    p.e.preventDefault();
    const { model } = this.store;
    this.store.select({
      activeCell: { col: 1, row: 1 },
      activeRange: normalizedRange(1, 1, model.cols, model.rows),
      auxRanges: []
    });
    this.report.selection(true);
    return true;
  }

  /**
   * Ctrl+Z / Cmd+Z — undo. Pure pass-through to the server;
   * the server owns the undo stack + replies with the
   * resulting cell updates.
   */
  private undo(p: KeyPress): boolean {
    if (!p.shortcut || (p.key !== 'z' && p.key !== 'Z') || p.e.shiftKey) {
      return false;
    }
    p.e.preventDefault();
    this.host.send({ type: 'onUndo', data: {} });
    return true;
  }

  /**
   * Ctrl+Y / Cmd+Shift+Z — redo. Cmd+Y is reserved on Mac
   * for "history menu" by some apps, so we also accept
   * Cmd+Shift+Z (the Mac convention) + Ctrl+Y (Windows).
   */
  private redo(p: KeyPress): boolean {
    const { key } = p;
    if (!p.shortcut || !(key === 'y' || key === 'Y' || ((key === 'z' || key === 'Z') && p.e.shiftKey))) {
      return false;
    }
    p.e.preventDefault();
    this.host.send({ type: 'onRedo', data: {} });
    return true;
  }

  /**
   * F2 opens the cell editor at the active cell, loaded with the
   * cell's existing value (Excel convention).
   *
   * So does Enter on a single cell, as it always has. With a range
   * selected Enter
   * walks inside it instead, and on a cell the user cannot see there
   * is nothing to edit, so it falls through to moving on.
   */
  private openEditor(p: KeyPress): boolean {
    const { model, selection } = this.store;
    const { activeCell } = selection;
    const entersEditor =
      p.key === 'F2' ||
      (p.key === 'Enter' &&
        !this.store.selectsRange() &&
        !model.hiddenRowIndexes.has(activeCell.row) &&
        !model.hiddenColumnIndexes.has(activeCell.col));
    if (!entersEditor || this.store.editor) {
      return false;
    }
    // F2 opens the existing value, so arrows move the caret.
    if (this.store.hasCells() && this.edit.openExisting(activeCell.col, activeCell.row)) {
      p.e.preventDefault();
    }
    return true;
  }

  /**
   * Type-to-edit: a key that types a character opens the cell editor at
   * the active cell with that character as the initial value - Excel's
   * way: typing directly starts replacing the cell's content. A
   * character typed with AltGr or Option counts; see typesCharacter.
   */
  private typeOverCell(p: KeyPress): boolean {
    if (this.store.editor || !p.types) {
      return false;
    }
    const { col, row } = this.store.selection.activeCell;
    if (this.store.hasCells() && this.edit.openTyping(col, row, p.key)) {
      p.e.preventDefault();
    }
    return true;
  }

  /**
   * Delete + Backspace clear the cell contents in the current
   * selection. Pure pass-through to the server — the connector
   * dispatches `deleteSelectedCells` and the server replies
   * with `update*CellValues` carrying the cleared cells. The
   * client doesn't optimistically clear locally so the
   * server's response is the single source of truth.
   */
  private clearCells(p: KeyPress): boolean {
    if (p.key !== 'Delete' && p.key !== 'Backspace') {
      return false;
    }
    if (this.store.hasCells()) {
      p.e.preventDefault();
      this.host.send({ type: 'deleteSelectedCells', data: {} });
    }
    return true;
  }

  /**
   * With a range selected, Enter and Tab walk the active cell around
   * inside it instead of moving on and dropping it: Enter down the
   * column and on to the next, Tab along the row and on to the next,
   * both wrapping at the end, shift reversing. The range stays, so the
   * server hears a selection that did not discard its old range.
   */
  private walkInsideRange(p: KeyPress): boolean {
    const walkDirection = inSelectionWalkDirection(p.key, p.e.shiftKey);
    if (walkDirection === null || !this.store.hasCells() || !this.store.selectsRange()) {
      return false;
    }
    p.e.preventDefault();
    const { model, selection } = this.store;
    this.moveInsideSelection(
      walkInsideSelection({
        range: selection.activeRange,
        col: selection.activeCell.col,
        row: selection.activeCell.row,
        direction: walkDirection,
        hiddenColumns: model.hiddenColumnIndexes,
        hiddenRows: model.hiddenRowIndexes
      })
    );
    return true;
  }

  /**
   * The arrows - and Tab and Enter, outside a range - step over hidden
   * rows and columns, onto a merge as a
   * whole and out of it on the far side, and not at all at the edge of
   * the sheet. Shift+arrow moves the side of the range away from the
   * active cell instead.
   */
  private moveOrGrow(p: KeyPress): boolean {
    const direction = keyDirection(p.key, p.e.shiftKey);
    if (direction === null) {
      return false;
    }
    if (!this.store.hasCells()) {
      return true;
    }
    p.e.preventDefault();
    const { selection } = this.store;
    if (p.e.shiftKey && p.key.startsWith('Arrow')) {
      const grown = moveRangeEdge(selection.activeRange, selection.activeCell, direction, this.store.sheetShape());
      if (grown === null) {
        return true;
      }
      this.store.select({ activeCell: selection.activeCell, activeRange: grown.range, auxRanges: [] });
      this.store.extendSelection(true);
      this.host.scrollIntoView(grown.moved);
    } else {
      const next = moveActiveCell(selection.activeCell, this.store.mergeEntry, direction, this.store.sheetShape());
      if (next === null) {
        return true;
      }
      this.moveTo(next);
    }
    this.report.selection();
    return true;
  }

  /**
   * Home / End go to the first / last column of the row, Ctrl+Home to
   * A1 and Ctrl+End to the last cell.
   */
  private homeOrEnd(p: KeyPress): boolean {
    const { model, selection } = this.store;
    const target = homeEndTarget(p.key, p.ctrlOrMeta, selection.activeCell, model.cols, model.rows);
    if (!target) {
      return false;
    }
    if (!this.store.hasCells()) {
      return true;
    }
    p.e.preventDefault();
    this.moveTo(selectCellAt(target.col, target.row, model.mergedRegions));
    this.report.selection();
    return true;
  }
}
