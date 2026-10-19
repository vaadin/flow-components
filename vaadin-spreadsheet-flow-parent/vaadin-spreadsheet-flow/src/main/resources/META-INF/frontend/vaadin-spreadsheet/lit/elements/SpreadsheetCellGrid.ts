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
import { Memo, sameInputs } from '../algorithms/Memo.js';
import type { CellData } from '../types/wire.js';
import { overlayAnimationStyles } from '../dom/OverlayAnimation.js';
import { type CellGridRenderState, emptyCellGridRenderState } from './types.js';
import {
  cellBoxOnGrid,
  columnWidthOf,
  drawnRanges,
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
  /** The cell editor: its input, its width, and the keyboard. */
  private readonly editor = new CellEditor(this, (cellStyle, value) => this.measureValue(cellStyle, value));
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
    return this.editor.draft();
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
   * After every render: measure what the new state needs measured, then
   * place the notes, and fit the editor and give it the keyboard.
   */
  updated(changedProps: Map<string, unknown>): void {
    this.stylesDrawn = this.measuringInputs();
    if (changedProps.has('renderState') && !this.judgedBeforeDrawing) {
      this.judgeDrawnCells();
    }
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

customElements.define('vaadin-spreadsheet-internal-cell-grid', SpreadsheetCellGrid);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-cell-grid': SpreadsheetCellGrid;
  }
}
