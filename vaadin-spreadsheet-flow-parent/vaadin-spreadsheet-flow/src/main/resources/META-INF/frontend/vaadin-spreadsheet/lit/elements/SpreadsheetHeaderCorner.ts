/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, nothing } from 'lit';
import { SpreadsheetPart } from './SpreadsheetPart.js';
import type { TemplateResult } from 'lit';

// One row (or column) of outline gutter. Shared with the header strips so
// the buttons stay in line with the gutters they belong to.
import {
  COLUMN_GROUPING_LEVEL_PX,
  columnGroupingGutterPx,
  ROW_GROUPING_LEVEL_PX,
  rowGroupingGutterPx
} from './headers/Grouping.js';
import { headerBg, headerBorderColor, headerFont, textColor } from './ThemeProperties.js';

export interface HeaderCornerRenderState {
  /** Width of the row-header column, 0 when the headers are hidden. */
  headerWidth: number;
  /** Height of the column-header row, 0 when the headers are hidden. */
  headerHeight: number;
  /** Deepest column outline level, 0 when no columns are grouped. */
  colGroupingMax: number;
  /** Deepest row outline level, 0 when no rows are grouped. */
  rowGroupingMax: number;
}

export function emptyHeaderCornerRenderState(): HeaderCornerRenderState {
  return {
    headerWidth: 0,
    headerHeight: 0,
    colGroupingMax: 0,
    rowGroupingMax: 0
  };
}

/**
 * The square where the two header strips meet.
 *
 * It also holds the outline level buttons - the numbered squares Excel
 * puts above and beside the outline gutters. Clicking one shows the sheet
 * down to that level: everything shallower expanded, that level collapsed.
 * The column levels stack down the gutter rows and the row levels run
 * along the gutter columns, each button sitting in line with the gutter
 * it belongs to.
 *
 * As in Excel there is one button more than there are levels, the last
 * opening every group; the gutters have a level for it. Without headings
 * there is nowhere for the buttons, and none show.
 *
 * Laid out in boxes of its own: the
 * square beside the letters and above the numbers, shaded as the letters
 * are; a box above it for the column levels and one beside it for the row
 * levels, each bordered off from the gutters; and, where the two gutters
 * meet, a box over the dotted lines that divide their levels.
 */
export class SpreadsheetHeaderCorner extends SpreadsheetPart {
  // Its own `header-corner` part is on the element itself, in the sheet's
  // shadow root: only the parts inside it are exported.
  static readonly EXPORTED_PARTS = 'outline-level-button';

  static properties = {
    renderState: { attribute: false }
  };

  static styles = css`
    :host {
      display: block;
      position: relative;
      box-sizing: border-box;
      background: ${headerBg};
      border-right: 1px solid ${headerBorderColor};
      border-bottom: 1px solid ${headerBorderColor};
      /* The headers' font, for the outline level numbers. */
      font-family: ${headerFont};
    }

    :host([hidden]) {
      display: none !important;
    }

    .corner,
    .col-group-summary,
    .row-group-summary,
    .grouping-corner {
      position: absolute;
      box-sizing: border-box;
    }

    /* Shaded from the header colour down to a shade darker, as the letters
       are, blended in sRGB as they are. Its right and bottom edges are the
       host's border. */
    .corner {
      background: linear-gradient(to bottom in srgb, ${headerBg} 2%, color-mix(in srgb, ${headerBg} 96%, #000) 98%);
    }

    /* The column levels, one under the other at the right. */
    .col-group-summary {
      border-bottom: 1px solid ${headerBorderColor};
    }

    /* The row levels, side by side in a line of the sheet's own font as
       tall as the letters, which sets how far down they sit. */
    .row-group-summary {
      border-right: 1px solid ${headerBorderColor};
      font-size: 14px;
      white-space: nowrap;
    }

    /* Where the gutters meet: over the dotted lines that run in under it. */
    .grouping-corner {
      z-index: 1;
      background: ${headerBg};
      border-right: 1px solid ${headerBorderColor};
      border-bottom: 1px solid ${headerBorderColor};
    }

    /* The dotted lines between the levels, carried on from the gutters
       across the level boxes. Each starts at the corner's edge and is an
       odd length, so the dots fall on the same
       pixels: along an even length the browser doubles the first dot and
       shifts the rest. */
    .level-separator {
      position: absolute;
      pointer-events: none;
    }

    .level-separator.cols {
      left: 0;
      height: 0;
      border-bottom: 1px dotted ${headerBorderColor};
    }

    .level-separator.rows {
      top: 0;
      width: 0;
      border-right: 1px dotted ${headerBorderColor};
    }

    /* Plain numbers. */
    .level-header {
      box-sizing: border-box;
      width: 11px;
      height: 18px;
      padding: 0;
      margin: 0;
      border: 0;
      background: none;
      color: #484848;
      font: inherit;
      font-size: 11px;
      line-height: 18px;
      text-align: center;
      cursor: pointer;
    }

    .col-group-summary .level-header {
      display: block;
      margin-left: auto;
      margin-right: 4px;
    }

    .col-group-summary .level-header > span {
      vertical-align: text-top;
    }

    .row-group-summary .level-header {
      display: inline-block;
      margin-left: 4px;
    }

    /* SpreadsheetTheme.LUMO. */
    :host([theme~='lumo']) {
      background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
    }

    :host([theme~='lumo']) .corner {
      background: none;
    }

    :host([theme~='lumo']) .grouping-corner {
      background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
    }

    :host([theme~='lumo']) .row-group-summary {
      font-size: var(--lumo-font-size-m);
    }

    :host([theme~='lumo']) .level-header {
      color: ${textColor};
      font-size: var(--lumo-font-size-s);
      cursor: var(--lumo-clickable-cursor);
    }

    :host([theme~='lumo']) .col-group-summary .level-header {
      margin-right: var(--lumo-space-xs);
    }

    :host([theme~='lumo']) .row-group-summary .level-header {
      margin-left: var(--lumo-space-xs);
    }
  `;

