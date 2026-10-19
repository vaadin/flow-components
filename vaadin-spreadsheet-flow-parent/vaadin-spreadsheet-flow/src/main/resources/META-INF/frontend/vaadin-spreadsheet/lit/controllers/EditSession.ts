/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { parseColRow, toKey } from '../algorithms/CellKeys.js';
import { keyDirection, moveActiveCell } from '../algorithms/KeyboardMoves.js';
import { initialEditValue } from '../algorithms/PercentageEntry.js';
import { inSelectionWalkDirection, walkInsideSelection } from '../algorithms/SelectionWalk.js';
import type { CellEditorState } from '../elements/types.js';
import { closeEditor, openEditor } from '../state/Interaction.js';
import type { SheetStore } from '../state/SheetStore.js';
import type { SpreadsheetEvent } from '../types/rpc-client-to-server.js';
import type { FormulaPointer } from './FormulaPointer.js';
import type { SelectionReporter } from './SelectionReporter.js';

/** A cell, by its column and row, 1-based. */
interface Cell {
  col: number;
  row: number;
}

/** What editing cells needs of the sheet. */
export interface EditSessionHost {
  /** Whether the formula field has the keyboard. */
  fieldHasFocus(): boolean;
  /** Whether the formula field has been typed into since it took the keyboard. */
  fieldEdited(): boolean;
  /** Whether the keyboard went to the formula bar. */
  isFormulaBar(target: EventTarget | null | undefined): boolean;
  /** Whether the keyboard went to the cells. */
  isCellGrid(target: EventTarget | null | undefined): boolean;
  /** Runs work as one of the sheet's own handlers: what it changes is drawn as it ends. */
  handle(work: () => void): void;
  /** Gives the keyboard back to the sheet, unless a custom editor has it. */
  returnFocusToSheet(): void;
  /** Puts the active cell elsewhere in the range selected, keeping the range, and tells the server. */
  moveInsideSelection(cell: Cell): void;
  /** Brings a cell into view. */
  scrollIntoView(cell: Cell): void;
  /** Asks for the sheet to be drawn. */
  scheduleRender(): void;
  /** Tells the server. */
  send(event: SpreadsheetEvent): void;
}

/**
 * Editing cells: in the cell editor, opened by a double-click, F2, Enter or
 * typing, and in the formula field beside the address field; and the note
 * of a cell. A committed value goes to the server and shows at once, and the
 * selection moves on the way the key that ended the edit says.
 */
export class EditSession {
  constructor(
    private readonly store: SheetStore,
    private readonly report: SelectionReporter,
    private readonly pointing: FormulaPointer,
    private readonly host: EditSessionHost
  ) {}

  /**
   * Opens the cell editor on what a cell holds, as a double-click, F2 or
   * Enter does; whether it opened. A locked cell does not open, and the
   * server is told it was tried.
   * <p>
   * The cell is brought into view first, as GWT did: the editor is drawn
   * over it, and a cell scrolled out of view is not drawn, so the keys
   * typed into an editor opened there were lost.
   */
  openExisting(col: number, row: number): boolean {
    if (this.store.isCellLockedForEdit(col, row)) {
      this.report.protectedWrite();
      return false;
    }
    this.host.scrollIntoView({ col, row });
    // Seeded with what the cell IS, not what it shows — the formula
    // behind a computed cell, and the unformatted value otherwise, which
    // is what carries a text cell's leading apostrophe.
    const editor: CellEditorState = {
      col,
      row,
      initialValue: this.store.formulaFieldValueFor(col, row),
      // Opening the existing value to change it: arrows move the caret.
      mode: 'edit'
    };
    this.store.changeEdit(openEditor(this.store.interaction.edit, editor, false));
    return true;
  }

  /**
   * Opens the cell editor on a key typed over a cell, with that character
   * as its first text - Excel's way: typing directly starts replacing the
   * cell's content; whether it opened. A locked cell does not open, and the
   * server is told it was tried. The cell is brought into view first - see
   * {@link openExisting}.
   */
  openTyping(col: number, row: number, key: string): boolean {
    if (this.store.isCellLockedForEdit(col, row)) {
      this.report.protectedWrite();
      return false;
    }
    this.host.scrollIntoView({ col, row });
    // Typing over the cell: an arrow commits and moves on.
    // A digit typed into a percentage cell means that many percent, so
    // the editor opens on "5%" rather than "5" - see PercentageEntry.
    // Space opens it empty: it starts an edit and types nothing.
    const editor: CellEditorState = {
      col,
      row,
      initialValue: key === ' ' ? '' : initialEditValue(key, this.store.sheetData.cells.get(toKey(col, row))),
      mode: 'enter'
    };
    this.store.changeEdit(openEditor(this.store.interaction.edit, editor, key !== ' ' && editor.initialValue !== key));
    return true;
  }

