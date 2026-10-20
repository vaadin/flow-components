/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { html, type TemplateResult } from 'lit';
import { toKey } from '../../algorithms/CellKeys.js';
import { cellCoordToAddress } from '../../algorithms/CellRef.js';
import { isFormula } from '../../algorithms/FormulaPointing.js';
import { composing } from '../../algorithms/KeyboardMoves.js';
import { caretPosition } from '../../algorithms/PercentageEntry.js';
import type { CellEditorState, CellGridRenderState } from '../types.js';
import { editorCellStyle } from './CellStyleRules.js';
import { columnWidthOf, drawnCellBox } from './GridGeometry.js';

/**
 * How wide the editor is for text drawn at a width: its cell - the whole merged cell when the cell starts one - and then
 * the columns after it, whole ones, until the text fits, and a pixel more,
 * over the gridline at its right. Only the columns drawn count; past them
 * the editor is out of sight anyway, and the text scrolls inside it.
 */
export function editorWidth(state: CellGridRenderState, col: number, row: number, textWidth: number): number {
  const index = state.mergedRegions.findIndex((region) => region.col1 === col && region.row1 === row);
  const merged = index < 0 ? undefined : state.mergedRegionBoxes?.[index];
  let last = merged === undefined ? col : state.mergedRegions[index].col2;
  let width = merged === undefined ? columnWidthOf(state, col) : merged.width;
  while (width < textWidth && last < state.viewport.lastColumnIndex) {
    last += 1;
    width += columnWidthOf(state, last);
  }
  return width + 1;
}

/** Whether a theme attribute's value names the Lumo variant. */
function isLumo(theme: string | null | undefined): boolean {
  return (theme ?? '').split(/\s+/).includes('lumo');
}

/** The keys that move the caret - or, in a formula, may point at a cell instead. */
const ARROW_KEYS: ReadonlySet<string> = new Set(['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight']);

/**
 * Tells the grid's listeners the edit ends: committed, or cancelled. The
 * detail carries the cell, so the value goes to the right one without
 * asking the grid again; the key and Shift, so the next active cell can be
 * picked (Excel convention: Enter moves down, Tab moves right, Shift
 * reverses each); and where the keyboard went when the editor was left.
 *
 * A listener that cancels the event keeps the edit going: left for the
 * formula field, the edit carries on there and the editor stays open.
 */
function endEdit(
  target: EventTarget,
  editor: CellEditorState,
  type: 'cell-editor-commit' | 'cell-editor-cancel',
  value: string,
  key?: string,
  shiftKey?: boolean,
  relatedTarget?: EventTarget | null
): void {
  // Closing the editor takes the input out of the document, and losing
  // the focus that way fires blur — which would commit a second time.
  // One edit, one `cellValueEdited`.
  if (target instanceof HTMLElement) {
    target.dataset.editorClosed = 'true';
  }
  const proceeds = target.dispatchEvent(
    new CustomEvent(type, {
      detail: { col: editor.col, row: editor.row, value, key, shiftKey, relatedTarget },
      bubbles: true,
      composed: true,
      cancelable: true
    })
  );
  if (!proceeds && target instanceof HTMLElement) {
    delete target.dataset.editorClosed;
  }
}

/**
 * A key in the editor. Enter and Tab commit, and Escape cancels. An arrow
 * may point at a cell for a formula; otherwise it commits the value typed
 * over a cell and moves on, or moves the caret in a value opened to be
 * changed. Every other key is the input's, typed into it, and goes no
 * further: the sheet would take it for its own.
 */
function editorKeyDown(e: KeyboardEvent, editor: CellEditorState): void {
  const input = e.currentTarget as HTMLInputElement;
  if (composing(e)) {
    // The key is the input method's: confirming a conversion with Enter
    // neither commits nor moves on.
    e.stopPropagation();
    return;
  }
  if (e.key === 'Enter' || e.key === 'Tab') {
    e.preventDefault();
    endEdit(input, editor, 'cell-editor-commit', input.value, e.key, e.shiftKey);
  } else if (e.key === 'Escape') {
    e.preventDefault();
    endEdit(input, editor, 'cell-editor-cancel', input.value);
  } else if (ARROW_KEYS.has(e.key)) {
    // In a formula the sheet may take the arrow, to point at a cell with
    // it. One it leaves moves the caret and never commits: a formula stays
    // open whatever the arrows do.
    const pointed = !input.dispatchEvent(
      new CustomEvent('cell-editor-arrow', {
        detail: { key: e.key, shiftKey: e.shiftKey, value: input.value, caret: input.selectionStart ?? 0 },
        bubbles: true,
        composed: true,
        cancelable: true
      })
    );
    if (pointed) {
      e.preventDefault();
    } else if (editor.mode === 'enter' && !isFormula(input.value)) {
      // Excel has two editing modes and they differ here: when the user
      // is typing over the cell, an arrow commits and moves on. Only
      // when they opened the existing value to change it does an arrow
      // move the caret instead.
      e.preventDefault();
      endEdit(input, editor, 'cell-editor-commit', input.value, e.key, e.shiftKey);
    }
  }
  // Other keys (typing) are NOT stopped so the input receives them.
  e.stopPropagation();
}

