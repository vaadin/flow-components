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
import { isCoherentSelection } from '../../state/SelectionState.js';
import type { QuadrantRenderOpts } from './GridGeometry.js';

/**
 * The references of the formula being written, as far as this quadrant
 * shows them: each one's cells tinted in its colour at a quarter, and the
 * one the caret is in ringed at three quarters, two pixels inside its
 * edge - drawn over the cells rather than in place of their fill.
 */
export function buildFormulaReferences(opts: QuadrantRenderOpts): TemplateResult[] {
  const out: TemplateResult[] = [];
  for (const ref of opts.state.formulaReferences ?? []) {
    const piece = rangePiece(opts, ref.range);
    if (piece === null) {
      continue;
    }
    const style =
      `left:${piece.left}px;top:${piece.top}px;` +
      `width:${piece.right - piece.left}px;height:${piece.bottom - piece.top}px;` +
      `background:rgba(${ref.color},0.25);` +
      (ref.atCaret ? `box-shadow:inset 0 0 0 2px rgba(${ref.color},0.75);` : '');
    out.push(html`<div class="formula-reference" style=${style}></div>`);
  }
  return out;
}

/**
 * The dashed outline the fill handle drags out.
 *
 * Always rendered, hidden when nothing is being painted: it is the only
 * feedback the user has for where a fill drag will land, and keeping the
 * element in the tree means an observer can ask whether it is showing
 * rather than whether it exists.
 */
export function buildPaintOutline(opts: QuadrantRenderOpts): TemplateResult | string {
  // `?? null` rather than `=== null`: a hand-built render state may leave
  // the field off entirely, and a missing outline is not a crash.
  const rect = opts.state.paintRect ?? null;
  if (rect === null) {
    // Kept in the tree, hidden, while nothing is painted: the scrolling
    // quadrant always has one.
    return opts.quadrantName === 'main' ? html`<div class="paintmode" style="display:none;"></div>` : '';
  }
  const piece = rangePiece(opts, rect);
  if (piece === null) {
    return '';
  }
  // Only the sides the drag ends at: a side that runs on into the next
  // pane, or out of view, has no border.
  const style =
    pieceBox(piece) +
    (piece.hasTop ? '' : 'border-top-width:0;') +
    (piece.hasLeft ? '' : 'border-left-width:0;') +
    (piece.hasRight ? '' : 'border-right-width:0;') +
    (piece.hasBottom ? '' : 'border-bottom-width:0;');
  // Deliberately not also `.sheet-selection`: that class is how the selection overlay is found, and two elements
  // answering to it makes every such lookup order-dependent.
  //
  // No `part` either: naming a part here would add to the documented
  // styling API, which is a product decision.
  return html`<div class="paintmode" style=${style}></div>`;
}

/**
 * The part of a cell rectangle one quadrant shows, in that quadrant's own
 * coordinates, and which of the rectangle's sides it holds. A rectangle
 * that crosses a freeze is drawn a piece per pane, and a side it runs on past - into the next pane, or out of view - is
 * not an edge of the piece.
 */
export interface RangePiece {
  left: number;
  top: number;
  right: number;
  bottom: number;
  hasTop: boolean;
  hasLeft: boolean;
  hasRight: boolean;
  hasBottom: boolean;
}

export function rangePiece(
  opts: QuadrantRenderOpts,
  rect: { c1: number; c2: number; r1: number; r2: number }
): RangePiece | null {
  const c1 = Math.max(rect.c1, opts.colStart);
  const c2 = Math.min(rect.c2, opts.colEnd);
  const r1 = Math.max(rect.r1, opts.rowStart);
  const r2 = Math.min(rect.r2, opts.rowEnd);
  if (c1 > c2 || r1 > r2) {
    return null;
  }
  return {
    left: opts.getColLeft(c1),
    top: opts.getRowTop(r1),
    right: opts.getColLeft(c2) + opts.getColWidth(c2),
    bottom: opts.getRowTop(r2) + opts.getRowHeight(r2),
    hasTop: r1 === rect.r1,
    hasLeft: c1 === rect.c1,
    hasRight: c2 === rect.c2,
    hasBottom: r2 === rect.r2
  };
}

