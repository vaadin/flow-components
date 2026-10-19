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
import { SpreadsheetPart } from './SpreadsheetPart.js';
import { cellTarget } from '../dom/IndexedTarget.js';

import { toKey } from '../algorithms/CellKeys.js';
import { Memo } from '../algorithms/Memo.js';
import { overlayAnimationStyles } from '../dom/OverlayAnimation.js';
import { type CellGridRenderState, emptyCellGridRenderState } from './types.js';
import {
  cellBoxOnGrid,
  frozenExtentOf,
  type QuadrantName,
  type QuadrantRenderOpts,
  quadrants
} from './cell-grid/GridGeometry.js';
import { buildCellStyleBlock, styleRuleInputs } from './cell-grid/CellStyleRules.js';
import { cellGridLumoStyles, cellGridStyles } from './cell-grid/CellGridStyles.js';
import { buildFormulaReferences, buildPaintOutline, buildSelectionOverlay } from './cell-grid/RangeOverlays.js';
import { buildPopupButtons, buildSheetOverlays } from './cell-grid/SheetOverlays.js';
import { buildMergedCellOverlays, renderQuadrant } from './cell-grid/QuadrantCells.js';
import { CommentNotes } from './cell-grid/CommentNotes.js';
import { CellEditor } from './cell-grid/CellEditor.js';
import { ValueMeasurer } from './cell-grid/ValueMeasurer.js';

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
  /** Cells given a run-on width whose value fits after all - see ValueMeasurer. */
  declare fittingSpills: ReadonlySet<string>;
  /**
   * The theme variant the connector copies from the host. A property, not
   * only an attribute the stylesheet reads, because the editor's inline
   * style follows it too.
   */
  declare theme: string | null;
  /** The style rules written from the workbook's styles, and what they were written from. */
  private readonly styleRules = new Memo<TemplateResult>();
  /** Measures values: the cells too narrow for theirs, and the run-on ones that fit after all. */
  private readonly measurer = new ValueMeasurer(this);
  /** The cell editor: its input, its width, and the keyboard. */
  private readonly editor = new CellEditor(this, (cellStyle, value) => this.measurer.measureValue(cellStyle, value));
  /** The notes: which show, and where. */
  private readonly notes = new CommentNotes(this);

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
    this.notes.listen(this.shadowRoot!);
  }

  /**
   * The render state with this element's own measurements folded in.
   */
  private measuredState(): CellGridRenderState {
    return { ...this.renderState, tooNarrowCells: this.tooNarrowCells, fittingSpills: this.fittingSpills };
  }

  /** What the open editor holds, or null when none is open. */
  editorValue(): string | null {
    return this.editor.value();
  }

  /**
   * Puts a value typed elsewhere - into the formula field, carrying the edit
   * on there - into the open editor, which widens or narrows to it.
   */
  setEditorValue(value: string, caret?: number): void {
    this.editor.setValue(value, caret);
  }

  /**
   * How wide a text is drawn in each of the given cell styles, in pixels:
   * measured in one layout, and leaving nothing behind. 0 each while
   * nothing is laid out - the grid is hidden, or not yet in the page.
   */
  textWidths(cellStyles: readonly string[], text: string): number[] {
    return this.measurer.textWidths(cellStyles, text);
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
    this.notes.willUpdate();
    this.measurer.willUpdate(changedProps.has('renderState'));
  }

  /**
   * After every render: measure what the new state needs measured, then
   * place the notes, and fit the editor and give it the keyboard.
   */
  updated(changedProps: Map<string, unknown>): void {
    this.measurer.updated(changedProps.has('renderState'));
    this.notes.updated();
    this.editor.updated();
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

  private readonly handleGridMouseLeave = (): void => {
    this.notes.hover(null);
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
    this.notes.hover(toKey(col, row));
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
    const editorInput = this.editor.render();
    const commentOverlays = this.notes.render();
    // A hidden stand-in for the editor, an input that is always there:
    // TestBench hands it out while no cell is being edited, so tests
    // that ask whether the editor is shown keep working.
    const standIn = html`<input class="cell-input-stand-in" hidden inert tabindex="-1" aria-hidden="true" />`;
    return html`${styleBlock}${mainCells}${mainMerged}${mainOverlays}${top}${left}${corner}${commentOverlays}${editorInput}${standIn}`;
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

customElements.define('vaadin-spreadsheet-internal-cell-grid', SpreadsheetCellGrid);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-cell-grid': SpreadsheetCellGrid;
  }
}
