/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css } from 'lit';
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
} from '../ThemeProperties.js';

/**
 * The cell grid's look: the cells, the outlines of the selection, the fill
 * and the formula's references, the frozen panes and their lines, the notes,
 * the corner markers, the filter buttons and the editor.
 */
export const cellGridStyles = css`
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
   its cells with it in one removal. A box of no size where its pane
   starts, which its cells are placed in as they would be in the pane:
   a row that comes into view is laid out on its own. With no box of its
   own, every cell of the pane was laid out again with it. */
  .cell-row {
    position: absolute;
    left: 0;
    top: 0;
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
    /* The outline and the handle as one layer, at the level its pieces
     hold among the cells, the pictures and the filter buttons. */
    z-index: 2;
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
  .comment-overlay.animate-in {
    animation: overlay-animate-in 120ms;
  }

  .comment-overlay.animate-out {
    animation: overlay-animate-out 120ms;
  }

  /* Comment notes. Anchored at the cell's top-right and above every
   cell, since the point is to be readable over the sheet. */
  /* A comment: a white card with a soft
   shadow, the author above the text in small bold type. */
  .comment-overlay {
    z-index: 20;
    padding: 7px;
    box-sizing: border-box;
    pointer-events: none;
    color: #474747;
    background: ${commentOverlayBg};
    border: 0;
    border-radius: 4px;
    /* The edge is the last shadow, a hairline that takes no room. */
    box-shadow: 0 4px 10px 0 rgba(0, 0, 0, 0.1), 0 3px 5px 0 rgba(0, 0, 0, 0.05), 0 0 0 1px ${commentOverlayBorderColor};
  }

  .comment-overlay .comment-overlay-label,
  .comment-overlay .comment-overlay-invalidformula {
    max-width: 168px;
    max-height: 140px;
    overflow: hidden;
    white-space: pre-wrap;
    overflow-wrap: break-word;
  }

  .comment-overlay .comment-overlay-invalidformula {
    color: #ed473b;
  }

  /* Between an error and the comment under it. */
  .comment-overlay .comment-overlay-separator {
    border-bottom: 1px solid #c7c7c7;
    margin-bottom: 7px;
  }

  /*
 * The note being written. Sized like the note it replaces so the
 * overlay does not jump as it opens.
 */
  .comment-overlay .comment-overlay-edit {
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

  .comment-overlay .comment-overlay-author {
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
  /* Dimmed while the sheet does not have the keyboard; the connector
   sets the attribute. All of it at once: dimmed piece by piece, the
   outline would show darker where its sides meet each other and the
   handle. */
  :host(:not([sheet-focused])) .sheet-selection {
    opacity: 0.6;
  }
  /* The outline's four sides, in the selection colour. Above the cells,
   which carry z-index 1 and paint an opaque background: without this the
   outline is in the DOM, correctly sized, and invisible - every cell
   covers it. No higher: a frozen pane, at 2 and later in the tree, covers
   the piece of the selection that scrolls under it. */
  .sheet-selection .s-top,
  .sheet-selection .s-bottom,
  .sheet-selection .s-left,
  .sheet-selection .s-right {
    position: absolute;
    background: ${selectionColor};
    z-index: 2;
  }
  .sheet-selection .s-top {
    top: 0;
    left: 0;
    width: 100%;
    height: 2px;
  }
  .sheet-selection .s-bottom {
    bottom: 0;
    left: 0;
    width: 100%;
    height: 2px;
  }
  .sheet-selection .s-left {
    top: 0;
    left: 0;
    width: 2px;
    height: 100%;
  }
  .sheet-selection .s-right {
    top: 0;
    right: 0;
    width: 2px;
    height: 100%;
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
  /* Used by touch, the corner handle stretches the selection: a round dot
   in the outline's colour, with 30 pixels round it a finger can find. A
   drag from it scrolls nothing. */
  .sheet-selection.touch .selection-corner {
    right: -5px;
    bottom: -5px;
    width: 9px;
    height: 9px;
    border-radius: 50%;
    background: ${selectionColor};
    outline: none;
    cursor: default;
    touch-action: none;
  }
  /* Once "Fill" is chosen: a fill handle at the middle of each side, a
   square in the fill colour ringed in white, with 30 pixels round it a
   finger can find. A drag from one scrolls nothing. */
  .sheet-selection .fill-touch-square {
    position: absolute;
    box-sizing: content-box;
    width: 6px;
    height: 6px;
    margin: -5px 0 0 -5px;
    background: ${selectionBorderColor};
    border: 2px solid #fff;
    pointer-events: auto;
    touch-action: none;
    z-index: 2;
  }
  .sheet-selection.touch .selection-corner::before,
  .sheet-selection .fill-touch-square::before {
    content: '';
    position: absolute;
    inset: -11px;
  }
  .sheet-selection .fill-touch-square.top {
    left: 50%;
    top: 0;
  }
  .sheet-selection .fill-touch-square.bottom {
    left: 50%;
    top: 100%;
  }
  .sheet-selection .fill-touch-square.left {
    left: 0;
    top: 50%;
  }
  .sheet-selection .fill-touch-square.right {
    left: 100%;
    top: 50%;
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

  /* Frozen-pane wrappers. Each frozen quadrant lives in its own wrapper, a
       box of no size where the grid starts, which its cells are placed in
       as they are in the grid. The wrapper is sticky, at the insets the grid
       gives it: the browser holds it there as it scrolls the sheet, in the
       same frame, so the pane stays on its edge of the view however fast
       the sheet scrolls. Of no size, it takes no room among the cells. */
  .quadrant-wrapper {
    position: sticky;
    width: 0;
    height: 0;
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
       quadrants inherit z-index from their wrapper (each wrapper,
       being sticky, is its own stacking context), so the
       cell-level rule below only takes effect for main cells. */
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

  /* The corner markers: 9px triangles in the cell's top-right corner,
   inside its border, so a cell with a thicker border keeps all of it -
   red for a formula that could not be evaluated, amber for a comment. */
  .invalidformula-indicator,
  .comment-indicator {
    position: absolute;
    right: 0;
    top: 0;
    width: 9px;
    height: 9px;
    clip-path: polygon(0 0, 100% 0, 100% 100%);
    z-index: 5;
    pointer-events: none;
  }

  .invalidformula-indicator {
    background: ${invalidformulaBg};
  }

  .comment-indicator {
    background: ${commentBg};
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
`;