  /**
   * Cell-editor commit. Called when the user presses Enter,
   * Tab, or blurs the input. Reads the typed value, dispatches
   * `cellValueEdited` to the server, closes the editor. When
   * the commit key was Enter/Tab, moves the active
   * cell in the canonical Excel direction:
   * Enter           → row + 1
   * Shift+Enter     → row - 1
   * Tab             → col + 1
   * Shift+Tab       → col - 1
   * blur (no key)   → stay at the committed cell
   */
  editorCommitted(
    e: CustomEvent<{
      col: number;
      row: number;
      value: string;
      key?: string;
      shiftKey?: boolean;
      relatedTarget?: EventTarget | null;
    }>
  ): void {
    const { col, row, value, key, shiftKey, relatedTarget } = e.detail;
    const editor = this.store.editor;
    // An editor that has been closed commits nothing: its input losing the
    // keyboard as it goes is not the user leaving it.
    if (editor?.col !== col || editor.row !== row) {
      return;
    }
    // Left for the formula bar, the edit carries on in the formula field:
    // nothing is committed yet, and the editor stays open. Gone on to the
    // address field instead, it ends once the keyboard has settled there.
    if (key === undefined && this.host.isFormulaBar(relatedTarget)) {
      e.preventDefault();
      setTimeout(() =>
        this.host.handle(() => {
          const open = this.store.editor;
          if (open?.col === col && open.row === row && !this.host.fieldHasFocus()) {
            this.commit(col, row, this.store.writing?.text ?? value, undefined, false, false);
          }
        })
      );
      return;
    }
    // Left for somewhere - a custom editor, a field of the page - the
    // keyboard stays where the user put it. A key that ended the edit, which
    // has nowhere it went, or a blur to nowhere gives it back to the sheet.
    this.commit(col, row, value, key, shiftKey === true, relatedTarget == null);
  }

  /**
   * Cell-editor cancel. User pressed Escape. Close
   * the editor without dispatching anything.
   */
  editorCancelled(): void {
    const editor = this.store.editor;
    this.pointing.end();
    this.store.changeEdit(closeEditor(this.store.interaction.edit));
    // The formula field showed what was typed; it shows the cell again.
    if (editor !== null) {
      this.store.showInFormulaField(this.store.formulaFieldValueFor(editor.col, editor.row));
    }
    this.host.returnFocusToSheet();
  }

  /**
   * A value typed into the formula field.
   *
   * It belongs to the cell the sheet has selected, so it is reported as
   * an ordinary edit of that cell. Enter then moves on to the next cell
   * down and Tab to the next one right, the way finishing an edit in
   * the cell itself does; shift+Tab commits without moving. Clicking away commits too and leaves the
   * selection alone - that is how a value typed here reaches the sheet
   * when the user goes straight back to the cell.
   */
  fieldCommitted(detail: { value: string; key?: string; shiftKey: boolean; relatedTarget?: EventTarget | null }): void {
    const { value, key, shiftKey, relatedTarget } = detail;
    // Back to the cell editor the edit came from: it carries on there.
    if (key === undefined && this.store.editor !== null && this.host.isCellGrid(relatedTarget)) {
      return;
    }
    const { col, row } = this.store.selection.activeCell;
    const { model } = this.store;
    if (model.rows <= 0 || model.cols <= 0) {
      return;
    }
    if (this.store.isCellLockedForEdit(col, row)) {
      this.report.protectedWrite();
      return;
    }
    // An edit started in the cell ends here too: its editor closes, and
    // the cell shows the value, as it does when the edit ends there.
    this.pointing.end();
    this.store.changeEdit(closeEditor(this.store.interaction.edit));
    this.store.showInFormulaField(value);
    this.showCommittedValue(col, row, value);
    this.host.send({ type: 'cellValueEdited', data: { row, col, value } });
    // shift+Tab stays put.
    const direction = key === undefined || (key === 'Tab' && shiftKey) ? null : keyDirection(key, false);
    if (direction === null) {
      return;
    }
    this.host.returnFocusToSheet();
    const next = moveActiveCell({ col, row }, this.store.mergeEntry, direction, this.store.sheetShape());
    if (next === null) {
      return;
    }
    this.store.pick(next);
    this.host.scrollIntoView(next.activeCell);
    this.report.selection();
  }

  /**
   * Escape in the formula field: put the cell's own value back. An edit
   * started in the cell is thrown away with it, and its editor closes.
   */
  fieldCancelled(): void {
    this.pointing.end();
    const { col, row } = this.store.selection.activeCell;
    this.store.showInFormulaField(this.store.formulaFieldValueFor(col, row));
    this.store.changeEdit(closeEditor(this.store.interaction.edit));
    this.host.returnFocusToSheet();
  }

  /**
   * Shows the value of a cell the server named in the formula field, unless
   * the user is writing one: in the cell editor, which the field mirrors,
   * or in the field itself, for the cell they are still on. A formula being
   * written in the field whose text this replaces is over, and so are its
   * colours.
   */
  showServerValue(value: string, sameCell: boolean): void {
    if (this.store.editor !== null || (sameCell && this.host.fieldEdited())) {
      return;
    }
    const draft = this.store.draft;
    if (draft !== null && draft.text !== value) {
      this.pointing.end();
    }
    this.store.showInFormulaField(value);
  }

