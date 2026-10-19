/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { toKey } from '../../algorithms/CellKeys.js';
import { sameInputs } from '../../algorithms/Memo.js';
import type { CellData } from '../../types/wire.js';
import type { CellGridRenderState } from '../types.js';
import { columnWidthOf, drawnRanges } from './GridGeometry.js';

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

/** What measuring needs of the grid it measures in. */
export interface ValueMeasurerHost {
  readonly renderState: CellGridRenderState;
  readonly theme: string | null;
  readonly shadowRoot: ShadowRoot | null;
  /** The cells whose value really is too wide for its column, by key: what the judging decides. */
  tooNarrowCells: ReadonlySet<string>;
  /** The cells given a run-on width whose value fits after all, by key: what the judging decides. */
  fittingSpills: ReadonlySet<string>;
}

/**
 * Measures values as the cells draw them: to judge the cells the server
 * flagged - a value too wide for its column, which shows hashes, and one
 * given a run-on width that fits after all - and for the editor and the
 * server, how wide a text is in a cell style. It decides the host's
 * {@code tooNarrowCells} and {@code fittingSpills}. The grid calls
 * {@link willUpdate} and {@link updated} in its own update.
 */
export class ValueMeasurer {
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

  constructor(private readonly host: ValueMeasurerHost) {}

  /**
   * Before drawing: judges the drawn cells of a new state now, while the
   * rules a value is measured in are already in the page. A probe then lays
   * out on its own, where after a drawing it laid out every new cell with
   * it, and the cells are drawn once, as judged. New styles have to be
   * drawn first.
   */
  willUpdate(stateChanged: boolean): void {
    this.judgedBeforeDrawing = stateChanged && sameInputs(this.measuringInputs(), this.stylesDrawn);
    if (this.judgedBeforeDrawing) {
      this.judgeDrawnCells();
    }
  }

  /** After drawing: notes the styles drawn, and judges a new state's cells if that could not be done before. */
  updated(stateChanged: boolean): void {
    this.stylesDrawn = this.measuringInputs();
    if (stateChanged && !this.judgedBeforeDrawing) {
      this.judgeDrawnCells();
    }
  }

  /** What a value would be drawn at, in its own style. */
  measureValue(cellStyle: string, value: string): number {
    if (this.valueProbe === null) {
      this.valueProbe = createValueProbe(cellStyle, value);
      this.host.shadowRoot!.appendChild(this.valueProbe);
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
    const root = this.host.shadowRoot;
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
    const state = this.host.renderState;
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
          } else if (this.host.tooNarrowCells.has(key)) {
            notNarrow.push(key);
          }
          if (flaggedSpill) {
            spills.push({ key, data, columnWidth });
          } else if (this.host.fittingSpills.has(key)) {
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
      this.host.tooNarrowCells,
      narrow,
      notNarrow,
      (columnWidth, textWidth) => textWidth > columnWidth
    );
    const fitting = this.judged(
      this.host.fittingSpills,
      spills,
      notSpill,
      (columnWidth, textWidth) => textWidth <= columnWidth
    );
    if (tooNarrow !== this.host.tooNarrowCells) {
      this.host.tooNarrowCells = tooNarrow;
    }
    if (fitting !== this.host.fittingSpills) {
      this.host.fittingSpills = fitting;
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
    this.host.shadowRoot!.append(...probes.map(([, , probe]) => probe));
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
    return [
      this.host.renderState.cellStyleToCSSStyle,
      this.host.renderState.conditionalFormattingStyles,
      this.host.theme
    ];
  }
}