/**
 * The cell grid in SpreadsheetTheme.LUMO. The colours come from the
 * documented properties' Lumo defaults on the host; the rest of the look is
 * here. After {@link cellGridStyles}, which these override.
 */
export const cellGridLumoStyles = css`
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

  :host([theme~='lumo']) .fill-touch-square {
    border-color: var(--lumo-base-color);
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

  :host([theme~='lumo']) .comment-overlay {
    padding: var(--lumo-space-s);
    border: 1px solid ${commentOverlayBorderColor};
    border-radius: var(--lumo-border-radius-m);
    color: var(--lumo-body-text-color);
    box-shadow: var(--lumo-box-shadow-m);
  }

  :host([theme~='lumo']) .comment-overlay .comment-overlay-author {
    padding-bottom: var(--lumo-space-s);
    font-size: var(--lumo-font-size-xs);
  }

  :host([theme~='lumo']) .comment-overlay .comment-overlay-invalidformula {
    color: var(--lumo-error-color);
  }

  :host([theme~='lumo']) .comment-overlay .comment-overlay-edit {
    font-family: var(--lumo-font-family);
    font-size: var(--lumo-font-size-s);
  }

  :host([theme~='lumo']) .comment-overlay .comment-overlay-separator {
    border-bottom-color: var(--lumo-contrast-20pct);
  }

  :host([theme~='lumo']) .comment-overlay-line {
    background: var(--lumo-contrast-10pct);
  }
`;