/** Where the caret is, for the reference it is in to be outlined. */
function caretMoved(e: Event): void {
  const input = e.currentTarget as HTMLInputElement;
  input.dispatchEvent(
    new CustomEvent('cell-editor-caret', {
      detail: { value: input.value, caret: input.selectionStart ?? 0 },
      bubbles: true,
      composed: true
    })
  );
}

/** Leaving the editor commits it, as in Excel, leaving the selection at the cell. */
function editorLeft(e: FocusEvent, editor: CellEditorState): void {
  const input = e.currentTarget as HTMLInputElement;
  if (input.dataset.editorClosed === 'true') {
    // Already committed or cancelled by a key; this blur is the
    // editor being taken off the sheet, not the user leaving it.
    return;
  }
  endEdit(input, editor, 'cell-editor-commit', input.value, undefined, undefined, e.relatedTarget);
}

/**
 * The editor's input over the cell being edited, or nothing while no cell
 * is - or while the cell is out of view, or hidden. Over the whole of a
 * merged cell, and as wide as the text in it needs once that has been
 * measured.
 */
export function cellEditorInput(
  state: CellGridRenderState,
  lumo = false,
  fitWidth?: number,
  onInput?: (e: Event) => void
): TemplateResult | string {
  const editor = state.editor;
  if (!editor) {
    return '';
  }
  const { col, row, initialValue } = editor;

  // Over the cell, the whole of a merged one, in a holder of no size where
  // the grid starts. Over a frozen cell the holder is held in place as the
  // cell's pane is, and the editor is placed as the cell is in its pane, so
  // it stays on the cell however fast the sheet scrolls. Over a scrolling
  // cell the holder is not held, and the editor goes where the cell is on
  // the grid. The holder is drawn either way, which keeps the input the
  // same one all through an edit.
  const box = drawnCellBox(state, col, row);
  if (box === null || box.width === 0 || box.height === 0) {
    return '';
  }
  const heldAcross = col <= state.horizontalSplitPosition;
  const heldDown = row <= state.verticalSplitPosition;
  const headers = state.headerExtent ?? { width: 0, height: 0 };
  const hold = (heldDown ? `top:${headers.height}px;` : '') + (heldAcross ? `left:${headers.width}px;` : '');
  const left = heldAcross ? box.left - state.scrollLeft : box.left;
  const top = heldDown ? box.top - state.scrollTop : box.top;
  const { height } = box;
  // As wide as the text in it needs, once that has been measured.
  const width = fitWidth ?? editorWidth(state, col, row, 0);

  // The Lumo theme puts every cell in its own font, which the cell's
  // styles would undo here: they reach the editor inline, past anything a
  // stylesheet says. So the theme's font goes last - and the sheet's text
  // colour, which Lumo's editor writes in whatever colour the cell is.
  const style =
    `left:${left}px;top:${top}px;width:${width}px;height:${height}px;` +
    editorCellStyle(state, col, row) +
    (lumo
      ? '--default-font-family:var(--lumo-font-family);--default-font-size:var(--lumo-font-size-s);' +
        'color:var(--default-color);'
      : '');

  return html`
    <div class="cell-editor-holder" style=${hold}>
      <input
        class="cell-editor"
        part="cell-input"
        aria-label=${`Edit cell ${cellCoordToAddress({ col, row })}`}
        .value=${initialValue}
        style=${style}
        @keydown=${(e: KeyboardEvent) => editorKeyDown(e, editor)}
        @keyup=${caretMoved}
        @mouseup=${caretMoved}
        @input=${onInput}
        @blur=${(e: FocusEvent) => editorLeft(e, editor)}
      />
    </div>
  `;
}

/** What the editor needs of the grid it is drawn in. */
export interface CellEditorHost {
  readonly renderState: CellGridRenderState;
  readonly theme: string | null;
  readonly shadowRoot: ShadowRoot | null;
  dispatchEvent(event: Event): boolean;
}

/**
 * The cell editor: the input over the cell being edited, dressed as the
 * cell, as wide as the text in it needs, given the keyboard as it appears,
 * and telling the grid's listeners what is typed and how the edit ends. The
 * grid calls {@link render} and {@link updated} in its own update.
 */
