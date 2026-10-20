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
import { parseColRow } from '../../algorithms/CellKeys.js';
import { composing } from '../../algorithms/KeyboardMoves.js';
import { ANIMATE_IN_CLASS, animateIn, animateOut } from '../../dom/OverlayAnimation.js';
import type { CellGridRenderState } from '../types.js';
import { drawnCellBox, frozenExtentOf, type QuadrantName, quadrantOf } from './GridGeometry.js';

/** How far from its cell a note hangs, across and up. */
export const COMMENT_GAP = 15;

/**
 * The line from a cell's top-right corner to its note: to the note's top-left corner when the note is to the right, to
 * its top-right corner when it had to go to the left, two pixels in for the
 * rounded corner. A one-pixel bar, turned about its start.
 */
export function commentLineStyle(
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
export interface DrawnNote {
  key: string;
  coord: { col: number; row: number };
  showsError: boolean;
  editing: boolean;
  comment: string | undefined;
  geometry: { left: number; top: number; width: number; height: number };
}

/** The notes on screen, in the order they are drawn. */
export function drawnNotes(state: CellGridRenderState, hoveredKey: string | null): DrawnNote[] {
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
export function buildCommentOverlays(
  state: CellGridRenderState,
  notes: readonly DrawnNote[],
  hoveredKey: string | null
): TemplateResult[] {
  const authors = state.commentAuthors ?? {};
  const invalidMessage = state.invalidFormulaMessage ?? null;
  const headers = state.headerExtent ?? { width: 0, height: 0 };

  const overlays: TemplateResult[] = [];
  for (const { key, coord, showsError, editing, comment, geometry } of notes) {
    // The pane it keeps to, and whether it is one of the workbook's
    // always-visible notes, which show only while their cell's corner is
    // in that pane - see CommentNotes.place.
    const quadrant = quadrantOf(coord.col, coord.row, state.horizontalSplitPosition, state.verticalSplitPosition);
    const pinned = key !== hoveredKey && !editing;
    // The note and its line are in a holder of no size where the grid
    // starts. On a frozen cell the holder is held in place as the cell's
    // pane is, and the note is placed as the cell is in its pane, so it stays
    // by the cell however fast the sheet scrolls. On a scrolling cell the
    // holder is not held, and the note goes by the cell on the grid.
    const heldAcross = coord.col <= state.horizontalSplitPosition;
    const heldDown = coord.row <= state.verticalSplitPosition;
    const hold = (heldDown ? `top:${headers.height}px;` : '') + (heldAcross ? `left:${headers.width}px;` : '');
    const cellLeft = heldAcross ? geometry.left - state.scrollLeft : geometry.left;
    const cellTop = heldDown ? geometry.top - state.scrollTop : geometry.top;
    // Where a note hangs: 15px right of the cell and 15px
    // above it, with a line from the cell's corner to the note's. Moved to
    // the cell's left, or down, once the note's size is known, should it
    // not fit the sheet - see CommentNotes.place.
    const cellRight = cellLeft + geometry.width;
    const style = `left:${cellRight + COMMENT_GAP}px;top:${cellTop - COMMENT_GAP}px;position:absolute;`;
    const line = html`<div
      class="comment-overlay-line"
      style=${commentLineStyle(cellRight, cellTop, cellRight + COMMENT_GAP, cellTop - COMMENT_GAP, 0)}
    ></div>`;
    const author = authors[key];
    overlays.push(html`
      <div class="comment-holder" style=${hold}>
        ${line}<div
          class="comment-overlay"
          part="comment-overlay"
          style=${style}
          data-cell-left=${cellLeft}
          data-cell-right=${cellRight}
          data-cell-top=${cellTop}
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
      </div>
    `);
  }
  return overlays;
}

/** What the notes need of the grid they are drawn in. */
export interface CommentNotesHost {
  readonly renderState: CellGridRenderState;
  readonly shadowRoot: ShadowRoot | null;
  /** The scroll container the grid is in, which the panes are measured from. */
  readonly parentElement: HTMLElement | null;
  readonly offsetLeft: number;
  readonly offsetTop: number;
  getBoundingClientRect(): DOMRect;
  requestUpdate(): void;
  dispatchEvent(event: Event): boolean;
}

/**
 * The cell grid's notes: which show - the workbook's always-visible ones,
 * the one under the pointer and the one being written - where each is
 * placed once its size is known, the ones that go fading out, and writing
 * one. The grid calls {@link willUpdate}, {@link render} and
 * {@link updated} in its own update, and tells it where the pointer is.
 */
export class CommentNotes {
  /** The cell the pointer is over, or null. Presentation-only state. */
  private hoveredKey: string | null = null;
  /** The notes the next drawing shows, worked out once before it. */
  private notes: readonly DrawnNote[] = [];
  /** The cells whose notes were drawn last time, so only new ones drop in. */
  private noteKeysShown: ReadonlySet<string> = new Set();

  constructor(private readonly host: CommentNotesHost) {}

  /**
   * Listens for a note being written to end: when the user leaves it, or
   * presses Escape. Delegated at the shadow root because the textarea comes
   * and goes with the overlay it lives in.
   */
  listen(root: ShadowRoot): void {
    root.addEventListener('focusout', this.editEnded);
    root.addEventListener('keydown', this.editKeyDown);
  }

  /**
   * The pointer is over a cell, or over none. Which cell decides whether a
   * note shows, and that is presentation only - the connector never needs
   * to know, and routing it through a full render-state push would repaint
   * the sheet on every pointer move.
   */
  hover(key: string | null): void {
    if (this.hoveredKey === key) {
      return;
    }
    const before = this.hoveredKey;
    this.hoveredKey = key;
    // Only a cell with a note or an invalid formula shows anything while
    // hovered: moving between other cells changes nothing that is drawn.
    if (this.showsOnHover(before) || this.showsOnHover(key)) {
      this.host.requestUpdate();
    }
  }

  /** Before drawing: works out the notes to show, and fades out the ones that go. */
  willUpdate(): void {
    this.notes = drawnNotes(this.host.renderState, this.hoveredKey);
    this.fadeOutLeaving();
  }

  /** The notes, and the lines to them, to draw. */
  render(): TemplateResult[] {
    return buildCommentOverlays(this.host.renderState, this.notes, this.hoveredKey);
  }

  /**
   * After drawing: places the notes, and gives the keyboard to the note
   * being written. The browser's autofocus works only once, on insertion,
   * so this focuses on every move into writing one.
   */
  updated(): void {
    // No note drawn now, nor before: nothing to place, and no note to give
    // the keyboard to. The sheet updates on every scroll step, and looking
    // through its every element for notes cost a scroll a few percent.
    if (this.notes.length === 0 && this.noteKeysShown.size === 0) {
      return;
    }
    const root = this.host.shadowRoot!;
    this.place(root);
    // The note being written, not the copy of one fading out.
    const note = root.querySelector(
      '.comment-overlay:not([inert]) .comment-overlay-edit'
    ) as HTMLTextAreaElement | null;
    if (note !== null && root.activeElement !== note) {
      note.focus();
      note.setSelectionRange(note.value.length, note.value.length);
    }
  }

  /** Whether hovering a cell shows something: its note, or its formula's error. */
  private showsOnHover(key: string | null): boolean {
    const { commentText, invalidFormulaCells } = this.host.renderState;
    return (
      key !== null && ((commentText !== undefined && key in commentText) || invalidFormulaCells?.has(key) === true)
    );
  }

  /**
   * A note that goes fades out: the sheet drops it at once, and a copy of
   * it, as it was, fades where it was and then goes too. Its line goes at
   * once. A note still
   * dropping into place just goes.
   */
  private fadeOutLeaving(): void {
    const root = this.host.shadowRoot;
    if (root === null || this.noteKeysShown.size === 0) {
      return;
    }
    const staying = new Set(this.notes.map((note) => note.key));
    for (const note of root.querySelectorAll<HTMLElement>('.comment-overlay:not(.animate-out)')) {
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
      // In a copy of its holder, so that it fades where it was, held as it
      // was.
      const holder = note.parentElement!.cloneNode(false) as HTMLElement;
      holder.appendChild(copy);
      root.appendChild(holder);
      animateOut(copy, () => holder.remove());
    }
  }

  /**
   * Move a note that does not fit its pane, once its size is known: to the
   * left of its cell when it runs past the
   * pane's right edge and fits there, up when it runs past the bottom, and
   * never above the top. The line to it follows. A workbook's
   * always-visible note shows only while its cell's top-right corner is in
   * the pane.
   */
  private place(root: ShadowRoot): void {
    // Not the copies of notes that have gone, fading out where they were.
    const notes = root.querySelectorAll<HTMLElement>('.comment-overlay:not(.animate-out)');
    // A note drops into place as it appears, and not again each time the
    // sheet repaints.
    const keys = new Set(Array.from(notes, (note) => note.dataset.key ?? ''));
    for (const note of Array.from(notes)) {
      if (!this.noteKeysShown.has(note.dataset.key ?? '')) {
        animateIn(note);
      }
    }
    this.noteKeysShown = keys;
    const container = this.host.parentElement;
    if (notes.length === 0 || container === null) {
      return;
    }
    const panes = this.panes(container);
    for (const note of Array.from(notes)) {
      const pane = panes[(note.dataset.quadrant as QuadrantName) ?? 'main'] ?? panes.main;
      // Where the cell is, and where the note goes, in its holder's
      // coordinates.
      const frame = (note.parentElement ?? this.host).getBoundingClientRect();
      const cellLeft = Number(note.dataset.cellLeft);
      const cellRight = Number(note.dataset.cellRight);
      const cellTop = Number(note.dataset.cellTop);
      const line = note.previousElementSibling as HTMLElement | null;
      const cornerX = frame.left + cellRight;
      const cornerY = frame.top + cellTop;
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
      let left = frame.left + cellRight + COMMENT_GAP;
      if (left + width > pane.right) {
        const leftOfCell = frame.left + cellLeft - COMMENT_GAP - width;
        if (pane.left < leftOfCell) {
          left = leftOfCell;
        }
      }
      let top = frame.top + cellTop - COMMENT_GAP;
      if (top + height > pane.bottom) {
        top = Math.max(pane.top, top - (top + height - pane.bottom + 5));
      } else if (top < pane.top) {
        top = pane.top;
      }
      const noteLeft = left - frame.left;
      const noteTop = top - frame.top;
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
  private panes(
    container: HTMLElement
  ): Record<QuadrantName, { left: number; right: number; top: number; bottom: number }> {
    const state = this.host.renderState;
    const frozen = frozenExtentOf(state);
    const box = container.getBoundingClientRect();
    const sheetLeft = box.left + container.clientLeft;
    const sheetTop = box.top + container.clientTop;
    // The grid sits right of the row numbers and under the column letters.
    const splitX = sheetLeft + this.host.offsetLeft + frozen.width;
    const splitY = sheetTop + this.host.offsetTop + frozen.height;
    // The frozen pane takes the pixel of the freeze line too, and the
    // scrolling pane starts past it.
    const gapX = state.horizontalSplitPosition > 0 ? 1 : 0;
    const gapY = state.verticalSplitPosition > 0 ? 1 : 0;
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

  /** Report the note as written, once, when it is left. */
  private readonly editEnded = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof HTMLTextAreaElement) || !target.classList.contains('comment-overlay-edit')) {
      return;
    }
    this.host.dispatchEvent(
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
  private readonly editKeyDown = (e: Event): void => {
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
}
