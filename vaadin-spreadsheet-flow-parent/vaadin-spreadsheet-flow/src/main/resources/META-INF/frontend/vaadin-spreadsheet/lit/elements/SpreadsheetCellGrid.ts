/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, nothing, type TemplateResult } from 'lit';
import type { DirectiveResult } from 'lit/directive.js';
import { repeat } from 'lit/directives/repeat.js';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';

import { parseColRow, toKey } from '../algorithms/CellKeys.js';
import { caretPosition } from '../algorithms/PercentageEntry.js';
import type { CellData } from '../types/wire.js';
import { overlayComponentSlotName } from '../dom/VirtualChildren.js';
import { isActiveCell, isCellSelected } from '../state/SelectionState.js';
import { type CellGridRenderState, emptyCellGridRenderState } from './types.js';

/**
 * Internal cell-grid Lit element. Renders absolutely-positioned cell
 * divs for the visible viewport portion of a spreadsheet.
 *
 * The element is driven entirely by its {@link renderState} property
 * — no internal state, no direct interaction with the connector.
 * The Spreadsheet connector builds a fresh {@code CellGridRenderState}
 * on every scroll / data update / selection change and assigns it
 * here. Lit's diff handles cell-element churn.
 *
 * This is slice 132's minimal render: cell positions + text content,
 * no styling, no merged regions, no selection overlays. Subsequent
 * slices fill in:
 * - Slice 135: per-cell CSS styling
 * - Slice 136: selection overlays
 * - Slice 137: merged-cell overlays + frozen-pane geometry
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
export class SpreadsheetCellGrid extends LitElement {
  /**
   * Parts this element re-exports to the tree above it.
   *
   * `::part()` crosses exactly one shadow boundary. Every part the
   * spreadsheet documents is emitted inside one of these sub-elements,
   * which sit inside the component's own shadow root — two boundaries
   * away from an application's stylesheet. Without re-exporting them,
   * `vaadin-spreadsheet::part(cell)` matches nothing, and the whole
   * documented styling API silently does nothing.
   */
  static readonly EXPORTED_PARTS =
    'cell,cell-range,cell-input,comment-triangle,invalid-triangle,popup-button,selection-corner';

  static properties = {
    renderState: { attribute: false },
    tooNarrowCells: { attribute: false }
  };

  static styles = css`
    :host {
      display: block;
      position: relative;
      overflow: hidden;
    }

    .cell {
      position: absolute;
      box-sizing: border-box;
      overflow: hidden;
      white-space: nowrap;
      /* A cell is a surface, not a window: it has a ground of its own,
         so a conditional format that stops matching leaves the cell
         white rather than transparent, and a cell under a merge or an
         overlay does not show what is behind the sheet. A cs<N> or
         cf<N> rule wins over this, being the more specific selector. */
      background: var(--vaadin-spreadsheet-cell-background, #fff);
      border-right: 1px solid var(--vaadin-spreadsheet-cell-border-color, #c7c7c7);
      border-bottom: 1px solid var(--vaadin-spreadsheet-cell-border-color, #c7c7c7);
      font-family: var(--vaadin-spreadsheet-cell-font, inherit);
      font-size: var(--vaadin-spreadsheet-cell-font-size, inherit);
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
      background: var(--vaadin-spreadsheet-cell-background, #fff);
    }

    /* Comment notes. Anchored at the cell's top-right and above every
       cell, since the point is to be readable over the sheet. */
    .v-spreadsheet-comment-overlay {
      z-index: 20;
      max-width: 260px;
      padding: 4px 6px;
      box-sizing: border-box;
      pointer-events: none;
      white-space: pre-wrap;
      background: var(--vaadin-spreadsheet-comment-overlay-bg, #ffffe1);
      border: 1px solid var(--vaadin-spreadsheet-comment-overlay-border-color, #c0c0c0);
      box-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
      font-size: 0.9em;
    }

    /*
     * The note being written. Sized like the note it replaces so the
     * overlay does not jump as it opens.
     */
    .v-spreadsheet-comment-overlay .comment-overlay-edit {
      display: block;
      box-sizing: border-box;
      width: 160px;
      height: 60px;
      resize: none;
      border: 0;
      background: transparent;
      font: inherit;
      color: inherit;
      outline: none;
    }

    .v-spreadsheet-comment-overlay .comment-overlay-author {
      font-weight: bold;
    }

    /* The fill handle's drag outline. Dashed, so it reads as a preview
       rather than a second selection. */
    .paintmode {
      position: absolute;
      pointer-events: none;
      box-sizing: border-box;
      border: 1px dashed var(--vaadin-spreadsheet-selection-border-color, #217346);
      z-index: 3;
    }
    .sheet-selection .s-top {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 2px;
      background: var(--vaadin-spreadsheet-selection-color, #1976d2);
      /* Above the cells, which carry z-index 1 and paint an
         opaque background. Without this the outline is in the DOM,
         correctly sized, and invisible - every cell covers it. */
      z-index: 3;
    }
    .sheet-selection .s-bottom {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 2px;
      background: var(--vaadin-spreadsheet-selection-color, #1976d2);
      /* Above the cells, which carry z-index 1 and paint an
         opaque background. Without this the outline is in the DOM,
         correctly sized, and invisible - every cell covers it. */
      z-index: 3;
    }
    .sheet-selection .s-left {
      position: absolute;
      top: 0;
      left: 0;
      width: 2px;
      height: 100%;
      background: var(--vaadin-spreadsheet-selection-color, #1976d2);
      /* Above the cells, which carry z-index 1 and paint an
         opaque background. Without this the outline is in the DOM,
         correctly sized, and invisible - every cell covers it. */
      z-index: 3;
    }
    .sheet-selection .s-right {
      position: absolute;
      top: 0;
      right: 0;
      width: 2px;
      height: 100%;
      background: var(--vaadin-spreadsheet-selection-color, #1976d2);
      /* Above the cells, which carry z-index 1 and paint an
         opaque background. Without this the outline is in the DOM,
         correctly sized, and invisible - every cell covers it. */
      z-index: 3;
    }
    .sheet-selection .selection-corner {
      position: absolute;
      right: -3px;
      bottom: -3px;
      width: 7px;
      height: 7px;
      background: var(--vaadin-spreadsheet-selection-color, #1976d2);
      cursor: crosshair;
      pointer-events: auto;
      /* Above the cells, which carry z-index 1. Without this the handle
         paints and can be found in the DOM but the cell under it wins
         every hit test, so a real drag selects cells instead of filling
         them. The border divs above are raised for the same reason. */
      z-index: 3;
    }
    /* Cells inside the active range get a subtle background
           highlight by default; matches the previous selected-cell-
           highlight affordance. */
    /* The selection tint over a cell has to sit on top of whatever the
       cell is already painted, not replace it: a background would hide a
       conditional format's fill for as long as the cell is selected, and
       Excel shows the fill through the selection. An inset shadow spread
       over the whole box tints instead of replacing. */
    .cell.cell-range {
      box-shadow: inset 0 0 0 9999px var(--vaadin-spreadsheet-cell-range-bg, rgba(25, 118, 210, 0.1));
    }

    /* The active range's overlay has nothing of its own underneath, so it
       paints the tint directly. Matched by class rather than by part: the
       cells carry part="cell cell-range" as well, and a background on them
       is what hid their fill. */
    .sheet-selection {
      background: var(--vaadin-spreadsheet-cell-range-bg, rgba(25, 118, 210, 0.1));
    }

    /* Frozen-pane wrappers (slice 141). Each frozen quadrant lives
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
       selection outline at 3. */
    .cell.overflowing {
      overflow: visible;
      z-index: 2;
    }

    /* Holds the text at the width the server worked out, so the browser
       does the clipping at the far end of the spill instead of at the
       cell's own edge. */
    .cell.overflowing .cell-spill {
      overflow: hidden;
      text-overflow: ellipsis;
      pointer-events: none;
    }

    /* Sheet overlays — images, charts, and embedded
           components positioned at absolute pixel coordinates on
           the sheet. Slice 160. z-index above cells but below
           the cell editor (which is 10). */
    .sheet-overlay {
      position: absolute;
      z-index: 4;
      box-sizing: border-box;
      pointer-events: auto;
    }
    .sheet-overlay-image {
      display: block;
    }

    /* Comment indicator — small red triangle in the top-right
           of a cell with a comment (slice 165). The triangle is
           drawn via border + clip-path so no extra image asset
           is needed. Excel renders red; gnumeric+LibreOffice
           render yellow. We follow Excel. */
    /* Invalid-formula indicator — Excel draws a small marker in the
       cell's top-LEFT corner when a formula cannot be evaluated,
       mirroring the comment triangle in the top-right. */
    .invalidformula-indicator {
      position: absolute;
      left: 0;
      top: 0;
      width: 8px;
      height: 8px;
      background: var(--vaadin-spreadsheet-invalidformula-bg, #d32f2f);
      clip-path: polygon(0 0, 0 100%, 100% 0);
      z-index: 5;
      pointer-events: none;
    }

    .comment-indicator {
      position: absolute;
      width: 8px;
      height: 8px;
      transform: translate(-100%, 0);
      background: var(--vaadin-spreadsheet-comment-bg, #d32f2f);
      clip-path: polygon(100% 0, 100% 100%, 0 0);
      z-index: 5;
      pointer-events: none;
    }

    /* Hyperlink cells get a visual underline + color hint
           (slice 165). The actual click handling lives in the
           connector; this style just matches the Excel convention. */
    .cell.link-cell {
      color: var(--vaadin-spreadsheet-link-fg, #1976d2);
      text-decoration: underline;
      cursor: pointer;
    }

    /* Popup-button anchor — small triangle in the bottom-right
           of a cell that opens a popup on click (slice 161). The
           popup itself is provided by the Flow wrapper via slot;
           this is just the click target + visual indicator. */
    .popup-button {
      position: absolute;
      width: 12px;
      height: 12px;
      transform: translate(-100%, -100%);
      background: var(--vaadin-spreadsheet-popup-button-bg, #1976d2);
      color: var(--vaadin-spreadsheet-popup-button-fg, #fff);
      cursor: pointer;
      font-size: 8px;
      text-align: center;
      line-height: 12px;
      z-index: 6;
      user-select: none;
    }
    .popup-button.active {
      background: var(--vaadin-spreadsheet-popup-button-active-bg, #0d47a1);
    }

    /* Cell editor input — absolutely positioned over the cell
           being edited, sized to match. z-index above all cells +
           overlays so the input + caret are always on top. Slice
           152. */
    .cell-editor {
      position: absolute;
      box-sizing: border-box;
      z-index: 10;
      border: 2px solid var(--vaadin-spreadsheet-editor-border, #1976d2);
      outline: none;
      font-family: var(--vaadin-spreadsheet-cell-font, inherit);
      font-size: var(--vaadin-spreadsheet-cell-font-size, inherit);
      padding: 0 2px;
    }
  `;

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
   * replaced with hashes, which is what the GWT client did too.
   */
  declare tooNarrowCells: ReadonlySet<string>;
  /** Hidden span the measuring is done in. */
  private valueProbe: HTMLSpanElement | null = null;

  constructor() {
    super();
    this.renderState = emptyCellGridRenderState();
    this.tooNarrowCells = new Set();
  }

  connectedCallback(): void {
    super.connectedCallback();
    // Set on attach rather than in the constructor: a custom element
    // may not have attributes when it is created.
    this.setAttribute('exportparts', SpreadsheetCellGrid.EXPORTED_PARTS);
  }

  /**
   * Wire up click delegation on first render — the shadow root
   * doesn't exist until Lit has produced its first render. A
   * single listener at the shadow-root level finds the clicked
   * `.cell` ancestor and dispatches a {@code cell-click} custom
   * event with the cell's coordinate + modifier-key state. The
   * connector listens for this event to update its selection
   * state + push a fresh render. Slice 146.
   */
  firstUpdated(): void {
    this.shadowRoot!.addEventListener('click', this.handleShadowClick);
    // Double-click delegation for cell-editor activation
    // (slice 152). Same pattern as the single-click handler;
    // dispatches `cell-doubleclick` so the connector can open
    // its editor at the dbl-clicked cell.
    this.shadowRoot!.addEventListener('dblclick', this.handleShadowDoubleClick);
    // Mouse drag selection (slice 155). The cell-grid emits
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
    // Right-click delegation (slice 166). preventDefault stops
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
    if ((e as KeyboardEvent).key === 'Escape') {
      e.preventDefault();
      target.blur();
    }
  };

  /**
   * After every render, focus the editor input if one is now
   * active. The browser auto-focus attribute only works once on
   * insertion; this hook focuses on every transition into edit
   * mode so re-activating an editor at a new cell still places
   * the caret inside.
   */
  /**
   * The render state with this element's own measurements folded in.
   */
  private measuredState(): CellGridRenderState {
    return { ...this.renderState, tooNarrowCells: this.tooNarrowCells };
  }

  /**
   * Measures the cells the server flagged as possibly too wide.
   * <p>
   * The server judges width by one character's width times the character
   * count, which is too coarse to replace a value with hashes on: it calls a
   * date that fits too wide. So it flags candidates and the real width is
   * measured here, the way the GWT client did. Only the flagged cells are
   * measured, and in a probe rather than in the cells themselves - a cell
   * already showing hashes measures as narrow and would flip back.
   */
  private remeasureTooNarrowCells(): void {
    const { cellData, viewport, colWidths } = this.renderState;
    const measured = new Set<string>();
    for (const [key, data] of cellData) {
      if (data.needsMeasure !== true) {
        continue;
      }
      const width = colWidths[data.col - viewport.firstColumnIndex] ?? 0;
      if (width > 0 && this.measureValue(data.cellStyle, data.value ?? '') > width) {
        measured.add(key);
      }
    }
    if (!sameKeys(measured, this.tooNarrowCells)) {
      this.tooNarrowCells = measured;
    }
  }

  /** What a value would be drawn at, in its own style. */
  private measureValue(cellStyle: string, value: string): number {
    if (this.valueProbe === null) {
      const probe = document.createElement('span');
      // Out of flow and off-screen so measuring never disturbs the sheet.
      probe.style.cssText = 'position:absolute;left:-10000px;top:0;visibility:hidden;width:auto;white-space:nowrap;';
      this.shadowRoot!.appendChild(probe);
      this.valueProbe = probe;
    }
    this.valueProbe.className = `cell ${cellStyle}`;
    this.valueProbe.textContent = value;
    return this.valueProbe.clientWidth;
  }

  updated(changedProps: Map<string, unknown>): void {
    if (changedProps.has('renderState')) {
      this.remeasureTooNarrowCells();
    }
    const note = this.shadowRoot!.querySelector('.comment-overlay-edit') as HTMLTextAreaElement | null;
    if (note !== null && this.shadowRoot!.activeElement !== note) {
      note.focus();
      note.setSelectionRange(note.value.length, note.value.length);
    }
    if (this.renderState.editor && changedProps.has('renderState')) {
      const input = this.shadowRoot!.querySelector('.cell-editor') as HTMLInputElement | null;
      if (input && document.activeElement !== input) {
        input.focus();
        // Caret at the end so typing extends the existing value (Excel
        // convention), except before a trailing percent sign: a cell opened
        // on "5%" has to take the next digit into the number, not after the
        // sign.
        const caret = caretPosition(input.value);
        input.setSelectionRange(caret, caret);
      }
    }
  }

  /**
   * Mousedown handler — emits {@code cell-mousedown} so the
   * connector can mark the start of a drag-select. The
   * connector decides whether to consume the event vs let the
   * normal click handler run (Excel: mousedown immediately
   * selects, then drag extends, then mouseup commits the
   * range). Slice 155.
   */
  private readonly handleShadowMouseDown = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }
    // The fill handle sits on top of the cell under it, and a drag from it
    // paints rather than selects — so it is checked first and the event
    // goes no further.
    if (target.closest('.selection-corner')) {
      e.preventDefault();
      this.dispatchEvent(new CustomEvent('selection-paint-start', { bubbles: true, composed: true }));
      return;
    }
    const cell = target.closest<HTMLElement>('.cell');
    if (!cell) {
      return;
    }
    const col = Number(cell.dataset.col);
    const row = Number(cell.dataset.row);
    if (!Number.isFinite(col) || !Number.isFinite(row) || col < 1 || row < 1) {
      return;
    }
    const ev = e as MouseEvent;
    this.dispatchEvent(
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
        composed: true
      })
    );
  };

  /**
   * Mouseover handler — emits {@code cell-mouseover} when the
   * mouse enters a new cell. Fires on every hover (not just
   * during drag); the connector filters by checking its
   * dragging flag. Slice 155.
   */
  /** The cell the pointer is over, or null. Presentation-only state. */
  private hoveredKey: string | null = null;

  private setHoveredKey(key: string | null): void {
    if (this.hoveredKey === key) {
      return;
    }
    this.hoveredKey = key;
    this.requestUpdate();
  }

  private readonly handleGridMouseLeave = (): void => {
    this.setHoveredKey(null);
  };

  private readonly handleShadowMouseOver = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }
    const cell = target.closest<HTMLElement>('.cell');
    if (!cell) {
      return;
    }
    const col = Number(cell.dataset.col);
    const row = Number(cell.dataset.row);
    if (!Number.isFinite(col) || !Number.isFinite(row) || col < 1 || row < 1) {
      return;
    }
    // Which cell the pointer is over decides whether a comment shows, and
    // that is presentation only — the connector never needs to know, and
    // routing it through a full render-state push would repaint the sheet
    // on every pointer move.
    this.setHoveredKey(toKey(col, row));
    this.dispatchEvent(
      new CustomEvent('cell-mouseover', {
        detail: { col, row },
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
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }
    const cell = target.closest<HTMLElement>('.cell');
    if (!cell) {
      return;
    }
    const col = Number(cell.dataset.col);
    const row = Number(cell.dataset.row);
    if (!Number.isFinite(col) || !Number.isFinite(row) || col < 1 || row < 1) {
      return;
    }
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
   * only menu shown. Slice 166.
   */
  private readonly handleShadowContextMenu = (event: Event): void => {
    const e = event as MouseEvent;
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }
    const cell = target.closest<HTMLElement>('.cell');
    if (!cell) {
      return;
    }
    const col = Number(cell.dataset.col);
    const row = Number(cell.dataset.row);
    if (!Number.isFinite(col) || !Number.isFinite(row) || col < 1 || row < 1) {
      return;
    }
    e.preventDefault();
    this.dispatchEvent(
      new CustomEvent('cell-contextmenu', {
        detail: { col, row, clientX: e.clientX, clientY: e.clientY },
        bubbles: true,
        composed: true
      })
    );
  };

  private readonly handleShadowClick = (e: Event): void => {
    const ev = e as MouseEvent;
    const target = ev.target;
    // The event target may be the ShadowRoot when the listener
    // catches synthetic events that don't originate from a node
    // inside the tree. ShadowRoot doesn't implement `closest`,
    // so we narrow to Element first.
    if (!(target instanceof Element)) {
      return;
    }
    const cell = target.closest<HTMLElement>('.cell');
    if (!cell) {
      return;
    }
    const col = Number(cell.dataset.col);
    const row = Number(cell.dataset.row);
    if (!Number.isFinite(col) || !Number.isFinite(row) || col < 1 || row < 1) {
      return;
    }
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
    const { viewport, mergedRegions, verticalSplitPosition, horizontalSplitPosition, scrollLeft, scrollTop } =
      this.renderState;

    if (viewport.lastColumnIndex < viewport.firstColumnIndex || viewport.lastRowIndex < viewport.firstRowIndex) {
      // Empty viewport — render an empty host. (The connector
      // may push an empty state during initial measurement.)
      return html``;
    }

    // Main (bottom-right) quadrant: scrolling rows + cols.
    // Emitted as direct children of the cell-grid so cells sit at
    // their virtual coordinates and scroll naturally with the
    // scroll container.
    const mainCells = renderQuadrant({
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
    });

    // Frozen quadrants live inside transform-translated wrappers
    // so their cells visually stay pinned as the user scrolls
    // (slice 141). The wrapper transform counters the scroll
    // container's shift; cells inside use coordinates relative
    // to the wrapper, which is itself anchored at the cell-grid
    // origin. CSS z-index on the wrappers stacks corner > top +
    // left > main.

    let topQuadrant: TemplateResult | string = '';
    if (verticalSplitPosition > 0) {
      const topCells = renderQuadrant({
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
      });
      const transform = `transform:translateY(${scrollTop}px);`;
      topQuadrant = html` <div class="quadrant-wrapper quadrant-wrapper-top" style=${transform}> ${topCells} </div> `;
    }

    let leftQuadrant: TemplateResult | string = '';
    if (horizontalSplitPosition > 0) {
      const leftCells = renderQuadrant({
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
      });
      const transform = `transform:translateX(${scrollLeft}px);`;
      leftQuadrant = html`
        <div class="quadrant-wrapper quadrant-wrapper-left" style=${transform}> ${leftCells} </div>
      `;
    }

    let cornerQuadrant: TemplateResult | string = '';
    if (verticalSplitPosition > 0 && horizontalSplitPosition > 0) {
      const cornerCells = renderQuadrant({
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
      });
      const transform = `transform:translate(${scrollLeft}px, ${scrollTop}px);`;
      cornerQuadrant = html`
        <div class="quadrant-wrapper quadrant-wrapper-corner" style=${transform}> ${cornerCells} </div>
      `;
    }

    const styleBlock = buildCellStyleBlock(this.renderState);
    const mergedOverlays = buildMergedCellOverlays(this.renderState);
    const selectionOverlay = buildSelectionOverlay(this.renderState);
    const editor = buildCellEditorOverlay(this.renderState);
    const sheetOverlays = buildSheetOverlays(this.renderState);
    const popupButtons = buildPopupButtons(this.renderState);
    const paintOutline = buildPaintOutline(this.renderState);
    const commentOverlays = buildCommentOverlays(this.renderState, this.hoveredKey);
    return html`${styleBlock}${mainCells}${topQuadrant}${leftQuadrant}${cornerQuadrant}${mergedOverlays}${selectionOverlay}${paintOutline}${sheetOverlays}${popupButtons}${commentOverlays}${editor}`;
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

/**
 * Render the cells of a single quadrant. Each cell carries a
 * {@code quadrant-<name>} class so CSS can apply z-index stacking
 * (corner > top, left > main). The per-cell position comes from the
 * caller's {@code getColLeft/getRowTop/getColWidth/getRowHeight}
 * closures — different quadrants use different position arrays
 * (frozen arrays vs main arrays).
 */
/**
 * Every coordinate a merged region covers, its anchor included.
 *
 * These cells are still rendered — an application, and the tests, ask
 * about a cell by its coordinates whether or not a merge sits over it —
 * but the merge owns the region's content, so they render their text and
 * nothing else. A second slot of the same name underneath would capture
 * the component and hide it below the merge.
 */
/** Whether two key sets hold the same keys. */
function sameKeys(a: ReadonlySet<string>, b: ReadonlySet<string>): boolean {
  if (a.size !== b.size) {
    return false;
  }
  for (const key of a) {
    if (!b.has(key)) {
      return false;
    }
  }
  return true;
}

function coveredByMergeCoords(
  mergedRegions: ReadonlyArray<{ col1: number; col2: number; row1: number; row2: number }>
): Set<string> {
  const covered = new Set<string>();
  for (const region of mergedRegions) {
    for (let row = region.row1; row <= region.row2; row++) {
      for (let col = region.col1; col <= region.col2; col++) {
        covered.add(toKey(col, row));
      }
    }
  }
  return covered;
}

function renderQuadrant(opts: QuadrantRenderOpts): TemplateResult[] {
  const { state, quadrantName, colStart, colEnd, rowStart, rowEnd, getColLeft, getColWidth, getRowTop, getRowHeight } =
    opts;
  const { cellData, selection } = state;

  const covered = coveredByMergeCoords(state.mergedRegions);
  const cells: TemplateResult[] = [];
  for (let row = rowStart; row <= rowEnd; row++) {
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
      // Text too wide for its column runs into the empty cells beside it.
      // The server settles how far, because it is the side that knows what
      // the neighbours hold; a cell showing a component or sitting under a
      // merge keeps to itself.
      const spill = slotName === undefined && !underMerge ? data?.overflowWidth ?? 0 : 0;
      const overflowing = spill > width;
      const text = overflowing ? html`<div class="cell-spill" style="width:${spill}px">${rawText}</div>` : rawText;
      const cellStyleClass = data?.cellStyle ?? '';
      const inSelection = isCellSelected(selection, col, row);
      const isActive = isActiveCell(selection, col, row);
      const cellRangeClass = inSelection && !isActive ? ' cell-range' : '';
      // Hyperlink cells get a `link-cell` class for visual
      // styling (blue + underline). The click handler in
      // the connector dispatches `linkCellClicked` based
      // on the same hyperlinks map. Slice 165.
      // Defensive default — older fixtures may not declare
      // the hyperlinkCells / commentCells sets (slice 165
      // introduced them).
      const hyperlinkCells = state.hyperlinkCells ?? new Set();
      const commentCells = state.commentCells ?? new Set();
      const invalidFormulaCells = state.invalidFormulaCells ?? new Set();
      const linkClass = hyperlinkCells.has(key) ? ' link-cell' : '';
      const classList =
        `cell col${col} row${row} quadrant-${quadrantName}` +
        (cellStyleClass ? ` ${cellStyleClass}` : '') +
        cellRangeClass +
        linkClass +
        (overflowing ? ' overflowing' : '');
      const style =
        `left:${left}px;top:${top}px;width:${width}px;height:${height}px;` +
        collapsedDeclaration(width, height) +
        textColorDeclaration(data);
      // Comment indicator — render an additional triangle
      // anchored at the cell's top-right when the cell has
      // a comment. Slice 165.
      const invalidIndicator =
        invalidFormulaCells.has(key) && !underMerge
          ? html`<div class="invalidformula-indicator" part="invalid-triangle"></div>`
          : '';
      // The triangle is a sibling of its cell, not a child — it is
      // positioned in grid coordinates like every other overlay — so it
      // carries the anchor as data attributes, the way the popup buttons
      // and sheet overlays do. Nothing else ties it to its cell.
      const commentIndicator =
        commentCells.has(key) && !underMerge
          ? html`<div
              class="comment-indicator"
              part="comment-triangle"
              data-col=${col}
              data-row=${row}
              style="left:${left + width}px;top:${top}px;"
            ></div>`
          : '';
      // Slice 173: `part` attribute mirrors the documented
      // public styling API at
      // ../docs/articles/components/spreadsheet/styling.adoc.
      // Cells inside the active range (but not the active
      // cell itself) carry the `cell-range` part so
      // `vaadin-spreadsheet::part(cell-range)` works.
      const cellPart = inSelection && !isActive ? 'cell cell-range' : 'cell';
      // data-col + data-row drive the click-delegation handler
      // in firstUpdated() (slice 146) — single source of truth
      // for the cell's coordinate when the event bubbles.
      cells.push(html`
        <div class=${classList} part=${cellPart} style=${style} data-col=${col} data-row=${row}>
          ${invalidIndicator}${text}
        </div>
        ${commentIndicator}
      `);
    }
  }
  return cells;
}

/**
 * Build merged-cell overlay divs for every {@link MergedRegion}
 * whose top-left coordinate falls inside the visible viewport. Each
 * overlay is a single div positioned at the merge's top-left cell,
 * sized to span the full region (sum of column widths × sum of row
 * heights).
 *
 * The CSS class list includes {@code cell col<C1> row<R1> merged-cell}
 * — the {@code merged-cell} class is the TestBench-side contract
 * (see {@code AbstractSpreadsheetIT.mergedCell:567-575}).
 *
 * Skips merges whose top-left is outside the visible viewport — the
 * connector / scroll handler is responsible for keeping the viewport
 * aligned. Per-cell styling, selection state, and cellData lookup
 * use the top-left cell's data; the merged overlay is treated as a
 * single rendered cell.
 */
function buildMergedCellOverlays(state: CellGridRenderState): TemplateResult[] {
  const { viewport, cellData, colX, rowY, colWidths, rowHeights, cellStyleToCSSStyle, mergedRegions, selection } =
    state;
  void cellStyleToCSSStyle; // styling slice 135 emits rules globally; we just attach the class

  const overlays: TemplateResult[] = [];
  for (const region of mergedRegions) {
    // Only render overlays whose top-left is in the viewport.
    if (
      region.col1 < viewport.firstColumnIndex ||
      region.col1 > viewport.lastColumnIndex ||
      region.row1 < viewport.firstRowIndex ||
      region.row1 > viewport.lastRowIndex
    ) {
      continue;
    }

    const colStartIdx = region.col1 - viewport.firstColumnIndex;
    const rowStartIdx = region.row1 - viewport.firstRowIndex;
    const left = colX[colStartIdx] ?? 0;
    const top = rowY[rowStartIdx] ?? 0;

    // Sum widths from col1 to min(col2, lastColumnIndex).
    let width = 0;
    const colEnd = Math.min(region.col2, viewport.lastColumnIndex);
    for (let col = region.col1; col <= colEnd; col++) {
      width += colWidths[col - viewport.firstColumnIndex] ?? 0;
    }
    // Sum heights from row1 to min(row2, lastRowIndex).
    let height = 0;
    const rowEnd = Math.min(region.row2, viewport.lastRowIndex);
    for (let row = region.row1; row <= rowEnd; row++) {
      height += rowHeights[row - viewport.firstRowIndex] ?? 0;
    }

    const key = toKey(region.col1, region.row1);
    const data = cellData.get(key);
    // A merged cell can hold a component or a custom editor just like a
    // plain one, and it is the only element in the region that renders the
    // slot — the cells underneath deliberately leave it out.
    const slotName = (state.cellSlots ?? {})[key];
    const text = slotName === undefined ? data?.value ?? '' : html`<slot name=${slotName}></slot>`;
    const cellStyleClass = data?.cellStyle ?? '';

    // Selection: a merged cell carries cell-range if its top-left
    // is in the selected range (excluding active cell). Matches
    // single-cell selection behavior.
    const inSelection = isCellSelected(selection, region.col1, region.row1);
    const isActive = isActiveCell(selection, region.col1, region.row1);
    const cellRangeClass = inSelection && !isActive ? ' cell-range' : '';

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

    overlays.push(html`
      <div class=${classList} part=${cellPart} style=${style} data-col=${region.col1} data-row=${region.row1}>
        ${text}
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
 * Wire contract: class names match the GWT-side
 * {@code SelectionWidget.java:116-126}. The overlay is positioned
 * absolutely within the cell-grid's shadow DOM. CSS styling for the
 * border-line children is deferred to the consumer (theme + a future
 * slice) — slice 137's scope is the structural DOM that TestBench
 * locates.
 */
/**
 * The dashed outline the fill handle drags out.
 *
 * Always rendered, hidden when nothing is being painted: it is the only
 * feedback the user has for where a fill drag will land, and keeping the
 * element in the tree means an observer can ask whether it is showing
 * rather than whether it exists.
 */
function buildPaintOutline(state: CellGridRenderState): TemplateResult {
  // `?? null` rather than `=== null`: a hand-built render state may leave
  // the field off entirely, and a missing outline is not a crash.
  const rect = state.paintRect ?? null;
  const geometry = rect === null ? null : rectGeometry(state, rect);
  const style =
    geometry === null
      ? 'display:none;'
      : `left:${geometry.left}px;top:${geometry.top}px;` +
        `width:${geometry.width}px;height:${geometry.height}px;` +
        'position:absolute;pointer-events:none;';
  // Deliberately not also `.sheet-selection`, which GWT put on it: that
  // class is how the selection overlay is found, and two elements
  // answering to it makes every such lookup order-dependent.
  //
  // No `part` either. GWT exposed the outline as a class and nothing more,
  // and naming a part here would add to the documented styling API, which
  // is a product decision rather than part of this rewrite.
  return html`<div class="paintmode" style=${style}></div>`;
}

/**
 * Pixel box of a cell rectangle inside the visible viewport, or null when
 * it falls entirely outside it.
 */
function rectGeometry(
  state: CellGridRenderState,
  rect: { c1: number; c2: number; r1: number; r2: number }
): { left: number; top: number; width: number; height: number } | null {
  const { viewport, colX, rowY, colWidths, rowHeights } = state;
  const leftCol = Math.max(rect.c1, viewport.firstColumnIndex);
  const rightCol = Math.min(rect.c2, viewport.lastColumnIndex);
  const topRow = Math.max(rect.r1, viewport.firstRowIndex);
  const bottomRow = Math.min(rect.r2, viewport.lastRowIndex);
  if (leftCol > rightCol || topRow > bottomRow) {
    return null;
  }
  const leftIndex = leftCol - viewport.firstColumnIndex;
  const rightIndex = rightCol - viewport.firstColumnIndex;
  const topIndex = topRow - viewport.firstRowIndex;
  const bottomIndex = bottomRow - viewport.firstRowIndex;
  const left = colX[leftIndex] ?? 0;
  const top = rowY[topIndex] ?? 0;
  const right = (colX[rightIndex] ?? 0) + (colWidths[rightIndex] ?? 0);
  const bottom = (rowY[bottomIndex] ?? 0) + (rowHeights[bottomIndex] ?? 0);
  return {
    left,
    top,
    width: Math.max(0, right - left),
    height: Math.max(0, bottom - top)
  };
}

/**
 * Comment overlays — the little yellow notes.
 *
 * Two things put one on screen: the workbook marking a comment "always
 * visible", and the pointer resting on a cell that has one. A cell whose
 * formula the server rejected shows the same overlay carrying the error
 * instead, which is why one function renders both.
 *
 * Class names are the GWT ones (`comment-overlay-label`,
 * `comment-overlay-author`, `comment-overlay-invalidformula`): applications
 * style these today, and renaming them would break that for no gain.
 */
function buildCommentOverlays(state: CellGridRenderState, hoveredKey: string | null): TemplateResult[] {
  const text = state.commentText ?? {};
  const authors = state.commentAuthors ?? {};
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

  const overlays: TemplateResult[] = [];
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
    const geometry = rectGeometry(state, {
      c1: coord.col,
      c2: coord.col,
      r1: coord.row,
      r2: coord.row
    });
    if (geometry === null) {
      continue;
    }
    // Anchored at the cell's top-right, the way Excel hangs a note off the
    // comment triangle.
    const style = `left:${geometry.left + geometry.width}px;top:${geometry.top}px;` + 'position:absolute;';
    const author = authors[key];
    overlays.push(html`
      <div class="v-spreadsheet-comment-overlay" style=${style}>
        ${showsError ? html`<div class="comment-overlay-invalidformula">${invalidMessage}</div>` : nothing}
        ${author ? html`<div class="comment-overlay-author">${author}</div>` : nothing}
        ${editing
          ? html`<textarea
              class="comment-overlay-edit"
              part="comment-edit"
              data-key=${key}
              .value=${comment ?? ''}
            ></textarea>`
          : comment === undefined
          ? nothing
          : html`<div class="comment-overlay-label">${comment}</div>`}
      </div>
    `);
  }
  return overlays;
}

function buildSelectionOverlay(state: CellGridRenderState): TemplateResult | string {
  const { viewport, selection, colX, rowY, colWidths, rowHeights } = state;
  const range = selection.activeRange;

  // Clamp the range to the visible viewport.
  const leftCol = Math.max(range.c1, viewport.firstColumnIndex);
  const rightCol = Math.min(range.c2, viewport.lastColumnIndex);
  const topRow = Math.max(range.r1, viewport.firstRowIndex);
  const bottomRow = Math.min(range.r2, viewport.lastRowIndex);

  if (leftCol > rightCol || topRow > bottomRow) {
    // Active range outside the visible viewport.
    return '';
  }

  const leftIndex = leftCol - viewport.firstColumnIndex;
  const rightIndex = rightCol - viewport.firstColumnIndex;
  const topIndex = topRow - viewport.firstRowIndex;
  const bottomIndex = bottomRow - viewport.firstRowIndex;

  const left = colX[leftIndex] ?? 0;
  const top = rowY[topIndex] ?? 0;
  const right = (colX[rightIndex] ?? 0) + (colWidths[rightIndex] ?? 0);
  const bottom = (rowY[bottomIndex] ?? 0) + (rowHeights[bottomIndex] ?? 0);

  const width = Math.max(0, right - left);
  const height = Math.max(0, bottom - top);
  const overlayStyle =
    `left:${left}px;top:${top}px;width:${width}px;height:${height}px;` + 'position:absolute;pointer-events:none;';

  // The active cell's coordinates ride on the overlay as data-col /
  // data-row (slice 188). Nothing else in the DOM identifies the active
  // cell — it renders as a plain `part="cell"`, indistinguishable from
  // an unselected one — so this is the only way to read "where is the
  // selection" from outside.
  //
  // Slice 173: the selection overlay carries `part="cell-range"`
  // so the documented selector
  // `vaadin-spreadsheet::part(cell-range)` can target it. The
  // selection corner handle (drag affordance in the bottom-right)
  // carries `part="selection-corner"`. Border divs are internal
  // (s-* names are GWT-era; here we keep them only as internal
  // class names — no part attribute since they aren't documented).
  return html`
    <div
      class="sheet-selection"
      part="cell-range"
      data-col=${selection.activeCell.col}
      data-row=${selection.activeCell.row}
      style=${overlayStyle}
    >
      <div class="s-top"></div>
      <div class="s-left"></div>
      <div class="s-right"></div>
      <div class="s-bottom"></div>
      <div class="selection-corner" part="selection-corner"></div>
    </div>
  `;
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

function textColorDeclaration(data: CellData | undefined): string {
  const color = data?.textColor;
  return color ? `color:#${color};` : '';
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
  for (const [index, body] of Object.entries(state.cellStyleToCSSStyle)) {
    rules.push(`.cell.cs${index} { ${body} }`);
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
  // `.col<N>.row<N>`; scope them to cells the way the GWT client did,
  // which prefixed them with its sheet id.
  for (const rule of state.shiftedCellBorderStyles ?? []) {
    rules.push(rule.replace(/\.col/g, '.cell.col'));
  }
  if (rules.length === 0) {
    return '';
  }
  // Compose the rules as plain text + use unsafeHTML — Lit's
  // template literal doesn't substitute into <style> children
  // safely otherwise. The content is server-controlled CSS, so
  // this is the same trust boundary the wrapper already exposes
  // (the GWT path emitted these rules via document.createElement
  // ('style') + appendChild).
  return html`<style>
    ${unsafeHTML(rules.join('\n'))}
  </style>`;
}

/**
 * Build the cell-editor overlay {@code <input>} when {@code state
 * .editor} is non-null (slice 152). The input is sized to match
 * the cell's pixel box using the appropriate quadrant arrays:
 *
 *  - col within frozen cols: use {@code frozenColX} / {@code
 *    frozenColWidths}
 *  - col within viewport: use {@code colX} / {@code colWidths}
 *  - row within frozen rows: use {@code frozenRowY} / {@code
 *    frozenRowHeights}
 *  - row within viewport: use {@code rowY} / {@code rowHeights}
 *
 * When the editor's coordinate falls outside both the viewport
 * and the frozen ranges, returns empty (defensive — the
 * connector should never push an editor at an off-screen cell;
 * this keeps the render safe regardless).
 *
 * On keydown:
 *   Enter / Tab → dispatch {@code cell-editor-commit} with the
 *     current input value; the connector dispatches
 *     `cellValueEdited` (slice 153) + clears editor state.
 *   Escape       → dispatch {@code cell-editor-cancel};
 *                  connector clears editor state without
 *                  dispatching cellValueEdited.
 *   Blur          → same as commit (Excel convention; slice 153).
 *
 * The detail event payload is {@code { col, row, value }} so the
 * connector can route the value to the correct cell without
 * re-querying its state.
 */
function buildCellEditorOverlay(state: CellGridRenderState): TemplateResult | string {
  const editor = state.editor;
  if (!editor) {
    return '';
  }
  const { col, row, initialValue } = editor;
  const { viewport, colX, colWidths, rowY, rowHeights } = state;
  const { frozenColX, frozenColWidths, frozenRowY, frozenRowHeights } = state;

  let left = 0;
  let width = 0;
  if (col >= viewport.firstColumnIndex && col <= viewport.lastColumnIndex) {
    const i = col - viewport.firstColumnIndex;
    left = colX[i] ?? 0;
    width = colWidths[i] ?? 0;
  } else if (col >= 1 && col <= frozenColX.length) {
    left = frozenColX[col - 1] ?? 0;
    width = frozenColWidths[col - 1] ?? 0;
  } else {
    return '';
  }

  let top = 0;
  let height = 0;
  if (row >= viewport.firstRowIndex && row <= viewport.lastRowIndex) {
    const i = row - viewport.firstRowIndex;
    top = rowY[i] ?? 0;
    height = rowHeights[i] ?? 0;
  } else if (row >= 1 && row <= frozenRowY.length) {
    top = frozenRowY[row - 1] ?? 0;
    height = frozenRowHeights[row - 1] ?? 0;
  } else {
    return '';
  }

  if (width === 0 || height === 0) {
    return '';
  }

  const style = `left:${left}px;top:${top}px;width:${width}px;height:${height}px;`;

  const dispatchEditorEvent = (
    target: EventTarget,
    type: 'cell-editor-commit' | 'cell-editor-cancel',
    value: string,
    key?: string,
    shiftKey?: boolean
  ): void => {
    // Closing the editor takes the input out of the document, and losing
    // the focus that way fires blur — which would commit a second time.
    // One edit, one `cellValueEdited`.
    if (target instanceof HTMLElement) {
      target.dataset.editorClosed = 'true';
    }
    target.dispatchEvent(
      new CustomEvent(type, {
        // Include the key + shift modifier so the connector can
        // pick the next active-cell direction after commit
        // (Excel convention: Enter moves down, Tab moves right;
        // shift reverses each). Slice 158.
        detail: { col, row, value, key, shiftKey },
        bubbles: true,
        composed: true
      })
    );
  };

  const ARROW_KEYS = ['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'];

  const onKeyDown = (e: KeyboardEvent): void => {
    const input = e.currentTarget as HTMLInputElement;
    if (e.key === 'Enter' || e.key === 'Tab') {
      e.preventDefault();
      dispatchEditorEvent(input, 'cell-editor-commit', input.value, e.key, e.shiftKey);
    } else if (e.key === 'Escape') {
      e.preventDefault();
      dispatchEditorEvent(input, 'cell-editor-cancel', input.value);
    } else if (editor.mode === 'enter' && ARROW_KEYS.includes(e.key)) {
      // Excel has two editing modes and they differ here: when the user
      // is typing over the cell, an arrow commits and moves on. Only
      // when they opened the existing value to change it does an arrow
      // move the caret instead.
      e.preventDefault();
      dispatchEditorEvent(input, 'cell-editor-commit', input.value, e.key, e.shiftKey);
    }
    // Other keys (typing) are NOT stopped so the input receives them.
    e.stopPropagation();
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
    dispatchEditorEvent(input, 'cell-editor-commit', input.value);
  };

  return html`
    <input
      class="cell-editor"
      part="cell-input"
      .value=${initialValue}
      style=${style}
      @keydown=${onKeyDown}
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
 * Vaadin-component slotting in a later slice. Slice 160.
 *
 * Keyed on the overlay id. The list arrives from the server's overlay map,
 * whose iteration order shifts whenever an entry is removed and re-added —
 * which is what scrolling one out of view and back does. Rendered
 * positionally, a reordering makes Lit rewrite each {@code <img>} in place,
 * so every image re-downloads and flickers even though nothing about it
 * changed. Keyed, Lit moves the existing elements instead.
 */
function buildSheetOverlays(state: CellGridRenderState): DirectiveResult {
  // Defensive default — some older test fixtures may not
  // declare the overlays array yet (slice 160 introduced it).
  // Real connector pushes always populate the field.
  const overlays = state.overlays ?? [];
  return repeat(
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
 * Build the popup-button rendering (slice 161). One small
 * triangle/icon per anchor cell at its bottom-right corner.
 * Click on a button dispatches `popup-button-click { col, row }`
 * which the connector translates to a `popupButtonClick`
 * spreadsheet-event for the server.
 */
function buildPopupButtons(state: CellGridRenderState): TemplateResult[] {
  const buttons = state.popupButtons ?? [];
  return buttons.map((b) => {
    const className = b.active ? 'popup-button active' : 'popup-button';
    const style = `left:${b.leftPx}px;top:${b.topPx}px;`;
    const onClick = (e: Event) => {
      e.stopPropagation();
      (e.currentTarget as Element).dispatchEvent(
        new CustomEvent('popup-button-click', {
          detail: { col: b.col, row: b.row },
          bubbles: true,
          composed: true
        })
      );
    };
    return html`
      <div class=${className} part="popup-button" style=${style} data-col=${b.col} data-row=${b.row} @click=${onClick}
        >▼</div
      >
    `;
  });
}

customElements.define('vaadin-spreadsheet-internal-cell-grid', SpreadsheetCellGrid);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-cell-grid': SpreadsheetCellGrid;
  }
}