export class CellEditor {
  /**
   * How wide the editor is for the text in it, and on which cell: measured
   * when it opens and as the user types, and kept so that drawing the sheet
   * again - on a scroll, say - does not shrink it back to its cell.
   */
  private fit: { key: string; width: number } | null = null;
  /** The editor input the keyboard was last given to - see updated(). */
  private focused: HTMLInputElement | null = null;

  /**
   * @param measure how wide a value is drawn in a cell style, in pixels
   */
  constructor(
    private readonly host: CellEditorHost,
    private readonly measure: (cellStyle: string, value: string) => number
  ) {}

  /** The editor to draw over the cell being edited, or nothing. */
  render(): TemplateResult | string {
    return cellEditorInput(this.host.renderState, isLumo(this.host.theme), this.fitWidth(), this.typed);
  }

  /**
   * After drawing: an editor just opened is as wide as the value it opens
   * with, and gets the keyboard. The browser's autofocus works only once, on
   * insertion, so this focuses on every move into editing, which also puts
   * the caret in an editor opened at another cell.
   */
  updated(): void {
    if (!this.host.renderState.editor) {
      // The next edit starts from its cell again, and no input has the
      // keyboard: none is drawn, so there is nothing to look up either.
      this.fit = null;
      this.focused = null;
      return;
    }
    if (this.fitWidth() === undefined) {
      // Just opened: as wide as the value it opens with.
      const input = this.input();
      const width = input === null ? null : this.fitTo(input.value);
      if (input !== null && width !== null) {
        input.style.width = `${width}px`;
      }
    }
    // The keyboard goes to an editor as it appears, and not again on every
    // repaint: the user may have taken it to the formula field to carry the
    // edit on there.
    const input = this.input();
    if (input !== null && input !== this.focused) {
      input.focus();
      // Caret at the end so typing extends the existing value (Excel
      // convention), except before a trailing percent sign: a cell opened
      // on "5%" has to take the next digit into the number, not after the
      // sign.
      const caret = caretPosition(input.value);
      input.setSelectionRange(caret, caret);
      // What it opened on, so that a formula's references are coloured
      // from the start.
      input.dispatchEvent(
        new CustomEvent('cell-editor-caret', {
          detail: { value: input.value, caret },
          bubbles: true,
          composed: true
        })
      );
    }
    this.focused = input;
  }

  /** What the open editor holds, or null when none is open. */
  value(): string | null {
    return this.input()?.value ?? null;
  }

  /**
   * Puts a value typed elsewhere - into the formula field, carrying the edit
   * on there - into the open editor, which widens or narrows to it.
   */
  setValue(value: string, caret?: number): void {
    const input = this.input();
    if (input === null) {
      return;
    }
    input.value = value;
    if (caret !== undefined) {
      input.setSelectionRange(caret, caret);
    }
    const width = this.fitTo(value);
    if (width !== null) {
      input.style.width = `${width}px`;
    }
  }

  /** The editor's input, while one is drawn. */
  private input(): HTMLInputElement | null {
    return this.host.shadowRoot?.querySelector<HTMLInputElement>('.cell-editor') ?? null;
  }

  /** The editor's measured width, while it is the editor open now. */
  private fitWidth(): number | undefined {
    const editor = this.host.renderState.editor;
    const fit = this.fit;
    return editor && fit?.key === toKey(editor.col, editor.row) ? fit.width : undefined;
  }

  /**
   * Measures text in the edited cell's style, and keeps the width the
   * editor needs for it.
   */
  private fitTo(value: string): number | null {
    const state = this.host.renderState;
    const editor = state.editor;
    if (!editor) {
      return null;
    }
    const { col, row } = editor;
    const cellStyle = state.cellData.get(toKey(col, row))?.cellStyle ?? 'cs0';
    const width = editorWidth(state, col, row, this.measure(cellStyle, value));
    this.fit = { key: toKey(col, row), width };
    return width;
  }

  /**
   * Widens or narrows the editor to the text typed into it, and says what
   * it now holds: the formula field shows it as it is typed.
   */
  private readonly typed = (e: Event): void => {
    const input = e.currentTarget as HTMLInputElement;
    const width = this.fitTo(input.value);
    if (width !== null) {
      input.style.width = `${width}px`;
    }
    const editor = this.host.renderState.editor;
    if (editor) {
      this.host.dispatchEvent(
        new CustomEvent('cell-editor-input', {
          detail: { col: editor.col, row: editor.row, value: input.value, caret: input.selectionStart ?? 0 },
          bubbles: true,
          composed: true
        })
      );
    }
  };
}