/**
 * The box a piece's outline is drawn in. One pixel up and left of the
 * cells where the rectangle's own top and left sides are: each side of the outline then lies on a gridline. A cell draws
 * only its right and bottom borders, so the gridlines above and to the left
 * of a range belong to the cells next to it.
 */
export function pieceBox(piece: RangePiece): string {
  const left = piece.left - (piece.hasLeft ? 1 : 0);
  const top = piece.top - (piece.hasTop ? 1 : 0);
  const width = Math.max(0, piece.right - left);
  const height = Math.max(0, piece.bottom - top);
  return `left:${left}px;top:${top}px;width:${width}px;height:${height}px;position:absolute;pointer-events:none;`;
}

/**
 * Build the {@code .sheet-selection} overlay div positioned over the
 * active range. The div contains 4 child elements
 * ({@code .s-top}, {@code .s-left}, {@code .s-right}, {@code .s-bottom})
 * which TestBench's {@code SpreadsheetElement.findSelectionOutline}
 * locates by class name.
 *
 * Returns an empty string when the active range doesn't overlap the
 * visible viewport (selection scrolled off-screen) — TestBench's
 * waitUntil lookup will then poll until the user scrolls back.
 *
 * Wire contract: the class names are those of earlier versions, which
 * tests look for. The overlay is positioned absolutely within the
 * cell-grid's shadow DOM.
 */
export function buildSelectionOverlay(opts: QuadrantRenderOpts): TemplateResult | string {
  const { selection } = opts.state;
  const range = selection.activeRange;
  const piece = rangePiece(opts, range);
  if (piece === null) {
    // None of the active range is in this quadrant.
    return '';
  }

  // The active cell's coordinates ride on the overlay as data-col /
  // data-row. Nothing else in the DOM identifies the active
  // cell — it renders as a plain `part="cell"`, indistinguishable from
  // an unselected one — so this is the only way to read "where is the
  // selection" from outside.
  //
  // The overlay carries no part. The documented `cell-range` part names
  // the cells of the range other than the active one; on the overlay too, a rule meant for those cells drew around
  // the whole range, the active cell included. The selection corner
  // handle (drag affordance in the bottom-right) carries
  // `part="selection-corner"`. Border divs are internal (their s-* names
  // come from earlier versions; no part attribute since they aren't
  // documented).
  //
  // A side the range runs on past is kept, hidden, so every piece has all
  // four; the handle is only where the range's last cell is.
  const side = (name: string, real: boolean): TemplateResult => html`<div class=${real ? name : `${name} cut`}></div>`;
  // A range all in hidden columns or rows draws nothing: drawn, it would
  // be a sliver of outline and fill handle on the gridline.
  if (piece.right <= piece.left || piece.bottom <= piece.top) {
    return html`
      <div
        class="sheet-selection"
        data-col=${selection.activeCell.col}
        data-row=${selection.activeCell.row}
        style=${pieceBox(piece)}
      ></div>
    `;
  }
  // With several ranges selected there is no outline and no fill handle -
  // the active cell carries the selected-cell-highlight
  // instead. The overlay stays, for the coordinates it carries.
  return html`
    <div
      class=${!isCoherentSelection(selection) ? 'sheet-selection multi-range' : 'sheet-selection'}
      data-col=${selection.activeCell.col}
      data-row=${selection.activeCell.row}
      style=${pieceBox(piece)}
    >
      ${side('s-top', piece.hasTop)}${side('s-left', piece.hasLeft)}${side('s-right', piece.hasRight)}${side(
        's-bottom',
        piece.hasBottom
      )}
      ${piece.hasRight && piece.hasBottom
        ? html`<div class="selection-corner" part="selection-corner"></div>`
        : nothing}
    </div>
  `;
}