  /**
   * A finished comment.
   *
   * The note shows what was typed straight away - the server sends the
   * comments back, and waiting for that leaves the old note on screen
   * in the meantime. A note emptied out is a note removed, which is
   * what the server makes of empty text.
   */
  noteCommitted(detail: { key?: string; text: string }): void {
    const { key, text } = detail;
    // Reported once. Finishing the note puts the keyboard back on the
    // sheet, which takes the focus off the textarea and asks again,
    // and the textarea is still there until the next render.
    const interaction = this.store.interaction;
    if (interaction.comment === null) {
      return;
    }
    this.store.interact({ ...interaction, comment: null });
    const coord = key === undefined ? null : parseColRow(key);
    if (coord === null || coord.col < 1 || coord.row < 1) {
      return;
    }
    this.store.change({ cellComments: { ...this.store.model.cellComments, [key as string]: text } });
    this.host.returnFocusToSheet();
    this.host.send({ type: 'updateCellComment', data: { text, col: coord.col, row: coord.row } });
  }

  /**
   * Commit an edit made in the cell editor: report the value, close the
   * editor and move on the way the key that ended the edit says - see
   * {@link editorCommitted}. The keyboard goes back to the sheet unless the
   * user has already put it somewhere.
   */
  private commit(
    col: number,
    row: number,
    value: string,
    key: string | undefined,
    shiftKey: boolean,
    takeKeyboard: boolean
  ): void {
    this.showCommittedValue(col, row, value);
    // Close the editor first so the re-render removes the
    // input — important to do BEFORE dispatching the server
    // event so the editor doesn't briefly flash on top of the
    // server's response.
    this.pointing.end();
    this.store.changeEdit(closeEditor(this.store.interaction.edit));
    this.store.showInFormulaField(value);
    if (takeKeyboard) {
      this.host.returnFocusToSheet();
    }
    // Move the active cell to the next slot per Excel
    // convention. For 'Enter' / 'Tab' the move
    // direction is fixed; blur leaves the selection at the
    // committed cell. Clamped to grid bounds.
    // Enter moves down, Tab right, shift reverses each; an arrow key
    // moves the way it points, which is what committing from Excel's
    // "enter" editing mode does.
    //
    // With a range selected the commit walks inside it, the same way
    // Enter and Tab do when nothing is being edited: filling a block
    // cell by cell is the reason the range is selected at all.
    const { model } = this.store;
    const hasCells = model.rows > 0 && model.cols > 0;
    const walkDirection = inSelectionWalkDirection(key, shiftKey);
    const walks = walkDirection !== null && hasCells && this.store.selectsRange();
    // Moving on goes by the arrows' rules: over hidden rows and columns,
    // and out of a merge on its far side.
    const direction = walks || key === undefined ? null : keyDirection(key, shiftKey);
    const next =
      direction !== null && hasCells
        ? moveActiveCell({ col, row }, this.store.mergeEntry, direction, this.store.sheetShape())
        : null;
    if (next !== null) {
      this.store.pick(next);
    }
    this.host.send({ type: 'cellValueEdited', data: { row, col, value } });
    // If we moved the active cell, also dispatch the
    // selection event so the server stays in sync: every
    // selection write sends an event.
    if (next !== null) {
      this.report.selection();
    }
    if (walks) {
      this.host.moveInsideSelection(
        walkInsideSelection({
          range: this.store.selection.activeRange,
          col,
          row,
          direction: walkDirection,
          hiddenColumns: model.hiddenColumnIndexes,
          hiddenRows: model.hiddenRowIndexes
        })
      );
    }
  }

  /**
   * Show a committed value in its cell at once. The server's
   * `cellsUpdated` callback may arrive much later (or not at all in offline
   * / TestBench scenarios); the cell is updated before the round-trip, as
   * Excel users expect - except for a formula, which keeps showing the old
   * value until the server has worked it out. Preserves
   * the existing CellData metadata when present (style index, locked flag).
   */
  private showCommittedValue(col: number, row: number, value: string): void {
    if (value.startsWith('=') || value.startsWith('+')) {
      return;
    }
    const cells = this.store.sheetData.cells;
    const cellKey = toKey(col, row);
    const previous = cells.get(cellKey);
    cells.set(cellKey, {
      col,
      row,
      value,
      cellStyle: previous?.cellStyle ?? 'cs0',
      locked: previous?.locked ?? false,
      needsMeasure: previous?.needsMeasure ?? false,
      isPercentage: previous?.isPercentage ?? false,
      // Not carried over: how far the old text ran into its neighbours says
      // nothing about how far the newly typed text should. The server sends
      // the new width with its answer.
      overflowWidth: 0,
      ...(previous?.formulaValue !== undefined ? { formulaValue: previous.formulaValue } : {}),
      ...(previous?.originalValue !== undefined ? { originalValue: previous.originalValue } : {}),
      ...(previous?.textColor !== undefined ? { textColor: previous.textColor } : {})
    });
    // Drawn whichever way the edit was committed: leaving the formula field
    // asks for no repaint of its own.
    this.host.scheduleRender();
  }
}
