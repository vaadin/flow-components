/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { html, nothing, type TemplateResult } from 'lit';
import { SpreadsheetPart } from './SpreadsheetPart.js';
import { cellTarget } from '../dom/IndexedTarget.js';
import { isFormula } from '../algorithms/FormulaPointing.js';
import { composing } from '../algorithms/KeyboardMoves.js';

import { parseColRow, toKey } from '../algorithms/CellKeys.js';
import { cellCoordToAddress } from '../algorithms/CellRef.js';
import { Memo, sameInputs } from '../algorithms/Memo.js';
import { caretPosition } from '../algorithms/PercentageEntry.js';
import type { CellData } from '../types/wire.js';
import { ANIMATE_IN_CLASS, animateIn, animateOut, overlayAnimationStyles } from '../dom/OverlayAnimation.js';
import { type CellGridRenderState, emptyCellGridRenderState } from './types.js';
import {
  cellBoxOnGrid,
  columnWidthOf,
  drawnCellBox,
  drawnRanges,
  frozenExtentOf,
  type QuadrantName,
  quadrantOf,
  type QuadrantRenderOpts,
  quadrants
} from './cell-grid/GridGeometry.js';
import { buildCellStyleBlock, editorCellStyle, styleRuleInputs } from './cell-grid/CellStyleRules.js';
import { cellGridLumoStyles, cellGridStyles } from './cell-grid/CellGridStyles.js';
import { buildFormulaReferences, buildPaintOutline, buildSelectionOverlay } from './cell-grid/RangeOverlays.js';
import { buildPopupButtons, buildSheetOverlays } from './cell-grid/SheetOverlays.js';
import { buildMergedCellOverlays, renderQuadrant } from './cell-grid/QuadrantCells.js';

/**
 * Internal cell-grid Lit element. Renders absolutely-positioned cell
 * divs for the visible viewport portion of a spreadsheet.
 *
 * The element is driven by its {@link renderState} property; of its
 * own it keeps only what it measures (text that runs out of its cell,
 * the editor's width) and the cell under the pointer, and it talks to
 * the connector only through events.
 * The Spreadsheet connector builds a fresh {@code CellGridRenderState}
 * on every scroll / data update / selection change and assigns it
 * here. Lit's diff handles cell-element churn.
 *
 * CSS class names on rendered cells follow the wire-format
 * convention from {@link toKey}: each cell carries
 * {@code class="cell col<C> row<R>"}. This is byte-equivalent to
 * the existing TestBench module's selector expectations and to the
 * pre-existing DOM contract.
 *
 * Properties are declared via Lit's {@code static properties} API
 * rather than TS decorators because the IT module's test runner
 * (esbuild plugin without {@code experimentalDecorators}) doesn't
 * parse the legacy decorator syntax. The static-properties API is
 * the equivalent + portable.
 */
export class SpreadsheetCellGrid extends SpreadsheetPart {
  static readonly EXPORTED_PARTS =
    'cell,cell-range,cell-input,comment-triangle,comment-overlay,comment-author,comment-text,comment-edit,' +
    'invalid-triangle,popup-button,selection-corner';

  static properties = {
    renderState: { attribute: false },
    tooNarrowCells: { attribute: false },
    fittingSpills: { attribute: false },
    theme: { type: String }
  };

  static styles = [overlayAnimationStyles, cellGridStyles, cellGridLumoStyles];

  /**
   * State driving the render. The connector assigns a fresh object
   * here; Lit's property change detection triggers re-render.
   * Defaults to an empty viewport so the element is safe to insert
   * + measure before any state has been pushed.
   *
   * Note: declared with {@code declare} (no initializer) so the
   * generated class field doesn't shadow Lit's reactive accessor.
   * Initialization happens in the constructor instead.
   */
  declare renderState: CellGridRenderState;
  /**
   * Cells whose value really is too wide for its column, by key.
   *
   * The server flags candidates with {@code needsMeasure}, but it judges
   * width by one character's width times the character count, which is too
   * coarse to decide on: it calls a date that fits too wide. So the flagged
   * ones - a handful at most - are measured here before their value is
   * replaced with hashes.
   */
  declare tooNarrowCells: ReadonlySet<string>;
  /** Cells given a run-on width whose value fits after all - see judgeDrawnCells. */
  declare fittingSpills: ReadonlySet<string>;
  /**
   * The theme variant the connector copies from the host. A property, not
   * only an attribute the stylesheet reads, because the editor's inline
   * style follows it too.
   */
  declare theme: string | null;
  /** Hidden span the measuring is done in. */
  private valueProbe: HTMLSpanElement | null = null;
  /**
   * Widths already measured, by cell style and value. The sheet draws again
   * on every scroll step and key press, and measuring every flagged cell
   * each time - a forced layout per cell - held a large workbook up for a
   * third of a second a frame. A value is measured once now.
   */
  private readonly measuredWidths = new Map<string, number>();
  /** The style tables and theme the measured widths hold for. */
  private measuredWith: readonly unknown[] = [];
  /** The width measured for each cell's data, while the styles it was measured in hold. */
  private widthByCell = new WeakMap<CellData, number>();
  /**
   * The style tables and theme the rules in the page were drawn with: the
   * ones values can be measured in before the next drawing.
   */
  private stylesDrawn: readonly unknown[] | null = null;
  /** Whether this update judged the drawn cells before drawing them. */
  private judgedBeforeDrawing = false;
  /** The style rules written from the workbook's styles, and what they were written from. */
  private readonly styleRules = new Memo<TemplateResult | string>();
  /**
   * How wide the editor is for the text in it, and on which cell: measured
   * when it opens and as the user types, and kept so that drawing the sheet
   * again - on a scroll, say - does not shrink it back to its cell.
   */
  private editorFit: { key: string; width: number } | null = null;
  /** The editor input the keyboard was last given to - see updated(). */
  private focusedEditor: HTMLInputElement | null = null;
  /** The cells whose notes were drawn last time, so only new ones drop in. */
  private noteKeysShown: ReadonlySet<string> = new Set();

  constructor() {
    super();
    this.renderState = emptyCellGridRenderState();
    this.tooNarrowCells = new Set();
    this.fittingSpills = new Set();
  }