  declare renderState: HeaderCornerRenderState;

  constructor() {
    super();
    this.renderState = emptyHeaderCornerRenderState();
  }

  connectedCallback(): void {
    super.connectedCallback();
    this.setAttribute('part', 'header-corner');
  }

  firstUpdated(): void {
    this.shadowRoot!.addEventListener('click', this.handleShadowClick);
  }

  private readonly handleShadowClick = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }
    const button = target.closest<HTMLElement>('.level-header');
    if (!button) {
      return;
    }
    const level = Number(button.dataset.level);
    if (!Number.isFinite(level) || level < 1) {
      return;
    }
    this.dispatchEvent(
      new CustomEvent('level-header-click', {
        detail: { cols: button.dataset.axis === 'cols', level },
        bubbles: true,
        composed: true
      })
    );
  };

  render(): TemplateResult | typeof nothing {
    const { headerWidth, headerHeight, colGroupingMax, rowGroupingMax } = this.renderState;
    // Without headings the corner is only where the gutters meet, and the
    // host's own border boxes it off.
    if (headerWidth === 0 || headerHeight === 0) {
      return nothing;
    }
    const rowGutterPx = rowGroupingGutterPx(rowGroupingMax, true);
    const colGutterPx = columnGroupingGutterPx(colGroupingMax, true);
    // The host's border is the right and bottom edge of everything here.
    const square = html`<div
      class="corner"
      style="left:${rowGutterPx}px;top:${colGutterPx}px;width:${headerWidth - 1}px;height:${headerHeight - 1}px;"
    ></div>`;
    if (colGroupingMax === 0 && rowGroupingMax === 0) {
      return square;
    }

    const parts: TemplateResult[] = [square];
    if (colGroupingMax > 0) {
      // One per column level and one more, under one another.
      const levels: TemplateResult[] = [];
      for (let level = 1; level <= colGroupingMax + 1; level++) {
        levels.push(
          html`<button
            class="level-header"
            part="outline-level-button"
            type="button"
            data-axis="cols"
            data-level=${level}
            ><span>${level}</span></button
          >`
        );
      }
      parts.push(
        html`<div
          class="col-group-summary"
          style="left:${rowGutterPx}px;top:0;width:${headerWidth - 1}px;height:${colGutterPx}px;"
          >${levels}</div
        >`
      );
      // Across the corner, its right border included.
      const length = (rowGutterPx + headerWidth) | 1;
      for (let level = 1; level <= colGroupingMax; level++) {
        parts.push(
          html`<div
            class="level-separator cols"
            style="top:${level * COLUMN_GROUPING_LEVEL_PX}px;width:${length}px;"
          ></div>`
        );
      }
    }
    if (rowGroupingMax > 0) {
      // One per row level and one more, side by side - with nothing
      // between them, as the space a line break leaves would push them
      // apart.
      const levels: TemplateResult[] = [];
      for (let level = 1; level <= rowGroupingMax + 1; level++) {
        levels.push(
          html`<button
            class="level-header"
            part="outline-level-button"
            type="button"
            data-axis="rows"
            data-level=${level}
            >${level}</button
          >`
        );
      }
      parts.push(
        html`<div
          class="row-group-summary"
          style="left:0;top:${colGutterPx}px;width:${rowGutterPx}px;height:${headerHeight -
          1}px;line-height:${headerHeight}px;"
          >${levels}</div
        >`
      );
      // Down the corner, its bottom border included.
      const length = (colGutterPx + headerHeight) | 1;
      for (let level = 1; level <= rowGroupingMax; level++) {
        parts.push(
          html`<div
            class="level-separator rows"
            style="left:${level * ROW_GROUPING_LEVEL_PX}px;height:${length}px;"
          ></div>`
        );
      }
    }
    if (colGroupingMax > 0 && rowGroupingMax > 0) {
      parts.push(
        html`<div class="grouping-corner" style="left:0;top:0;width:${rowGutterPx}px;height:${colGutterPx}px;"></div>`
      );
    }
    return html`${parts}`;
  }
}

customElements.define('vaadin-spreadsheet-internal-header-corner', SpreadsheetHeaderCorner);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-header-corner': SpreadsheetHeaderCorner;
  }
}
