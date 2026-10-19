/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, nothing, type TemplateResult } from 'lit';
import { SpreadsheetPart } from './SpreadsheetPart.js';
import { cellTarget } from '../dom/IndexedTarget.js';
import { keyedList } from '../dom/KeyedList.js';
import { isFormula } from '../algorithms/FormulaPointing.js';
import { composing } from '../algorithms/KeyboardMoves.js';

import { parseColRow, toKey } from '../algorithms/CellKeys.js';
import { cellCoordToAddress } from '../algorithms/CellRef.js';
import { Memo, sameInputs } from '../algorithms/Memo.js';
import { caretPosition } from '../algorithms/PercentageEntry.js';
import type { CellData } from '../types/wire.js';
import { ANIMATE_IN_CLASS, animateIn, animateOut, overlayAnimationStyles } from '../dom/OverlayAnimation.js';
import { isCellEditorSlot, overlayComponentSlotName } from '../dom/VirtualChildren.js';
import { isActiveCell, isCellSelected, isCoherentSelection } from '../state/SelectionState.js';
import { type CellGridRenderState, emptyCellGridRenderState } from './types.js';
import {
  cellBackground,
  cellBorderColor,
  cellRangeBg,
  commentBg,
  commentOverlayBg,
  commentOverlayBorderColor,
  editorBorder,
  headerBorderColor,
  invalidformulaBg,
  popupButtonActiveBg,
  popupButtonBg,
  popupButtonFg,
  selectionBorderColor,
  selectionColor
} from './ThemeProperties.js';

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

  static styles = [
    overlayAnimationStyles,
    css`
      /* Not clipped: a note hangs over the headers when its cell is in the
       first row or column, and it is kept inside
       the visible sheet, so it never stretches the scroll area. */
      :host {
        display: block;
        position: relative;
      }

      :host([hidden]) {
        display: none !important;
      }

      /* A row of cells, keyed as one: scrolling a row out of view takes
       its cells with it in one removal. No box of its own. */
      .cell-row {
        display: contents;
      }

      .cell {
        position: absolute;
        box-sizing: border-box;
        overflow: hidden;
        white-space: nowrap;
        /* A column flexbox with its content at the bottom. Excel's default vertical alignment is the bottom of
         the row, and the server's per-style rules say otherwise through
         justify-content - which only means anything here because the cell
         is a flex container. As a block, every cell put its text at the
         top, and top/centre/bottom alignment from a workbook did nothing. */
        display: flex;
        flex-direction: column;
        justify-content: flex-end;
        /* Pinned rather than inherited: a Vaadin page sets a body line height
         of 1.625, which spaced wrapped text so far apart that the second
         line of a cell fell out of its row. */
        line-height: normal;
        /* The two pixels either side that the server's fit check assumes
         when it decides whether a value fits its column. */
        padding: 0 2px;
        /* A cell is a surface, not a window: it has a ground of its own,
         so a conditional format that stops matching leaves the cell
         white rather than transparent, and a cell under a merge or an
         overlay does not show what is behind the sheet. A cs<N> or
         cf<N> rule wins over this, being the more specific selector. */
        background: ${cellBackground};
        border-right: 1px solid ${cellBorderColor};
        border-bottom: 1px solid ${cellBorderColor};
        /* Unset, the workbook's default font, which its default style
         declares on every cell - and until the styles arrive, the default
         the host declares for it. */
        font-family: var(--vaadin-spreadsheet-cell-font, var(--default-font-family));
        font-size: var(--vaadin-spreadsheet-cell-font-size, var(--default-font-size));
      }

      /* The server appends an "r" to a cell's class list when the value is
       right-aligned — numbers and dates under most formats. It rides on
       the cell class rather than a cs<N> rule because alignment follows
       the value, not the style index. */
      .cell.r {
        text-align: right;
      }

      /* Selection overlay: the wrapper .sheet-selection is
           positioned by the connector with inline left/top/width/
           height. The four border divs inside form the visible
           outline. Their bounding box feeds the TestBench
           isElementSelected check, so they must occupy the
           wrapper's full perimeter. */
      .sheet-selection {
        position: absolute;
        pointer-events: none;
        box-sizing: border-box;
      }

      /* A merged cell covers the cells it spans, which stay in the DOM
       underneath so they can still be found by their coordinates. It
       needs a ground of its own or their content shows through. A cs<N>
       rule wins over this, being the more specific selector, so a styled
       merge keeps its own background. */
      .merged-cell {
        background: ${cellBackground};
      }

      /* Dropping into place as it appears and fading out as it goes. */
      .v-spreadsheet-comment-overlay.animate-in {
        animation: overlay-animate-in 120ms;
      }

      .v-spreadsheet-comment-overlay.animate-out {
        animation: overlay-animate-out 120ms;
      }

      /* Comment notes. Anchored at the cell's top-right and above every
       cell, since the point is to be readable over the sheet. */
      /* A comment: a white card with a soft
       shadow, the author above the text in small bold type. */
      .v-spreadsheet-comment-overlay {
        z-index: 20;
        padding: 7px;
        box-sizing: border-box;
        pointer-events: none;
        color: #474747;
        background: ${commentOverlayBg};
        border: 0;
        border-radius: 4px;
        /* The edge is the last shadow, a hairline that takes no room. */
        box-shadow: 0 4px 10px 0 rgba(0, 0, 0, 0.1), 0 3px 5px 0 rgba(0, 0, 0, 0.05),
          0 0 0 1px ${commentOverlayBorderColor};
      }

      .v-spreadsheet-comment-overlay .comment-overlay-label,
      .v-spreadsheet-comment-overlay .comment-overlay-invalidformula {
        max-width: 168px;
        max-height: 140px;
        overflow: hidden;
        white-space: pre-wrap;
        overflow-wrap: break-word;
      }

      .v-spreadsheet-comment-overlay .comment-overlay-invalidformula {
        color: #ed473b;
      }

      /* Between an error and the comment under it. */
      .v-spreadsheet-comment-overlay .comment-overlay-separator {
        border-bottom: 1px solid #c7c7c7;
        margin-bottom: 7px;
      }

      /*
     * The note being written. Sized like the note it replaces so the
     * overlay does not jump as it opens.
     */
      .v-spreadsheet-comment-overlay .comment-overlay-edit {
        display: block;
        box-sizing: border-box;
        /* The card lets the pointer through; the note being written takes
         it, so a click places the caret instead of selecting the cell
         underneath. */
        pointer-events: auto;
        width: 160px;
        height: 60px;
        resize: none;
        border: 0;
        background: transparent;
        /* The note's input, in the sheet's own font rather than the page's
         the note is shown in. */
        font-family: Helvetica;
        font-size: 14px;
        font-weight: 300;
        color: inherit;
        outline: none;
      }

      /* From a cell's corner to its note. Under the note, over the cells. */
      .comment-overlay-line {
        position: absolute;
        height: 1px;
        transform-origin: 0% 50%;
        background: #a7a7a7;
        z-index: 19;
        pointer-events: none;
      }

      .v-spreadsheet-comment-overlay .comment-overlay-author {
        padding-bottom: 7px;
        font-size: 11px;
        font-weight: bold;
        white-space: nowrap;
      }

      /* A reference of the formula being written: its cells tinted in its
         colour, and the one the caret is in outlined. Over the cells, and never in the way of a click. */
      .formula-reference {
        position: absolute;
        pointer-events: none;
        box-sizing: border-box;
        z-index: 2;
      }

      /* The fill handle's drag outline: the fill colour around a pale green ground, so it reads as what the
       drag will fill rather than as a second selection. */
      .paintmode {
        position: absolute;
        pointer-events: none;
        box-sizing: border-box;
        border: 2px solid ${selectionBorderColor};
        background: rgba(235, 247, 233, 0.8);
        z-index: 2;
      }
      /* The outline and the handle as one layer, at the level its pieces
       hold among the cells, the pictures and the filter buttons. */
      .sheet-selection {
        z-index: 2;
      }
      /* Dimmed while the sheet does not have the keyboard; the connector
       sets the attribute. All of it at once: dimmed piece by piece, the
       outline would show darker where its sides meet each other and the
       handle. */
      :host(:not([sheet-focused])) .sheet-selection {
        opacity: 0.6;
      }
      .sheet-selection .s-top {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 2px;
        background: ${selectionColor};
        /* Above the cells, which carry z-index 1 and paint an
         opaque background. Without this the outline is in the DOM,
         correctly sized, and invisible - every cell covers it. No
         higher: a frozen pane, at 2 and later in the tree, covers the
         piece of the selection that scrolls under it. */
        z-index: 2;
      }
      .sheet-selection .s-bottom {
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 2px;
        background: ${selectionColor};
        /* Above the cells, which carry z-index 1 and paint an
         opaque background. Without this the outline is in the DOM,
         correctly sized, and invisible - every cell covers it. No
         higher: a frozen pane, at 2 and later in the tree, covers the
         piece of the selection that scrolls under it. */
        z-index: 2;
      }
      .sheet-selection .s-left {
        position: absolute;
        top: 0;
        left: 0;
        width: 2px;
        height: 100%;
        background: ${selectionColor};
        /* Above the cells, which carry z-index 1 and paint an
         opaque background. Without this the outline is in the DOM,
         correctly sized, and invisible - every cell covers it. No
         higher: a frozen pane, at 2 and later in the tree, covers the
         piece of the selection that scrolls under it. */
        z-index: 2;
      }
      .sheet-selection .s-right {
        position: absolute;
        top: 0;
        right: 0;
        width: 2px;
        height: 100%;
        background: ${selectionColor};
        /* Above the cells, which carry z-index 1 and paint an
         opaque background. Without this the outline is in the DOM,
         correctly sized, and invisible - every cell covers it. No
         higher: a frozen pane, at 2 and later in the tree, covers the
         piece of the selection that scrolls under it. */
        z-index: 2;
      }
      /* The fill handle, in the fill colour the drag preview uses - a green
       square, ringed in white so it stands off the outline, hung from the
       foot of the right side, two pixels past it. */
      .sheet-selection .selection-corner {
        position: absolute;
        right: -2px;
        bottom: -1px;
        width: 6px;
        height: 6px;
        background: ${selectionBorderColor};
        outline: 2px solid #fff;
        cursor: crosshair;
        pointer-events: auto;
        /* Above the cells, which carry z-index 1. Without this the handle
         paints and can be found in the DOM but the cell under it wins
         every hit test, so a real drag selects cells instead of filling
         them. The border divs above are raised for the same reason. */
        z-index: 2;
      }

      /* A side the range runs on past, into the next pane or out of view. */
      .sheet-selection .cut {
        visibility: hidden;
      }
      /* The selection tint over a cell has to sit on top of whatever the
       cell is already painted, not replace it: a background would hide a
       conditional format's fill for as long as the cell is selected, and
       Excel shows the fill through the selection. An inset shadow spread
       over the whole box tints instead of replacing. */
      /* The default tint turns a white cell the range colour,
       rgb(237, 245, 253), while a fill underneath still shows through. */
      .cell.cell-range {
        box-shadow: inset 0 0 0 9999px ${cellRangeBg};
      }

      /* The active cell of a selection of several ranges: a dark line just
       inside its edge. */
      .cell.selected-cell-highlight {
        outline: 1px solid #222;
        outline-offset: -2px;
        z-index: 1;
      }

      .sheet-selection.multi-range {
        background: none;
      }

      .sheet-selection.multi-range > * {
        display: none;
      }

      /* Frozen-pane wrappers. Each frozen quadrant lives
           in its own wrapper div that's transform-translated by the
           current scroll offset, so the cells inside stay visually
           pinned to their viewport edge as the scroll container
           scrolls. The transform property creates a containing block,
           so the cells' absolute positions remain relative to the
           wrapper's top-left — which is itself anchored at the
           cell-grid's origin. */
      .quadrant-wrapper {
        position: absolute;
        top: 0;
        left: 0;
      }
      .quadrant-wrapper-top {
        z-index: 2;
      }
      .quadrant-wrapper-left {
        z-index: 2;
      }
      .quadrant-wrapper-corner {
        z-index: 3;
      }

      /* The line along a freeze, in the frozen pane's wrapper so it stays
       put while the cells scroll under it. Above the wrapper's own cells,
       and out of the way of a click on the cell beneath. */
      .freeze-line {
        position: absolute;
        z-index: 4;
        background: #6a6a6a;
        pointer-events: none;
      }

      /* Per-cell z-index for the unwrapped main quadrant. Frozen
           quadrants inherit z-index from their wrapper (each wrapper
           is its own stacking context via the transform property),
           so the cell-level rule below only takes effect for main
           cells. */
      .cell.quadrant-main {
        z-index: 1;
      }

      /* A cell whose text runs past its own column. It has to stop
       clipping, and it has to paint over the empty neighbours the text
       reaches into - they come later in the DOM and have a ground of
       their own, so at equal z-index they would cover the text. Comes
       after the rule above, which is equally specific. Still below the
       selection outline, which comes later in the tree. */
      .cell.overflowing {
        overflow: visible;
        z-index: 2;
      }

      /* A cell showing a custom editor, padded on every side; a plain cell
       pads only its sides. */
      .cell.custom-editor-cell {
        padding: 2px;
      }

      /* Text too wide for its column with nowhere to go: its holder reaches
       past the cell's edge, and the neighbour
       covers what is past it. Not raised, unlike a cell running on. */
      .cell.cut-short {
        overflow: visible;
      }

      /* Holds text too wide for its column, so the browser cuts it short
       with an ellipsis: at the width the server worked out - the column's
       own when the text has nowhere to go. */
      .cell-spill {
        overflow: hidden;
        text-overflow: ellipsis;
        pointer-events: none;
      }

      /* A right-aligned value running on: from the cell's left edge, cut off
       plainly where a neighbour begins. */
      .cell-spill.runs-right {
        text-align: left;
        text-overflow: clip;
      }

      /* Sheet overlays — images, charts, and embedded
           components positioned at absolute pixel coordinates on
           the sheet, each in the pane of its anchor cell.
           Above the cells and below the editor and the notes; in the
           scrolling quadrant no higher than the frozen panes, which
           cover what scrolls under them. */
      .sheet-overlay {
        position: absolute;
        z-index: 2;
        box-sizing: border-box;
        pointer-events: auto;
      }
      .sheet-overlay-image {
        display: block;
      }

      /* The corner markers: 9px triangles in the cell's
       top-right corner, inside its border - red for a formula that could
       not be evaluated, amber for a comment. */
      .invalidformula-indicator {
        position: absolute;
        right: 0;
        top: 0;
        width: 9px;
        height: 9px;
        background: ${invalidformulaBg};
        clip-path: polygon(0 0, 100% 0, 100% 100%);
        z-index: 5;
        pointer-events: none;
      }

      /* In the cell's top-right corner, inside its border: a cell with a
       thicker border keeps all of it. */
      .comment-indicator {
        position: absolute;
        right: 0;
        top: 0;
        width: 9px;
        height: 9px;
        background: ${commentBg};
        clip-path: polygon(0 0, 100% 0, 100% 100%);
        z-index: 5;
        pointer-events: none;
      }

      /* A link looks however its workbook styles it; the colour an application may give links is laid among the cell
       styles - see buildCellStyleBlock. */
      .cell.link-cell {
        cursor: pointer;
      }

      /* The filter button: a small white box with
       a grey arrow, tucked into the bottom-right corner of its cell inside
       the cell's borders. Placed at that corner and pulled back over it. */
      .popup-button {
        position: absolute;
        box-sizing: border-box;
        width: 13px;
        height: 13px;
        transform: translate(calc(-100% - 2px), calc(-100% - 1px));
        display: flex;
        align-items: center;
        justify-content: center;
        border: 1px solid #d3d3d3;
        border-radius: 2px;
        background: ${popupButtonBg};
        color: ${popupButtonFg};
        cursor: pointer;
        font-size: 9px;
        line-height: 1;
        z-index: 2;
        user-select: none;
      }
      /* A filter in use, marked along the top. */
      .popup-button.active {
        background: ${popupButtonActiveBg};
        box-shadow: inset 0 2px 0 0 rgb(0, 171, 238);
      }

      /* Cell editor input — absolutely positioned over the cell
           being edited, sized to match. z-index above all cells +
           overlays so the input + caret are always on top. Slice
           152. Framed with no border, lifted
           off the sheet on a soft shadow, the text at the cell's
           edge. The ring is the editor colour, which only the Lumo
           theme and an application set. */
      /* Never shown, whatever an application does to inputs. */
      .cell-input-stand-in {
        display: none !important;
      }

      .cell-editor {
        position: absolute;
        box-sizing: border-box;
        z-index: 10;
        border: 0;
        outline: 1px solid ${editorBorder};
        box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.75);
        font-family: var(--vaadin-spreadsheet-cell-font, var(--default-font-family));
        font-size: var(--vaadin-spreadsheet-cell-font-size, var(--default-font-size));
        padding: 0;
      }

      /* The filter arrow, drawn after the button's content, so a theme can
       swap the glyph. */
      .popup-button::after {
        content: '▼';
      }

      /*
     * SpreadsheetTheme.LUMO. The colours come
     * from the documented properties' Lumo defaults on the host; the rest
     * of the look is here.
     */

      /* The Lumo font on every cell - the workbook's own fonts too - and
       Lumo's small size where the workbook leaves
       the size to its default. Every style rule sets these on the cells it
       styles; this is more specific than any of them. */
      :host([theme~='lumo']) .cell {
        --default-font-family: var(--lumo-font-family);
        --default-font-size: var(--lumo-font-size-s);
      }

      /* No more specific than the cell's own rule, so a workbook's indent
       still wins. */
      :where(:host([theme~='lumo'])) .cell {
        padding: 0 var(--lumo-space-xs);
      }

      :host([theme~='lumo']) .cell.selected-cell-highlight {
        outline-color: var(--lumo-contrast-80pct);
      }

      :host([theme~='lumo']) .cell.custom-editor-cell {
        padding: var(--lumo-space-xs);
      }

      /* Ringed in the editor colour, which the theme sets, on Lumo's
       small shadow, one pixel up and left of the cell it covers. */
      :host([theme~='lumo']) .cell-editor {
        box-shadow: var(--lumo-box-shadow-s);
        margin: -1px 0 0 -1px;
      }

      :host([theme~='lumo']) .selection-corner {
        outline-color: var(--lumo-base-color);
      }

      :host([theme~='lumo']) .paintmode {
        background: var(--lumo-success-color-10pct);
      }

      /* Lumo's line colour over the sheet's ground, made solid: the line
       is the panes' border, over the sheet's own ground whatever scrolls
       past. See-through, it darkened over the cells under
       it and where its pieces overlapped in the frozen corner. */
      :host([theme~='lumo']) .freeze-line {
        background: linear-gradient(var(--lumo-contrast-50pct), var(--lumo-contrast-50pct)) var(--lumo-base-color);
      }

      /* A square as tall as its cell, tinted like the headers, with Lumo's
       chevron for an arrow. The row height arrives as --_cell-height. */
      :host([theme~='lumo']) .popup-button {
        width: calc(var(--_cell-height, 20px) + 1px);
        height: calc(var(--_cell-height, 20px) + 1px);
        border-color: ${headerBorderColor};
        background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
        border-radius: var(--lumo-border-radius-s);
        font-size: var(--lumo-font-size-s);
        cursor: var(--lumo-clickable-cursor);
      }

      :host([theme~='lumo']) .popup-button::after {
        content: var(--lumo-icons-angle-down);
        font-family: 'lumo-icons';
        font-size: var(--lumo-font-size-xs);
      }

      :host([theme~='lumo']) .popup-button.active {
        box-shadow: var(--lumo-box-shadow-xs) inset;
      }

      :host([theme~='lumo']) .v-spreadsheet-comment-overlay {
        padding: var(--lumo-space-s);
        border: 1px solid ${commentOverlayBorderColor};
        border-radius: var(--lumo-border-radius-m);
        color: var(--lumo-body-text-color);
        box-shadow: var(--lumo-box-shadow-m);
      }

      :host([theme~='lumo']) .v-spreadsheet-comment-overlay .comment-overlay-author {
        padding-bottom: var(--lumo-space-s);
        font-size: var(--lumo-font-size-xs);
      }

      :host([theme~='lumo']) .v-spreadsheet-comment-overlay .comment-overlay-invalidformula {
        color: var(--lumo-error-color);
      }

      :host([theme~='lumo']) .v-spreadsheet-comment-overlay .comment-overlay-edit {
        font-family: var(--lumo-font-family);
        font-size: var(--lumo-font-size-s);
      }

      :host([theme~='lumo']) .v-spreadsheet-comment-overlay .comment-overlay-separator {
        border-bottom-color: var(--lumo-contrast-20pct);
      }

      :host([theme~='lumo']) .comment-overlay-line {
        background: var(--lumo-contrast-10pct);
      }
    `
  ];

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
    const state = this.renderState;
    return {
      width: frozenExtent(state.frozenColX, state.frozenColWidths, state.horizontalSplitPosition),
      height: frozenExtent(state.frozenRowY, state.frozenRowHeights, state.verticalSplitPosition)
    };
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
      const pane = panes[(note.dataset.quadrant as QuadrantRenderOpts['quadrantName']) ?? 'main'] ?? panes.main;
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
  ): Record<QuadrantRenderOpts['quadrantName'], { left: number; right: number; top: number; bottom: number }> {
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
    const { viewport, verticalSplitPosition, horizontalSplitPosition, scrollLeft, scrollTop } = this.renderState;

    if (viewport.lastColumnIndex < viewport.firstColumnIndex || viewport.lastRowIndex < viewport.firstRowIndex) {
      // Empty viewport — render an empty host. (The connector
      // may push an empty state during initial measurement.)
      return html``;
    }

    // Main (bottom-right) quadrant: scrolling rows + cols.
    // Emitted as direct children of the cell-grid so cells sit at
    // their virtual coordinates and scroll naturally with the
    // scroll container.
    const mainOpts: QuadrantRenderOpts = {
      state: this.measuredState(),
      quadrantName: 'main',
      colStart: viewport.firstColumnIndex,
      colEnd: viewport.lastColumnIndex,
      rowStart: viewport.firstRowIndex,
      rowEnd: viewport.lastRowIndex,
      getColLeft: (col) => this.renderState.colX[col - viewport.firstColumnIndex] ?? 0,
      getColWidth: (col) => this.renderState.colWidths[col - viewport.firstColumnIndex] ?? 0,
      getRowTop: (row) => this.renderState.rowY[row - viewport.firstRowIndex] ?? 0,
      getRowHeight: (row) => this.renderState.rowHeights[row - viewport.firstRowIndex] ?? 0
    };
    const mainCells = renderQuadrant(mainOpts);
    // Straight after the cells they cover, so the frozen quadrants, which
    // come later at the same z-index, stay on top of a merge scrolling
    // under them. The selection, the fill outline, the pictures and the
    // filter buttons of the scrolling quadrant go there too, for the same
    // reason: under a frozen pane they scroll out of sight, instead of
    // painting over it.
    const mainMerged = buildMergedCellOverlays(mainOpts);
    const mainOverlays = this.quadrantOverlays(mainOpts);

    // Frozen quadrants live inside transform-translated wrappers
    // so their cells visually stay pinned as the user scrolls.
    // The wrapper transform counters the scroll
    // container's shift; cells inside use coordinates relative
    // to the wrapper, which is itself anchored at the cell-grid
    // origin. CSS z-index on the wrappers stacks corner > top +
    // left > main.

    const frozenHeight = frozenExtent(
      this.renderState.frozenRowY,
      this.renderState.frozenRowHeights,
      verticalSplitPosition
    );
    const frozenWidth = frozenExtent(
      this.renderState.frozenColX,
      this.renderState.frozenColWidths,
      horizontalSplitPosition
    );

    let topQuadrant: TemplateResult | string = '';
    if (verticalSplitPosition > 0) {
      const topOpts: QuadrantRenderOpts = {
        state: this.measuredState(),
        quadrantName: 'top',
        colStart: viewport.firstColumnIndex,
        colEnd: viewport.lastColumnIndex,
        rowStart: 1,
        rowEnd: verticalSplitPosition,
        getColLeft: (col) => this.renderState.colX[col - viewport.firstColumnIndex] ?? 0,
        getColWidth: (col) => this.renderState.colWidths[col - viewport.firstColumnIndex] ?? 0,
        getRowTop: (row) => this.renderState.frozenRowY[row - 1] ?? 0,
        getRowHeight: (row) => this.renderState.frozenRowHeights[row - 1] ?? 0
      };
      const topCells = renderQuadrant(topOpts);
      const topMerged = buildMergedCellOverlays(topOpts);
      const transform = `transform:translateY(${scrollTop}px);`;
      const line = freezeLine('rows', frozenHeight, this.renderState.totalGridWidthPx);
      topQuadrant = html`
        <div class="quadrant-wrapper quadrant-wrapper-top" style=${transform}>
          ${topCells}${topMerged}${line}${this.quadrantOverlays(topOpts)}
        </div>
      `;
    }

    let leftQuadrant: TemplateResult | string = '';
    if (horizontalSplitPosition > 0) {
      const leftOpts: QuadrantRenderOpts = {
        state: this.measuredState(),
        quadrantName: 'left',
        colStart: 1,
        colEnd: horizontalSplitPosition,
        rowStart: viewport.firstRowIndex,
        rowEnd: viewport.lastRowIndex,
        getColLeft: (col) => this.renderState.frozenColX[col - 1] ?? 0,
        getColWidth: (col) => this.renderState.frozenColWidths[col - 1] ?? 0,
        getRowTop: (row) => this.renderState.rowY[row - viewport.firstRowIndex] ?? 0,
        getRowHeight: (row) => this.renderState.rowHeights[row - viewport.firstRowIndex] ?? 0
      };
      const leftCells = renderQuadrant(leftOpts);
      const leftMerged = buildMergedCellOverlays(leftOpts);
      const transform = `transform:translateX(${scrollLeft}px);`;
      const line = freezeLine('columns', frozenWidth, this.renderState.totalGridHeightPx);
      leftQuadrant = html`
        <div class="quadrant-wrapper quadrant-wrapper-left" style=${transform}>
          ${leftCells}${leftMerged}${line}${this.quadrantOverlays(leftOpts)}
        </div>
      `;
    }

    let cornerQuadrant: TemplateResult | string = '';
    if (verticalSplitPosition > 0 && horizontalSplitPosition > 0) {
      const cornerOpts: QuadrantRenderOpts = {
        state: this.measuredState(),
        quadrantName: 'corner',
        colStart: 1,
        colEnd: horizontalSplitPosition,
        rowStart: 1,
        rowEnd: verticalSplitPosition,
        getColLeft: (col) => this.renderState.frozenColX[col - 1] ?? 0,
        getColWidth: (col) => this.renderState.frozenColWidths[col - 1] ?? 0,
        getRowTop: (row) => this.renderState.frozenRowY[row - 1] ?? 0,
        getRowHeight: (row) => this.renderState.frozenRowHeights[row - 1] ?? 0
      };
      const cornerCells = renderQuadrant(cornerOpts);
      const cornerMerged = buildMergedCellOverlays(cornerOpts);
      const transform = `transform:translate(${scrollLeft}px, ${scrollTop}px);`;
      // The left pane's cells cover the top pane's line where the frozen
      // columns are, and the top pane's cover the left pane's; the corner
      // sits above both and draws the two pieces they hide.
      const lines = html`${freezeLine('rows', frozenHeight, frozenWidth + 1)}${freezeLine(
        'columns',
        frozenWidth,
        frozenHeight + 1
      )}`;
      cornerQuadrant = html`
        <div class="quadrant-wrapper quadrant-wrapper-corner" style=${transform}>
          ${cornerCells}${cornerMerged}${lines}${this.quadrantOverlays(cornerOpts)}
        </div>
      `;
    }

    // Written again only when what they are written from changes, not on
    // every drawing: a workbook's rules run to hundreds of kilobytes of
    // selectors, and a smooth scroll draws every frame.
    const state = this.renderState;
    const styleBlock = this.styleRules.get(
      [
        state.cellStyleToCSSStyle,
        state.displayGridlines,
        state.rowIndexToStyleIndex,
        state.columnIndexToStyleIndex,
        state.conditionalFormattingStyles,
        state.shiftedCellBorderStyles
      ],
      () => buildCellStyleBlock(state)
    );
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
    return html`${styleBlock}${mainCells}${mainMerged}${mainOverlays}${topQuadrant}${leftQuadrant}${cornerQuadrant}${commentOverlays}${editor}${standIn}`;
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

interface QuadrantRenderOpts {
  state: CellGridRenderState;
  quadrantName: 'main' | 'top' | 'left' | 'corner';
  colStart: number;
  colEnd: number;
  rowStart: number;
  rowEnd: number;
  getColLeft: (col: number) => number;
  getColWidth: (col: number) => number;
  getRowTop: (row: number) => number;
  getRowHeight: (row: number) => number;
}

/** The quadrant a cell is drawn in, by which side of each split it lies. */
function quadrantOf(
  col: number,
  row: number,
  frozenCols: number,
  frozenRows: number
): QuadrantRenderOpts['quadrantName'] {
  const top = row <= frozenRows;
  const left = col <= frozenCols;
  if (top) {
    return left ? 'corner' : 'top';
  }
  return left ? 'left' : 'main';
}

/** Where the frozen rows or columns end: the far edge of the last one. */
function frozenExtent(starts: ReadonlyArray<number>, sizes: ReadonlyArray<number>, count: number): number {
  return count > 0 ? (starts[count - 1] ?? 0) + (sizes[count - 1] ?? 0) : 0;
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

/** The value a style body gives a property last, if it gives one. */
function lastValueOf(body: string | undefined, property: string): string | undefined {
  let value: string | undefined;
  for (const declaration of (body ?? '').split(';')) {
    const colon = declaration.indexOf(':');
    if (colon > 0 && declaration.slice(0, colon).trim() === property) {
      value = declaration.slice(colon + 1).trim();
    }
  }
  return value;
}

/** Rows or columns by the style index they are formatted with. */
function groupByStyle(byIndex: Readonly<Record<number, number>>): Map<number, number[]> {
  const out = new Map<number, number[]>();
  for (const [where, style] of Object.entries(byIndex)) {
    const list = out.get(style) ?? [];
    list.push(Number(where));
    out.set(style, list);
  }
  return out;
}

/**
 * Every coordinate of a quadrant that a merged region covers, anchors
 * included.
 *
 * These cells are still rendered — an application, and the tests, ask
 * about a cell by its coordinates whether or not a merge sits over it —
 * but the merge owns the region's content, so they render their text and
 * nothing else. A second slot of the same name underneath would capture
 * the component and hide it below the merge.
 *
 * Each region is clipped to the quadrant first: a merge as tall as the
 * sheet cost a million coordinates on every render otherwise.
 */
export function coveredByMergeCoords(
  mergedRegions: ReadonlyArray<{ col1: number; col2: number; row1: number; row2: number }>,
  colStart: number,
  colEnd: number,
  rowStart: number,
  rowEnd: number
): Set<string> {
  const covered = new Set<string>();
  for (const region of mergedRegions) {
    const lastRow = Math.min(region.row2, rowEnd);
    const lastCol = Math.min(region.col2, colEnd);
    for (let row = Math.max(region.row1, rowStart); row <= lastRow; row++) {
      for (let col = Math.max(region.col1, colStart); col <= lastCol; col++) {
        covered.add(toKey(col, row));
      }
    }
  }
  return covered;
}

/**
 * The cell ranges the grid draws: the scrolling quadrant's, and those of
 * the frozen panes there are. 1-based and inclusive; empty while nothing
 * is measured.
 */
export function drawnRanges(state: CellGridRenderState): Array<{ c1: number; c2: number; r1: number; r2: number }> {
  const { viewport, verticalSplitPosition, horizontalSplitPosition } = state;
  if (viewport.lastColumnIndex < viewport.firstColumnIndex || viewport.lastRowIndex < viewport.firstRowIndex) {
    return [];
  }
  const cols = { c1: viewport.firstColumnIndex, c2: viewport.lastColumnIndex };
  const rows = { r1: viewport.firstRowIndex, r2: viewport.lastRowIndex };
  const ranges = [{ ...cols, ...rows }];
  if (verticalSplitPosition > 0) {
    ranges.push({ ...cols, r1: 1, r2: verticalSplitPosition });
  }
  if (horizontalSplitPosition > 0) {
    ranges.push({ c1: 1, c2: horizontalSplitPosition, ...rows });
  }
  if (verticalSplitPosition > 0 && horizontalSplitPosition > 0) {
    ranges.push({ c1: 1, c2: horizontalSplitPosition, r1: 1, r2: verticalSplitPosition });
  }
  return ranges;
}

/**
 * Render the cells of a single quadrant. Each cell carries a
 * {@code quadrant-<name>} class so CSS can apply z-index stacking
 * (corner > top, left > main). The per-cell position comes from the
 * caller's {@code getColLeft/getRowTop/getColWidth/getRowHeight}
 * closures — different quadrants use different position arrays
 * (frozen arrays vs main arrays).
 */
function renderQuadrant(opts: QuadrantRenderOpts): unknown {
  const { state, quadrantName, colStart, colEnd, rowStart, rowEnd, getColLeft, getColWidth, getRowTop, getRowHeight } =
    opts;
  const { cellData, selection } = state;

  const covered = coveredByMergeCoords(state.mergedRegions, colStart, colEnd, rowStart, rowEnd);
  const rows: Array<{ row: number; cells: Array<{ key: string; template: TemplateResult }> }> = [];
  for (let row = rowStart; row <= rowEnd; row++) {
    const cells: Array<{ key: string; template: TemplateResult }> = [];
    const top = getRowTop(row);
    const height = getRowHeight(row);
    for (let col = colStart; col <= colEnd; col++) {
      const left = getColLeft(col);
      const width = getColWidth(col);
      // A cell in a hidden row or column is still rendered, collapsed by
      // `display:none` — see collapsedDeclaration. Leaving it out instead
      // makes it unfindable, and "is this cell hidden?" is a question
      // both the tests and an application's own scripting ask.
      const key = toKey(col, row);
      const data = cellData.get(key);
      // A cell holding a server-side component or a custom editor shows
      // that instead of its own text — it is what the cell is for.
      const underMerge = covered.has(key);
      const slotName = underMerge ? undefined : (state.cellSlots ?? {})[key];
      // A number too wide for its column shows as hashes rather than a
      // clipped prefix: "$246 913" where the value is 246 trillion reads as
      // a number, and a wrong one. The server sets needsMeasure when its
      // fit check says the value does not fit.
      const tooNarrow = data?.needsMeasure === true && (state.tooNarrowCells?.has(key) ?? false);
      const cellText = tooNarrow ? '###' : data?.value ?? '';
      const rawText = slotName === undefined ? cellText : html`<slot name=${slotName}></slot>`;
      // Text too wide for its column runs into the empty cells beside it,
      // and what still does not fit ends in an ellipsis. The server settles
      // how far, because it is the side that knows what the neighbours hold;
      // a cell showing a component or sitting under a merge keeps to itself.
      const fitsAfterAll = state.fittingSpills?.has(key) ?? false;
      const spill = slotName === undefined && !underMerge && !fitsAfterAll ? data?.overflowWidth ?? 0 : 0;
      // A cell the server sent nothing for still wears the workbook's
      // default style. Without it the cell falls through to the host's own
      // font and colour - Helvetica 14px in grey - instead of the sheet's
      // default, Calibri 11pt in black, which every cell has before anything
      // else applies.
      const cellStyleClass = data?.cellStyle ?? 'cs0';
      // A right-aligned value too wide for its cell - a date, a number in
      // scientific notation - overflows it as any value does: it starts at the cell's left edge and runs on to the right, over
      // its own padding and border too, until a neighbour holding a value
      // cuts it off, without an ellipsis.
      const runsRight = spill > 0 && cellStyleClass.split(/\s+/).includes('r');
      const overflowing = runsRight || spill > width;
      // With nowhere to go the text is still held at the column's width from
      // the padding on: it reaches past the cell's
      // right edge, under the neighbour, which is drawn later on a ground of
      // its own - see .cut-short.
      const cutShort = spill > 0 && !overflowing;
      const text =
        spill === 0
          ? rawText
          : runsRight
          ? html`<div class="cell-spill runs-right" style="width:${spill - 2}px">${rawText}</div>`
          : html`<div class="cell-spill" style="width:${spill}px">${rawText}</div>`;
      const inSelection = isCellSelected(selection, col, row);
      const isActive = isActiveCell(selection, col, row);
      const cellRangeClass =
        inSelection && !isActive
          ? ' cell-range'
          : isActive && !isCoherentSelection(selection)
          ? ' selected-cell-highlight'
          : '';
      // Hyperlink cells get a `link-cell` class: a pointer cursor,
      // the workbook styling the link itself. The click handler in
      // the connector dispatches `linkCellClicked` based
      // on the same hyperlinks map.
      // Defensive default — some test fixtures do not declare
      // the hyperlinkCells / commentCells sets.
      const hyperlinkCells = state.hyperlinkCells ?? new Set();
      const commentCells = state.commentCells ?? new Set();
      const invalidFormulaCells = state.invalidFormulaCells ?? new Set();
      const linkClass = hyperlinkCells.has(key) ? ' link-cell' : '';
      const classList =
        `cell col${col} row${row} quadrant-${quadrantName}` +
        (cellStyleClass ? ` ${cellStyleClass}` : '') +
        cellRangeClass +
        linkClass +
        (overflowing ? ' overflowing' : '') +
        (cutShort ? ' cut-short' : '') +
        (slotName !== undefined && isCellEditorSlot(slotName) ? ' custom-editor-cell' : '');
      const style =
        `left:${left}px;top:${top}px;width:${width}px;height:${height}px;` +
        collapsedDeclaration(width, height) +
        textColorDeclaration(data);
      // Comment indicator — render an additional triangle
      // anchored at the cell's top-right when the cell has
      // a comment.
      const invalidIndicator =
        invalidFormulaCells.has(key) && !underMerge
          ? html`<div class="invalidformula-indicator" part="invalid-triangle"></div>`
          : '';
      // In the cell's corner, inside its border. It carries the cell's anchor as data attributes too, so it can
      // be found by the cell it marks.
      const commentIndicator =
        commentCells.has(key) && !underMerge
          ? html`<div class="comment-indicator" part="comment-triangle" data-col=${col} data-row=${row}></div>`
          : '';
      // `part` attribute mirrors the documented
      // public styling API at
      // ../docs/articles/components/spreadsheet/styling.adoc.
      // Cells inside the active range (but not the active
      // cell itself) carry the `cell-range` part so
      // `vaadin-spreadsheet::part(cell-range)` works.
      const cellPart = inSelection && !isActive ? 'cell cell-range' : 'cell';
      // data-col + data-row drive the click-delegation handler
      // in firstUpdated() — single source of truth
      // for the cell's coordinate when the event bubbles.
      // On one line, and the marker only where there is one: whitespace
      // between the tags is a text node of its own and every binding a
      // marker, and with a thousand cells drawn they doubled the nodes the
      // sheet makes and restyles as it scrolls.
      const content =
        invalidIndicator === '' && commentIndicator === '' ? text : html`${invalidIndicator}${commentIndicator}${text}`;
      cells.push({
        key,
        template: html`<div class=${classList} part=${cellPart} style=${style} data-col=${col} data-row=${row}
          >${content}</div
        >`
      });
    }
    rows.push({ row, cells });
  }
  // Keyed by row, and each row's cells by cell, so a cell keeps its element
  // as the sheet scrolls: cells are placed in sheet coordinates, and one
  // still in view needs no change at all. A row scrolled out of view goes in
  // one removal. Keyed by cell alone, each step removed every cell of every
  // row that left the view one by one, which took most of a frame while
  // scrolling fast; matched by position instead, every cell was rewritten -
  // its class, place and text - and restyled on every step.
  return keyedList(
    rows,
    (group) => group.row,
    (group) =>
      html`<div class="cell-row"
        >${keyedList(
          group.cells,
          (cell) => cell.key,
          (cell) => cell.template
        )}</div
      >`
  );
}

/**
 * Build merged-cell overlay divs for every {@link MergedRegion} whose
 * top-left cell lies in one quadrant, placed with that quadrant's
 * geometry. Each overlay is a single div positioned at the merge's
 * top-left cell, sized to span the region as far as the quadrant shows it.
 *
 * The CSS class list includes {@code cell col<C1> row<R1> merged-cell}
 * — the {@code merged-cell} class is the TestBench-side contract
 * (see {@code AbstractSpreadsheetIT.mergedCell:567-575}).
 *
 * Every quadrant draws its own: a frozen quadrant's overlays go in its
 * wrapper, so they stay put with its cells. Drawn for the scrolling
 * quadrant alone, a merge in the frozen rows - the title across the top of
 * a report - never appeared, and its first cell showed through instead,
 * clipped at its own column. Merges whose top-left is outside the quadrant
 * are still skipped. Per-cell styling, selection state, and cellData lookup
 * use the top-left cell's data; the merged overlay is treated as a single
 * rendered cell.
 */
function buildMergedCellOverlays(opts: QuadrantRenderOpts): TemplateResult[] {
  const { state, quadrantName, colStart, colEnd, rowStart, rowEnd } = opts;
  const { getColLeft, getColWidth, getRowTop, getRowHeight } = opts;
  const { cellData, mergedRegions, mergedRegionBoxes, selection } = state;

  const overlays: TemplateResult[] = [];
  for (let index = 0; index < mergedRegions.length; index++) {
    const region = mergedRegions[index];
    const box = mergedRegionBoxes?.[index];
    let left: number;
    let top: number;
    let width = 0;
    let height = 0;
    if (box) {
      // Drawn in the quadrant of its top-left cell, as far as it reaches,
      // for as long as any of it is in view.
      const owner = quadrantOf(region.col1, region.row1, state.horizontalSplitPosition, state.verticalSplitPosition);
      const inView =
        region.col2 >= colStart && region.col1 <= colEnd && region.row2 >= rowStart && region.row1 <= rowEnd;
      if (owner !== quadrantName || !inView) {
        continue;
      }
      ({ left, top, width, height } = box);
    } else {
      if (region.col1 < colStart || region.col1 > colEnd || region.row1 < rowStart || region.row1 > rowEnd) {
        continue;
      }
      left = getColLeft(region.col1);
      top = getRowTop(region.row1);
      // As far as the quadrant shows the region.
      for (let col = region.col1; col <= Math.min(region.col2, colEnd); col++) {
        width += getColWidth(col);
      }
      for (let row = region.row1; row <= Math.min(region.row2, rowEnd); row++) {
        height += getRowHeight(row);
      }
    }

    const key = toKey(region.col1, region.row1);
    const data = cellData.get(key);
    // A merged cell can hold a component or a custom editor just like a
    // plain one, and it is the only element in the region that renders the
    // slot — the cells underneath deliberately leave it out.
    const slotName = (state.cellSlots ?? {})[key];
    const text = slotName === undefined ? data?.value ?? '' : html`<slot name=${slotName}></slot>`;
    // Same default as an ordinary cell - see buildCells.
    const cellStyleClass = data?.cellStyle ?? 'cs0';

    // Selection: a merged cell carries cell-range if its top-left
    // is in the selected range (excluding active cell). Matches
    // single-cell selection behavior.
    const inSelection = isCellSelected(selection, region.col1, region.row1);
    const isActive = isActiveCell(selection, region.col1, region.row1);
    const cellRangeClass =
      inSelection && !isActive
        ? ' cell-range'
        : isActive && !isCoherentSelection(selection)
        ? ' selected-cell-highlight'
        : '';

    const classList =
      `cell col${region.col1} row${region.row1} merged-cell` +
      (cellStyleClass ? ` ${cellStyleClass}` : '') +
      cellRangeClass;
    // The merged cell is drawn over the cells it spans, which are still
    // in the DOM underneath — so it needs a z-index above them, and a
    // ground of its own or their text shows through.
    const style =
      `left:${left}px;top:${top}px;width:${width}px;height:${height}px;z-index:2;` + textColorDeclaration(data);
    // data-col / data-row are what the click delegation reads. Without
    // them a merged region matched `.cell` and then parsed NaN, so
    // every click on one was dropped — a merged cell could not be
    // selected at all. `part` for the same reason a plain cell has it:
    // a merged cell is still a cell to a stylesheet.
    const cellPart = inSelection && !isActive ? 'cell cell-range' : 'cell';
    // The markers go on the merged cell: the cells underneath leave theirs
    // out.
    const invalidIndicator = (state.invalidFormulaCells ?? new Set()).has(key)
      ? html`<div class="invalidformula-indicator" part="invalid-triangle"></div>`
      : '';
    const commentIndicator = (state.commentCells ?? new Set()).has(key)
      ? html`<div
          class="comment-indicator"
          part="comment-triangle"
          data-col=${region.col1}
          data-row=${region.row1}
        ></div>`
      : '';

    overlays.push(html`
      <div class=${classList} part=${cellPart} style=${style} data-col=${region.col1} data-row=${region.row1}>
        ${invalidIndicator}${commentIndicator}${text}
      </div>
    `);
  }
  return overlays;
}

/**
 * The references of the formula being written, as far as this quadrant
 * shows them: each one's cells tinted in its colour at a quarter, and the
 * one the caret is in ringed at three quarters, two pixels inside its
 * edge - drawn over the cells rather than in place of their fill.
 */
function buildFormulaReferences(opts: QuadrantRenderOpts): TemplateResult[] {
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
function buildPaintOutline(opts: QuadrantRenderOpts): TemplateResult | string {
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
interface RangePiece {
  left: number;
  top: number;
  right: number;
  bottom: number;
  hasTop: boolean;
  hasLeft: boolean;
  hasRight: boolean;
  hasBottom: boolean;
}

function rangePiece(
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
function pieceBox(piece: RangePiece): string {
  const left = piece.left - (piece.hasLeft ? 1 : 0);
  const top = piece.top - (piece.hasTop ? 1 : 0);
  const width = Math.max(0, piece.right - left);
  const height = Math.max(0, piece.bottom - top);
  return `left:${left}px;top:${top}px;width:${width}px;height:${height}px;position:absolute;pointer-events:none;`;
}

/**
 * Where a cell is on screen, in the grid's own coordinates, or null when it
 * is not drawn. A frozen cell sits in its pane's wrapper, which the scroll
 * offset moves along with the view, so its box moves with it: something at
 * grid level placed by the frozen arrays alone is left behind on a scroll.
 */
function cellBoxOnGrid(
  state: CellGridRenderState,
  col: number,
  row: number
): { left: number; top: number; width: number; height: number } | null {
  const { viewport, horizontalSplitPosition, verticalSplitPosition } = state;
  let left: number;
  let width: number;
  if (col >= 1 && col <= horizontalSplitPosition) {
    left = (state.frozenColX[col - 1] ?? 0) + state.scrollLeft;
    width = state.frozenColWidths[col - 1] ?? 0;
  } else if (col >= viewport.firstColumnIndex && col <= viewport.lastColumnIndex) {
    left = state.colX[col - viewport.firstColumnIndex] ?? 0;
    width = state.colWidths[col - viewport.firstColumnIndex] ?? 0;
  } else {
    return null;
  }
  let top: number;
  let height: number;
  if (row >= 1 && row <= verticalSplitPosition) {
    top = (state.frozenRowY[row - 1] ?? 0) + state.scrollTop;
    height = state.frozenRowHeights[row - 1] ?? 0;
  } else if (row >= viewport.firstRowIndex && row <= viewport.lastRowIndex) {
    top = state.rowY[row - viewport.firstRowIndex] ?? 0;
    height = state.rowHeights[row - viewport.firstRowIndex] ?? 0;
  } else {
    return null;
  }
  return { left, top, width, height };
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

/** A drawn column's width, frozen ones included; 0 when it is not drawn. */
function columnWidthOf(state: CellGridRenderState, col: number): number {
  if (col >= 1 && col <= state.horizontalSplitPosition) {
    return state.frozenColWidths[col - 1] ?? 0;
  }
  return state.colWidths[col - state.viewport.firstColumnIndex] ?? 0;
}

/**
 * The box a cell is drawn in: its own, or the merged cell's when the cell
 * starts a merge - the box a note hangs off and the editor lies over. In grid coordinates, frozen panes included; null while the
 * cell is not drawn.
 */
function drawnCellBox(
  state: CellGridRenderState,
  col: number,
  row: number
): { left: number; top: number; width: number; height: number } | null {
  const cell = cellBoxOnGrid(state, col, row);
  if (cell === null) {
    return null;
  }
  const index = state.mergedRegions.findIndex((region) => region.col1 === col && region.row1 === row);
  const merged = index < 0 ? undefined : state.mergedRegionBoxes?.[index];
  return merged === undefined ? cell : { left: cell.left, top: cell.top, width: merged.width, height: merged.height };
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
function buildSelectionOverlay(opts: QuadrantRenderOpts): TemplateResult | string {
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

/** Whether a theme attribute's value names the Lumo variant. */
function isLumo(theme: string | null | undefined): boolean {
  return (theme ?? '').split(/\s+/).includes('lumo');
}

/**
 * A hidden row or column collapses its cells to nothing.
 *
 * `display:none` rather than a zero-sized box: a box with no width still
 * paints its borders, still answers a hit test on its edge, and still
 * reports itself as laid out. Hidden means gone.
 */
function collapsedDeclaration(width: number, height: number): string {
  return width === 0 || height === 0 ? 'display:none;' : '';
}

/**
 * Inline {@code color:} declaration for a cell whose number format names a
 * colour — `[Red]#,##0` and friends.
 *
 * This cannot ride on a `cs<N>` class: the colour depends on the cell's
 * *value* (a four-part format paints positive, negative, zero and text
 * differently) while the style index describes the format itself. So the
 * server resolves it per cell and sends the hex digits without the `#`.
 */
function textColorDeclaration(data: CellData | undefined): string {
  const color = data?.textColor;
  return color ? `color:#${color};` : '';
}

/**
 * The most selectors one rule lists.
 *
 * Blink and WebKit keep the index of a selector inside its rule in 13 bits
 * and silently drop every selector that starts past the 8192nd simple
 * selector: a rule listing 3432 cells as `.cell.col<N>.row<N>` styled the
 * first 2731 of them and no more. A thousand selectors of
 * the three classes these carry stay well inside the limit.
 */
const MAX_SELECTORS_PER_RULE = 1000;

/** One rule per {@link MAX_SELECTORS_PER_RULE} selectors, each with the block. */
function chunkedRules(selectors: readonly string[], block: string): string[] {
  const rules: string[] = [];
  for (let start = 0; start < selectors.length; start += MAX_SELECTORS_PER_RULE) {
    rules.push(`${selectors.slice(start, start + MAX_SELECTORS_PER_RULE).join(', ')} ${block}`);
  }
  return rules;
}

/**
 * Build a {@code <style>} TemplateResult containing one rule per
 * entry in {@code cellStyleToCSSStyle}. Each rule has the form
 * {@code .cell.cs<N> { <body> }}. The body is taken verbatim from
 * the map (the server side composes the CSS declarations; we just
 * place them inside braces).
 *
 * Returns an empty fragment when the map is empty.
 */
function buildCellStyleBlock(state: CellGridRenderState): TemplateResult | string {
  const rules: string[] = [];
  const styles = state.cellStyleToCSSStyle;
  // A sheet with its gridlines turned off. First, and no more specific than the style rules, so the
  // borders the workbook gives its cells - in their styles, conditional
  // formats and the borders shifted onto a neighbour - still draw.
  if (state.displayGridlines === false) {
    rules.push('.cell.cell { border-right: 0; border-bottom: 0; }');
  }
  // The workbook's default style under every cell. A style is sent with only what it changes from the default, so a
  // cell styled cs5 whose font colour is automatic carries no colour of its
  // own: scoped to .cs0 alone, the default reached none of those cells and
  // they fell through to the host's grey. Two classes, so it beats the
  // element's own .cell rule and loses to every cs<N> rule below it.
  //
  // The font goes through the documented properties so an application can
  // still set the cell font, which the workbook's default would otherwise
  // override on every cell. Unset, they resolve to the workbook's own font.
  if (styles[0] !== undefined) {
    rules.push(
      `.cell.cell { ${styles[0]} ` +
        'font-family: var(--vaadin-spreadsheet-cell-font, var(--default-font-family)); ' +
        'font-size: var(--vaadin-spreadsheet-cell-font-size, var(--default-font-size)); }'
    );
  }
  // Links: --vaadin-spreadsheet-link-fg when an application sets it, for a
  // link whose own style sets no colour. Unset, the default style's colour.
  // After the default rule, before the
  // cell styles, so a workbook's styled link keeps its own colour.
  rules.push(
    `.cell.link-cell { color: var(--vaadin-spreadsheet-link-fg, ${
      lastValueOf(styles[0], 'color') ?? 'var(--default-color)'
    }); }`
  );
  // A whole row or column formatted in the workbook styles those of its
  // cells that have no style of their own - which wear cs0.
  const rowsByStyle = groupByStyle(state.rowIndexToStyleIndex ?? {});
  const colsByStyle = groupByStyle(state.columnIndexToStyleIndex ?? {});
  for (const [key, body] of Object.entries(styles)) {
    const index = Number(key);
    if (index === 0) {
      // Already laid under every cell above. A second .cs0 rule would come
      // later and set the font directly, past the documented properties.
      continue;
    }
    const selectors = [`.cell.cs${index}`];
    for (const row of rowsByStyle.get(index) ?? []) {
      selectors.push(`.cell.row${row}.cs0`);
    }
    for (const col of colsByStyle.get(index) ?? []) {
      selectors.push(`.cell.col${col}.cs0`);
    }
    rules.push(...chunkedRules(selectors, `{ ${body} }`));
  }
  // Conditional formatting after the per-cell styles, and in ascending
  // key order: a rule that matches beats the cell's own style, and a
  // later rule beats an earlier one.
  const conditional = state.conditionalFormattingStyles ?? {};
  for (const index of Object.keys(conditional)
    .map(Number)
    .sort((a, b) => a - b)) {
    rules.push(`.cell.cf${index} { ${conditional[index]} }`);
  }
  // Shifted borders last. These arrive as complete rules selecting
  // `.col<N>.row<N>`; scope them to cells.
  for (const rule of state.shiftedCellBorderStyles ?? []) {
    const open = rule.indexOf('{');
    if (open < 0) {
      continue;
    }
    const selectors = rule
      .slice(0, open)
      .split(',')
      .map((selector) => selector.trim().replace(/\.col/g, '.cell.col'));
    rules.push(...chunkedRules(selectors, rule.slice(open)));
  }
  if (rules.length === 0) {
    return '';
  }
  // The rules go in as text, never as markup. A workbook names its own
  // fonts, and a name is written into these rules as it is.
  return html`<style>
    ${rules.join('\n')}
  </style>`;
}

/**
 * What of a cell's style the editor over it wears: how its text looks, and
 * its fill, so typing over a yellow cell stays on yellow. Not its borders,
 * indent or wrapping - those are the cell's box, and the editor keeps a box
 * of its own.
 */
const EDITOR_PROPERTIES: ReadonlySet<string> = new Set([
  'background-color',
  'color',
  'font-family',
  'font-size',
  'font-style',
  'font-weight',
  'text-align',
  'text-decoration',
  '--default-font-family',
  '--default-font-size'
]);

/** The declarations of a style body the editor wears, in order. */
function editorDeclarations(body: string | undefined): string[] {
  const out: string[] = [];
  for (const declaration of (body ?? '').split(';')) {
    const colon = declaration.indexOf(':');
    if (colon > 0 && EDITOR_PROPERTIES.has(declaration.slice(0, colon).trim())) {
      out.push(declaration.trim());
    }
  }
  return out;
}

/**
 * Inline style that dresses the editor as the cell it edits.
 *
 * The user types in the cell's font, colour, fill and alignment. The cell's
 * own classes would bring its borders and indent too, and holding those off would take
 * `!important` - which no `::part(cell-input)` rule of an application could
 * then override. So the styles are gathered here instead, in the order the
 * rules of {@link buildCellStyleBlock} and the element's `.cell.r` resolve
 * for the cell: the default, its own style, the conditional formats it
 * matches, the right alignment of a number, and - for a cell with no style
 * of its own - its row or column style, whose rule is the most specific.
 */
function editorCellStyle(state: CellGridRenderState, col: number, row: number): string {
  const classes = (state.cellData.get(toKey(col, row))?.cellStyle ?? 'cs0').split(/\s+/);
  const styles = state.cellStyleToCSSStyle;
  const indexes = (prefix: string): number[] =>
    classes
      .filter((c) => new RegExp(`^${prefix}\\d+$`).test(c))
      .map((c) => Number(c.slice(prefix.length)))
      .sort((a, b) => a - b);

  // The default's font reaches the editor through the documented
  // properties its own rule reads, as the default rule does for a cell.
  const declarations = editorDeclarations(styles[0]).filter((d) => !/^font-(family|size)\s*:/.test(d));
  const own = indexes('cs')[0] ?? 0;
  if (own !== 0) {
    declarations.push(...editorDeclarations(styles[own]));
  }
  for (const index of indexes('cf')) {
    declarations.push(...editorDeclarations(state.conditionalFormattingStyles?.[index]));
  }
  if (classes.includes('r')) {
    declarations.push('text-align: right');
  }
  if (own === 0) {
    const lines = [state.rowIndexToStyleIndex?.[row], state.columnIndexToStyleIndex?.[col]];
    for (const index of lines.filter((i): i is number => !!i).sort((a, b) => a - b)) {
      declarations.push(...editorDeclarations(styles[index]));
    }
  }
  return declarations.map((d) => `${d};`).join('');
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

/**
 * Build the sheet-overlay rendering — one element per positioned
 * overlay. IMAGE overlays render as {@code <img class="sheet-
 * overlay sheet-overlay-image" src="...">}. COMPONENT overlays
 * render as a placeholder {@code <div class="sheet-overlay
 * sheet-overlay-component" data-overlay-id="...">} pending
 * Vaadin-component slotting in a later slice.
 *
 * Keyed on the overlay id. The list arrives from the server's overlay map,
 * whose iteration order shifts whenever an entry is removed and re-added —
 * which is what scrolling one out of view and back does. Rendered
 * positionally, a reordering makes Lit rewrite each {@code <img>} in place,
 * so every image re-downloads and flickers even though nothing about it
 * changed. Keyed, Lit moves the existing elements instead.
 */
function buildSheetOverlays(state: CellGridRenderState, quadrant: QuadrantRenderOpts['quadrantName']): unknown {
  // Defensive default — some test fixtures do not declare the
  // overlays array. Real connector pushes always populate the field.
  //
  // Each in the pane its anchor cell is in, so a picture anchored in a frozen row stays with that row. The frozen
  // panes' coordinates start where the sheet does, so the position the
  // connector worked out serves in any pane.
  const overlays = (state.overlays ?? []).filter(
    (overlay) =>
      quadrantOf(overlay.col, overlay.row, state.horizontalSplitPosition, state.verticalSplitPosition) === quadrant
  );
  return keyedList(
    overlays,
    (overlay) => overlay.id,
    (overlay) => {
      const style =
        `left:${overlay.leftPx}px;top:${overlay.topPx}px;` + `width:${overlay.widthPx}px;height:${overlay.heightPx}px;`;
      if (overlay.type === 'IMAGE') {
        const src = overlay.src ?? '';
        return html`
          <img
            class="sheet-overlay sheet-overlay-image"
            data-overlay-id=${overlay.id}
            data-col=${overlay.col}
            data-row=${overlay.row}
            src=${src}
            style=${style}
          />
        `;
      }
      // COMPONENT overlay — a chart or any component the server put on the
      // sheet. Flow created the element and left it out of the document;
      // the connector parents it to this element and points its `slot`
      // attribute here. The wrapper div carries the geometry so the
      // component itself only has to fill it.
      return html`
        <div
          class="sheet-overlay sheet-overlay-component"
          data-overlay-id=${overlay.id}
          data-col=${overlay.col}
          data-row=${overlay.row}
          style=${style}
        >
          <slot name=${overlayComponentSlotName(overlay.id)}></slot>
        </div>
      `;
    }
  );
}

/**
 * Build the popup-button rendering. One small
 * triangle/icon per anchor cell at its bottom-right corner.
 * Click on a button dispatches `popup-button-click { col, row }`
 * which the connector translates to a `popupButtonClick`
 * spreadsheet-event for the server.
 */
function buildPopupButtons(state: CellGridRenderState, quadrant: QuadrantRenderOpts['quadrantName']): TemplateResult[] {
  // In the pane of the cell each belongs to, like the sheet overlays: a
  // filter button in a frozen header row stays in that row on a scroll.
  const buttons = (state.popupButtons ?? []).filter(
    (b) => quadrantOf(b.col, b.row, state.horizontalSplitPosition, state.verticalSplitPosition) === quadrant
  );
  return buttons.map((b) => {
    const className = b.active ? 'popup-button active' : 'popup-button';
    // The row's height rides along for a theme whose button is as tall as
    // its cell.
    const style = `left:${b.leftPx}px;top:${b.topPx}px;--_cell-height:${b.cellHeightPx ?? 20}px;`;
    const press = (button: Element, keyboard: boolean) => {
      button.dispatchEvent(
        new CustomEvent('popup-button-click', {
          detail: { col: b.col, row: b.row, keyboard },
          bubbles: true,
          composed: true
        })
      );
    };
    const onClick = (e: Event) => {
      e.stopPropagation();
      press(e.currentTarget as Element, false);
    };
    // A button to assistive technology and the keyboard, pressed with Enter
    // or Space like any button. The
    // keys are its own: the sheet would edit the cell with them.
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key !== 'Enter' && e.key !== ' ') {
        return;
      }
      e.preventDefault();
      e.stopPropagation();
      press(e.currentTarget as Element, true);
    };
    return html`
      <div
        class=${className}
        part="popup-button"
        role="button"
        tabindex="0"
        aria-label="Open pop-up"
        style=${style}
        data-col=${b.col}
        data-row=${b.row}
        @click=${onClick}
        @keydown=${onKeyDown}
      ></div>
    `;
  });
}

customElements.define('vaadin-spreadsheet-internal-cell-grid', SpreadsheetCellGrid);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-cell-grid': SpreadsheetCellGrid;
  }
}