  /**
   * Wire up click delegation on first render — the shadow root
   * doesn't exist until Lit has produced its first render. A
   * single listener at the shadow-root level finds the clicked
   * `.cell` ancestor and dispatches a {@code cell-click} custom
   * event with the cell's coordinate + modifier-key state. The
   * connector listens for this event to update its selection
   * state + push a fresh render.
   */
  firstUpdated(): void {
    this.shadowRoot!.addEventListener('click', this.handleShadowClick);
    // Double-click delegation for cell-editor activation.
    // Same pattern as the single-click handler;
    // dispatches `cell-doubleclick` so the connector can open
    // its editor at the dbl-clicked cell.
    this.shadowRoot!.addEventListener('dblclick', this.handleShadowDoubleClick);
    // Mouse drag selection. The cell-grid emits
    // `cell-mousedown` + `cell-mouseover`; the connector
    // tracks a dragging flag + extends the range as the
    // mouse moves over new cells. mouseup is bound at the
    // document level by the connector so a drag that ends
    // outside the spreadsheet still terminates cleanly.
    this.shadowRoot!.addEventListener('mousedown', this.handleShadowMouseDown);
    this.shadowRoot!.addEventListener('mouseover', this.handleShadowMouseOver);
    // mouseleave on the grid itself: mouseout fires between cells too, and
    // would flicker the comment off and on as the pointer crosses a border.
    this.addEventListener('mouseleave', this.handleGridMouseLeave);
    // Right-click delegation. preventDefault stops
    // the browser's native context menu so the server-supplied
    // ContextMenu component is the only one shown. Dispatches
    // `cell-contextmenu` with the cell coordinate; the connector
    // selects the cell (if outside selection) and forwards as
    // contextMenuOpenOnSelection.
    this.shadowRoot!.addEventListener('contextmenu', this.handleShadowContextMenu);
    // A note being written ends when the user leaves it, or presses
    // Escape. Delegated at the shadow root because the textarea comes
    // and goes with the overlay it lives in.
    this.shadowRoot!.addEventListener('focusout', this.handleCommentEditEnd);
    this.shadowRoot!.addEventListener('keydown', this.handleCommentEditKeyDown);
  }

  /** Report the note as written, once, when it is left. */
  private readonly handleCommentEditEnd = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof HTMLTextAreaElement) || !target.classList.contains('comment-overlay-edit')) {
      return;
    }
    this.dispatchEvent(
      new CustomEvent('cell-comment-commit', {
        detail: { key: target.dataset.key, text: target.value },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Escape ends the note. Every other key belongs to the textarea, and
   * so does the event: without stopping it, typing a note would also be
   * typing into the cell underneath.
   */
  private readonly handleCommentEditKeyDown = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof HTMLTextAreaElement) || !target.classList.contains('comment-overlay-edit')) {
      return;
    }
    e.stopPropagation();
    // Escape dropping a conversion leaves the note open.
    if ((e as KeyboardEvent).key === 'Escape' && !composing(e as KeyboardEvent)) {
      e.preventDefault();
      target.blur();
    }
  };

  /**
   * The render state with this element's own measurements folded in.
   */
  private measuredState(): CellGridRenderState {
    return { ...this.renderState, tooNarrowCells: this.tooNarrowCells, fittingSpills: this.fittingSpills };
  }

  /**
   * Judges the drawn cells the server flagged against their measured width.
   * <ul>
   * <li>A value flagged {@code needsMeasure} that is too wide for its column
   * shows hashes. The server judges width by one character's width times the
   * character count, which is too coarse to replace a value with hashes on:
   * it calls a date that fits too wide.
   * <li>A value given a run-on width that fits its column after all - the
   * probe has the cell's padding - is drawn as an ordinary cell. The
   * server's guess, too wide, drew a right-aligned number from the cell's
   * left edge as though it ran on.
   * </ul>
   * Measured in a probe rather than in the cells themselves: a cell already
   * showing hashes measures as narrow and would flip back.
   * <p>
   * Only the drawn cells are looked at, in one walk for both: the cache
   * holds every cell loaded since the sheet opened, and a smooth scroll
   * draws every frame. A width is found by the cell's data object, which a
   * cell keeps until the server sends it again. A set that comes out as it
   * was is kept, so it asks for no second render.
   */
  private judgeDrawnCells(): void {
    this.forgetWidthsOfOldStyles();
    const state = this.renderState;
    const narrow: JudgedCell[] = [];
    const spills: JudgedCell[] = [];
    const notNarrow: string[] = [];
    const notSpill: string[] = [];
    const unmeasured: CellData[] = [];
    for (const range of drawnRanges(state)) {
      for (let row = range.r1; row <= range.r2; row++) {
        for (let col = range.c1; col <= range.c2; col++) {
          const key = toKey(col, row);
          const data = state.cellData.get(key);
          const columnWidth = data === undefined ? 0 : columnWidthOf(state, col);
          const flaggedNarrow = data !== undefined && columnWidth > 0 && data.needsMeasure === true;
          const flaggedSpill = data !== undefined && columnWidth > 0 && (data.overflowWidth ?? 0) > 0;
          if (flaggedNarrow) {
            narrow.push({ key, data, columnWidth });
          } else if (this.tooNarrowCells.has(key)) {
            notNarrow.push(key);
          }
          if (flaggedSpill) {
            spills.push({ key, data, columnWidth });
          } else if (this.fittingSpills.has(key)) {
            notSpill.push(key);
          }
          if ((flaggedNarrow || flaggedSpill) && !this.widthByCell.has(data)) {
            unmeasured.push(data);
          }
        }
      }
    }
    this.measureAll(unmeasured);
    const tooNarrow = this.judged(
      this.tooNarrowCells,
      narrow,
      notNarrow,
      (columnWidth, textWidth) => textWidth > columnWidth
    );
    const fitting = this.judged(
      this.fittingSpills,
      spills,
      notSpill,
      (columnWidth, textWidth) => textWidth <= columnWidth
    );
    if (tooNarrow !== this.tooNarrowCells) {
      this.tooNarrowCells = tooNarrow;
    }
    if (fitting !== this.fittingSpills) {
      this.fittingSpills = fitting;
    }
  }

  /**
   * The keys that meet a condition: those of the flagged cells that meet it,
   * and of no cell that is drawn without the flag. The same set as before
   * when nothing changed.
   */
  private judged(
    current: ReadonlySet<string>,
    flagged: readonly JudgedCell[],
    unflagged: readonly string[],
    meets: (columnWidth: number, textWidth: number) => boolean
  ): ReadonlySet<string> {
    let next: Set<string> | null = null;
    const changing = (): Set<string> => (next ??= new Set(current));
    for (const key of unflagged) {
      changing().delete(key);
    }
    for (const { key, data, columnWidth } of flagged) {
      const textWidth = this.widthByCell.get(data) ?? this.measureValue(data.cellStyle, data.value ?? '');
      const yes = meets(columnWidth, textWidth);
      if (yes !== current.has(key)) {
        if (yes) {
          changing().add(key);
        } else {
          changing().delete(key);
        }
      }
    }
    return next ?? current;
  }

  /**
   * Measures, in one layout, the values of the given cells not measured yet
   * - one probe for each value, all read after the last is placed - and
   * notes each cell's width. A value measured before, in another cell, is
   * not measured again. The widths stand while the styles they were
   * measured in do.
   */
  private measureAll(cells: readonly CellData[]): void {
    const waiting = new Map<string, CellData[]>();
    for (const cell of cells) {
      const key = widthKey(cell.cellStyle, cell.value ?? '');
      const width = this.measuredWidths.get(key);
      if (width !== undefined) {
        this.widthByCell.set(cell, width);
        continue;
      }
      const alike = waiting.get(key);
      if (alike === undefined) {
        waiting.set(key, [cell]);
      } else {
        alike.push(cell);
      }
    }
    if (waiting.size === 0) {
      return;
    }
    const probes: Array<[string, CellData[], HTMLSpanElement]> = [];
    for (const [key, alike] of waiting) {
      probes.push([key, alike, createValueProbe(alike[0].cellStyle, alike[0].value ?? '')]);
    }
    this.shadowRoot!.append(...probes.map(([, , probe]) => probe));
    for (const [key, alike, probe] of probes) {
      const width = probe.clientWidth;
      this.measuredWidths.set(key, width);
      for (const cell of alike) {
        this.widthByCell.set(cell, width);
      }
    }
    for (const [, , probe] of probes) {
      probe.remove();
    }
  }

  /** Forget the widths measured in styles the grid no longer draws with. */
  private forgetWidthsOfOldStyles(): void {
    const inputs = this.measuringInputs();
    if (!sameInputs(inputs, this.measuredWith) || this.measuredWidths.size > MEASURED_WIDTHS_LIMIT) {
      this.measuredWidths.clear();
      this.widthByCell = new WeakMap();
      this.measuredWith = inputs;
    }
  }

  /** What a value's width depends on besides the value: the style tables and the theme. */
  private measuringInputs(): readonly unknown[] {
    return [this.renderState.cellStyleToCSSStyle, this.renderState.conditionalFormattingStyles, this.theme];
  }

  /** The editor's measured width, while it is the editor open now. */
  private editorFitWidth(): number | undefined {
    const editor = this.renderState.editor;
    const fit = this.editorFit;
    return editor && fit?.key === toKey(editor.col, editor.row) ? fit.width : undefined;
  }

  /**
   * Widens or narrows the editor to the text typed into it, and says what
   * it now holds: the formula field shows it as it is typed.
   */
  private readonly handleEditorInput = (e: Event): void => {
    const input = e.currentTarget as HTMLInputElement;
    const width = this.fitEditor(input.value);
    if (width !== null) {
      input.style.width = `${width}px`;
    }
    const editor = this.renderState.editor;
    if (editor) {
      this.dispatchEvent(
        new CustomEvent('cell-editor-input', {
          detail: { col: editor.col, row: editor.row, value: input.value, caret: input.selectionStart ?? 0 },
          bubbles: true,
          composed: true
        })
      );
    }
  };

  /** What the open editor holds, or null when none is open. */
  editorValue(): string | null {
    return this.shadowRoot?.querySelector<HTMLInputElement>('.cell-editor')?.value ?? null;
  }

  /**
   * Puts a value typed elsewhere - into the formula field, carrying the edit
   * on there - into the open editor, which widens or narrows to it.
   */
  setEditorValue(value: string, caret?: number): void {
    const input = this.shadowRoot?.querySelector<HTMLInputElement>('.cell-editor') ?? null;
    if (input === null) {
      return;
    }
    input.value = value;
    if (caret !== undefined) {
      input.setSelectionRange(caret, caret);
    }
    const width = this.fitEditor(value);
    if (width !== null) {
      input.style.width = `${width}px`;
    }
  }

  /**
   * Measures text in the edited cell's style, and keeps the width the
   * editor needs for it.
   */
  private fitEditor(value: string): number | null {
    const editor = this.renderState.editor;
    if (!editor) {
      return null;
    }
    const { col, row } = editor;
    const cellStyle = this.renderState.cellData.get(toKey(col, row))?.cellStyle ?? 'cs0';
    const width = editorWidth(this.renderState, col, row, this.measureValue(cellStyle, value));
    this.editorFit = { key: toKey(col, row), width };
    return width;
  }

  /** What a value would be drawn at, in its own style. */
  private measureValue(cellStyle: string, value: string): number {
    if (this.valueProbe === null) {
      this.valueProbe = createValueProbe(cellStyle, value);
      this.shadowRoot!.appendChild(this.valueProbe);
    }
    this.valueProbe.className = `cell ${cellStyle}`;
    this.valueProbe.textContent = value;
    return this.valueProbe.clientWidth;
  }

  /**
   * How wide a text is drawn in each of the given cell styles, in pixels:
   * measured in one layout, and leaving nothing behind. 0 each while
   * nothing is laid out - the grid is hidden, or not yet in the page.
   */
  textWidths(cellStyles: readonly string[], text: string): number[] {
    const root = this.shadowRoot;
    if (root === null) {
      return cellStyles.map(() => 0);
    }
    const probes = cellStyles.map((cellStyle) => createValueProbe(cellStyle, text));
    root.append(...probes);
    const widths = probes.map((probe) => probe.clientWidth);
    for (const probe of probes) {
      probe.remove();
    }
    return widths;
  }

  /** What the cell editor holds and where its caret is; null while none is open. */
  editorDraft(): { value: string; caret: number } | null {
    const input = this.shadowRoot?.querySelector<HTMLInputElement>('.cell-editor') ?? null;
    if (input === null) {
      return null;
    }
    return { value: input.value, caret: input.selectionStart ?? input.value.length };
  }

  /** The popup button drawn on a cell, or null when there is none in view. */
  popupButton(col: number, row: number): HTMLElement | null {
    return this.shadowRoot?.querySelector<HTMLElement>(`.popup-button[data-col="${col}"][data-row="${row}"]`) ?? null;
  }

  /**
   * Where a cell is drawn on screen, frozen panes included. Null while the cell is outside the
   * rendered range.
   */
  cellClientRect(col: number, row: number): DOMRect | null {
    const box = cellBoxOnGrid(this.renderState, col, row);
    if (box === null) {
      return null;
    }
    const grid = this.getBoundingClientRect();
    return new DOMRect(grid.left + box.left, grid.top + box.top, box.width, box.height);
  }

  /** How far the frozen columns reach across and the frozen rows down. */
  frozenExtent(): { width: number; height: number } {
    return frozenExtentOf(this.renderState);
  }

  willUpdate(changedProps: Map<string, unknown>): void {
    this.fadeOutLeavingNotes();
    // Judged before drawing while the rules a value is measured in are
    // already in the page: a probe then lays out on its own, where after a
    // drawing it laid out every new cell with it, and the cells are drawn
    // once, as judged. New styles have to be drawn first.
    this.judgedBeforeDrawing = changedProps.has('renderState') && sameInputs(this.measuringInputs(), this.stylesDrawn);
    if (this.judgedBeforeDrawing) {
      this.judgeDrawnCells();
    }
  }

  /**
   * A note that goes fades out: the sheet drops it at once, and a copy of
   * it, as it was, fades where it was and then goes too. Its line goes at
   * once. A note still
   * dropping into place just goes.
   */
  private fadeOutLeavingNotes(): void {
    const root = this.shadowRoot;
    if (root === null || this.noteKeysShown.size === 0) {
      return;
    }
    const staying = new Set(drawnNotes(this.renderState, this.hoveredKey).map((note) => note.key));
    for (const note of root.querySelectorAll<HTMLElement>('.v-spreadsheet-comment-overlay:not(.animate-out)')) {
      if (
        staying.has(note.dataset.key ?? '') ||
        note.style.display === 'none' ||
        note.classList.contains(ANIMATE_IN_CLASS)
      ) {
        continue;
      }
      // What was typed into a note being written comes along: a copy of a
      // text box keeps its value. A copy for the eye only: given the
      // keyboard, its text box took the keys meant for the sheet - an undo
      // right after a note became an undo of the note's text.
      const copy = note.cloneNode(true) as HTMLElement;
      copy.inert = true;
      root.appendChild(copy);
      animateOut(copy, () => copy.remove());
    }
  }

  /**
   * After every render: measure what the new state needs measured, place
   * the notes, and give the keyboard to the note or editor being written.
   * The browser's autofocus works only once, on insertion, so this focuses
   * on every move into editing, which also puts the caret in an editor
   * opened at another cell.
   */
  updated(changedProps: Map<string, unknown>): void {
    this.stylesDrawn = this.measuringInputs();
    if (changedProps.has('renderState') && !this.judgedBeforeDrawing) {
      this.judgeDrawnCells();
    }
    this.placeCommentOverlays();
    // The note being written, not the copy of one fading out.
    const note = this.shadowRoot!.querySelector(
      '.v-spreadsheet-comment-overlay:not([inert]) .comment-overlay-edit'
    ) as HTMLTextAreaElement | null;
    if (note !== null && this.shadowRoot!.activeElement !== note) {
      note.focus();
      note.setSelectionRange(note.value.length, note.value.length);
    }
    if (!this.renderState.editor) {
      // The next edit starts from its cell again.
      this.editorFit = null;
    } else if (this.editorFitWidth() === undefined) {
      // Just opened: as wide as the value it opens with.
      const input = this.shadowRoot!.querySelector('.cell-editor') as HTMLInputElement | null;
      const width = input === null ? null : this.fitEditor(input.value);
      if (input !== null && width !== null) {
        input.style.width = `${width}px`;
      }
    }
    // The keyboard goes to an editor as it appears, and not again on every
    // repaint: the user may have taken it to the formula field to carry the
    // edit on there.
    const input = this.shadowRoot!.querySelector('.cell-editor') as HTMLInputElement | null;
    if (input !== null && input !== this.focusedEditor) {
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
    this.focusedEditor = input;
  }

  /**
   * Move a note that does not fit its pane, once its size is known: to the
   * left of its cell when it runs past the
   * pane's right edge and fits there, up when it runs past the bottom, and
   * never above the top. The line to it follows. A workbook's
   * always-visible note shows only while its cell's top-right corner is in
   * the pane.
   */
  private placeCommentOverlays(): void {
    // Not the copies of notes that have gone, fading out where they were.
    const notes = this.shadowRoot!.querySelectorAll<HTMLElement>('.v-spreadsheet-comment-overlay:not(.animate-out)');
    // A note drops into place as it appears, and not again each time the
    // sheet repaints.
    const keys = new Set(Array.from(notes, (note) => note.dataset.key ?? ''));
    for (const note of Array.from(notes)) {
      if (!this.noteKeysShown.has(note.dataset.key ?? '')) {
        animateIn(note);
      }
    }
    this.noteKeysShown = keys;
    const container = this.parentElement;
    if (notes.length === 0 || container === null) {
      return;
    }
    const panes = this.notePanes(container);
    const grid = this.getBoundingClientRect();
    for (const note of Array.from(notes)) {
      const pane = panes[(note.dataset.quadrant as QuadrantName) ?? 'main'] ?? panes.main;
      const cellLeft = Number(note.dataset.cellLeft);
      const cellRight = Number(note.dataset.cellRight);
      const cellTop = Number(note.dataset.cellTop);
      const line = note.previousElementSibling as HTMLElement | null;
      const cornerX = grid.left + cellRight;
      const cornerY = grid.top + cellTop;
      const cornerInPane =
        cornerX >= pane.left && cornerX < pane.right && cornerY >= pane.top && cornerY <= pane.bottom;
      const shown = note.dataset.pinned !== 'true' || cornerInPane;
      note.style.display = shown ? '' : 'none';
      if (!shown) {
        if (line !== null && line.classList.contains('comment-overlay-line')) {
          line.style.display = 'none';
        }
        continue;
      }
      const width = note.offsetWidth;
      const height = note.offsetHeight;
      let left = grid.left + cellRight + COMMENT_GAP;
      if (left + width > pane.right) {
        const leftOfCell = grid.left + cellLeft - COMMENT_GAP - width;
        if (pane.left < leftOfCell) {
          left = leftOfCell;
        }
      }
      let top = grid.top + cellTop - COMMENT_GAP;
      if (top + height > pane.bottom) {
        top = Math.max(pane.top, top - (top + height - pane.bottom + 5));
      } else if (top < pane.top) {
        top = pane.top;
      }
      const noteLeft = left - grid.left;
      const noteTop = top - grid.top;
      note.style.left = `${noteLeft}px`;
      note.style.top = `${noteTop}px`;
      if (line !== null && line.classList.contains('comment-overlay-line')) {
        line.style.cssText = commentLineStyle(cellRight, cellTop, noteLeft, noteTop, width);
      }
    }
  }

  /**
   * The panes a note keeps to, on screen:
   * the scrolling pane under the column letters and right of the row
   * numbers, past the frozen rows and columns; a frozen pane takes in the
   * headers along its frozen side.
   */
  private notePanes(
    container: HTMLElement
  ): Record<QuadrantName, { left: number; right: number; top: number; bottom: number }> {
    const box = container.getBoundingClientRect();
    const sheetLeft = box.left + container.clientLeft;
    const sheetTop = box.top + container.clientTop;
    // The grid sits right of the row numbers and under the column letters.
    const splitX = sheetLeft + this.offsetLeft + this.frozenExtent().width;
    const splitY = sheetTop + this.offsetTop + this.frozenExtent().height;
    // The frozen pane takes the pixel of the freeze line too, and the
    // scrolling pane starts past it.
    const gapX = this.renderState.horizontalSplitPosition > 0 ? 1 : 0;
    const gapY = this.renderState.verticalSplitPosition > 0 ? 1 : 0;
    const scrollingX = { left: splitX + gapX, right: box.right };
    const frozenX = { left: sheetLeft, right: splitX + gapX };
    const scrollingY = { top: splitY + gapY, bottom: box.bottom };
    const frozenY = { top: sheetTop, bottom: splitY + gapY };
    return {
      main: { ...scrollingX, ...scrollingY },
      top: { ...scrollingX, ...frozenY },
      left: { ...frozenX, ...scrollingY },
      corner: { ...frozenX, ...frozenY }
    };
  }

  /**
   * Mousedown handler — emits {@code cell-mousedown} so the
   * connector can mark the start of a drag-select. The
   * connector decides whether to consume the event vs let the
   * normal click handler run (Excel: mousedown immediately
   * selects, then drag extends, then mouseup commits the
   * range).
   */
  private readonly handleShadowMouseDown = (e: Event): void => {
    // The right button leaves the selection to the context menu. A press of
    // its own would select the cell under it, and a
    // right-click on a range would lose the range the menu is for.
    if ((e as MouseEvent).button === 2) {
      return;
    }
    // The fill handle sits on top of the cell under it, and a drag from it
    // paints rather than selects — so it is checked first and the event
    // goes no further.
    if (e.target instanceof Element && e.target.closest('.selection-corner')) {
      e.preventDefault();
      this.dispatchEvent(new CustomEvent('selection-paint-start', { bubbles: true, composed: true }));
      return;
    }
    const at = cellTarget(e);
    if (at === null) {
      return;
    }
    const { col, row } = at;
    const ev = e as MouseEvent;
    // Taken to point at the cell while a formula is written, the press
    // must not move the keyboard out of the editor - that would end the
    // edit it is pointing for.
    const taken = !this.dispatchEvent(
      new CustomEvent('cell-mousedown', {
        detail: {
          col,
          row,
          shiftKey: ev.shiftKey,
          ctrlKey: ev.ctrlKey,
          metaKey: ev.metaKey,
          altKey: ev.altKey
        },
        bubbles: true,
        composed: true,
        cancelable: true
      })
    );
    if (taken) {
      ev.preventDefault();
    }
  };

  /** The cell the pointer is over, or null. Presentation-only state. */
  private hoveredKey: string | null = null;

  private setHoveredKey(key: string | null): void {
    if (this.hoveredKey === key) {
      return;
    }
    const before = this.hoveredKey;
    this.hoveredKey = key;
    // Only a cell with a note or an invalid formula shows anything while
    // hovered: moving between other cells changes nothing that is drawn.
    if (this.showsOnHover(before) || this.showsOnHover(key)) {
      this.requestUpdate();
    }
  }

  /** Whether hovering a cell shows something: its note, or its formula's error. */
  private showsOnHover(key: string | null): boolean {
    const { commentText, invalidFormulaCells } = this.renderState;
    return (
      key !== null && ((commentText !== undefined && key in commentText) || invalidFormulaCells?.has(key) === true)
    );
  }

  private readonly handleGridMouseLeave = (): void => {
    this.setHoveredKey(null);
    this.dispatchEvent(new CustomEvent('grid-mouseleave', { bubbles: true, composed: true }));
  };

  /**
   * Mouseover handler — emits {@code cell-mouseover} when the
   * mouse enters a new cell. Fires on every hover (not just
   * during drag); the connector filters by checking its
   * dragging flag.
   */
  private readonly handleShadowMouseOver = (e: Event): void => {
    const at = cellTarget(e);
    if (at === null) {
      return;
    }
    const { cell, col, row } = at;
    // Which cell the pointer is over decides whether a comment shows, and
    // that is presentation only — the connector never needs to know, and
    // routing it through a full render-state push would repaint the sheet
    // on every pointer move.
    this.setHoveredKey(toKey(col, row));
    // Where the cell is on screen, for what is shown beside it: a link's
    // tooltip.
    const box = cell.getBoundingClientRect();
    this.dispatchEvent(
      new CustomEvent('cell-mouseover', {
        detail: { col, row, box },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Double-click handler — opens the cell editor at the clicked
   * coordinate. Same shape as {@link handleShadowClick} but
   * dispatches {@code cell-doubleclick} (no modifier-key
   * metadata since the editor doesn't react to those). Slice
   * 152.
   */
  private readonly handleShadowDoubleClick = (e: Event): void => {
    const at = cellTarget(e);
    if (at === null) {
      return;
    }
    const { col, row } = at;
    this.dispatchEvent(
      new CustomEvent('cell-doubleclick', {
        detail: { col, row },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * Right-click handler — emits {@code cell-contextmenu} with the
   * cell coordinate. preventDefault stops the browser's native
   * context menu so the server-side {@code ContextMenu} is the
   * only menu shown.
   */
  private readonly handleShadowContextMenu = (event: Event): void => {
    const e = event as MouseEvent;
    const at = cellTarget(e);
    if (at === null) {
      return;
    }
    const { col, row } = at;
    // A Ctrl-click on a Mac: the browser reports it as a right-click and
    // fires no click at all. It is the Ctrl-click it is, adding the cell to
    // the selection, so it is reported as one first.
    if (e.button === 0 && e.ctrlKey) {
      this.dispatchEvent(
        new CustomEvent('cell-click', {
          detail: { col, row, shiftKey: e.shiftKey, ctrlKey: true, metaKey: e.metaKey, altKey: e.altKey },
          bubbles: true,
          composed: true
        })
      );
    }
    // The browser's own menu stays unless the sheet takes the right-click
    // for a menu of its own.
    const taken = !this.dispatchEvent(
      new CustomEvent('cell-contextmenu', {
        detail: { col, row, clientX: e.clientX, clientY: e.clientY },
        bubbles: true,
        composed: true,
        cancelable: true
      })
    );
    if (taken) {
      e.preventDefault();
    }
  };

  private readonly handleShadowClick = (e: Event): void => {
    const ev = e as MouseEvent;
    // Nothing for an event from the shadow root itself - a synthetic one
    // dispatched there - which is no cell.
    const at = cellTarget(e);
    if (at === null) {
      return;
    }
    const { col, row } = at;
    this.dispatchEvent(
      new CustomEvent('cell-click', {
        detail: {
          col,
          row,
          shiftKey: ev.shiftKey,
          ctrlKey: ev.ctrlKey,
          metaKey: ev.metaKey,
          altKey: ev.altKey
        },
        bubbles: true,
        composed: true
      })
    );
  };

  render(): TemplateResult {
    // The scrolling pane, then the frozen ones there are. None while nothing
    // is measured: the connector may push an empty state while it measures.
    const [main, ...frozen] = quadrants(this.measuredState());
    if (main === undefined) {
      return html``;
    }

    // The scrolling pane's cells are direct children of the cell-grid, so
    // they sit at their sheet coordinates and scroll with the scroll
    // container.
    const mainCells = renderQuadrant(main);
    // Straight after the cells they cover, so the frozen quadrants, which
    // come later at the same z-index, stay on top of a merge scrolling
    // under them. The selection, the fill outline, the pictures and the
    // filter buttons of the scrolling quadrant go there too, for the same
    // reason: under a frozen pane they scroll out of sight, instead of
    // painting over it.
    const mainMerged = buildMergedCellOverlays(main);
    const mainOverlays = this.quadrantOverlays(main);
    const pane = (name: QuadrantName): TemplateResult | string => {
      const quadrant = frozen.find((q) => q.quadrantName === name);
      return quadrant === undefined ? '' : this.frozenPane(quadrant);
    };
    const top = pane('top');
    const left = pane('left');
    const corner = pane('corner');

    // Written again only when what they are written from changes, not on
    // every drawing.
    const state = this.renderState;
    const styleBlock = this.styleRules.get(styleRuleInputs(state), () => buildCellStyleBlock(state));
    // The notes and the editor float above every pane, so they stay at
    // grid level, placed where their cell is on screen.
    const editor = buildCellEditorOverlay(
      this.renderState,
      isLumo(this.theme),
      this.editorFitWidth(),
      this.handleEditorInput
    );
    const commentOverlays = buildCommentOverlays(this.renderState, this.hoveredKey);
    // A hidden stand-in for the editor, an input that is always there:
    // TestBench hands it out while no cell is being edited, so tests
    // that ask whether the editor is shown keep working.
    const standIn = html`<input class="cell-input-stand-in" hidden inert tabindex="-1" aria-hidden="true" />`;
    return html`${styleBlock}${mainCells}${mainMerged}${mainOverlays}${top}${left}${corner}${commentOverlays}${editor}${standIn}`;
  }

  /**
   * A frozen pane, in a wrapper moved along with the scroll so that its
   * cells stay pinned to their edge of the view: the wrapper's transform
   * counters the scroll container's shift, and the cells are placed in the
   * wrapper, which is anchored at the grid's origin. The wrappers stack the
   * corner over the top and left panes, and those over the scrolling one.
   *
   * Each draws the line along its freeze. The left pane's cells cover the
   * top pane's line where the frozen columns are, and the top pane's cover
   * the left pane's; the corner sits above both and draws the two pieces
   * they hide.
   */
  private frozenPane(quadrant: QuadrantRenderOpts): TemplateResult {
    const { state, quadrantName } = quadrant;
    const { scrollLeft, scrollTop } = state;
    const { width, height } = frozenExtentOf(state);
    let transform: string;
    let lines: TemplateResult;
    if (quadrantName === 'top') {
      transform = `transform:translateY(${scrollTop}px);`;
      lines = freezeLine('rows', height, state.totalGridWidthPx);
    } else if (quadrantName === 'left') {
      transform = `transform:translateX(${scrollLeft}px);`;
      lines = freezeLine('columns', width, state.totalGridHeightPx);
    } else {
      transform = `transform:translate(${scrollLeft}px, ${scrollTop}px);`;
      lines = html`${freezeLine('rows', height, width + 1)}${freezeLine('columns', width, height + 1)}`;
    }
    return html`
      <div class="quadrant-wrapper quadrant-wrapper-${quadrantName}" style=${transform}>
        ${renderQuadrant(quadrant)}${buildMergedCellOverlays(quadrant)}${lines}${this.quadrantOverlays(quadrant)}
      </div>
    `;
  }

  /**
   * What a quadrant draws over its cells: the pictures anchored in it, its
   * piece of the selection and of the fill outline, and its filter buttons
   * - each in its own pane.
   *
   * All on the same level, so the order is the stacking: the selection goes
   * over the pictures.
   */
  private quadrantOverlays(opts: QuadrantRenderOpts): TemplateResult {
    const state = opts.state;
    return html`${buildSheetOverlays(state, opts.quadrantName)}${buildSelectionOverlay(opts)}${buildPaintOutline(
      opts
    )}${buildFormulaReferences(opts)}${buildPopupButtons(state, opts.quadrantName)}`;
  }
}

/**
 * The line along a freeze: the dark border of a frozen pane, one pixel past
 * the last frozen row or column.
 */
function freezeLine(along: 'rows' | 'columns', at: number, length: number): TemplateResult {
  const style =
    along === 'rows'
      ? `top:${at}px;left:0;width:${length}px;height:1px;`
      : `left:${at}px;top:0;width:1px;height:${length}px;`;
  return html`<div class="freeze-line" style=${style}></div>`;
}

/** A value probe: out of flow and off-screen, so measuring never disturbs the sheet. */
const VALUE_PROBE_STYLE = 'position:absolute;left:-10000px;top:0;visibility:hidden;width:auto;white-space:nowrap;';

/** A span that draws a value as a cell in the given style would, to measure it in. */
function createValueProbe(cellStyle: string, value: string): HTMLSpanElement {
  const probe = document.createElement('span');
  probe.style.cssText = VALUE_PROBE_STYLE;
  probe.className = `cell ${cellStyle}`;
  probe.textContent = value;
  return probe;
}

/** How many measured widths are kept before starting over. */
const MEASURED_WIDTHS_LIMIT = 50000;

/** A drawn cell the server flagged, with its key and the width of its column. */
interface JudgedCell {
  key: string;
  data: CellData;
  columnWidth: number;
}

/** The key a measured width is kept under. */
function widthKey(cellStyle: string, value: string): string {
  return `${cellStyle}\u0000${value}`;
}

/**
 * How wide the editor is for text drawn at a width: its cell - the whole merged cell when the cell starts one - and then
 * the columns after it, whole ones, until the text fits, and a pixel more,
 * over the gridline at its right. Only the columns drawn count; past them
 * the editor is out of sight anyway, and the text scrolls inside it.
 */
function editorWidth(state: CellGridRenderState, col: number, row: number, textWidth: number): number {
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

/** How far from its cell a note hangs, across and up. */
const COMMENT_GAP = 15;

/**
 * The line from a cell's top-right corner to its note: to the note's top-left corner when the note is to the right, to
 * its top-right corner when it had to go to the left, two pixels in for the
 * rounded corner. A one-pixel bar, turned about its start.
 */
function commentLineStyle(
  cellRight: number,
  cellTop: number,
  noteLeft: number,
  noteTop: number,
  noteWidth: number
): string {
  const targetTop = noteTop + 2;
  let across: number;
  let down = cellTop - targetTop;
  let degrees: number;
  if (noteLeft > cellRight) {
    across = noteLeft + 2 - cellRight;
    if (down > 0) {
      degrees = -(Math.atan(down / across) * 180) / Math.PI;
    } else {
      down = Math.abs(down);
      degrees = 0;
    }
  } else {
    across = cellRight - (noteLeft + noteWidth);
    if (down > 0) {
      degrees = -180 + (Math.atan(down / across) * 180) / Math.PI;
    } else {
      down = Math.abs(down);
      degrees = -180;
    }
  }
  const length = Math.sqrt(across * across + down * down) + 1;
  return `left:${cellRight}px;top:${cellTop}px;width:${length}px;transform:rotate(${degrees}deg);`;
}

/** A note on screen: whose it is, and what it shows. */
interface DrawnNote {
  key: string;
  coord: { col: number; row: number };
  showsError: boolean;
  editing: boolean;
  comment: string | undefined;
  geometry: { left: number; top: number; width: number; height: number };
}

/** The notes on screen, in the order they are drawn. */
function drawnNotes(state: CellGridRenderState, hoveredKey: string | null): DrawnNote[] {
  const text = state.commentText ?? {};
  const invalidCells = state.invalidFormulaCells ?? new Set<string>();
  const invalidMessage = state.invalidFormulaMessage ?? null;

  const editingKey = state.editingCommentKey ?? null;
  const keys = new Set<string>(state.visibleCommentKeys ?? []);
  if (hoveredKey !== null && (hoveredKey in text || invalidCells.has(hoveredKey))) {
    keys.add(hoveredKey);
  }
  // The note being written stays up whether or not it is hovered, and
  // whether or not it has any text yet.
  if (editingKey !== null) {
    keys.add(editingKey);
  }

  const notes: DrawnNote[] = [];
  for (const key of keys) {
    const coord = parseColRow(key);
    if (coord.col < 1 || coord.row < 1) {
      continue;
    }
    // An invalid formula only shows while hovered — an always-visible
    // comment is the workbook's choice, an error message is feedback.
    const showsError = key === hoveredKey && invalidCells.has(key) && invalidMessage !== null;
    const editing = key === editingKey;
    const comment = text[key];
    if (!showsError && !editing && comment === undefined) {
      continue;
    }
    // On screen, frozen panes included: the note floats above every pane,
    // so it is drawn at grid level wherever its cell is.
    const geometry = drawnCellBox(state, coord.col, coord.row);
    if (geometry === null) {
      continue;
    }
    notes.push({ key, coord, showsError, editing, comment, geometry });
  }
  return notes;
}

/**
 * Comment overlays — the little yellow notes.
 *
 * Two things put one on screen: the workbook marking a comment "always
 * visible", and the pointer resting on a cell that has one. A cell whose
 * formula the server rejected shows the same overlay carrying the error
 * instead, which is why one function renders both.
 *
 * Class names are those of earlier versions (`comment-overlay-label`,
 * `comment-overlay-author`, `comment-overlay-invalidformula`). A page's
 * styles reached them while notes were drawn outside the component's
 * shadow root; here they are two shadow roots in, so the card,
 * its author and its text are the parts `comment-overlay`, `comment-author`
 * and `comment-text`, which is what an application styles now.
 */
function buildCommentOverlays(state: CellGridRenderState, hoveredKey: string | null): TemplateResult[] {
  const authors = state.commentAuthors ?? {};
  const invalidMessage = state.invalidFormulaMessage ?? null;

  const overlays: TemplateResult[] = [];
  for (const { key, coord, showsError, editing, comment, geometry } of drawnNotes(state, hoveredKey)) {
    // The pane it keeps to, and whether it is one of the workbook's
    // always-visible notes, which show only while their cell's corner is
    // in that pane - see placeCommentOverlays.
    const quadrant = quadrantOf(coord.col, coord.row, state.horizontalSplitPosition, state.verticalSplitPosition);
    const pinned = key !== hoveredKey && !editing;
    // Where a note hangs: 15px right of the cell and 15px
    // above it, with a line from the cell's corner to the note's. Moved to
    // the cell's left, or down, once the note's size is known, should it
    // not fit the sheet - see placeCommentOverlays.
    const cellRight = geometry.left + geometry.width;
    const style = `left:${cellRight + COMMENT_GAP}px;top:${geometry.top - COMMENT_GAP}px;position:absolute;`;
    overlays.push(
      html`<div
        class="comment-overlay-line"
        style=${commentLineStyle(cellRight, geometry.top, cellRight + COMMENT_GAP, geometry.top - COMMENT_GAP, 0)}
      ></div>`
    );
    const author = authors[key];
    overlays.push(html`
      <div
        class="v-spreadsheet-comment-overlay"
        part="comment-overlay"
        style=${style}
        data-cell-left=${geometry.left}
        data-cell-right=${cellRight}
        data-cell-top=${geometry.top}
        data-quadrant=${quadrant}
        data-pinned=${pinned ? 'true' : 'false'}
        data-key=${key}
      >
        ${showsError ? html`<div class="comment-overlay-invalidformula">${invalidMessage}</div>` : nothing}
        ${showsError && (editing || comment !== undefined)
          ? html`<div class="comment-overlay-separator"></div>`
          : nothing}
        ${author ? html`<div class="comment-overlay-author" part="comment-author">${author}</div>` : nothing}
        ${editing
          ? html`<textarea
              class="comment-overlay-edit"
              part="comment-edit"
              data-key=${key}
              .value=${comment ?? ''}
            ></textarea>`
          : comment === undefined
          ? nothing
          : html`<div class="comment-overlay-label" part="comment-text">${comment}</div>`}
      </div>
    `);
  }
  return overlays;
}

/** Whether a theme attribute's value names the Lumo variant. */
function isLumo(theme: string | null | undefined): boolean {
  return (theme ?? '').split(/\s+/).includes('lumo');
}

/**
 * Build the cell-editor overlay {@code <input>} when {@code state
 * .editor} is non-null. The input is sized to match
 * the cell's pixel box using the appropriate quadrant arrays:
 *
 *  - col within frozen cols: use {@code frozenColX} / {@code
 * frozenColWidths}
 *  - col within viewport: use {@code colX} / {@code colWidths}
 *  - row within frozen rows: use {@code frozenRowY} / {@code
 * frozenRowHeights}
 *  - row within viewport: use {@code rowY} / {@code rowHeights}
 *
 * When the editor's coordinate falls outside both the viewport
 * and the frozen ranges, returns empty (defensive — the
 * connector should never push an editor at an off-screen cell;
 * this keeps the render safe regardless).
 *
 * On keydown:
 * Enter / Tab → dispatch {@code cell-editor-commit} with the
 * current input value; the connector dispatches
 *     `cellValueEdited` + clears editor state.
 * Escape       → dispatch {@code cell-editor-cancel};
 * connector clears editor state without
 * dispatching cellValueEdited.
 * Blur          → same as commit (Excel convention).
 *
 * The detail event payload is {@code { col, row, value }} so the
 * connector can route the value to the correct cell without
 * re-querying its state.
 */
function buildCellEditorOverlay(
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

  // Over the cell where it is on screen: a frozen cell moves with the view,
  // and an editor placed by the frozen arrays alone was left behind as soon
  // as the sheet scrolled. Over the whole of a merged cell.
  const box = drawnCellBox(state, col, row);
  if (box === null || box.width === 0 || box.height === 0) {
    return '';
  }
  const { left, top, height } = box;
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

  const dispatchEditorEvent = (
    target: EventTarget,
    type: 'cell-editor-commit' | 'cell-editor-cancel',
    value: string,
    key?: string,
    shiftKey?: boolean,
    relatedTarget?: EventTarget | null
  ): void => {
    // Closing the editor takes the input out of the document, and losing
    // the focus that way fires blur — which would commit a second time.
    // One edit, one `cellValueEdited`.
    if (target instanceof HTMLElement) {
      target.dataset.editorClosed = 'true';
    }
    const proceeds = target.dispatchEvent(
      new CustomEvent(type, {
        // Include the key + shift modifier so the connector can
        // pick the next active-cell direction after commit
        // (Excel convention: Enter moves down, Tab moves right;
        // shift reverses each). Left for somewhere, where
        // to: the edit carries on in the formula field, so the
        // connector stops the commit then and the editor stays open.
        detail: { col, row, value, key, shiftKey, relatedTarget },
        bubbles: true,
        composed: true,
        cancelable: true
      })
    );
    if (!proceeds && target instanceof HTMLElement) {
      delete target.dataset.editorClosed;
    }
  };

  const ARROW_KEYS = ['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'];

  const onKeyDown = (e: KeyboardEvent): void => {
    const input = e.currentTarget as HTMLInputElement;
    if (composing(e)) {
      // The key is the input method's: confirming a conversion with Enter
      // neither commits nor moves on.
      e.stopPropagation();
      return;
    }
    if (e.key === 'Enter' || e.key === 'Tab') {
      e.preventDefault();
      dispatchEditorEvent(input, 'cell-editor-commit', input.value, e.key, e.shiftKey);
    } else if (e.key === 'Escape') {
      e.preventDefault();
      dispatchEditorEvent(input, 'cell-editor-cancel', input.value);
    } else if (ARROW_KEYS.includes(e.key)) {
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
        dispatchEditorEvent(input, 'cell-editor-commit', input.value, e.key, e.shiftKey);
      }
    }
    // Other keys (typing) are NOT stopped so the input receives them.
    e.stopPropagation();
  };

  // Where the caret is, for the reference it is in to be outlined.
  const onCaret = (e: Event): void => {
    const input = e.currentTarget as HTMLInputElement;
    input.dispatchEvent(
      new CustomEvent('cell-editor-caret', {
        detail: { value: input.value, caret: input.selectionStart ?? 0 },
        bubbles: true,
        composed: true
      })
    );
  };

  const onBlur = (e: FocusEvent): void => {
    const input = e.currentTarget as HTMLInputElement;
    if (input.dataset.editorClosed === 'true') {
      // Already committed or cancelled by a key; this blur is the
      // editor being taken off the sheet, not the user leaving it.
      return;
    }
    // Blur commit doesn't pass key/shift — the connector
    // leaves the selection at the committed cell.
    dispatchEditorEvent(input, 'cell-editor-commit', input.value, undefined, undefined, e.relatedTarget);
  };

  return html`
    <input
      class="cell-editor"
      part="cell-input"
      aria-label=${`Edit cell ${cellCoordToAddress({ col, row })}`}
      .value=${initialValue}
      style=${style}
      @keydown=${onKeyDown}
      @keyup=${onCaret}
      @mouseup=${onCaret}
      @input=${onInput}
      @blur=${onBlur}
    />
  `;
}

customElements.define('vaadin-spreadsheet-internal-cell-grid', SpreadsheetCellGrid);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-cell-grid': SpreadsheetCellGrid;
  }
}
